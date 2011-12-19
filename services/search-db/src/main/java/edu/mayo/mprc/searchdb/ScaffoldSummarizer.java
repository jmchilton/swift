package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.scaffoldparser.Scaffold;
import edu.mayo.mprc.scaffoldparser.ScaffoldParser;

import java.io.InputStream;

/**
 * Loads information from Scaffold.xml file and summarizes it on peptide level.
 * Since the Scaffold parser loads entire .xml into the memory, we can create the full dataset and present it
 * as a whole, instead of streaming.
 * <p/>
 * The information presented has following levels:
 * <p/>
 * <ol>
 * <li>Scaffold information - for the entire invocation of Scaffold. Mostly to provide Scaffold version</li>
 * <li>Biological sample information. A biological sample denotes what goes together. It is important to know, because
 * protein-level calculations and thresholds are done per sample.</li>
 * <li>Mass-spec sample information. A mass spec sample is from one run of the instrument. Contains information about the file name,
 * total amount of identified spectra and such.</li>
 * <li>Peptide information. Each sample has multiple peptides identified. Provide total count of spectra for given peptide+modification combination.</li>
 * </ol>
 *
 * @author Roman Zenka
 */
public class ScaffoldSummarizer {
	/**
	 * Loads Scaffold.xml export file and populates the summaries.
	 *
	 * @param scaffoldStream Scaffold .xml stream to load.
	 */
	public void load(InputStream scaffoldStream) {
		final Scaffold scaffold = ScaffoldParser.loadScaffoldXml(scaffoldStream);
	}
}
