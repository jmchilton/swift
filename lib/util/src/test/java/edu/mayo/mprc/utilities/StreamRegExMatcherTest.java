package edu.mayo.mprc.utilities;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Eric Winter
 */
@Test(groups = {"unit", "fast", "integration"}, sequential = true)
public final class StreamRegExMatcherTest {

	private File getSomeHeadersFile() throws IOException {
		return TestingUtilities.getTempFileFromResource(StreamRegExMatcherTest.class, "/someheaders.txt", true, null);
	}

	@Test
	public void testReplacement() {
		StreamRegExMatcher matcher = null;
		try {
			matcher = new StreamRegExMatcher(Pattern.compile("protein (.+) - Homo"), getSomeHeadersFile());
		} catch (IOException e) {
			Assert.fail("Couldn't even create the neccesary object.", e);
			return;
		}

		matcher.replaceAll("$1");
		matcher.close();

		String results = matcher.getContents().split("\\r?\\n")[0];
		String expectedResults = ">P31946|1433B_HUMAN 14-3-3 beta/alpha sapiens (Human)";

		Assert.assertEquals(results, expectedResults, "Replacement didn't work properly");
	}

	@Test
	public void testReplacementInPlace() throws IOException {
		StreamRegExMatcher matcher = null;
		File originalFile = getSomeHeadersFile();
		File copiedFile = TestingUtilities.getUniqueTempFile(true, null, ".tmp");

		FileUtilities.tryCopyFile(originalFile, copiedFile, true);
		try {
			matcher = new StreamRegExMatcher(Pattern.compile("protein (.+) - Homo"), copiedFile);
		} catch (IOException e) {
			Assert.fail("Couldn't even create the neccesary object.", e);
			return;
		}

		matcher.replaceAll("$1");

		String results = matcher.getContents().split("\\r?\\n")[0];
		String expectedResults = ">P31946|1433B_HUMAN 14-3-3 beta/alpha sapiens (Human)";
		Assert.assertEquals(results.trim(), expectedResults.trim(), "Replacement didn't work properly");

		matcher.writeContentsToFile(copiedFile);

		matcher.close();

		String fileResults = Files.toString(copiedFile, Charsets.UTF_8).split("\\r?\\n")[0];
		Assert.assertEquals(fileResults, expectedResults, "File did not get properly saved");
	}

	@Test
	public void testReplaceAllMap() throws IOException {

		File testInput = TestingUtilities.getTempFileMarker(null);

		String sampleFileContents =
				"I saw my dog take a jump off of the cliff.  Where is Spot?";


		Map<Pattern, String> replacements = new HashMap<Pattern, String>();

		replacements.put(Pattern.compile("dog take"), "a\\\\");
		replacements.put(Pattern.compile("j\\S{3}\\soff"), "b");
		replacements.put(Pattern.compile("c(\\S*)f"), "C$1$1F");

		FileUtilities.writeStringToFile(testInput, sampleFileContents, false);
		StreamRegExMatcher matcher = new StreamRegExMatcher(testInput);
		matcher.replaceAll(replacements);

		String result = matcher.getContents();

		String expected = "I saw my a\\ a b of the CliflifF.  Where is Spot?";

		Assert.assertEquals(result, expected);

	}
}



