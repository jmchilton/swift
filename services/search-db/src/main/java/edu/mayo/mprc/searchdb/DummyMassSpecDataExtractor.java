package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.searchdb.dao.TandemMassSpectrometrySample;

import java.io.File;
import java.util.Date;

/**
 * Produces dummy information about mass spec files.
 *
 * @author Roman Zenka
 */
public class DummyMassSpecDataExtractor implements MassSpecDataExtractor {
	@Override
	public TandemMassSpectrometrySample getTandemMassSpectrometrySample(String biologicalSampleName, String fractionName) {
		return new TandemMassSpectrometrySample(
				new File(fractionName),
				new Date(),
				0,
				0,
				0,
				"Dummy",
				new Date(),
				0.0,
				"",
				"",
				"",
				"",
				""
		);
	}
}
