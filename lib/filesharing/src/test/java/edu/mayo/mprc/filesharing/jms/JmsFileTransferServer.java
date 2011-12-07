package edu.mayo.mprc.filesharing.jms;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.filesharing.FileTransferHandler;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.activemq.broker.BrokerService;

import java.net.URI;

/**
 * Test class.
 */
public final class JmsFileTransferServer {
	private static URI uri;
	private static BrokerService broker;
	private static String brokerUri;

	private JmsFileTransferServer() {
	}

	public static void main(String[] args) {
		if (args.length > 0) {

			brokerUri = args[0];

			try {
				uri = new URI(brokerUri);
				broker = new BrokerService();
				broker.setPersistent(false);
				broker.setUseJmx(true);
				broker.addConnector(uri);
				broker.start();

				FileUtilities.out("Broker URI: " + broker.getTransportConnectors().get(0).getUri().toString());

				(new Thread() {
					public void run() {
						JmsFileTransferHandlerFactory factory = new JmsFileTransferHandlerFactory(uri, null, null);
						FileTransferHandler fileSharing = factory.createFileSharing("server");
						fileSharing.startProcessingRequests();
					}
				}).start();

			} catch (Exception e) {
				throw new MprcException("Could not start broker for uri " + uri.toString(), e);
			}
		} else {
			FileUtilities.err("Usage: java -jar filesharing.jar edu.mayo.mprc.filesharing.jms.JmsFileTransferServer <brokerURI>");
		}
	}
}