package edu.mayo.mprc.swift;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Check that we parse the Swift command line properly.
 *
 * @author Roman Zenka
 */
public final class TestCommandLineParsing {
	private File test;

	@BeforeClass
	public void setup() throws IOException {
		test = File.createTempFile("test", ".txt");
	}

	@AfterClass
	public void teardown() {
		FileUtilities.cleanupTempFile(test);
	}

	@Test
	public void shouldProvideHelp() {
		CommandLineParser parser = new CommandLineParser(new String[]{"--help"});
		Assert.assertEquals(parser.getCommand(), "help");
		Assert.assertEquals(parser.getError(), null);
	}

	@Test
	public void shouldNoticeMissingParams() {
		CommandLineParser parser2 = new CommandLineParser(new String[]{"?"});
		Assert.assertEquals(parser2.getCommand(), "help");
		Assert.assertEquals(parser2.getError(), "You must specify either the --daemon, --sge or --run options.");
	}

	@Test(expectedExceptions = MprcException.class)
	public void shouldNoticeMissingConfig() {
		CommandLineParser parser = new CommandLineParser(new String[]{"--install", "test.xml", "--daemon", "main-daemon"});
	}

	@Test
	public void shouldNotAllowSgeRun() {
		CommandLineParser parser = new CommandLineParser(new String[]{"--install", test.toString(), "--daemon", "main-daemon", "--sge", "sgefile", "--run", "run command"});
		Assert.assertEquals(parser.getCommand(), "help");
		Assert.assertNotNull(parser.getError());
	}

	@Test
	public void shouldLoadPresentConfig() {
		CommandLineParser parser = new CommandLineParser(new String[]{"--install", test.toString(), "--daemon", "main-daemon"});
		Assert.assertEquals(parser.getCommand(), "run-swift");
		Assert.assertEquals(parser.getError(), null);
	}

	@Test
	public void shouldSupportCustomCommand() throws IOException {
		CommandLineParser parser = new CommandLineParser(new String[]{"--install", test.toString(), "--run", "my-command command-params"});
		Assert.assertEquals(parser.getCommand(), "my-command");
		Assert.assertEquals(parser.getParameter(), "command-params");
		Assert.assertEquals(parser.getError(), null);
	}

}
