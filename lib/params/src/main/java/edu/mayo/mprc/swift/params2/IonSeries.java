package edu.mayo.mprc.swift.params2;

import edu.mayo.mprc.database.EvolvableBase;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a single class of fragment ions produced during tandem mass spectrometry, eg, "a" series.
 * <p/>
 * See <a href="http://www.matrixscience.com/help/fragmentation_help.html">http://www.matrixscience.com/help/fragmentation_help.html</a> but note that most search engines
 * don't support all the series that mascot does.  (Nor do most instruments produce these ions.)
 * <p/>
 * This is an immutable class. Do not get fooled by presence of setters - these are for Hibernate use only.
 */
public class IonSeries extends EvolvableBase {

	private String name;

	private static final List<IonSeries> DEFAULT_SERIES = Arrays.asList(
			new IonSeries("a"),
			new IonSeries("b"),
			new IonSeries("c"),
			new IonSeries("d"),
			new IonSeries("v"),
			new IonSeries("w"),
			new IonSeries("x"),
			new IonSeries("y"),
			new IonSeries("z"));

	IonSeries() {
	}

	public IonSeries(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return getName();
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		IonSeries ionSeries = (IonSeries) obj;

		return !(getName() != null ? !getName().equals(ionSeries.getName()) : ionSeries.getName() != null);

	}

	public int hashCode() {
		int result;
		result = (getName() != null ? getName().hashCode() : 0);
		return result;
	}

	public IonSeries copy() {
		final IonSeries series = new IonSeries(getName());
		series.setId(getId());
		return series;
	}

	public static List<IonSeries> getInitial() {
		return DEFAULT_SERIES;
	}
}
