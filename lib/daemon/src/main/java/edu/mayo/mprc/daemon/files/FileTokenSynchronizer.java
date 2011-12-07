package edu.mayo.mprc.daemon.files;

/**
 * A file token represents a file on a remote system. The synchronizer will make sure that the remote file matches
 * the local file (and vice versa). Download means - they have a newer version, toss the local one. Upload means -
 * I have a newer version, toss the remote one.
 */
public interface FileTokenSynchronizer {
	/**
	 * I have just made a new file at the location identified by FileToken. I am uploading the file to the
	 * requestor so they get the latest version.
	 * If this system and the token source system have a shared file system, no synchronization is
	 * done. Method returns and executes synchronization in background.
	 *
	 * @param myToken
	 */
	void upload(FileToken myToken);

	/**
	 * Same as {@link #upload}, only blocks until the transfer is complete.
	 *
	 * @param myToken
	 */
	void uploadAndWait(FileToken myToken);

	/**
	 * I received a FileToken created by somebody else. Download their version.
	 * Method returns and executes synchronization in background.
	 *
	 * @param theirToken
	 */
	void download(FileToken theirToken);

	/**
	 * Same as {@link #download} only blocks until the transfer is complete.
	 *
	 * @param theirToken
	 */
	void downloadAndWait(FileToken theirToken);
}
