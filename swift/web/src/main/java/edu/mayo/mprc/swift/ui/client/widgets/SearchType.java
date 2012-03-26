package edu.mayo.mprc.swift.ui.client.widgets;

// Enum implemented by hand
public final class SearchType {
	private int type;

	private SearchType(final int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public static SearchType fromType(final int i) {
		switch (i) {
			case 0:
				return OneToOne;
			case 1:
				return ManyToOne;
			case 2:
				return ManyToSamples;
			case 3:
				return Custom;
			default:
				throw new RuntimeException("Unknown search type index " + i);
		}
	}

	public static final SearchType OneToOne = new SearchType(0);
	public static final SearchType ManyToOne = new SearchType(1);
	public static final SearchType ManyToSamples = new SearchType(2);
	public static final SearchType Custom = new SearchType(3);
}

