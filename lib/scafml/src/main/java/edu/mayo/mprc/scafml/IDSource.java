package edu.mayo.mprc.scafml;

/**
 * Produces new IDs by incrementing a counter.
 */
public final class IDSource {
	private int currentId;

	public IDSource(int startId) {
		currentId = startId;
	}

	public synchronized int getNextID() {
		return currentId++;
	}

}
