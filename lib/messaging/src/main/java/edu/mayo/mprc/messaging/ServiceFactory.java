package edu.mayo.mprc.messaging;

import edu.mayo.mprc.MprcException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Returns a service running at given URI.
 * <p/>
 * Currently supports URI in the following form:
 * <dl>
 * <dt><code>jms.tcp://JMS.BROKER.URL:PORT?BROKER_SETTINGS&simplequeue=QUEUE_NAME</code></dt>
 * <dd>
 * JMS connection.
 * Everything between the <code>jms.</code> and <code>&simplequeue=QUEUE_NAME</code>
 * is preserved and passed to the underlying JMS implementation.</dd>
 */
public final class ServiceFactory {
	private static final String JMS_PREFIX = "jms.";
	/**
	 * Matches the part of URI for the message broker.
	 */
	private static final Pattern URI_BROKER_PART = Pattern.compile(Pattern.quote(JMS_PREFIX) + "(.*)[&?]simplequeue=.*");
	/**
	 * Matches the part of URI defining the queue name.
	 */
	private static final Pattern URI_SIMPLE_QUEUE_PART = Pattern.compile("[&?]simplequeue=([^&?]*)$");

	public ServiceFactory() {
	}

	/**
	 * Creates a service ({@link Service})
	 * <p/>
	 * The implementation can be virtually anything. What gets created is determined
	 * by the URI format that is passed in. So far we implement only simple JMS queues.
	 *
	 * @param serviceUri URI specifying the service.
	 * @return Service running at the given URI.
	 * @throws MprcException Service could not be created.
	 */
	public Service createService(URI serviceUri) {
		// TODO: This is hardcoded right now. Eventually would allow registering of new URI handlers.
		if (null == serviceUri) {
			throw new MprcException("URI must not be null");
		}

		String uriString = serviceUri.toString();
		if (uriString.startsWith(JMS_PREFIX)) {
			return createJmsQueue(serviceUri);
		}

		throw new MprcException("Unsupported URI " + serviceUri.toString());
	}

	static String extractJmsQueueName(URI serviceUri) {
		// Parse the query part into queue name
		String uriString = serviceUri.toString();
		Matcher matcher = URI_SIMPLE_QUEUE_PART.matcher(uriString);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			throw new MprcException("The JMS service URI '" + uriString + "' has incorrect format. It should end with ?simplequeue=NAME");
		}
	}

	static UserInfo extractJmsUserinfo(URI serviceURI) {
		return new UserInfo(serviceURI);
	}

	/**
	 * Extract JMS broker URI from our wrapper that also specifies queue and protocol.
	 * E.g. an uri in from <tt>jms.tcp://localhost?simplequeue=NAME</tt> becomes
	 * <tt>tcp://localhost</tt>
	 *
	 * @param serviceUri
	 * @return
	 * @throws URISyntaxException
	 */
	public static URI extractJmsBrokerUri(URI serviceUri) {
		if (serviceUri == null) {
			throw new MprcException("The service uri must not be null.");
		}
		// We split the original URI. We extract the original JMS-specific part. The rest is removed.

		String uriString = serviceUri.toString();

		Matcher matcher = URI_BROKER_PART.matcher(uriString);
		if (matcher.matches()) {
			final String uriPart = matcher.group(1);
			try {
				return new URI(uriPart);
			} catch (URISyntaxException e) {
				throw new MprcException("Unsupported JMS service URI: " + uriPart, e);
			}
		} else {
			throw new MprcException("The JMS service URI '" + serviceUri.toString() + "'has incorrect format (expected jms.<activemq uri>?simplequeue=<queue name>");
		}
	}

	/**
	 * Creates a simple message queue. The queue allows one producer to send messages to one consumer.
	 * The consumer can send responses back.
	 *
	 * @return Service based on a simple queue that can be used for both sending and receiving of messages.
	 * @throws edu.mayo.mprc.MprcException SimpleQueue could not be created.
	 */
	public static Service createJmsQueue(URI serviceUri) {
		URI broker = extractJmsBrokerUri(serviceUri);
		String name = extractJmsQueueName(serviceUri);
		UserInfo info = extractJmsUserinfo(serviceUri);

		return new SimpleQueueService(broker, name, info.getUserName(), info.getPassword());
	}
}
