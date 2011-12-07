package edu.mayo.mprc.chem;

import com.google.common.base.Function;

/**
 * Class holds information about specific Amino acid.
 */
public final class AminoAcid {
	private final char code;
	private final String code3;
	private final String formula;
	private final double monoisotopicMass;
	private final double averageMass;

	public static final GetCode GET_CODE = new GetCode();

	public AminoAcid(char code, String code3, String formula, double monoisotopicMass, double averageMass) {
		this.code = code;
		this.code3 = code3;
		this.formula = formula;
		this.monoisotopicMass = monoisotopicMass;
		this.averageMass = averageMass;
	}

	public char getCode() {
		return code;
	}

	public double getMonoisotopicMass() {
		return monoisotopicMass;
	}

	public double getAverageMass() {
		return averageMass;
	}

	public String getCode3() {
		return code3;
	}

	public String getFormula() {
		return formula;
	}

	public static class GetCode implements Function<AminoAcid, Character> {
		@Override
		public Character apply(AminoAcid from) {
			return from.getCode();
		}
	}
}
