package edu.mayo.mprc.chem;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Mercury6Test {

	@Test
	public void shouldCalculateTrivialDistribution() {
		PeriodicTable pt = PeriodicTableFactory.getTestPeriodicTable();
		Chemical c = new Chemical("S1", pt);
		ChargeUnit proton = ChargeUnit.getProtonChargeUnit(pt);
		IsotopicDistribution d = c.getIsotopicDistribution(1, proton, 1E-5, 0);
		// Assert.assertEquals(d.getMassOfIsotope(0), proton.neutralToCharged(c.getMonoisotopicMass(), 1), "Mass of monoisotope does not match");
		// Assert.assertEquals(d.toString(), "", "Carbon distribution failed");
	}

	@Test
	public void shouldMatchRaamsResults() throws IOException {
		PeriodicTable pt = PeriodicTableFactory.getTestPeriodicTable();
		ChargeUnit proton = ChargeUnit.getProtonChargeUnit(pt);

		List<String> lines = CharStreams.readLines(
				Resources.newReaderSupplier(
						Resources.getResource("edu/mayo/mprc/chem/mercury_out.txt"),
						Charsets.UTF_8));
		int state = 0;
		Chemical chemical = null;
		int chargeState = 1;
		List<DistributionItem> items = null;

		int lineNum = 0;
		for (String line : lines) {
			line = line.trim();
			switch (state) {
				case 0:
					if (line.length() == 0 || line.charAt(0) == '#') {
						continue;
					}
					chemical = new Chemical(line, pt);
					state++;
					break;
				case 1:
					chargeState = Integer.parseInt(line);
					items = new ArrayList<DistributionItem>();
					state++;
					break;
				case 2:
					if (line.length() != 0 && line.charAt(0) != '#') {
						DistributionItem item = new DistributionItem(line);
						items.add(item);
					} else {
						IsotopicDistribution d = chemical.getIsotopicDistribution(chargeState, proton, -1, 0);
						compareDistributions(items, lineNum, line, d);
						state = 0;
					}
					break;
			}
			lineNum++;
		}
	}

	private class DistributionItem {
		private double mass;
		private double intensity;
		private boolean mono;
		private boolean max;

		public DistributionItem(String line) {
			String[] items = line.split("\\s+");
			mass = Double.parseDouble(items[0]);
			intensity = Double.parseDouble(items[1]);
			mono = false;
			max = false;
			for (int i = 2; i < items.length; i++) {
				mono |= "mono".equals(items[i]);
				max |= "max".equals(items[i]);
			}
		}
	}

	private void compareDistributions(List<DistributionItem> items, int lineNum, String line, IsotopicDistribution d) {
		int index = 0;
		for (DistributionItem item : items) {
			String lineInfo = "mercury_out.txt:" + lineNum + ": " + line + "\nCalculated distribution: " + d.toString();
			Assert.assertEquals(d.getMassOfIsotope(index), item.mass, 1E-3, lineInfo + "Mass does not match the calculation");
			Assert.assertEquals(d.getIntensityOfIsotope(index), item.intensity, 1E-3, lineInfo + "Intensity does not match the calculation");
			Assert.assertEquals(d.getMonoisotope() == index, item.mono, lineInfo + "Monoisotope assignment mismatch");
			Assert.assertEquals(d.getMostAbundantIsotope() == index, item.max, lineInfo + "Max abundant isotope assignment mismatch");
			index++;
		}
	}

}
