package edu.mayo.mprc.swift.ui.client.widgets;

import com.google.gwt.user.client.ui.Label;

public final class FilePathWidget extends Label {
	private String fullPath;
	private String prefixPath;

	public FilePathWidget(final String fullPath) {
		this.fullPath = fullPath;
		prefixPath = "";
		setTitle(this.fullPath);
	}

	public String getFullPath() {
		return fullPath;
	}

	public String getPrefixPath() {
		return prefixPath;
	}

	public void setPrefixPath(final String prefixPath) {
		this.prefixPath = prefixPath;
		setText(getDisplayText());
	}

	/**
	 * @return The name of the file, without extension.
	 */
	public String getFileNameWithoutExtension() {
		final String path = fullPath;
		return getFileNameWithoutExtension(path);
	}

	public static String getFileNameWithoutExtension(final String path) {
		// Crop path
		final int lastSlashPos = path.lastIndexOf('/');
		final String name = path.substring(lastSlashPos + 1);
		// Crop extension
		final int dotPos = name.lastIndexOf('.');
		return name.substring(0, dotPos);
	}

	private String getDisplayText() {
		if (fullPath.startsWith(prefixPath)) {
			return fullPath.substring(prefixPath.length());
		} else {
			return fullPath;
		}
	}
}
