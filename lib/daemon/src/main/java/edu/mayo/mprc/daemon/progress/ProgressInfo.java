package edu.mayo.mprc.daemon.progress;

import java.io.Serializable;

/**
 * Progress information.
 * All progress infos must define serial id in following form:
 * <code>private static final long serialVersionUID = yyyymmdd;</code>
 * ... where <code>yyyymmdd</code> is the date of last modification.
 */
public interface ProgressInfo extends Serializable {

}
