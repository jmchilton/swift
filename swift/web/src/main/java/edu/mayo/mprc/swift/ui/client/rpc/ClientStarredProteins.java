package edu.mayo.mprc.swift.ui.client.rpc;

public final class ClientStarredProteins implements ClientValue {
	private static final long serialVersionUID = 20101221L;
	private String starred;
	private String delimiter;
	private boolean regularExpression;
	private boolean matchName;

	public ClientStarredProteins() {
	}

	public ClientStarredProteins(final String starred, final String delimiter, final boolean regularExpression, final boolean matchName) {
		this.starred = starred;
		this.delimiter = delimiter;
		this.regularExpression = regularExpression;
		this.matchName = matchName;
	}

	public String getStarred() {
		return starred;
	}

	public void setStarred(final String starred) {
		this.starred = starred;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(final String delimiter) {
		this.delimiter = delimiter;
	}

	public boolean isRegularExpression() {
		return regularExpression;
	}

	public void setRegularExpression(final boolean regularExpression) {
		this.regularExpression = regularExpression;
	}

	public boolean isMatchName() {
		return matchName;
	}

	public void setMatchName(final boolean matchName) {
		this.matchName = matchName;
	}
}

