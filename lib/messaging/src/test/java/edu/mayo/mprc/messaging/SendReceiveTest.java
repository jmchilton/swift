package edu.mayo.mprc.messaging;

import edu.mayo.mprc.MprcException;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple test of the messaging.
 */
public final class SendReceiveTest extends MessagingTestBase {
	private static final Logger LOGGER = Logger.getLogger(SendReceiveTest.class);
	private static final String REQUEST_1 = "Request!";
	private static final String RESPONSE_1 = "Response!";
	private static final String REQUEST_2 = "Request 2!";
	private static final String RESPONSE_2 = "Response 2!";
	private static final int TOTAL_REQUESTS = 1023;
	public static final int PRIORITY = 5;
	private final AtomicInteger numRequests = new AtomicInteger(0);
	private final AtomicInteger numResponses = new AtomicInteger(0);
	private int expectedNumRequests;
	private int expectedNumResponses;

	private static final int MAX_WAIT_FOR_MESSAGES_TO_ARRIVE = 10000;

	private void logChatty(String message) {
		// LOGGER.debug(message);
	}

	private void restartBroker() {
		LOGGER.debug("Restarting broker");
		broker.deleteAllMessages();
		LOGGER.debug("Broker restarted");
	}

	private void init() throws Exception {
		startBroker();
		numRequests.set(0);
		numResponses.set(0);
		expectedNumRequests = 0;
		expectedNumResponses = 0;
	}

	private void cleanup() throws Exception {
		int totalSleep = 0;
		while (numRequests.get() != expectedNumRequests || numResponses.get() != expectedNumResponses) {
			logChatty("Waiting for messages to be delivered. Requests :" + numRequests.get() + "/" + expectedNumRequests + " Responses: " + numResponses.get() + "/" + expectedNumResponses);
			Thread.sleep(100);
			totalSleep += 100;
			if (totalSleep >= MAX_WAIT_FOR_MESSAGES_TO_ARRIVE) {
				Assert.fail("Slept for " + MAX_WAIT_FOR_MESSAGES_TO_ARRIVE + " and yet the expected amount of messages was not received: "
						+ numRequests.get() + "/" + expectedNumRequests + " requests, "
						+ numResponses.get() + "/" + expectedNumResponses + " responses.");
				break;
			}
		}

		Assert.assertEquals(numRequests.get(), expectedNumRequests, "The requests did not arrive ok.");
		Assert.assertEquals(numResponses.get(), expectedNumResponses, "The responses did not arrive ok.");
	}

	@Test(enabled = true, groups = {"unit", "fast"})
	public void testJmsRequestOnly() throws Exception {
		init();
		expectedNumRequests = 1;
		expectedNumResponses = 0;

		(new Thread() {
			public void run() {
				Request request = receiveRequest();
				service.stopReceiving();

				Assert.assertEquals(request.getMessageData(), REQUEST_1, "Received wrong data");
				sendResponse(request);
				numRequests.incrementAndGet();
			}
		}).start();

		// Not interested in response
		LOGGER.debug("Sending single request");
		service.sendRequest(REQUEST_1, PRIORITY, null);
		cleanup();
	}

	private void sendResponse(Request request) {
		try {
			logChatty("Sending response to request");
			request.sendResponse(RESPONSE_1, true);
		} catch (MprcException e) {
			Assert.fail(e.getMessage());
		}
	}

	private Request receiveRequest() {
		logChatty("Waiting to receive request");
		Request request = service.receiveRequest(10000);
		if (request == null) {
			throw new MprcException("No request received within 10 seconds");
		}
		logChatty("Request received: " + request.toString());
		request.processed();
		return request;
	}

	@Test(enabled = true, groups = {"unit", "fast"}, dependsOnMethods = {"testJmsRequestOnly"})
	public void testJmsRequestResponse() throws Exception {
		init();
		expectedNumRequests = 1;
		expectedNumResponses = 1;

		(new Thread() {
			public void run() {
				Request request = receiveRequest();
				service.stopReceiving();

				Assert.assertEquals(request.getMessageData(), REQUEST_1, "Received wrong data");
				sendResponse(request);
				numRequests.incrementAndGet();
			}
		}).start();

		// Send request
		logChatty("Sending request, waiting for response");
		service.sendRequest(REQUEST_1, PRIORITY, new ResponseListener() {
			public void responseReceived(Serializable response, boolean isClosing) {
				Assert.assertEquals(response, RESPONSE_1, "Response does not match expectations");
				Assert.assertTrue(isClosing, "This must be the only message sent");
				numResponses.incrementAndGet();
			}
		});
		cleanup();
	}

	@Test(enabled = true, groups = {"unit", "fast"})
	public void testMultipleMessages() throws Exception {
		init();
		expectedNumRequests = TOTAL_REQUESTS;
		expectedNumResponses = TOTAL_REQUESTS;

		final Thread thread = new Thread() {
			public void run() {
				while (TOTAL_REQUESTS > numRequests.get()) {
					Request request = null;
					try {
						logChatty("Receiving request");
						request = service.receiveRequest(0);
						if (request != null) {
							logChatty("Request received: " + request.toString());
							String response = "response " + Integer.toString(numRequests.get() + 1);
							logChatty("Sending response: " + response);
							request.sendResponse(response, TOTAL_REQUESTS - 1 == numRequests.get());
							logChatty("Response sent: " + response);
						} else {
							LOGGER.debug("Request was null - no request arrived within timeout");
						}
					} catch (MprcException e) {
						Assert.fail(e.getMessage());
					} finally {
						if (request != null) {
							numRequests.incrementAndGet();
							logChatty("Request marked as processed: " + request.toString());
							request.processed();
						}
					}
				}
				service.stopReceiving();
			}
		};
		thread.start();

		ResponseListener listener = new ResponseListener() {
			private AtomicInteger expectedRequest = new AtomicInteger(1);

			public void responseReceived(Serializable response, boolean isLast) {
				logChatty("Response received: " + response.toString() + " is last: " + isLast);
				numResponses.incrementAndGet();
//				final int expected = expectedRequest.getAndIncrement();
				// Assert.assertEquals(response, "response " + Integer.toString(expected), "Response does not match expectations");
//				Assert.assertEquals(isLast, TOTAL_REQUESTS == expected, "Closing flag not set properly");
			}
		};

		for (int i = 1; i <= TOTAL_REQUESTS; i++) {
			// Send request
			logChatty("Sending request #" + i);
			service.sendRequest("request " + Integer.toString(i), PRIORITY, listener);
		}

		thread.join(10000);
		cleanup();
	}

	@Test(enabled = true, groups = {"unit", "fast"})
	public void testMultipleMessagesWithRestart() throws Exception {
		init();
		restartBroker();
		testMultipleMessages();
		cleanup();
	}

	@Test(dependsOnMethods = {"testMultipleMessagesWithRestart"})
	public void shouldContinueProcessingWhenInterrupted() throws Exception {
		init();

		// Send two messages
		for (int i = 1; i <= 2; i++) {
			service.sendRequest(Integer.valueOf(i), PRIORITY, new ResponseListener() {
				@Override
				public void responseReceived(Serializable response, boolean isLast) {
					LOGGER.debug(response);
				}
			});
		}


		// Process first request
		{
			final Request request = service.receiveRequest(10000);
			Assert.assertEquals(request.getMessageData(), 1, "Request value does not match");
			request.processed();
		}

		// Process second request but do not acknowledge
		{
			final Request request = service.receiveRequest(10000);
			Assert.assertEquals(request.getMessageData(), 2, "Request value does not match");
			// Close the receiver instead
			service.stopReceiving();
		}

		// Process second request
		{
			final Request request = service.receiveRequest(10000);
			Assert.assertEquals(request.getMessageData(), 2, "Request value does not match");
			request.processed();
		}

		final Request request = service.receiveRequest(10);
		Assert.assertNull(request, "There should be no request left");

		service.stopReceiving();

		cleanup();
	}

}
