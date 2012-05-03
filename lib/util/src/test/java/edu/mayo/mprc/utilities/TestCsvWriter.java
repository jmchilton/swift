package edu.mayo.mprc.utilities;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Check the idiosyncracies of the Csv writer.
 *
 * @author Roman Zenka
 */
public final class TestCsvWriter {
	@Test
	public static void shouldHandleComplexCases() throws IOException {
		Writer stringWriter = new StringWriter();
		CsvWriter writer = new CsvWriter(stringWriter);
		writer.writeNext(new String[]{"1", "2", "3"});
		writer.writeNext(new String[]{"hello", "\"hello\"", "=hello"});
		writer.writeNext(new String[]{"line1\nline2", "line1\r\nline2", "line1\n\rline2"});
		writer.writeNext(new String[]{null, "empty before"});
		writer.writeNext(new String[]{null, null, "12345"});
		writer.close();
		stringWriter.close();
		Assert.assertEquals(
				TestingUtilities.canonicalizeNewLines(stringWriter.toString()),
				TestingUtilities.canonicalizeNewLines(TestingUtilities.resourceToString("edu/mayo/mprc/utilities/test.csv")));
	}

}
