package edu.mayo.mprc.myrimatch;

import com.google.common.base.Joiner;
import edu.mayo.mprc.utilities.ResourceUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;

public class MyrimatchPepXmlReaderTest {
	@Test
	public static void shouldParsePepXml() {
		final InputStream stream = ResourceUtilities.getStream("classpath:edu/mayo/mprc/myrimatch/result.pepXML", MyrimatchPepXmlReaderTest.class);
		MyrimatchPepXmlReader reader = new MyrimatchPepXmlReader();
		reader.load(stream);
		String line = reader.getLineForKey("3");

		Assert.assertEquals(line.replaceAll("[a-zA-Z0-9_.~+-]+", ""), reader.getEmptyLine(), "Empty line has same amount of tabs as normal line");
		Assert.assertEquals(reader.getHeaderLine(),
				"Myrimatch Peptide"
						+ '\t' + "Myrimatch Protein"
						+ '\t' + "Myrimatch Total Proteins"
						+ '\t' + "Myrimatch Num Matched Ions"
						+ '\t' + "Myrimatch Total Num Ions"
						+ '\t' + "Myrimatch mvh"
						+ '\t' + "Myrimatch mz Fidelity"
						+ '\t' + "Myrimatch xcorr", "Myrimatch header does not match");
		Assert.assertEquals(line, Joiner.on('\t').join(
				"SSGSSYPSLLQCLK", // peptide
				"CONTAM_TRYP_PIG", // protein
				"1", // total proteins
				"24", // matched ions
				"25", // total ions
				"112.38684~", // mvh
				"138.23982~", // mz fidelity
				"6.08570~" // xcorr
		), "Myrimatch reader read wrong data");
	}
}
