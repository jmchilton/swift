package edu.mayo.mprc.swift;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test whether Swift can run with particular command line options.
 *
 * @author Roman Zenka
 */
public final class TestSwiftStartup {
	/**
	 * Running Swift with no arguments yields an error.
	 */
	@Test
	public void shouldFailEmpty() {
		Assert.assertEquals(Swift.runSwift(), ExitCode.Error);
	}

	/**
	 * Swift with --help will produce help and end ok.
	 */
	@Test
	public void shouldProvideHelp() {
		Assert.assertEquals(Swift.runSwift("--help"), ExitCode.Ok);
	}

	/**
	 * Swift with --sge without the actual install config should complain and terminate.
	 */
	@Test
	public void shouldRunSge() {
		try {
			Swift.runSwift("--sge", "nonexistent");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("installation config"));
			return;
		}
		Assert.fail("Exception should be thrown");
	}

}
