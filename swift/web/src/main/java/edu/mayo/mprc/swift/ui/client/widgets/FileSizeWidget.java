package edu.mayo.mprc.swift.ui.client.widgets;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public final class FileSizeWidget extends FlowPanel {
	private long fileSize;
	private Label text;
	private Label bar;
	private static final String[] units = {"B", "K", "M", "G", "T", "P", "E", "Z", "Y"};

	/**
	 * @param fileSize Size of the file. When negative, it means the file does not exist.
	 */
	public FileSizeWidget(final long fileSize) {
		this.addStyleName("file-size");
		if (fileSize >= 0) {
			this.fileSize = fileSize;
			this.text = new Label(sizeToText(this.fileSize));
			this.text.addStyleName("file-size-text");
			this.bar = new Label();
			this.bar.addStyleName("file-size-bar");
			this.bar.setWidth("0");
			this.add(this.bar);
		} else {
			this.fileSize = 0;
			this.text = new Label("missing");
			this.text.addStyleName("file-size-text-missing");
		}
		this.add(this.text);
	}

	public void setMaxSize(final long maxSize) {
		if (this.bar != null) {
			if (maxSize <= 0) {
				return;
			}
			final double percent = 100.0 * (double) this.fileSize / (double) maxSize;
			this.bar.setWidth(String.valueOf((int) (percent + 0.5)) + '%');
		}
	}

	public long getFileSize() {
		return fileSize;
	}

	private static String sizeToText(final long fileSize) {
		double size = fileSize;
		int unit = 0;
		while (size >= 1024) {
			unit++;
			size /= 1024;
		}

		return NumberFormat.getDecimalFormat().format(size) + units[unit];
	}
}

