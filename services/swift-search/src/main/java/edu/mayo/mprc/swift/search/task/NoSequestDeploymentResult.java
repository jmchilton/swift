package edu.mayo.mprc.swift.search.task;


import edu.mayo.mprc.enginedeployment.DeploymentResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

final class NoSequestDeploymentResult implements DatabaseDeploymentResult {
	private File fastaFile;

	public NoSequestDeploymentResult(File fastaFile) {
		this.fastaFile = fastaFile;
	}

	public String getShortDbName() {
		assert false : "Short database name is not used by Sequest";
		return null;
	}

	/**
	 * @return Sequest .hdr file, or (in case there was no Sequest deployment done) a path to the FASTA file.
	 */
	public File getFileToDeploy() {
		return getFastaFile();
	}

	/**
	 * @return The path to the taxonomy.xml file.
	 */
	public File getTaxonXml() {
		assert false : "Sequest deployment does not define Tandem's taxonomy";
		return null;
	}

	public File getFastaFile() {
		return fastaFile;
	}

	public List<File> getGeneratedFiles() {
		return new ArrayList<File>(0);
	}

	public DeploymentResult getDeploymentResult() {
		return null;
	}
}
