package edu.mayo.mprc.messaging;

import edu.mayo.mprc.MprcException;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class ActiveMQConnectionPool {
	private static final Logger LOGGER = Logger.getLogger(ActiveMQConnectionPool.class);

	private static final String JMS_BROKER_DOWN_MESSAGE = "Connection refused";
	private static final int DEFAULT_RECONNECTION_DELAY_IN_SECONDS = 10;
	private static final Map<ConnectionInfo, Connection> CONNECTIONS = new HashMap<ConnectionInfo, Connection>(1);
	private static final String RECONNECTION_DELAY_PROPERTY = "edu.mayo.mprc.messaging.jms.SimpleQueueService.reconnectionDelay";

	private ActiveMQConnectionPool() {
	}

	/**
	 * Provides a cached connection to given broker. If the broker is down, the method will block indefinitely,
	 * waiting for the broker to come up.
	 *
	 * @param broker   URI of the broker
	 * @param userName Broker login
	 * @param password Broker password
	 * @return
	 */
	public static Connection getConnectionToBroker(URI broker, String userName, String password) {
		final ConnectionInfo info = new ConnectionInfo(broker, userName, password);
		synchronized (CONNECTIONS) {
			Connection connection = CONNECTIONS.get(info);
			if (connection == null) {
				LOGGER.info("Connecting to broker: " + broker + (userName != null ? (" as user " + userName) : ""));
				QueueConnectionFactory connectionFactory = new ActiveMQConnectionFactory(broker);

				int connectionTrialDelay = getConnectionTrialDelay();

				boolean run = true;
				while (run) {
					try {
						if (userName != null && password != null) {
							connection = connectionFactory.createConnection(userName, password);
						} else {
							connection = connectionFactory.createConnection();
						}
						run = false;
					} catch (JMSException e) {
						if (e.getMessage().indexOf(broker.toString()) == -1 || e.getMessage().indexOf(JMS_BROKER_DOWN_MESSAGE) == -1) {
							throw new MprcException("Could not connect to JMS broker", e);
						} else {
							LOGGER.info("JMS broker connection could not be established. Will try to reconnect in " + connectionTrialDelay + " seconds. Broker URI: " + broker.toString());

							try {
								TimeUnit.SECONDS.sleep(connectionTrialDelay);
							} catch (InterruptedException ignore) {
								LOGGER.warn("Cannot connect to broker, interrupted while waiting for retrial.", e);
								run = false;
							}
						}
					}
				}
				CONNECTIONS.put(info, connection);
			}
			return connection;
		}
	}

	private static int getConnectionTrialDelay() {
		int connectionTrialDelay = DEFAULT_RECONNECTION_DELAY_IN_SECONDS;
		try {
			connectionTrialDelay = Integer.parseInt(System.getProperty(RECONNECTION_DELAY_PROPERTY, DEFAULT_RECONNECTION_DELAY_IN_SECONDS + ""));
		} catch (NumberFormatException ignore) {
			//SWALLOWED
		}
		return connectionTrialDelay;
	}

	private static final class ConnectionInfo {
		private final URI broker;
		private final String userName;
		private final String password;

		private ConnectionInfo(URI broker, String userName, String password) {
			this.broker = broker;
			this.userName = userName;
			this.password = password;
		}

		public URI getBroker() {
			return broker;
		}

		public String getUserName() {
			return userName;
		}

		public String getPassword() {
			return password;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof ConnectionInfo)) {
				return false;
			}

			ConnectionInfo that = (ConnectionInfo) o;

			if (broker != null ? !broker.equals(that.broker) : that.broker != null) {
				return false;
			}
			if (password != null ? !password.equals(that.password) : that.password != null) {
				return false;
			}
			if (userName != null ? !userName.equals(that.userName) : that.userName != null) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = broker != null ? broker.hashCode() : 0;
			result = 31 * result + (userName != null ? userName.hashCode() : 0);
			result = 31 * result + (password != null ? password.hashCode() : 0);
			return result;
		}
	}

}
