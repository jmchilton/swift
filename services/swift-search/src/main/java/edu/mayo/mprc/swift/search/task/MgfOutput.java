package edu.mayo.mprc.swift.search.task;

import edu.mayo.mprc.workflow.engine.Task;

import java.io.File;

/**
 * Any task that produces an mgf file.
 */
interface MgfOutput extends Task {
	File getFilteredMgfFile();
}
