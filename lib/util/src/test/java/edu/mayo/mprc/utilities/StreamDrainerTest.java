package edu.mayo.mprc.utilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class StreamDrainerTest {

	private static final Logger LOGGER = Logger.getLogger(StreamDrainerTest.class);

	@Test
	public void shouldSupportNullLogs() {
		StreamDrainer testDrainer = new StreamDrainer(null, LOGGER, Level.DEBUG, 1, null);
		Assert.assertEquals(testDrainer.getLog(), "");
	}

	@Test
	public void shouldSupportRawLogs() {
		StreamDrainer testDrainer = new StreamDrainer(null, LOGGER, Level.DEBUG, 3, null);
		testDrainer.addLine("one");
		testDrainer.addLine("two");
		testDrainer.addLine("three");
		Assert.assertEquals(testDrainer.getLog(), "one\ntwo\nthree\n");
	}

	@Test
	public void shouldSupportOverflowLogs() {
		StreamDrainer testDrainer = new StreamDrainer(null, LOGGER, Level.DEBUG, 2, null);
		testDrainer.addLine("one");
		testDrainer.addLine("two");
		testDrainer.addLine("three");
		Assert.assertEquals(testDrainer.getLog(), "Most recent 2 of 3 total log lines:\n\ttwo\n\tthree\n");
	}

	@Test
	public void shouldSupportMonitor() {
		final MyLogMonitor monitor = new MyLogMonitor();
		StreamDrainer testDrainer = new StreamDrainer(null, LOGGER, Level.DEBUG, 2, monitor);
		testDrainer.addLine("one");
		testDrainer.addLine("two");
		testDrainer.addLine("three");
		Assert.assertEquals(monitor.getConcatenated(), "onetwothree");
	}

	private static class MyLogMonitor implements LogMonitor {
		StringBuilder concatenated = new StringBuilder(20);

		@Override
		public void line(String line) {
			concatenated.append(line);
		}

		public String getConcatenated() {
			return concatenated.toString();
		}
	}
}
