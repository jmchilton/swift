package edu.mayo.mprc.daemon;

import edu.mayo.mprc.daemon.files.FileTokenHolder;

/**
 * Any work packet sent to the daemon has to implement this interface.
 * All work packets must define serial id in following form:
 * <code>private static final long serialVersionUID = yyyymmdd;</code>
 * ... where <code>yyyymmdd</code> is the date of last modification.
 */
public interface WorkPacket extends FileTokenHolder {
	String getTaskId();

	/**
	 * @return True if the work requested should be redone from scratch, ignoring any previous cached results.
	 */
	boolean isFromScratch();
}
