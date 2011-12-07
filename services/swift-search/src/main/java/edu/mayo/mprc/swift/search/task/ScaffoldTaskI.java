package edu.mayo.mprc.swift.search.task;

import edu.mayo.mprc.swift.dbmapping.FileSearch;
import edu.mayo.mprc.workflow.engine.Task;

import java.io.File;

public interface ScaffoldTaskI extends Task {
	String getScaffoldVersion();

	File getResultingFile();

	File getScaffoldXmlFile();

	File getScaffoldPeptideReportFile();

	File getScaffoldSpectraFile();

	void addInput(FileSearch fileSearch, EngineSearchTask search);

	/**
	 * Which input file/search parameters tuple gets outputs from which engine search.
	 */
	void addDatabase(String id, DatabaseDeployment dbDeployment);
}
