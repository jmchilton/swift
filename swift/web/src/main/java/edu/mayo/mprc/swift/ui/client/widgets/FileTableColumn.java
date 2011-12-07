package edu.mayo.mprc.swift.ui.client.widgets;

import com.google.gwt.user.client.ui.Widget;

class FileTableColumn {
	private int column;
	private String title;
	private Widget widget;
	private String columnStyle;

	FileTableColumn(int column, String title, Widget widget) {
		this(column, title, widget, null);
	}

	FileTableColumn(int column, String title, Widget widget, String columnStyle) {
		this.column = column;
		this.title = title;
		this.widget = widget;
		this.columnStyle = columnStyle;
	}

	public void init(FileTable table) {
		if (this.widget != null) {
			table.setWidget(table.getHeaderRowIndex(), column, widget);
		} else {
			table.setHTML(table.getHeaderRowIndex(), column, this.title);
		}
		if (columnStyle != null) {
			table.getColumnFormatter().addStyleName(column, columnStyle);
		}
	}

	public Widget getWidget() {
		return widget;
	}
}
