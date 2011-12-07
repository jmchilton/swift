package edu.mayo.mprc.chem;

import java.util.ArrayList;
import java.util.List;

/**
 * An atomic Element, with a name, e.g. <i>Praseodymium</i> and symbol, e.g. <i>Pr</i>.
 * Contains a list of isotopes that can be used for calculating average mass of the element.
 */
public final class Element {
	public Element(String name, String symbol) {
		this.name = name;
		this.symbol = symbol;
		init();
	}

	/**
	 * The name of this element, eg "Carbon" or "Sodium."
	 */
	public String getName() {
		return name;
	}

	/**
	 * The character symbol for this element, eg C or Na.
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * Adds an Isotope to this Element.
	 */
	public void addIsotope(Isotope isotope) {
		invalidateMasses();
		isotopes.add(isotope);
	}

	public int getNumIsotopes() {
		return isotopes.size();
	}

	public Isotope getIsotope(int num) {
		return isotopes.get(num);
	}

	public Isotope getIsotopeByAtomicWeight(int atomicNumber) {
		for (Isotope isotope : isotopes) {
			int n = isotope.getIntMass();
			if (atomicNumber == n) {
				return isotope;
			}
		}
		return null;
	}

	/**
	 * The average mass of an element is an abundance weighted average of
	 * the masses of isotopes of that element.
	 */
	public double getAverageMass() {
		calcMasses();
		return averageMass;
	}

	/**
	 * The mass of the most abundance isotope of this element.
	 */
	public double getMostAbundantMass() {
		calcMasses();
		return mostAbundantMass;
	}

	/**
	 * The mass of the first (lowest mass) isotope (the monoisotope).
	 */
	public double getMonoisotopicMass() {
		calcMasses();
		return monoisotopicMass;
	}

	public String toString() {
		StringBuilder ret = new StringBuilder(symbol);
		for (int i = 0; i < isotopes.size(); ++i) {
			ret
					.append(i == 0 ? " " : ", ")
					.append(isotopes.get(i).getMass())
					.append(" ")
					.append(isotopes.get(i).getAbundance());
		}
		return ret.toString();
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		Element element = (Element) obj;

		if (name != null ? !name.equals(element.name) : element.name != null) {
			return false;
		}
		return !(symbol != null ? !symbol.equals(element.symbol) : element.symbol != null);
	}

	public int hashCode() {
		int result;
		result = (name != null ? name.hashCode() : 0);
		result = 31 * result + (symbol != null ? symbol.hashCode() : 0);
		return result;
	}

	private void init() {
		massesCalced = false;
	}

	private void invalidateMasses() {
		massesCalced = false;
	}

	private void calcMasses() {
		if (!massesCalced) {
			doCalcMasses();
		}
	}

	private void doCalcMasses() {
		averageMass = 0.;
		mostAbundantMass = 0.;
		monoisotopicMass = isotopes.get(0).getMass();
		double abun = 0.;
		for (Isotope i : isotopes) {
			averageMass += i.getMass() * i.getAbundance();
			if (i.getAbundance() > abun) {
				mostAbundantMass = i.getMass();
				abun = i.getAbundance();
			}
		}
		massesCalced = true;
	}

	private final String name;
	private final String symbol;

	private List<Isotope> isotopes = new ArrayList<Isotope>();

	private boolean massesCalced;
	private double averageMass;
	private double mostAbundantMass;
	private double monoisotopicMass;
}
