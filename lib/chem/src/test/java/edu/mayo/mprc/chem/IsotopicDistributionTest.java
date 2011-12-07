package edu.mayo.mprc.chem;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class IsotopicDistributionTest {
	@Test
	public void shouldAddDistributions() {
		PeriodicTable pt = PeriodicTableFactory.getTestPeriodicTable();
		Chemical water = new Chemical("H20", pt);
		MassIntensityArray a = new MassIntensityArray();
		IsotopicDistribution empty = new IsotopicDistribution(water.getMonoisotopicMass(), water, a);
		Assert.assertEquals(empty.toString(), "", "Empty isotopic distribution should have empty toString");

		a.add(water.getMonoisotopicMass(), 2);
		a.add(water.getMonoisotopicMass() + pt.getProtonMass(), 4);
		a.add(water.getMonoisotopicMass() + 2 * pt.getProtonMass(), 3);
		IsotopicDistribution d = new IsotopicDistribution(water.getMonoisotopicMass(), water, a);

		Assert.assertEquals(d.toString(), "20.156500641999997 2.0 mono\n21.163777101999997 4.0 max\n22.171053561999997 3.0\n", "toString does not work");

		Assert.assertEquals(d.getNumIsotopes(), 3, "Wrong number of isotopes");
		Assert.assertEquals(d.getMostAbundantIsotope(), 1, "Most abundant isotope does not match");
		Assert.assertEquals(d.getMonoisotope(), 0, "Monoisotope does not match");

		IsotopicDistribution d2 = d.clone();
		Assert.assertEquals(d2.toString(), "20.156500641999997 2.0 mono\n21.163777101999997 4.0 max\n22.171053561999997 3.0\n", "toString does not work");

		IsotopicDistribution dPlusD2 = d.add(d2, 0.5, 0);
		Assert.assertEquals(dPlusD2.toString(), "20.156500641999997 2.0 mono\n21.163777101999997 4.0 max\n22.171053561999997 3.0\n", "adding distributions fail");

		MassIntensityArray b = new MassIntensityArray();
		b.add(water.getMonoisotopicMass(), 1);
		b.add(water.getMonoisotopicMass() + pt.getProtonMass(), 3);
		b.add(water.getMonoisotopicMass() + 2 * pt.getProtonMass(), 6);
		IsotopicDistribution e = new IsotopicDistribution(water.getMonoisotopicMass(), water, b);

		IsotopicDistribution d_plus_e_frac_0 = d.add(e, 0, 0);
		Assert.assertEquals(d_plus_e_frac_0.toString(), "20.156500641999997 2.0 mono\n21.163777101999997 4.0 max\n22.171053561999997 3.0\n", "1*d + 0*e fail");

		IsotopicDistribution d_plus_e_frac_1 = d.add(e, 1, 0);
		Assert.assertEquals(d_plus_e_frac_1.toString(), "20.156500641999997 1.0 mono\n21.163777101999997 3.0\n22.171053561999997 6.0 max\n", "0*d + 1*e fail");

		IsotopicDistribution d_plus_e_frac_half = d.add(e, 0.5, 0);
		Assert.assertEquals(d_plus_e_frac_half.toString(), "20.156500641999997 1.5 mono\n21.163777101999997 3.5\n22.171053561999997 4.5 max\n", "0.5d + 0.5e fail");

		b.add(water.getMonoisotopicMass() + 3 * pt.getProtonMass(), 7);
		IsotopicDistribution e1 = new IsotopicDistribution(water.getMonoisotopicMass(), water, b);
		Assert.assertEquals(e1.toString(), "20.156500641999997 1.0 mono\n21.163777101999997 3.0\n22.171053561999997 6.0\n23.178330021999997 7.0 max\n", "e + one more isotope");

		IsotopicDistribution d_plus_e1_frac_half = d.add(e1, 0.5, 0);
		Assert.assertEquals(d_plus_e1_frac_half.toString(), "20.156500641999997 1.5 mono\n21.163777101999997 3.5\n22.171053561999997 4.5 max\n23.178330021999997 3.5\n", "0.5d + 0.5e1 fail");

		IsotopicDistribution e1_plus_d_frac_half = e1.add(d, 0.5, 0);
		Assert.assertEquals(e1_plus_d_frac_half.toString(), "20.156500641999997 1.5 mono\n21.163777101999997 3.5\n22.171053561999997 4.5 max\n23.178330021999997 3.5\n", "0.5d + 0.5e1 fail");
	}

	@Test
	public void shouldAddWithTolerance() {
		PeriodicTable pt = PeriodicTableFactory.getTestPeriodicTable();
		Chemical water = new Chemical("H20", pt);
		MassIntensityArray a = new MassIntensityArray();
		a.add(water.getMonoisotopicMass(), 2);
		a.add(water.getMonoisotopicMass() + pt.getProtonMass(), 4);
		a.add(water.getMonoisotopicMass() + 2 * pt.getProtonMass(), 3);
		IsotopicDistribution d = new IsotopicDistribution(water.getMonoisotopicMass(), water, a);

		double ppm = 100;
		double shift = d.getTheoreticalMonoisotopicMZ() * ppm / 1E6;
		IsotopicDistribution d2 = d.copy(d.getTheoreticalMonoisotopicMZ() + shift, shift, 1.0);

		IsotopicDistribution sum = d.add(d2, 0.5, ppm + 1E-10); // We use PPM slightly above the shift. All peaks should match
		Assert.assertEquals(sum.getMassOfIsotope(0), water.getMonoisotopicMass() + shift / 2, "The first peak masses are united");
		Assert.assertEquals(sum.getMassOfIsotope(d.getNumIsotopes() - 1), water.getMonoisotopicMass() + 2 * pt.getProtonMass() + shift / 2, "The last peak masses are united");

		IsotopicDistribution sum2 = d.add(d2, 0.5, ppm * 0.9); // We use PPM below the shift. No peaks should match
		Assert.assertEquals(sum2.getNumIsotopes(), 6, "No peaks should merge");
		Assert.assertEquals(sum2.getMassOfIsotope(1), water.getMonoisotopicMass() + shift, "1st result peak != 1st peak of d2");
		Assert.assertEquals(sum2.getMassOfIsotope(5), water.getMonoisotopicMass() + 2 * pt.getProtonMass() + shift, "5th result peak != 2nd peak of d2");

		// We use PPM slightly above the PPM for last peak of d. Only last peaks would match, the rest should be separate
		IsotopicDistribution sum3 = d.add(d2, 0.5, shift / (water.getMonoisotopicMass() + 2 * pt.getProtonMass()) * 1E6 + 1E-6);
		Assert.assertEquals(sum3.getNumIsotopes(), 5, "Only last peak should merge");
		Assert.assertEquals(sum3.getMassOfIsotope(0), water.getMonoisotopicMass(), "0th result peak != 0th of d");
		Assert.assertEquals(sum3.getMassOfIsotope(1), water.getMonoisotopicMass() + shift, "1st result peak != 0th of d2");
		Assert.assertEquals(sum3.getMassOfIsotope(4), water.getMonoisotopicMass() + 2 * pt.getProtonMass() + shift / 2, "4th result peak != 2nd of d + 2nd of d2");
	}

	@Test
	public void shouldCopy() throws CloneNotSupportedException {
		PeriodicTable pt = PeriodicTableFactory.getTestPeriodicTable();
		Chemical water = new Chemical("H20", pt);
		MassIntensityArray a = new MassIntensityArray();
		a.add(water.getMonoisotopicMass(), 2);
		a.add(water.getMonoisotopicMass() + pt.getProtonMass(), 4);
		a.add(water.getMonoisotopicMass() + 2 * pt.getProtonMass(), 3);
		IsotopicDistribution d = new IsotopicDistribution(water.getMonoisotopicMass(), water, a);

		IsotopicDistribution copyOfD = d.copy(d.getTheoreticalMonoisotopicMZ() + 10, 10, 2);
		Assert.assertEquals(copyOfD.toString(), "30.156500641999997 4.0 mono\n31.163777101999997 8.0 max\n32.171053562 6.0\n", "d, mass shifted+10, abundance times 2");
	}

	@Test
	public void shouldSupportExtraPeaks() throws CloneNotSupportedException {
		PeriodicTable pt = PeriodicTableFactory.getTestPeriodicTable();
		Chemical water = new Chemical("H20", pt);
		{
			MassIntensityArray a = new MassIntensityArray();
			a.add(99, 20);
			a.add(100, 40); // mono
			a.add(101, 30);
			a.add(102, 100); // max
			a.add(103, 30);
			IsotopicDistribution d = new IsotopicDistribution(100.2, water, a);
			Assert.assertEquals(d.toString(), "100.0 40.0 mono\n101.0 30.0\n102.0 100.0 max\n103.0 30.0\n", "Everything before monoisotopic peak is dropped");
		}

		{
			MassIntensityArray a = new MassIntensityArray();
			a.add(99, 20);
			a.add(100, 40); // mono
			a.add(101, 100); // max
			a.add(102, 30);
			a.add(103, 30);
			IsotopicDistribution d = new IsotopicDistribution(100.2, water, "Water", 0.1, 3, a);
			Assert.assertEquals(d.toString(), "97.0 0.0\n98.0 0.0\n99.0 20.0\n100.0 40.0 mono\n101.0 100.0 max\n102.0 30.0\n103.0 30.0\n104.0 0.0\n105.0 0.0\n106.0 0.0\n", "Extra peaks are allowed");
		}

		{
			MassIntensityArray a = new MassIntensityArray();
			a.add(99, 20);
			a.add(100, 40);
			a.add(101, 30);
			a.add(102, 100); // mono max
			a.add(103, 30);
			IsotopicDistribution d = new IsotopicDistribution(101.7, water, "Water", 0.1, 3, a);
			Assert.assertEquals(d.toString(), "99.0 20.0\n100.0 40.0\n101.0 30.0\n102.0 100.0 mono max\n103.0 30.0\n104.0 0.0\n105.0 0.0\n106.0 0.0\n", "No extra peaks added to the front");
		}

		{
			MassIntensityArray a = new MassIntensityArray();
			a.add(99, 20);
			a.add(100, 40);
			a.add(101, 30);
			a.add(102, 100); // mono max
			a.add(103, 30);
			IsotopicDistribution d = new IsotopicDistribution(101.7, water, "Water", 0.5, 0, a);
			Assert.assertEquals(d.toString(), "102.0 100.0 mono max\n", "Dropping peaks under 50% of max peak");
		}

		{
			MassIntensityArray a = new MassIntensityArray();
			a.add(99, 20);
			a.add(100, 40);
			a.add(101, 30);
			a.add(102, 100); // mono max
			a.add(103, 30);
			IsotopicDistribution d = new IsotopicDistribution(101.7, water, "Water", 0.5, 1, a);
			Assert.assertEquals(d.toString(), "101.0 30.0\n102.0 100.0 mono max\n103.0 30.0\n", "Dropping peaks under 50% of max peak, adding 1 extra peak");
		}

		{
			MassIntensityArray a = new MassIntensityArray();
			a.add(99, 20);
			a.add(100, 40);
			a.add(101, 30); // mono
			a.add(102, 100); // max
			a.add(103, 30);
			a.add(104, 35);
			a.add(105, 34);
			a.add(106, 20);
			a.add(107, 10);
			IsotopicDistribution d = new IsotopicDistribution(101.1, water, "Water", 0.35, 0, a);
			Assert.assertEquals(d.toString(), "101.0 30.0 mono\n102.0 100.0 max\n103.0 30.0\n104.0 35.0\n", "Dropping peaks under 35% of max peak and all before mono");
		}

		{
			MassIntensityArray a = new MassIntensityArray();
			a.add(99, 20);
			a.add(100, 100); // max
			a.add(101, 30);
			IsotopicDistribution d = new IsotopicDistribution(97, water, "Water", 0.1, 0, a);
			Assert.assertEquals(d.toString(), "97.0 0.0 mono\n98.0 0.0\n99.0 20.0\n100.0 100.0 max\n101.0 30.0\n", "Mono mass not present - adding extra peaks");
		}


	}

}
