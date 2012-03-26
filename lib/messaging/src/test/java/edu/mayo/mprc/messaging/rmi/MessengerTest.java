package edu.mayo.mprc.messaging.rmi;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.Semaphore;

@Test(sequential = true)
public final class MessengerTest {

	private BoundMessenger<SimpleOneWayMessenger> boundMessenger;
	private OneWayMessenger otherEnd;
	private boolean messageReceived;
	private Semaphore semaphore = new Semaphore(1);

	private BoundMessenger<SimpleOneWayMessenger> boundOneWayMessenger;
	private RemoteObjectHandler remoteObjectHandler = new RemoteObjectHandler();
	private MessengerFactory messengerFactory = new MessengerFactory(remoteObjectHandler);
	private OneWayMessenger oneWayOtherEnd;

	@Test
	public void createMessengerFromMessengerFactoryTest() throws UnknownHostException, RemoteException {
		boundMessenger = messengerFactory.createMessenger();

		boolean found = false;

		final String[] remoteObjectNames = remoteObjectHandler.listlocalRemoteObjectName();

		for (final String remoteObjectName : remoteObjectNames) {
			if (remoteObjectName.equals(boundMessenger.getMessengerInfo().getMessengerRemoteName())) {
				found = true;
				break;
			}
		}

		Assert.assertTrue(found, "Messenger [" + boundMessenger.getMessengerInfo().getMessengerRemoteName() + "] not in local Registry");
	}

	@Test(dependsOnMethods = {"createMessengerFromMessengerFactoryTest"})
	public void messageSendTest() throws NotBoundException, RemoteException, InterruptedException {

		try {
			messageReceived = false;
			final String testMessage = "test message";

			semaphore.acquire();

			boundMessenger.getMessenger().addMessageListener(new MessageListener() {
				private static final long serialVersionUID = 20101221L;

				public void messageReceived(final Object message) {
					if (message.equals(testMessage)) {
						messageReceived = true;
					}
					semaphore.release();
				}
			});

			otherEnd = (OneWayMessenger) remoteObjectHandler.getRemoteObject(boundMessenger.getMessengerInfo().getRegistryInfo(), boundMessenger.getMessengerInfo().getMessengerRemoteName());

			otherEnd.sendMessage(testMessage);

			semaphore.acquire();

			Assert.assertTrue(messageReceived, "Failed to send test message.");
		} finally {
			semaphore.release();
		}
	}

	@Test(dependsOnMethods = {"messageSendTest"})
	public void callbackTest() throws NotBoundException, RemoteException, InterruptedException {
		try {
			messageReceived = false;
			final String testMessage = "test message";

			semaphore.acquire();

			final SimpleOneWayMessenger callbackMessenger = new SimpleOneWayMessenger();

			callbackMessenger.addMessageListener(new MessageListener() {
				private static final long serialVersionUID = 20101221L;

				public void messageReceived(final Object message) {
					if (message.equals(testMessage)) {
						messageReceived = true;
					}
					semaphore.release();
				}
			});

			boundMessenger.getMessenger().sendMessage(testMessage);

			semaphore.acquire();

			Assert.assertTrue(messageReceived, "Failed to send test message.");
		} finally {
			semaphore.release();

		}
	}

	@Test(dependsOnMethods = {"callbackTest"})
	public void createOneWayMessengerFromMessengerFactoryTest() throws UnknownHostException, RemoteException {
		boundOneWayMessenger = messengerFactory.createOneWayMessenger();

		boolean found = false;

		final String[] remoteObjectNames = remoteObjectHandler.listlocalRemoteObjectName();

		for (final String remoteObjectName : remoteObjectNames) {
			if (remoteObjectName.equals(boundOneWayMessenger.getMessengerInfo().getMessengerRemoteName())) {
				found = true;
				break;
			}
		}

		Assert.assertTrue(found, "Messenger [" + boundOneWayMessenger.getMessengerInfo().getMessengerRemoteName() + "] not in local Registry");
	}

	@Test(dependsOnMethods = {"createOneWayMessengerFromMessengerFactoryTest"})
	public void messageSendOneWayTest() throws NotBoundException, RemoteException, InterruptedException {

		try {
			messageReceived = false;
			final String testMessage = "test message";

			semaphore.acquire();

			boundOneWayMessenger.getMessenger().addMessageListener(new MessageListener() {
				private static final long serialVersionUID = 20101221L;

				public void messageReceived(final Object message) {
					if (message.equals(testMessage)) {
						messageReceived = true;
					}
					semaphore.release();
				}
			});

			oneWayOtherEnd = (OneWayMessenger) remoteObjectHandler.getRemoteObject(boundOneWayMessenger.getMessengerInfo().getRegistryInfo(), boundOneWayMessenger.getMessengerInfo().getMessengerRemoteName());

			oneWayOtherEnd.sendMessage(testMessage);

			semaphore.acquire();

			Assert.assertTrue(messageReceived, "Failed to send test message.");
		} finally {
			semaphore.release();
		}
	}
}
