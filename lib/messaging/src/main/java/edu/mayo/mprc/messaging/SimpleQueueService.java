package edu.mayo.mprc.messaging;

import edu.mayo.mprc.MprcException;
import org.apache.log4j.Logger;

import javax.jms.*;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A JMS queue that allows request-response communication.
 * The responses are transferred using temporary queue, as described here:
 * http://activemq.apache.org/how-should-i-implement-request-response-with-jms.html
 * <p/>
 * Never share the service among multiple threads - it is single-threaded only due to usage of Session.
 */
class SimpleQueueService implements Service {
	private static final Logger LOGGER = Logger.getLogger(SimpleQueueService.class);

	private final Connection connection;
	private final ThreadLocal<Session> session = new ThreadLocal<Session>();
	// This is where the requests are sent to
	private final ThreadLocal<Destination> requestDestination = new ThreadLocal<Destination>();
	// Cached producer for sending messages to the queue.
	private final ThreadLocal<MessageProducer> producer = new ThreadLocal<MessageProducer>();
	// Cached consumer for receiving messages.
	private final ThreadLocal<MessageConsumer> consumer = new ThreadLocal<MessageConsumer>();

	private final String queueName;
	private TemporaryQueue responseQueue;
	/**
	 * Map from correlation ID (request ID) to the response listener. Has to be synchronized, as an entry removal occurs
	 * asynchronously when message arrives, which could collide with entry adding.
	 */
	private final Map<String, ResponseListener> responseMap = Collections.synchronizedMap(new HashMap<String, ResponseListener>());
	private AtomicInteger uniqueId = new AtomicInteger(0);

	/**
	 * The very last response to a request is marked with this boolean property set to true.
	 */
	public static final String LAST_RESPONSE = "is_last";

	/**
	 * Establishes a link of given name on a given broker.
	 * Each link consists of two JMS queues - one for sending request and a temporary response queue
	 * for each of the requests.
	 *
	 * @param broker Broker the queue lives on.
	 * @param name   Name of the queue.
	 * @throws edu.mayo.mprc.MprcException JMS failures.
	 */
	SimpleQueueService(URI broker, String name, String userName, String password) {
		this.queueName = name;

		try {
			connection = ActiveMQConnectionPool.getConnectionToBroker(broker, userName, password);

			final TemporaryQueue queue = responseQueue();
			final Session tempQueueSession = connection.createSession(/*transacted?*/false, /*acknowledgment*/Session.CLIENT_ACKNOWLEDGE);
			MessageConsumer tempQueueConsumer = tempQueueSession.createConsumer(queue);
			tempQueueConsumer.setMessageListener(new TempQueueMessageListener());

			// start the connection and start listening for events
			connection.start();

			LOGGER.info("Connected to JMS broker: " + broker.toString() + " queue: " + queueName);
		} catch (JMSException e) {
			throw new MprcException("Queue could not be created", e);
		}
	}

	private synchronized TemporaryQueue responseQueue() throws JMSException {
		if (responseQueue == null) {
			responseQueue = session().createTemporaryQueue();
		}
		return responseQueue;
	}

	public String getName() {
		return queueName;
	}

	public void sendRequest(Serializable request, ResponseListener listener) {
		try {
			ObjectMessage objectMessage = session().createObjectMessage(request);

			if (null != listener) {
				// User wants response to the message.

				// Register the new listener on the temporary queue and remember its correlation ID
				final String correlationId = String.valueOf(uniqueId.incrementAndGet());
				responseMap.put(correlationId, listener);

				// Replies go our temporary queue
				objectMessage.setJMSReplyTo(responseQueue());
				// Correlation ID matches the responses with the response listener
				objectMessage.setJMSCorrelationID(correlationId);
			}
			LOGGER.debug("Sending message " + objectMessage.toString() + " id: " + objectMessage.getJMSMessageID());
			messageProducer().send(requestDestination(), objectMessage);

			LOGGER.info("Request sent to queue: " + queueName);
		} catch (JMSException e) {
			throw new MprcException("Could not send message", e);
		}
	}

	private synchronized MessageProducer messageProducer() throws JMSException {
		if (null == producer.get()) {
			producer.set(session().createProducer(null));
		}
		return producer.get();
	}

	/**
	 * Wraps received message into an object that allows the receiver to send a response (if requested by sender).
	 *
	 * @param message Message to wrap
	 * @return Wrapped message
	 */
	private JmsRequest wrapReceivedMessage(Message message) {
		return new JmsRequest((ObjectMessage) message, this);
	}

	/**
	 * To be used by JmsRequest for sending responses.
	 *
	 * @param response        User response.
	 * @param originalMessage Message this was response to.
	 * @param isLast          True if the message is the last one.
	 */
	void sendResponse(Serializable response, ObjectMessage originalMessage, boolean isLast) {
		try {
			if (originalMessage.getJMSCorrelationID() != null) {
				// Response was requested
				ObjectMessage responseMessage = session().createObjectMessage(response);
				responseMessage.setBooleanProperty(SimpleQueueService.LAST_RESPONSE, isLast);
				responseMessage.setJMSCorrelationID(originalMessage.getJMSCorrelationID());
				messageProducer().send(originalMessage.getJMSReplyTo(), responseMessage);
				LOGGER.debug("Message sent: " + responseMessage.getJMSMessageID() + " timestamp: " + responseMessage.getJMSTimestamp());
			}

		} catch (JMSException e) {
			throw new MprcException(e);
		}
	}

	public Request receiveRequest(long timeout) {
		try {
			Message message = messageConsumer().receive(timeout);
			if (message != null) {
				LOGGER.info("Request received from queue: " + queueName);
				return wrapReceivedMessage(message);
			} else {
				return null;
			}
		} catch (JMSException e) {
			throw new MprcException("Could not receive message", e);
		}
	}

	public synchronized void stopReceiving() {
		if (null != consumer.get()) {
			try {
				consumer.get().close();
			} catch (JMSException e) {
				throw new MprcException(e);
			} finally {
				consumer.set(null);
			}
		}
	}

	private synchronized MessageConsumer messageConsumer() throws JMSException {
		if (null == consumer.get()) {
			consumer.set(session().createConsumer(requestDestination()));
		}
		return consumer.get();
	}

	private Session session() {
		if (this.session.get() == null) {
			try {
				final Session value = connection.createSession(/*transacted?*/false, /*acknowledgment*/Session.CLIENT_ACKNOWLEDGE);
				this.session.set(value);
			} catch (JMSException e) {
				throw new MprcException("Could not open JMS session", e);
			}
		}
		return this.session.get();
	}

	private Destination requestDestination() {
		if (this.requestDestination.get() == null) {
			try {
				// This is where the requests go
				final Destination value = this.session().createQueue(this.queueName);
				this.requestDestination.set(value);
			} catch (JMSException e) {
				throw new MprcException("Could not create JMS destination", e);
			}
		}
		return this.requestDestination.get();
	}

	private class TempQueueMessageListener implements MessageListener {
		@Override
		public void onMessage(Message message) {
			try {
				processMessage(message);
			} finally {
				acknowledgeMessage(message);
			}
		}

		/**
		 * Must never throw an exception.
		 */
		private void processMessage(Message message) {
			boolean isLast = true;
			ResponseListener listener = null;
			try {
				final ObjectMessage objectMessage = (ObjectMessage) message;
				final Serializable messageData = objectMessage.getObject();
				final String listenerId = objectMessage.getJMSCorrelationID();
				listener = responseMap.get(listenerId);
				isLast = objectMessage.getBooleanProperty(SimpleQueueService.LAST_RESPONSE);
				if (listener == null) {
					LOGGER.error("No registered listener for response");
				} else {
					if (isLast) {
						responseMap.remove(listenerId);
					}
					listener.responseReceived(messageData, isLast);
				}
			} catch (Exception t) {
				// SWALLOWED: This method cannot throw exceptions, but it can pass them as a received object.
				if (null != listener) {
					try {
						listener.responseReceived(t, isLast);
					} catch (Exception e) {
						// SWALLOWED
						LOGGER.warn("The response listener failed", e);
					}
				} else {
					LOGGER.error("No registered listener for response, cannot report error", t);
				}
			}
		}

		private void acknowledgeMessage(Message message) {
			if (message == null) {
				return;
			}
			try {
				message.acknowledge();
			} catch (JMSException e) {
				//SWALLOWED
				try {
					LOGGER.error("Failed to acknowledge message received. Message destination: " + message.getJMSDestination(), e);
				} catch (JMSException ignore) {
					//SWALLOWED
				}
			}
		}
	}
}
