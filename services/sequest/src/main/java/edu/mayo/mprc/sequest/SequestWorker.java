package edu.mayo.mprc.sequest;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.sequest.core.Mgf2SequestCaller;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;


public final class SequestWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(SequestWorker.class);
	public static final String TYPE = "sequest";
	public static final String NAME = "Sequest";
	public static final String DESC = "Sequest search engine support. <p>Swift was tested against cluster version of Sequest on Linux, utilizing PVM.</p>";

	private File pvmHosts;
	private String sequestCommand = "sequest";

	private static final String PVM_HOSTS = "pvmHosts";
	private static final String SEQUEST_COMMAND = "sequestCommand";

	public void processRequest(WorkPacket workPacket, ProgressReporter progressReporter) {
		try {
			progressReporter.reportStart();
			process(workPacket);
			workPacket.synchronizeFileTokensOnReceiver();
			progressReporter.reportSuccess();
		} catch (Exception t) {
			progressReporter.reportFailure(t);
		}
	}

	private void process(WorkPacket workPacket) {
		SequestMGFWorkPacket sequestWorkPacket = null;
		if (workPacket instanceof SequestMGFWorkPacket) {
			sequestWorkPacket = (SequestMGFWorkPacket) workPacket;

			assert sequestWorkPacket.getInputFile() != null : "Sequest search failed: The .mgf file was not specified";
			assert sequestWorkPacket.getDatabaseFile() != null : "Sequest search failed: The .hdr file was not specified";
			assert sequestWorkPacket.getOutputFile() != null : "Sequest search failed: The output folder was not specified";
			assert sequestWorkPacket.getSearchParamsFile() != null : "Sequest search failed: The search parameters were not specified";

		} else {
			throw new DaemonException("Unexpected packet type " + workPacket.getClass().getName() + ", expected " + SequestMGFWorkPacket.class.getName());
		}

		LOGGER.debug("Starting sequest search"
				+ "\n\tmgf file: " + sequestWorkPacket.getInputFile()
				+ "\n\thdr file: " + sequestWorkPacket.getDatabaseFile()
				+ "\n\toutput file: " + sequestWorkPacket.getOutputFile()
				+ "\n\tsearch params: " + sequestWorkPacket.getSearchParamsFile());

		sequestWorkPacket.waitForInputFiles();

		FileUtilities.ensureFolderExists(sequestWorkPacket.getOutputFile().getParentFile());

		Mgf2SequestCaller m = new Mgf2SequestCaller();

		m.setHostsFile(pvmHosts);
		m.setSequestExe(sequestCommand);

		m.callSequest(
				sequestWorkPacket.getOutputFile(),
				sequestWorkPacket.getSearchParamsFile(),
				sequestWorkPacket.getInputFile(),
				120 * 1000/* start timeout */,
				10 * 60 * 1000 /* watchdog timeout */,
				sequestWorkPacket.getDatabaseFile()
		);

		FileUtilities.restoreUmaskRights(sequestWorkPacket.getOutputFile().getParentFile(), true);

		LOGGER.debug("Sequest search done");
	}

	public File getPvmHosts() {
		return pvmHosts;
	}

	public void setPvmHosts(File pvmHosts) {
		this.pvmHosts = pvmHosts;
	}

	public String getSequestCommand() {
		return sequestCommand;
	}

	public void setSequestCommand(String sequestCommand) {
		this.sequestCommand = sequestCommand;
	}

	@Override
	public String toString() {
		return "Sequest worker";
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {
		@Override
		public Worker create(Config config, DependencyResolver dependencies) {
			SequestWorker worker = new SequestWorker();
			worker.setPvmHosts(new File(config.getPvmHosts()).getAbsoluteFile());
			worker.setSequestCommand(config.getSequestCommand());
			return worker;
		}
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {
		private String pvmHosts;
		private String sequestCommand;

		public Config() {
		}

		public Config(String sequestCommand, String pvmHosts) {
			this.pvmHosts = pvmHosts;
			this.sequestCommand = sequestCommand;
		}

		public String getSequestCommand() {
			return sequestCommand;
		}

		public void setSequestCommand(String sequestCommand) {
			this.sequestCommand = sequestCommand;
		}

		public String getPvmHosts() {
			return pvmHosts;
		}

		public void setPvmHosts(String pvmHosts) {
			this.pvmHosts = pvmHosts;
		}

		public Map<String, String> save(DependencyResolver resolver) {
			Map<String, String> map = new TreeMap<String, String>();
			map.put(PVM_HOSTS, pvmHosts);
			map.put(SEQUEST_COMMAND, sequestCommand);
			return map;
		}

		public void load(Map<String, String> values, DependencyResolver resolver) {
			pvmHosts = values.get(PVM_HOSTS);
			sequestCommand = values.get(SEQUEST_COMMAND);
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder.property(SEQUEST_COMMAND, "Sequest Command", "Sequest command line executable.")
					.required()
					.executable(Arrays.asList("-v"))

					.property(PVM_HOSTS, "PVM Host List File", "Sequest pvm host list file. <a href=\"http://www.netlib.org/pvm3/book/node137.html\">See documentation</a>."
							+ "<p>Swift needs this file to know all the nodes where Sequest runs. "
							+ " Since Sequest has been extremely unstable for us, we are sometimes forced to connect to the nodes and clean up the Sequest daemons."
							+ " Make sure the sequest node can <tt>ssh</tt> to all the child nodes, otherwise the Swift cleanup will not work.</p>"
							+ "<p>This file must be formatted as follows:</p>" +
							"<pre>node1 &lt;options&gt;\n" +
							"node2 &lt;options&gt;\n" +
							"node3 &lt;options&gt;\n" +
							"...</pre>" +
							"<p>The first part of each entry is the name of a node, the options are ignored by Swift.</p>")
					.required()
					.existingFile();
		}
	}
}
