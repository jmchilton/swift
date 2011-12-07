package edu.mayo.mprc.swift.ui.client.widgets;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

public final class ColumnSelectListener implements ClickListener {
	private int column;
	private FileTable table;

	public ColumnSelectListener(int column, FileTable table) {
		this.column = column;
		this.table = table;
	}

	public void onClick(Widget widget) {
		CheckBox mainCheckBox = (CheckBox) widget;
		for (int row = table.getFirstDataRow(); row < table.getRowCount(); row++) {
			table.setChecked(row, column, mainCheckBox.isChecked());
		}
	}
}
