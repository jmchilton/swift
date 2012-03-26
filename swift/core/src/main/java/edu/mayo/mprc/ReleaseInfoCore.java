package edu.mayo.mprc;

import java.io.Serializable;
import java.util.ResourceBundle;

/**
 * A java file that will have some revision information inserted before being compiled.  This will allow the application
 * to access this information at run-time.
 */
public final class ReleaseInfoCore implements Serializable {
	private static final long serialVersionUID = 20080128;

	public static String infoString() {
		final ResourceBundle bundle = ResourceBundle.getBundle("build");
		final String buildNumber = bundle.getString("build.number");
		return buildNumber;
	}

	public static void main(final String[] args) {
		System.out.println(ReleaseInfoCore.infoString());
	}

}
