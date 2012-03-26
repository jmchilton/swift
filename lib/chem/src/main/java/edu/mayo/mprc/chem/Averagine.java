package edu.mayo.mprc.chem;

/**
 * The amino acid "Averagine," representing the average chemical composition
 * of amino acids in a protein database.
 * <p/>
 * See: Senko et al.  J Am Soc Mass Spectrom 1995, 6, 229-233.
 */
public final class Averagine extends Chemical {

	public Averagine(final PeriodicTable pt) {
		super("C4.9384 H7.7583 N1.3577 O1.4773 S0.0417", pt, "Averagine");
	}

}
