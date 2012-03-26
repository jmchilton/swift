package edu.mayo.mprc.swift.ui.client.rpc.files;

import com.google.gwt.user.client.ui.TreeItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An element of directory structure - either a file or directory.
 *
 * @author: Roman Zenka
 */
public abstract class Entry implements Serializable {
	private static final long serialVersionUID = 20111119L;
	private Entry parent;
	private String name;
	private List<Entry> children;

	public abstract TreeItem createTreeItem();

	protected Entry() {
		this(null);
	}

	protected Entry(final String name) {
		parent = null;
		this.name = name;
		children = new ArrayList<Entry>(5);
	}

	public void addChild(final Entry entry) {
		children.add(entry);
		entry.setParent(this);
	}

	public List<Entry> getChildrenList() {
		return children;
	}

	public Entry getParent() {
		return parent;
	}

	public void setParent(final Entry parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

}
