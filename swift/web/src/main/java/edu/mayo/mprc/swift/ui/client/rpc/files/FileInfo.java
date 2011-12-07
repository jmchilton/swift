package edu.mayo.mprc.swift.ui.client.rpc.files;

import java.io.Serializable;

public final class FileInfo implements Serializable {
	private static final long serialVersionUID = 20111119L;
	private String relativePath;
	private long size;

	public FileInfo() {

	}

	public FileInfo(String relativePath, long size) {
		this.relativePath = relativePath;
		this.size = size;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public FileInfo setRelativePath(String relativePath) {
		this.relativePath = relativePath;
		return this;
	}

	public long getSize() {
		return size;
	}

	public FileInfo setSize(long size) {
		this.size = size;
		return this;
	}
}
