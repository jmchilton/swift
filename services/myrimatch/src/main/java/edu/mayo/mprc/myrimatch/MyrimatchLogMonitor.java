package edu.mayo.mprc.myrimatch;

import edu.mayo.mprc.daemon.progress.PercentDone;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.utilities.LogMonitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class MyrimatchLogMonitor implements LogMonitor {

	private static final int TIME_BETWEEN_UPDATES_MS = 1000;
	private final ProgressReporter progressReporter;
	private long lastTimeMs = System.currentTimeMillis();
	private static final Pattern PATTERN = Pattern.compile("Searched (\\d+) of (\\d+) proteins; \\d+ per second, .* elapsed, .* remaining.");


	public MyrimatchLogMonitor(ProgressReporter progressReporter) {
		this.progressReporter = progressReporter;
	}

	@Override
	public void line(String line) {
		if (line.startsWith("Searched ")) {
			final long time = System.currentTimeMillis();
			if (time - lastTimeMs < TIME_BETWEEN_UPDATES_MS) {
				return;
			}
			lastTimeMs = time;

			final Matcher matcher = PATTERN.matcher(line);
			if (matcher.matches()) {
				try {
					int proteins = Integer.valueOf(matcher.group(1));
					int total = Integer.valueOf(matcher.group(2));
					progressReporter.reportProgress(new PercentDone(100.0f * proteins / total));
				} catch (NumberFormatException ignore) {
					// SWALLOWED: We ignore these exceptions - we will just not be able to report progress
				}
			}
		}
	}
}
