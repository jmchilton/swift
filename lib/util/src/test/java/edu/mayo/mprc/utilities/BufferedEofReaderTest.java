package edu.mayo.mprc.utilities;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Check the BufferedEofReader with a variety of scenarios.
 *
 * @author Roman Zenka
 */
public final class BufferedEofReaderTest {

	@Test
	public void shouldSupportEmptyReaders() throws IOException {
		BufferedEofReader eofReader = null;
		try {
			eofReader = makeEofReader("");
			Assert.assertEquals(eofReader.readLine(), null);
			Assert.assertTrue(eofReader.isEof());
		} finally {
			FileUtilities.closeQuietly(eofReader);
		}
	}

	@Test
	public void shouldSupportBasicUsage() throws IOException {
		BufferedEofReader eofReader = null;
		try {
			eofReader = makeEofReader("a\nb\nc\n");
			Assert.assertEquals(eofReader.isEof(), false);
			Assert.assertEquals(eofReader.isEof(), false);

			Assert.assertEquals(eofReader.readLine(), "a");
			Assert.assertFalse(eofReader.isEof());
			Assert.assertFalse(eofReader.isEof());

			Assert.assertEquals(eofReader.readLine(), "b");
			Assert.assertFalse(eofReader.isEof());
			Assert.assertEquals(eofReader.readLine(), "c");
			Assert.assertTrue(eofReader.isEof());
			Assert.assertEquals(eofReader.readLine(), null);
			Assert.assertTrue(eofReader.isEof());
			Assert.assertEquals(eofReader.readLine(), null);
			Assert.assertTrue(eofReader.isEof());

		} finally {
			FileUtilities.closeQuietly(eofReader);
		}
	}


	private BufferedEofReader makeEofReader(String data) {
		final StringReader reader = new StringReader(data);
		final BufferedReader bufferedReader = new BufferedReader(reader);
		return new BufferedEofReader(bufferedReader);
	}
}
