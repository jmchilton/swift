package edu.mayo.mprc.swift.params2.mapping;

public enum ValidationSeverity {
	NONE(0), INFO(1), WARNING(2), ERROR(3);

	public int rank;

	ValidationSeverity(final int rank) {
		this.rank = rank;
	}

}
