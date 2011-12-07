package edu.mayo.mprc.daemon.files;

import edu.mayo.mprc.config.DaemonConfigInfo;

import java.io.Serializable;

public interface FileToken extends Serializable {

	/**
	 * Returns the DaemonConfigInfo of the daemon that created this file token.
	 *
	 * @return
	 */
	DaemonConfigInfo getSourceDaemonConfigInfo();

	/**
	 * Returns Token path identifying this file token, for example, the token path for a file
	 * object represented by a file token can be the absolute path of the file.
	 *
	 * @return
	 */
	String getTokenPath();
}
