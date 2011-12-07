package edu.mayo.mprc.filesharing.jms;

import edu.mayo.mprc.filesharing.FileTransfer;
import edu.mayo.mprc.filesharing.FileTransferHandler;
import edu.mayo.mprc.utilities.FileUtilities;

import java.io.File;
import java.net.URI;

/**
 * Test class.
 */
public final class JmsFileTransferClient {
	private JmsFileTransferClient() {
	}

	public static void main(String[] args) throws Exception {
		if (args.length >= 3) {
			String brokerURl = args[0];
			String remoteFilePath = args[1];
			File localFile = new File(args[2]);

			JmsFileTransferHandlerFactory factory = new JmsFileTransferHandlerFactory(new URI(brokerURl), null, null);
			FileTransferHandler fileSharing = factory.createFileSharing("client");
			fileSharing.startProcessingRequests();

			long startTime = System.currentTimeMillis();

			FileTransfer fileTransfer = fileSharing.getFile("server", remoteFilePath, localFile);
			File result = fileTransfer.done().get(0);

			long finishTime = System.currentTimeMillis();

			if (result.length() > 0) {
				FileUtilities.out("File transfer succeeded.");
				FileUtilities.out("Transfer of " + result.length() + " bytes took " + (finishTime - startTime) / 1000 + " seconds.");
			} else {
				FileUtilities.out("File transfer failed.");
				fileTransfer.getErrorException().printStackTrace();
			}

			System.exit(0);
		} else {
			FileUtilities.err("Usage: java -jar filesharing.jar edu.mayo.mprc.filesharing.jms.JmsFileTransferClient <brokerURI> <remoteFilePath> <localFilePath>");
			System.exit(1);
		}
	}
}
