package edu.mayo.mprc.utilities;

import java.util.ArrayList;
import java.util.List;

public class CollectingLogMonitor implements LogMonitor {
	private final List<String> lines = new ArrayList<String>(100);

	@Override
	public void line(final String line) {
		lines.add(line);
	}

	public List<String> getLines() {
		return lines;
	}
}
