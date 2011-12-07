package edu.mayo.mprc.chem;

import java.io.PrintWriter;

public interface Molecule {
	double getMonoisotopicMass();

	double getAverageMass();

	String getName();

	PrintWriter write(PrintWriter writer);
}
