package edu.mayo.mprc.swift.search.task;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.scaffold.ScaffoldWorkPacket;
import edu.mayo.mprc.scaffoldparser.spectra.ScaffoldSpectraReader;
import edu.mayo.mprc.swift.db.SwiftDao;
import edu.mayo.mprc.swift.dbmapping.FileSearch;
import edu.mayo.mprc.swift.dbmapping.ReportData;
import edu.mayo.mprc.swift.dbmapping.SearchRun;
import edu.mayo.mprc.swift.dbmapping.SwiftSearchDefinition;
import edu.mayo.mprc.utilities.progress.ProgressInfo;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

final class ScaffoldTask extends AsyncTaskBase implements ScaffoldTaskI {

	private String experiment;

	/**
	 * Key: Input file search specification.
	 * Value: List of searches performed on the file.
	 */
	private LinkedHashMap<FileSearch, InputFileSearches> inputs = new LinkedHashMap<FileSearch, InputFileSearches>();
	/**
	 * Key: Name of the database
	 * Value: The task that deployed the database
	 */
	private Map<String, DatabaseDeployment> databases = new HashMap<String, DatabaseDeployment>();
	private final File outputFolder;
	private final SwiftSearchDefinition swiftSearchDefinition;
	private final SwiftDao swiftDao;
	private final SearchRun searchRun;

	public ScaffoldTask(String experiment, SwiftSearchDefinition definition, DaemonConnection scaffoldDaemon,
	                    SwiftDao swiftDao, SearchRun searchRun,
	                    File outputFolder, FileTokenFactory fileTokenFactory, boolean fromScratch) {
		super(scaffoldDaemon, fileTokenFactory, fromScratch);
		this.experiment = experiment;
		this.swiftSearchDefinition = definition;
		this.outputFolder = outputFolder;
		this.swiftDao = swiftDao;
		this.searchRun = searchRun;
		setName("Scaffold");
		setDescription("Scaffold search " + this.experiment);
	}

	public void addInput(FileSearch fileSearch, EngineSearchTask search) {
		InputFileSearches searches = inputs.get(fileSearch);
		if (searches == null) {
			searches = new InputFileSearches();
			inputs.put(fileSearch, searches);
		}
		searches.addSearch(search);
	}

	public void addDatabase(String id, DatabaseDeployment dbDeployment) {
		databases.put(id, dbDeployment);
	}

	@Override
	public void setReportData(ReportData reportData) {
		// Do nothing. We do not care for .sfd file
	}

	@Override
	public ReportData getReportData() {
		// We do not care about reports for old version of Scaffold. Report nothing.
		return null;
	}

	/**
	 * @return Work packet to be sent asynchronously. If it returns null, it means the work was done without a need
	 *         to send a work packet.
	 */
	public WorkPacket createWorkPacket() {
		setDescription("Scaffold search " + this.experiment);
		File scaffoldFile = new File(outputFolder, experiment + ".sfd");
		if (!isFromScratch() && scaffoldFile.exists() && scaffoldFile.isFile() && scaffoldFile.length() > 0) {
			return null;
		}

		for (Map.Entry<String, DatabaseDeployment> entry : databases.entrySet()) {
			if (entry.getValue() == null || entry.getValue().getFastaFile() == null) {
				throw new DaemonException("Scaffold deployer probably returned invalid data - null fasta path for database " + entry.getKey());
			}
		}

		// Sanity check - make sure that Scaffold gets some input files
		if (inputs.size() == 0) {
			throw new DaemonException("There are no files defined for this experiment");
		}

		Map<String, File> fastaFiles = new HashMap<String, File>();
		for (Map.Entry<String, DatabaseDeployment> entry : databases.entrySet()) {
			fastaFiles.put(entry.getKey(), entry.getValue().getFastaFile());
		}
		SearchResults searchResults = new SearchResults();
		for (Map.Entry<FileSearch, InputFileSearches> entry : inputs.entrySet()) {
			FileSearchResult result = new FileSearchResult(entry.getKey().getInputFile());
			for (EngineSearchTask search : entry.getValue().getSearches()) {
				result.addResult(
						search.getSearchEngine().getCode(),
						search.getOutputFile());
			}
			searchResults.addResult(result);
		}

		return new ScaffoldWorkPacket(
				outputFolder,
				ScafmlDump.dumpScafmlFile(experiment, swiftSearchDefinition, inputs, outputFolder, searchResults, fastaFiles),
				this.experiment,
				getFullId(),
				isFromScratch());
	}

	@Override
	public String getScaffoldVersion() {
		return "2";
	}

	public File getResultingFile() {
		return new File(outputFolder, experiment + ".sfd");
	}

	public File getScaffoldXmlFile() {
		return new File(outputFolder, experiment + ".xml");
	}

	public File getScaffoldPeptideReportFile() {
		return new File(outputFolder, experiment + ".peptide-report.xls");
	}

	public File getScaffoldSpectraFile() {
		return new File(outputFolder, experiment + ScaffoldSpectraReader.EXTENSION);
	}

	public void onSuccess() {
		completeWhenFilesAppear(getResultingFile(), getScaffoldSpectraFile());
		swiftDao.begin();
		try {
			// Scaffold finished. Store the resulting file.
			swiftDao.storeReport(searchRun.getId(), getResultingFile());
			swiftDao.commit();
		} catch (Exception t) {
			swiftDao.rollback();
			throw new MprcException("Could not store change in task information", t);
		}
	}

	public void onProgress(ProgressInfo progressInfo) {
	}

}
