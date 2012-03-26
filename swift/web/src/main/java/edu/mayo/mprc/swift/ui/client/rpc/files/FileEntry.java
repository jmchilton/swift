package edu.mayo.mprc.swift.ui.client.rpc.files;

import com.google.gwt.user.client.ui.TreeItem;
import edu.mayo.mprc.common.client.StringUtilities;
import edu.mayo.mprc.swift.ui.client.widgets.TreeCheckBox;

/**
 * A file. FileEntry knows about its size and maybe some other information.
 *
 * @author: Roman Zenka
 */
public final class FileEntry extends Entry {
	private static final long serialVersionUID = 20101221L;

	public FileEntry() {
	}

	public FileEntry(final String name) {
		super(name);
	}

	public TreeItem createTreeItem() {
		final TreeCheckBox checkBox = new TreeCheckBox(getName(), false);
		final TreeItem newItem = new TreeItem(checkBox);
		checkBox.setTreeItem(newItem);
		if (StringUtilities.toUpperCase(getName()).endsWith(".RAW")) {
			newItem.addStyleName("file-raw");
		} else if (getName().endsWith(".mgf")) {
			newItem.addStyleName("file-mgf");
		}
		return newItem;
	}
}
