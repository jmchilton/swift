package edu.mayo.mprc.daemon;

import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.daemon.progress.ProgressListener;

/**
 * A connection to a specific runner within another daemon. Technically, this should be called "RunnerConnection".
 */
public interface DaemonConnection {
	/**
	 * @return The file token factory that to translate work packets with files to be sent over the net.
	 */
	FileTokenFactory getFileTokenFactory();

	/**
	 * @return Unique name of this connection.
	 */
	String getConnectionName();

	/**
	 * Sends work to daemon. The progress is monitored by supplied listener. The listener must not be null.
	 *
	 * @param workPacket Any data that represents work for the daemon.
	 * @param listener   Gets information about the work progress. Must not be null.
	 */
	void sendWork(WorkPacket workPacket, ProgressListener listener);

	/**
	 * Receive work.
	 *
	 * @param timeout The method returns null after the timeout passes without any work arriving.
	 * @return Requested work + means of reporting progress.
	 */
	DaemonRequest receiveDaemonRequest(long timeout);

	/**
	 * When you no longer want to receive messages.
	 */
	void close();
}
