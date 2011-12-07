package edu.mayo.mprc.chem;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import edu.mayo.mprc.MprcException;

import java.io.BufferedReader;
import java.io.StringReader;

public final class PeriodicTableFactory {
	private static PeriodicTable defaultTable = null;

	private PeriodicTableFactory() {
	}

	/**
	 * @return Default periodic table.
	 * @deprecated Use Spring to inject this value.
	 */
	public static synchronized PeriodicTable getDefaultPeriodicTable() {
		if (defaultTable == null) {
			try {
				final String tableData = Resources.toString(Resources.getResource("edu/mayo/mprc/chem/ISOTOPE.DAT"), Charsets.UTF_8);
				defaultTable = PeriodicTableFactory.parsePeriodicTable(tableData);
			} catch (Exception e) {
				throw new MprcException("Cannot parse default periodic table", e);
			}
		}
		return defaultTable;
	}

	public static synchronized PeriodicTable getTestPeriodicTable() {
		return getDefaultPeriodicTable();
	}

	public static PeriodicTable parsePeriodicTable(String buffer) {
		int i;
		PeriodicTable pt = new PeriodicTable();
		Element curr;
		try {
			BufferedReader reader = new BufferedReader(new StringReader(buffer));
			while (true) {
				int EZNI;
				String buff;
				String line;
				line = reader.readLine();
				if (line == null) {
					break;
				}

				String[] parts = line.split("\\s+", 2);
				buff = parts[0];
				EZNI = Integer.parseInt(parts[1]);
				curr = new Element(buff, buff);
				pt.addElement(curr);

				for (i = 0; i < EZNI; i++) {
					double EZM;
					double EZP;
					line = reader.readLine();
					if (line == null) {
						break;
					}
					String[] isotope = line.split("\\s+", 2);
					EZM = Double.parseDouble(isotope[0]);
					EZP = Double.parseDouble(isotope[1]);
					curr.addIsotope(new Isotope(EZM, EZP));
				}
				// Skip a line
				reader.readLine();
			}
			if (pt.getElementBySymbol("H+") == null) {
				throw new MprcException("The periodic table does not define a proton (H+)");
			}
			pt.setProtonMass(pt.getElementBySymbol("H+").getIsotope(0).getMass());
			if (pt.getElementBySymbol("E-") == null) {
				throw new MprcException("The periodic table does not define an electron (E-)");
			}
			pt.setElectronMass(pt.getElementBySymbol("E-").getIsotope(0).getMass());
			return pt;
		} catch (Exception e) {
			throw new MprcException(e);
		}
	}
}
