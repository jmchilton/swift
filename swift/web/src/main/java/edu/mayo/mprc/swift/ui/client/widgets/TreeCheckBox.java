package edu.mayo.mprc.swift.ui.client.widgets;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TreeItem;

public final class TreeCheckBox extends CheckBox {
	private TreeItem treeItem;

	public TreeCheckBox() {
	}

	public TreeCheckBox(String s) {
		super(s);
	}

	public TreeCheckBox(String s, boolean b) {
		super(s, b);
	}

	public TreeCheckBox(Element element) {
		super(element);
	}

	public TreeItem getTreeItem() {
		return treeItem;
	}

	public void setTreeItem(TreeItem treeItem) {
		this.treeItem = treeItem;
	}
}
