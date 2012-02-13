package edu.mayo.mprc.scaffold;

import edu.mayo.mprc.utilities.LogMonitor;
import edu.mayo.mprc.utilities.progress.PercentDone;
import edu.mayo.mprc.utilities.progress.ProgressReporter;

public class ScaffoldLogMonitor implements LogMonitor {
	private static final int TIME_BETWEEN_UPDATES_MS = 1000;
	private final ProgressReporter progressReporter;
	private long lastTimeMs = System.currentTimeMillis();


	public ScaffoldLogMonitor(ProgressReporter progressReporter) {
		this.progressReporter = progressReporter;
	}

	@Override
	public void line(String line) {
		if (line.length() > 2 && line.charAt(0) == '%' && line.charAt(line.length() - 1) == '%') {
			final long time = System.currentTimeMillis();
			if (time - lastTimeMs < TIME_BETWEEN_UPDATES_MS) {
				return;
			}
			lastTimeMs = time;
			// Percent complete line.
			final String substring = line.substring(1, line.length() - 1);
			try {
				float percentComplete = Float.parseFloat(substring);
				progressReporter.reportProgress(new PercentDone(percentComplete));
			} catch (NumberFormatException ignore) {
				// SWALLOWED: We ignore these exceptions - we will just not be able to report progress
			}
		}
	}
}
