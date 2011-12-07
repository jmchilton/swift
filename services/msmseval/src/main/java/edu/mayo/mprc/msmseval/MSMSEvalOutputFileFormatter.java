package edu.mayo.mprc.msmseval;

import edu.mayo.mprc.io.ValueSeparatedFileReader;
import edu.mayo.mprc.io.mgf.SpectrumNumberExtractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class MSMSEvalOutputFileFormatter {
	private MSMSEvalOutputFileFormatter() {
	}

	/**
	 * Replaces the first column values of the msmsEval output file with the mgf scan numbers.
	 *
	 * @param msmsEvalOutputFile
	 * @param mzXMLScanToMGFTitle
	 * @return Returns formatted file. Formatted file name is the same as the out put file name but with extension .mod.csv instead
	 *         of .csv.
	 * @throws IOException
	 */
	public static File replaceMzXMLScanIdsWithMgfNumbers(File msmsEvalOutputFile, File formattedOutputFile, Map<Integer, String> mzXMLScanToMGFTitle) throws IOException {
		ValueSeparatedFileReader valueSeparatedFileReader = null;
		BufferedWriter bufferedWriter = null;

		try {
			valueSeparatedFileReader = new ValueSeparatedFileReader(msmsEvalOutputFile, ",");
			bufferedWriter = new BufferedWriter(new FileWriter(formattedOutputFile));

			SpectrumNumberExtractor spectrumNumberExtractor = new SpectrumNumberExtractor();

			List<String> row = null;

			boolean wroteColumnHeaders = false;

			while (valueSeparatedFileReader.hasMoreRows()) {
				row = valueSeparatedFileReader.nextRow();

				if (wroteColumnHeaders) {
					//Replace the current
					row.set(0, Integer.toString(spectrumNumberExtractor.extractSpectrumNumberFromTitle(mzXMLScanToMGFTitle.get(Integer.parseInt(row.get(0))))));
				} else {
					wroteColumnHeaders = true;
				}

				String columnValue = null;

				for (Iterator<String> iterator = row.iterator(); iterator.hasNext(); ) {
					columnValue = iterator.next();

					bufferedWriter.write(columnValue);

					if (iterator.hasNext()) {
						bufferedWriter.write(",");
					} else {
						bufferedWriter.write("\n");
					}
				}
			}

		} finally {
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}

			if (valueSeparatedFileReader != null) {
				valueSeparatedFileReader.close();
			}
		}

		return formattedOutputFile;
	}
}
