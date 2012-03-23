package edu.mayo.mprc.swift;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.swift.commands.SwiftCommandLine;
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
		SwiftCommandLine cmd = parser.getCommandLine();
		Assert.assertEquals(cmd.getCommand(), "help");
		Assert.assertEquals(cmd.getError(), null);
	}

	@Test
	public void shouldNoticeMissingParams() {
		CommandLineParser parser = new CommandLineParser(new String[]{"?"});
		SwiftCommandLine cmd = parser.getCommandLine();
		Assert.assertEquals(cmd.getCommand(), "help");
		Assert.assertEquals(cmd.getError(), "You must specify either the --daemon, --sge or --run options.");
	}

	@Test(expectedExceptions = MprcException.class)
	public void shouldNoticeMissingConfig() {
		CommandLineParser parser = new CommandLineParser(new String[]{"--install", "test.xml", "--daemon", "main-daemon"});
	}

	@Test
	public void shouldNotAllowSgeRun() {
		CommandLineParser parser = new CommandLineParser(new String[]{"--install", test.toString(), "--daemon", "main-daemon", "--sge", "sgefile", "--run", "run command"});
		SwiftCommandLine cmd = parser.getCommandLine();
		Assert.assertEquals(cmd.getCommand(), "help");
		Assert.assertNotNull(cmd.getError());
	}

	@Test
	public void shouldLoadPresentConfig() {
		CommandLineParser parser = new CommandLineParser(new String[]{"--install", test.toString(), "--daemon", "main-daemon"});
		SwiftCommandLine cmd = parser.getCommandLine();
		Assert.assertEquals(cmd.getCommand(), "run-swift");
		Assert.assertEquals(cmd.getError(), null);
	}

	@Test
	public void shouldSupportCustomCommand() {
		CommandLineParser parser = new CommandLineParser(new String[]{"--install", test.toString(), "--run", "my-command command-params"});
		SwiftCommandLine cmd = parser.getCommandLine();
		Assert.assertEquals(cmd.getCommand(), "my-command");
		Assert.assertEquals(cmd.getParameter(), "command-params");
		Assert.assertEquals(cmd.getError(), null);
	}

}
