package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;

/**
 * Represents number of spectra identified for a peptide + modifications + raw file + search definition combo.
 * <p/>
 * This means that a single search definition can have only the latest output stored. If it gets rerun, the output gets
 * overwritten.
 *
 * @author Roman Zenka
 */
public class SpectralCount extends PersistableBase {
	private int searchDefinitionId;
	private int fileSearchId;
	private int peptideSequenceId;
	private int spectralCount;
}
