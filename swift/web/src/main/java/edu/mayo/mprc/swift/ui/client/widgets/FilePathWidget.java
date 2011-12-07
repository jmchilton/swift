package edu.mayo.mprc.swift.ui.client.widgets;

import com.google.gwt.user.client.ui.Label;

public final class FilePathWidget extends Label {
	private String fullPath;
	private String prefixPath;

	public FilePathWidget(String fullPath) {
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

	public void setPrefixPath(String prefixPath) {
		this.prefixPath = prefixPath;
		setText(getDisplayText());
	}

	/**
	 * @return The name of the file, without extension.
	 */
	public String getFileNameWithoutExtension() {
		String path = fullPath;
		return getFileNameWithoutExtension(path);
	}

	public static String getFileNameWithoutExtension(String path) {
		// Crop path
		int lastSlashPos = path.lastIndexOf('/');
		String name = path.substring(lastSlashPos + 1);
		// Crop extension
		int dotPos = name.lastIndexOf('.');
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
