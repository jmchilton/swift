package edu.mayo.mprc.sge;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Convert a daemon with a certain work packet on Sun Grid Engine into a command line.
 */
public final class GridScriptFactory {

	private String javaCommand = "java";

	private String swiftJar;
	private static final String LOG4J_CONFIGURATION = "log4j.configuration";
	private static final String SWIFT_HOME = "swift.home";

	public String getJavaCommand() {
		return javaCommand;
	}

	public void setJavaCommand(String javaCommand) {
		this.javaCommand = javaCommand;
	}

	public String getSwiftJar() {
		return swiftJar;
	}

	public void setSwiftJar(String swiftJar) {
		this.swiftJar = swiftJar;
	}

	private static boolean isWrapper(String wrapper) {
		return !(wrapper == null || wrapper.length() == 0);
	}

	public String getApplicationName(String wrapper) {
		if (isWrapper(wrapper)) {
			return wrapper;
		}
		return javaCommand;
	}


	// We need to pass certain system properties along to make sure logging will keep working

	public List<String> getParameters(String wrapper, File serializedWorkPacket) {
		List<String> params = new ArrayList<String>(6);

		if (isWrapper(wrapper)) {
			params.add(getJavaCommand());
		}

		if (System.getProperty(LOG4J_CONFIGURATION) != null) {
			params.add("-D" + LOG4J_CONFIGURATION + "=" + System.getProperty(LOG4J_CONFIGURATION));
		}
		if (System.getProperty(SWIFT_HOME) != null) {
			params.add("-D" + SWIFT_HOME + "=" + System.getProperty(SWIFT_HOME));
		}

		params.add("-jar");
		params.add(new File(swiftJar).getAbsolutePath());

		params.add("--sge");
		params.add(serializedWorkPacket.getAbsolutePath());

		return params;
	}

}

