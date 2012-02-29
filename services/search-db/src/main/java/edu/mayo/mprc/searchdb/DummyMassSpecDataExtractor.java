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
	private Date now;

	public DummyMassSpecDataExtractor(Date now) {
		this.now = now;
	}

	@Override
	public TandemMassSpectrometrySample getTandemMassSpectrometrySample(String biologicalSampleName, String msmsSampleName) {
		return new TandemMassSpectrometrySample(
				new File(msmsSampleName),
				new Date(),
				0,
				0,
				0,
				"Dummy instrument",
				"Dummy #",
				now,
				0.0,
				"",
				"",
				"",
				"",
				""
		);
	}
}
