package edu.mayo.mprc.swift;

public final class UserMessage {

	private String message;

	public UserMessage() {
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean messageDefined() {
		return message != null && message.trim().length() > 0;
	}
}
