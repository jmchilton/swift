package edu.mayo.mprc.scaffold;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.StringUtilities;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides methods for creating Scaffold argument list.
 */
final class ScaffoldArgsBuilder {

	/**
	 * Scaffold install directory.
	 */
	private File installDir;

	/**
	 * @param installDir Scaffold install directory.
	 */
	public ScaffoldArgsBuilder(final File installDir) {
		this.installDir = installDir;
	}

	/**
	 * Builds arguments to call scaffold with. This includes the classpath, memory limit, etc.
	 *
	 * @param memoryLimit   Scaffold memory limit, e.g. "256M".
	 * @param mainClassName Name of the Scaffold class to be executed. For batch searches, use {@link #getScaffoldBatchClassName()}
	 * @return List of scaffold command-line arguments
	 */
	public List<String> buildScaffoldArgs(final String memoryLimit, final String mainClassName) {
		final List<String> args = new ArrayList<String>();
		args.add("-classpath");
		args.add(getScaffoldClassPath());

		args.add("-Xmx" + memoryLimit);

		args.add("-DscaffoldRoot=" + installDir);

		args.add("-Djava.awt.headless=true");

		args.add(mainClassName);

		if (isCommercial()) {
			// force scaffold to not prompt for license.
			args.add("-f");
		}
		return args;
	}

	/**
	 * Returns scaffold work folder - can be either the folder where output files go, or scaffold install dir,
	 * depending on Scaffold version.
	 *
	 * @param outputFolder Where the search output is to be produced.
	 * @return Scaffold working folder.
	 */
	public File getWorkFolder(final File outputFolder) {
		final File workFolder;
		if (isCommercial()) {
			workFolder = new File(installDir.getAbsolutePath());
		} else {
			workFolder = outputFolder;
		}
		return workFolder;
	}

	/**
	 * Returns a name of the class that starts Scaffold in the batch search mode.
	 *
	 * @return
	 */
	public String getScaffoldBatchClassName() {
		return isCommercial() ?
				"com.proteomesoftware.scaffoldlauncher.Batch" :
				"com.proteomesoftware.scaffold.ScaffoldBatch";
	}

	/**
	 * returns the name of the class we should execute to perform indexing
	 */
	public String getScaffoldIndexerClassName() {
		return "com.proteomesoftware.scaffold.DatabaseIndexer";
	}

	/**
	 * Returns true if given directory contains commercial Scaffold.
	 *
	 * @return <code>true</code> if this is commercial Scaffold version.
	 */
	public boolean isCommercial() {
		return new File(installDir, "ScaffoldBatch").exists();
	}

	private String getScaffoldClassPath() {
		// Find all the Scaffold jars
		final List<File> jarFiles = new ArrayList<File>();
		if (isCommercial()) {
			addJarsFromDirectory(installDir, jarFiles);
		} else {
			addJarsFromDirectory(new File(installDir, "lib"), jarFiles);
			jarFiles.add(new File(installDir, "bin"));
		}

		final StringBuilder classPath = new StringBuilder();
		for (final File jar : jarFiles) {
			if (classPath.length() != 0) {
				classPath.append(File.pathSeparator);
			}
			classPath.append(jar.getAbsolutePath());
		}
		return classPath.toString();
	}

	/**
	 * Go through given directory and add all found jar files to a given list.
	 *
	 * @param directory Directory to search for .jar files.
	 * @param jars      Discovered jar files are added to this list.
	 */
	private static void addJarsFromDirectory(final File directory, final List<File> jars) {
		final File[] files = directory.listFiles(new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				return StringUtilities.endsWithIgnoreCase(name, ".jar");
			}
		});
		if (files == null) {
			throw new MprcException("No scaffold .jar files found in " + directory.getAbsolutePath());
		}
		jars.addAll(Arrays.asList(files));
	}
}
