package edu.mayo.mprc.daemon;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.FactoryBase;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.PropertyChangeListener;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.config.ui.UiResponse;
import edu.mayo.mprc.utilities.exceptions.ExceptionUtilities;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.HashMap;
import java.util.Map;

/**
 * This module runs an embedded message broker at given URL.
 * <p/>
 * The broker is being set as non-persistent so we do not have to deal with the broker creating temporary
 * files. If the user wants permanent broker, they can install standalone ActiveMQ and configure it separately.
 */
public final class MessageBroker {
	private static final Logger LOGGER = Logger.getLogger(MessageBroker.class);
	public static final String TYPE = "messageBroker";
	public static final String NAME = "Message Broker";
	public static final String DESC = "Daemons need a JMS (Java Messaging Service) message broker to communicate with each other. The broker can be external or embedded within Swift. It is enough for one of the daemons to define access to the broker. All other daemons then connect to it using the URI you configure.</p><p>A more robust alternative is to use an external broker. Swift was tested with Apache ActiveMQ 5.2.0, which can be obtained at <a href=\"http://activemq.apache.org/\">http://activemq.apache.org/</a>. To use external broker, download, configure and run it, fill in the broker URI and uncheck the 'Run embedded broker' checkbox.</p>";

	private String brokerUrl;
	private String embeddedBrokerUrl;
	private BrokerService broker;
	private boolean embedded;
	private boolean useJmx;

	private static final String EMBEDDED = "embedded";
	private static final String USE_JMX = "useJmx";
	public static final String BROKER_URL = "brokerUrl";
	private static final String EMBEDDED_BROKER_URL = "embeddedBrokerUrl";

	public MessageBroker() {
	}

	public String getBrokerUrl() {
		return brokerUrl;
	}

	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}

	public String getEmbeddedBrokerUrl() {
		return embeddedBrokerUrl;
	}

	public void setEmbeddedBrokerUrl(String embeddedBrokerUrl) {
		this.embeddedBrokerUrl = embeddedBrokerUrl;
	}

	public boolean isEmbedded() {
		return embedded;
	}

	public void setEmbedded(boolean embedded) {
		this.embedded = embedded;
	}

	public boolean isUseJmx() {
		return useJmx;
	}

	public void setUseJmx(boolean useJmx) {
		this.useJmx = useJmx;
	}

	public void start() {
		broker = new BrokerService();
		try {
			broker.addConnector(brokerUrl);
			broker.setPersistent(false);
			broker.setUseJmx(useJmx);
			broker.start();
		} catch (Exception e) {
			throw new MprcException("The message broker failed to start", e);
		}
	}

	/**
	 * A factory capable of creating the resource
	 */
	public static final class Factory extends FactoryBase<Config, MessageBroker> {
		@Override
		public MessageBroker create(Config config, DependencyResolver dependencies) {
			final MessageBroker broker = new MessageBroker();

			broker.setEmbedded(config.isEmbedded());
			broker.setUseJmx(config.isUseJmx());

			broker.setBrokerUrl(config.effectiveBrokerUrl());

			if (broker.isEmbedded()) {
				broker.start();
			}
			return broker;
		}
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {
		private String brokerUrl;
		private String embeddedBrokerUrl;
		private String embedded;
		private String useJmx;

		public Config() {
		}

		public static Config getEmbeddedBroker() {
			final Config config = new Config();
			config.brokerUrl = "vm://broker";
			config.embeddedBrokerUrl = "vm://broker";
			config.embedded = "true";
			config.useJmx = "false";

			return config;
		}

		@Override
		public Map<String, String> save(DependencyResolver resolver) {
			final Map<String, String> map = new HashMap<String, String>(1);
			map.put(BROKER_URL, brokerUrl);
			map.put(EMBEDDED_BROKER_URL, embeddedBrokerUrl);
			map.put(EMBEDDED, embedded);
			map.put(USE_JMX, useJmx);
			return map;
		}

		@Override
		public void load(Map<String, String> values, DependencyResolver resolver) {
			brokerUrl = values.get(BROKER_URL);
			embeddedBrokerUrl = values.get(EMBEDDED_BROKER_URL);
			embedded = values.get(EMBEDDED);
			useJmx = values.get(USE_JMX);
		}

		@Override
		public int getPriority() {
			return 10;
		}

		public String getBrokerUrl() {
			return brokerUrl;
		}

		public void setBrokerUrl(String brokerUrl) {
			this.brokerUrl = brokerUrl;
		}

		public String getEmbeddedBrokerUrl() {
			return embeddedBrokerUrl;
		}

		public String getEmbedded() {
			return embedded;
		}

		public String getUseJmx() {
			return useJmx;
		}

		public boolean isUseJmx() {
			return getUseJmx().equalsIgnoreCase("true");
		}

		public boolean isEmbedded() {
			return getEmbedded() != null && getEmbedded().equalsIgnoreCase("true");
		}

		public String validate() {
			Connection connection = null;
			try {
				if (brokerUrl != null && brokerUrl.length() > 0) {
					ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
					connection = connectionFactory.createConnection();
				} else {
					return "JMS broker URL is not valid.";
				}
			} catch (JMSException e) {
				return "JMS broker connection could not be established Error: " + e.getMessage();
			} finally {
				try {
					if (connection != null) {
						connection.close();
					}
				} catch (JMSException e) {
					//SWALLOWED
					LOGGER.warn("Error closing JMS broker connection.", e);
				}
			}
			return null;
		}

		public String effectiveBrokerUrl() {
			if (isEmbedded() && getEmbeddedBrokerUrl().trim().length() > 0) {
				return getEmbeddedBrokerUrl().trim();
			} else {
				return getBrokerUrl().trim();
			}
		}
	}

	public static final class Ui implements ServiceUiFactory {

		public static final String DEFAULT_PORT = "61616";

		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder.property(BROKER_URL, "Connection URI", "The URI defines where the broker runs (hostname and port) as well as the protocol used to communicate with it." +
					" The initial default value is set to use failover. This settign will allow for reconnection attempts if JMS broker system goes down.")
					.required().defaultValue(getDefaultBrokerUrl(daemon))
					.validateOnDemand(new PropertyChangeListener() {
						@Override
						public void propertyChanged(ResourceConfig config, String propertyName, String newValue, UiResponse response, boolean validationRequested) {
							if (!(config instanceof Config)) {
								ExceptionUtilities.throwCastException(config, Config.class);
								return;
							}
							final Config brokerConfig = (Config) config;
							final String error = brokerConfig.validate();
							response.displayPropertyError(config, propertyName, error);
						}

						@Override
						public void fixError(ResourceConfig config, String propertyName, String action) {
						}
					})

					.property(EMBEDDED, "Run embedded broker",
							"When this field is checked, Swift will run its own broker within the daemon. The configuration will be taken from the connection URI."
									+ " Embedded ActiveMQ 5.2.0 can take a multitude of URI formats."
									+ " Check out <a href=\"http://activemq.apache.org/uri-protocols.html\">http://activemq.apache.org/uri-protocols.html</a> for several tips."
									+ " We have experimented mostly with the <tt>tcp://host:port</tt> protocol. "
									+ "<p>Note: When running the embedded broker, we manually switch off persistence. URIs that enable persistence may fail.</p>"
									+ " <p>When this field is unchecked, we assume that external broker is already running at the given URI.</p>")
					.required().boolValue().defaultValue("true").enable(EMBEDDED_BROKER_URL, true)

					.property(EMBEDDED_BROKER_URL, "Embedded broker URI",
							"The URI defines embedded JMS broker system. This URI may be different from the connection URI, for example, connection URI may have "
									+ "failover configuration options while this URI will not.").defaultValue("tcp://" + daemon.getHostName() + ":" + DEFAULT_PORT)
					.defaultValue("tcp://" + daemon.getHostName() + ":" + DEFAULT_PORT)

					.property(USE_JMX, "Enable the use of JMX", "").boolValue().defaultValue("false");
		}

		public static String getDefaultBrokerUrl(DaemonConfig daemon) {
			return "failover:(tcp://" + daemon.getHostName() + ":" + DEFAULT_PORT + ")";
		}
	}
}
