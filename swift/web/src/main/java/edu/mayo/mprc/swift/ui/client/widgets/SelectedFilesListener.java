package edu.mayo.mprc.swift.ui.client.widgets;

import edu.mayo.mprc.swift.ui.client.rpc.files.FileInfo;

public interface SelectedFilesListener {
	void selectedFiles(FileInfo[] info);
}
