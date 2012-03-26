package edu.mayo.mprc.swift.ui.client.rpc.files;

import java.io.Serializable;

public final class FileInfo implements Serializable {
	private static final long serialVersionUID = 20111119L;
	private String relativePath;
	private long size;

	public FileInfo() {

	}

	public FileInfo(final String relativePath, final long size) {
		this.relativePath = relativePath;
		this.size = size;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public FileInfo setRelativePath(final String relativePath) {
		this.relativePath = relativePath;
		return this;
	}

	public long getSize() {
		return size;
	}

	public FileInfo setSize(final long size) {
		this.size = size;
		return this;
	}
}
