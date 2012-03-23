package edu.mayo.mprc.swift.commands;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.qa.RAWDumpWorker;
import edu.mayo.mprc.swift.search.SwiftSearcher;

import java.util.List;

/**
 * @author Roman Zenka
 */
public class LoadToSearchDb implements SwiftCommand {
	@Override
	public String getName() {
		return "load-to-search-db";
	}

	@Override
	public String getDescription() {
		return "Loads a specified Swift search results (using the search database id) into the search database.";
	}

	/**
	 * Load given search results into the database.
	 * This is equivalent to a "shortened" Swift search that:
	 * 1) dumps .RAW metadata
	 * 2) dumps Scaffold spectrum report (if missing) using Scaffold 3
	 * 3) loads the FASTA database
	 * 4) loads the Scaffold dump
	 *
	 * @param environment The Swift environment to execute within.
	 */
	public void run(SwiftEnvironment environment) {
		try {
			final List<ResourceConfig> searchers = environment.getDaemonConfig().getApplicationConfig().getModulesOfConfigType(SwiftSearcher.Config.class);
			if (searchers.size() != 1) {
				throw new MprcException("More than one Swift Searcher defined in this Swift install");
			}
			final SwiftSearcher.Config searcherConfig = (SwiftSearcher.Config) searchers.get(0);

			if (searcherConfig.getRawdump() == null) {
				throw new MprcException("The swift searcher does not define a " + RAWDumpWorker.NAME + " module");
			}

			DaemonConnection rawDump = environment.getConnection(searcherConfig.getRawdump());

		} catch (Exception e) {
			throw new MprcException("Could not load into Swift search database", e);
		}
	}


}
