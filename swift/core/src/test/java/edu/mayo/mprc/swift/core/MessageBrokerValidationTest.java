package edu.mayo.mprc.swift.core;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.MessageBroker;
import edu.mayo.mprc.messaging.JmsBrokerThread;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;

public final class MessageBrokerValidationTest {

	@Test
	public void validateJMSBrokerTest() {
		JmsBrokerThread broker = null;
		try {
			broker = new JmsBrokerThread(new URI("tcp://localhost:8783"), null);
			MessageBroker.Config config = new MessageBroker.Config();
			config.setBrokerUrl(broker.getURI().toString());
			Assert.assertNull(config.validate(), "JMS broker validation failed. Validation should had been successful.");
		} catch (Exception e) {
			throw new MprcException("JMS broker validation test failed", e);
		} finally {
			if (broker != null) {
				broker.stopBroker();
			}
		}
	}

	@Test(dependsOnMethods = {"validateJMSBrokerTest"})
	public void validateJMSBrokerFailedTest() {
		MessageBroker.Config config = new MessageBroker.Config();
		config.setBrokerUrl("http://localhost:1234");
		Assert.assertNotNull(config.validate(), "JMS broker validation did not fail. Validation should had failed.");
	}
}
