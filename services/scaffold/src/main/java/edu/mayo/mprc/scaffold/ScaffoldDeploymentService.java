package edu.mayo.mprc.scaffold;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.enginedeployment.DeploymentRequest;
import edu.mayo.mprc.enginedeployment.DeploymentResult;
import edu.mayo.mprc.enginedeployment.DeploymentService;
import edu.mayo.mprc.fasta.DatabaseAnnotation;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ProcessCaller;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Deploys a Scaffold database - a two step process:
 * <ul>
 * <li>Deploy the .fasta file</li>
 * <li>Index the .fasta file</li>
 * </ul>
 * Indexing means running Scaffold's
 * <code>com.proteomesoftware.scaffold.DatabaseIndexer <fasta file> [<db accession regex>] [<db description regex>]</code>
 * The db accession and db description regex's are the same as the ones we use in the .scafml file.
 */
public final class ScaffoldDeploymentService extends DeploymentService<DeploymentResult> {
	private static final Logger LOGGER = Logger.getLogger(ScaffoldDeploymentService.class);
	public static final String TYPE = "scaffoldDeployer";
	public static final String NAME = "Scaffold DB Deployer";
	public static final String DESC = "Indexes the FASTA database to be used by Scaffold 2. Scaffold 2 has its own indexing process.";

	private String memoryLimit = "256M";
	private String scaffoldJavaVmPath;

	private static final String SCAFFOLD_JAVA_VM_PATH = "scaffoldJavaVmPath";
	private static final String DEPLOYABLE_DB_FOLDER = "deployableDbFolder";
	private static final String INSTALL_DIR = "installDir";

	public ScaffoldDeploymentService() {
	}

	public DeploymentResult performDeployment(DeploymentRequest request) {
		final DeploymentResult reportInto = new DeploymentResult();

		if (wasPreviouslyDeployed(request, reportInto)) {
			return reportInto;
		}

		LOGGER.debug("Asked to deploy curation " + request.getShortName());
		final File toDeploy = this.getFileToDeploy(request.getShortName());
		LOGGER.debug("Deploying file " + toDeploy.getAbsolutePath());
		if (!toDeploy.exists()) {
			LOGGER.debug("Creating file to deploy at " + toDeploy.getAbsolutePath());
			FileUtilities.copyFile(getCurationFile(request), toDeploy, /*overwrite*/false);
		}

		produceIndexFile(toDeploy, request.getAnnotation());

		LOGGER.debug("Setting deployed file to " + toDeploy.getAbsolutePath());

		reportInto.setDeployedFile(toDeploy);

		List<File> generatedFiles = new LinkedList<File>();
		generatedFiles.add(getExistingIndex(toDeploy));
		reportInto.setGeneratedFiles(generatedFiles);

		return reportInto;
	}

	@Override
	public DeploymentResult performUndeployment(DeploymentRequest request) {
		DeploymentResult reportResult = new DeploymentResult();
		File deployedFile = this.getFileToDeploy(request.getShortName());

		this.cleanUpDeployedFiles(deployedFile, reportResult);

		return reportResult;
	}

	@Override
	protected void validateAndDeleteDeploymentRelatedFiles(File deployedFastaFile, File deploymentFolder, List<File> deletedFiles, List<File> notDeletedFiles) {
		File[] deploymentFiles = deploymentFolder.listFiles();

		Pattern pattern = Pattern.compile(deployedFastaFile.getName() + "\\.\\d+\\.index");

		for (File deploymentFile : deploymentFiles) {
			if (!deploymentFile.isDirectory()
					&& (pattern.matcher(deploymentFile.getName()).matches()
					|| deploymentFile.getName().equals(deployedFastaFile.getName()))) {
				if (FileUtilities.deleteNow(deploymentFile)) {
					deletedFiles.add(deploymentFile);
					continue;
				}
			}

			notDeletedFiles.add(deploymentFile);
		}
	}

	private void produceIndexFile(File toDeploy, DatabaseAnnotation annotation) {
		ScaffoldArgsBuilder execution = new ScaffoldArgsBuilder(getEngineRootFolder());

		// Returns work folder for scaffold. Depending on the version, it is either the folder where the output is produced,
		// or the Scaffold install folder itself.
		File workFolder = execution.getWorkFolder(toDeploy.getParentFile());

		List<String> args = execution.buildScaffoldArgs(memoryLimit, execution.getScaffoldIndexerClassName());

		// Make sure the work folder is there.
		FileUtilities.ensureFolderExists(workFolder);

		List<String> thisargs = new ArrayList<String>(args.size() + 4);
		thisargs.add(scaffoldJavaVmPath);
		for (String arg : args) {
			thisargs.add(arg);
		}
		thisargs.add(toDeploy.getAbsolutePath());
		thisargs.add(annotation.getAccessionRegex());
		thisargs.add(annotation.getDescriptionRegex());

		ProcessBuilder processBuilder = new ProcessBuilder(thisargs)
				.directory(workFolder);

		ProcessCaller caller = new ProcessCaller(processBuilder);

		caller.run();
		int exitValue = caller.getExitValue();

		LOGGER.debug("Scaffold finished with exit value " + String.valueOf(exitValue));
		if (exitValue != 0) {
			throw new DaemonException("Non-zero exit value=" + exitValue + " for call: " + caller.getFailedCallDescription());
		}

		if (null == getExistingIndex(toDeploy)) {
			throw new MprcException("Index creation failed.  Could not find any created index with the fasta file: " + toDeploy.getAbsolutePath());
		}
	}

	protected File getExistingIndex(File sisterFasta) {
		final Pattern indexPattern = Pattern.compile(Pattern.quote(sisterFasta.getName()) + "\\.[^.]+\\.index");
		File[] deployedIndices = sisterFasta.getParentFile().listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return indexPattern.matcher(name).matches();
			}
		});

		if (deployedIndices.length == 0) {
			return null;
		} else {
			return deployedIndices[0];
		}
	}

	/**
	 * Finds out the path to the file that we will want to deploy to.  This does not perform the copy action but only finds
	 * out where we want to place the FASTA file.
	 */
	protected File getFileToDeploy(String uniqueName) {
		File deploymentFolder = getCurrentDeploymentFolder(uniqueName);
		FileUtilities.ensureFolderExists(deploymentFolder);
		return new File(deploymentFolder, uniqueName + ".fasta");
	}

	private File getCurrentDeploymentFolder(String uniqueName) {
		return new File(getConfigDeploymentFolder(), uniqueName);
	}

	public boolean wasPreviouslyDeployed(DeploymentRequest request, DeploymentResult reportInto) {
		final File toCheckForPreviousDeployment = this.getFileToDeploy(request.getShortName());

		if (!toCheckForPreviousDeployment.exists()) {
			return false;
		}
		final File deployedIndex = getExistingIndex(toCheckForPreviousDeployment);

		if (null != deployedIndex) {
			reportInto.setDeployedFile(toCheckForPreviousDeployment);
			List<File> generatedFiles = new LinkedList();
			generatedFiles.add(deployedIndex);
			reportInto.setGeneratedFiles(generatedFiles);
			LOGGER.debug("File already deployed: " + deployedIndex.getAbsolutePath());
			return true;
		}
		return false;
	}

	public String getScaffoldJavaVmPath() {
		return scaffoldJavaVmPath;
	}

	public void setScaffoldJavaVmPath(String scaffoldJavaVmPath) {
		this.scaffoldJavaVmPath = scaffoldJavaVmPath;
	}

	public String getMemoryLimit() {
		return memoryLimit;
	}

	public void setMemoryLimit(String memoryLimit) {
		this.memoryLimit = memoryLimit;
	}

	private static final Map<String, List<ProgressReporter>> CO_DEPLOYMENTS = new HashMap<String, List<ProgressReporter>>();

	public Map<String, List<ProgressReporter>> getCoDeployments() {
		return CO_DEPLOYMENTS;
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {
		private String scaffoldJavaVmPath;
		private String deployableDbFolder;
		private String installDir;

		public Config() {
		}

		public Config(String scaffoldJavaVmPath, String deployableDbFolder, String installDir) {
			this.scaffoldJavaVmPath = scaffoldJavaVmPath;
			this.deployableDbFolder = deployableDbFolder;
			this.installDir = installDir;
		}

		public String getScaffoldJavaVmPath() {
			return scaffoldJavaVmPath;
		}

		public void setScaffoldJavaVmPath(String scaffoldJavaVmPath) {
			this.scaffoldJavaVmPath = scaffoldJavaVmPath;
		}

		public String getDeployableDbFolder() {
			return deployableDbFolder;
		}

		public void setDeployableDbFolder(String deployableDbFolder) {
			this.deployableDbFolder = deployableDbFolder;
		}

		public String getInstallDir() {
			return installDir;
		}

		public void setInstallDir(String installDir) {
			this.installDir = installDir;
		}

		public Map<String, String> save(DependencyResolver resolver) {
			Map<String, String> map = new TreeMap<String, String>();
			map.put(SCAFFOLD_JAVA_VM_PATH, scaffoldJavaVmPath);
			map.put(DEPLOYABLE_DB_FOLDER, deployableDbFolder);
			map.put(INSTALL_DIR, installDir);
			return map;
		}

		public void load(Map<String, String> values, DependencyResolver resolver) {
			scaffoldJavaVmPath = values.get(SCAFFOLD_JAVA_VM_PATH);
			deployableDbFolder = values.get(DEPLOYABLE_DB_FOLDER);
			installDir = values.get(INSTALL_DIR);
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {
		@Override
		public Worker create(Config config, DependencyResolver dependencies) {
			ScaffoldDeploymentService worker = null;
			try {
				worker = new ScaffoldDeploymentService();
				worker.setDeployableDbFolder(new File(config.getDeployableDbFolder()).getAbsoluteFile());
				worker.setScaffoldJavaVmPath(config.getScaffoldJavaVmPath());
				worker.setEngineRootFolder(new File(config.getInstallDir()).getAbsoluteFile());

			} catch (Exception e) {
				throw new MprcException("Scaffold deployment service worker could not be created.", e);
			}
			return worker;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder.property(INSTALL_DIR, "Installation Folder", "Scaffold installation folder").existingDirectory().required()
					.property(DEPLOYABLE_DB_FOLDER, "Database Folder", "Folder where deployer copies database files to")
					.required()
					.existingDirectory()
					.defaultValue("var/scaffold_index")

					.property(SCAFFOLD_JAVA_VM_PATH, "Java VM Path", "<tt>java</tt> executable to run Scaffold with")
					.required()
					.executable(Arrays.asList("-version"))
					.defaultValue("java");
		}
	}
}
