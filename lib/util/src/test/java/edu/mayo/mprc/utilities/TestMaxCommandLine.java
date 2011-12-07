package edu.mayo.mprc.utilities;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * this is used to test the ProcessRunner class, @see edu.mayo.mprc.swift.core.ProcessRunner
 */
@Test(sequential = true)
public final class TestMaxCommandLine {
	private static final Logger LOGGER = Logger.getLogger(TestMaxCommandLine.class);


	@Test
	public void testGetMaxCommandLengthLinux() {
		if (FileUtilities.isLinuxPlatform()) {
			LOGGER.debug("running testGetMaxCommandLengthLinux");

			long result = MaxCommandLine.findMaxCallLength(1000 * 100 * 14L, "echo");
			LOGGER.debug("max call length=" + result);
			Assert.assertTrue(result > 10000);
		}
	}

	@Test
	public void testGetMaxCommandLengthWindows() {
		if (FileUtilities.isWindowsPlatform()) {
			LOGGER.debug("running testGetMaxCommandLengthWindows");
			long result = MaxCommandLine.findMaxCallLength(1000, "cmd /c echo");
			LOGGER.debug("max call length=" + result);
			Assert.assertTrue(result > 32000);
		}
	}
}
