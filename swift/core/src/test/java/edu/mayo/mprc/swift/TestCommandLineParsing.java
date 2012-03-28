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
		final CommandLineParser parser = new CommandLineParser(new String[]{"--help"});
		final SwiftCommandLine cmd = parser.getCommandLine();
		Assert.assertEquals(cmd.getCommand(), "help");
		Assert.assertEquals(cmd.getError(), null);
	}

	@Test
	public void shouldNoticeMissingParams() {
		final CommandLineParser parser = new CommandLineParser(new String[]{"?"});
		final SwiftCommandLine cmd = parser.getCommandLine();
		Assert.assertEquals(cmd.getCommand(), "help");
		Assert.assertEquals(cmd.getError(), "You must specify either the --daemon, --sge or --run options.");
	}

	@Test(expectedExceptions = MprcException.class)
	public void shouldNoticeMissingConfig() {
		final CommandLineParser parser = new CommandLineParser(new String[]{"--install", "test.xml", "--daemon", "main-daemon"});
	}

	@Test
	public void shouldNotAllowSgeRun() {
		final CommandLineParser parser = new CommandLineParser(new String[]{"--install", test.toString(), "--daemon", "main-daemon", "--sge", "sgefile", "--run", "run command"});
		final SwiftCommandLine cmd = parser.getCommandLine();
		Assert.assertEquals(cmd.getCommand(), "help");
		Assert.assertNotNull(cmd.getError());
	}

	@Test
	public void shouldLoadPresentConfig() {
		final CommandLineParser parser = new CommandLineParser(new String[]{"--install", test.toString(), "--daemon", "main-daemon"});
		final SwiftCommandLine cmd = parser.getCommandLine();
		Assert.assertEquals(cmd.getCommand(), "run-swift");
		Assert.assertEquals(cmd.getError(), null);
	}

	@Test
	public void shouldSupportCustomCommand() {
		final CommandLineParser parser = new CommandLineParser(new String[]{"--install", test.toString(), "--run", "my-command:command-params"});
		final SwiftCommandLine cmd = parser.getCommandLine();
		Assert.assertEquals(cmd.getCommand(), "my-command");
		Assert.assertEquals(cmd.getParameter(), "command-params");
		Assert.assertEquals(cmd.getError(), null);
	}

}
