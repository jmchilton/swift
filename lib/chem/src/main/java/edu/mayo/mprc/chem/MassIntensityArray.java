package edu.mayo.mprc.chem;

import java.util.ArrayList;
import java.util.List;

/**
 * Array of mass-intensity pairs.
 * Currently implemented as a list of Doubles. Masses are on even, intensities on odd positions.
 * This should be optimized.
 */
public final class MassIntensityArray {
	private List<Double> massIntensities;

	public MassIntensityArray() {
		massIntensities = new ArrayList<Double>();
	}

	public MassIntensityArray(final MassIntensityArray a) {
		massIntensities = new ArrayList<Double>(a.massIntensities);
	}

	public void add(final double mass, final double intensity) {
		massIntensities.add(mass);
		massIntensities.add(intensity);
	}

	public int size() {
		return massIntensities.size() / 2;
	}

	public double getMass(final int index) {
		return massIntensities.get(index * 2);
	}

	public void setMass(final int index, final double mass) {
		massIntensities.set(index * 2, mass);
	}

	public double getIntensity(final int index) {
		return massIntensities.get(index * 2 + 1);
	}

	public void setIntensity(final int index, final double intensity) {
		massIntensities.set(index * 2 + 1, intensity);
	}

	/**
	 * TODO: This should be optimized - we should allocate sufficient space at the beginning for inserting
	 * all the values at once.
	 */
	public void insertNCopiesBeforeStart(final int copies, final double mass, final double intensity) {
		for (int i = 0; i < copies; i++) {
			massIntensities.add(i * 2, mass);
			massIntensities.add(i * 2 + 1, intensity);
		}
	}

	/**
	 * Erases mass/intensity pairs from fromIndex (inclusive) to toIndex (exclusive). Similar to
	 *
	 * @param fromIndex First index to be erased.
	 * @param toIndex   The index of the element just after the last element to be erased. If toIndex==fromIndex, no erasing is performed.
	 */
	public void erase(final int fromIndex, final int toIndex) {
		for (int i = 0; i < (toIndex - fromIndex) * 2; i++) {
			massIntensities.remove(fromIndex * 2);
		}
	}
}

