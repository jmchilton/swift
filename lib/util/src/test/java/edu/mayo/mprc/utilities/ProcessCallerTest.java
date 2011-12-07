package edu.mayo.mprc.utilities;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class ProcessCallerTest {
	private static final String OUTPUT = "Hello World!";

	@Test
	public void shouldCallBasicCommand() {
		ProcessBuilder builder = new ProcessBuilder()
				.command(getSimpleCommand());
		ProcessCaller caller = new ProcessCaller(builder);
		caller.setLogToConsole(false);
		caller.setRetainLogs(true);
		caller.run();
		Assert.assertEquals(caller.getOutputLog().trim(), OUTPUT);
	}

	@Test
	public void shouldCallBasicCommandWithLogging() {
		ProcessBuilder builder = new ProcessBuilder()
				.command(getSimpleCommand());
		ProcessCaller caller = new ProcessCaller(builder);
		caller.setLogToConsole(true);
		caller.run();
		Assert.assertEquals(caller.getOutputLog().trim(), OUTPUT);
	}

	@Test
	public void shouldGetDescription() {
		ProcessBuilder builder = new ProcessBuilder()
				.command(getSimpleCommand())
				.directory(new java.io.File("."));
		ProcessCaller caller = new ProcessCaller(builder);
		final String description = caller.getCallDescription();
		if (FileUtilities.isWindowsPlatform()) {
			Assert.assertEquals(description, "\n\tcd .\n\tcmd /c \"echo " + OUTPUT + "\"");
		} else {
			Assert.assertEquals(description, "\n\tcd .\n\tsh -c \'echo " + OUTPUT + "'");
		}
	}

	@Test
	public void shouldUseMonitors() {
		ProcessBuilder builder = new ProcessBuilder()
				.command(getSimpleCommand());
		ProcessCaller caller = new ProcessCaller(builder);
		final MyLogMonitor outputMonitor = new MyLogMonitor();
		caller.setOutputMonitor(outputMonitor);
		caller.run();
		Assert.assertEquals(outputMonitor.getLastLine(), OUTPUT);
	}

	private List<String> getSimpleCommand() {
		if (FileUtilities.isWindowsPlatform()) {
			return Arrays.asList("cmd", "/c", "echo " + OUTPUT);
		} else {
			return Arrays.asList("sh", "-c", "echo " + OUTPUT);
		}
	}

	private static class MyLogMonitor implements LogMonitor {
		private String lastLine;

		@Override
		public void line(String line) {
			lastLine = line;
		}

		public String getLastLine() {
			return lastLine;
		}
	}
}
