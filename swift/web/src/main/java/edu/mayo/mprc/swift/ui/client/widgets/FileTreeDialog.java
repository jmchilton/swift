package edu.mayo.mprc.swift.ui.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.swift.ui.client.Service;
import edu.mayo.mprc.swift.ui.client.ServiceAsync;
import edu.mayo.mprc.swift.ui.client.rpc.files.FileInfo;

/**
 * A dialog box that contains a file tree.
 *
 * @author: Roman Zenka
 */
public final class FileTreeDialog extends DialogBox implements ClickListener {
	private final Button okButton;
	private final Button cancelButton;
	private SelectedFilesListener selectedFilesListener;

	public FileTreeDialog(int width, int height) {
		super(false);

		DockPanel caption = new DockPanel();
		caption.add(new Label("Select files and folders"), DockPanel.CENTER);
		caption.addStyleName("file-dialog-caption");

		cancelButton = new Button("Cancel");
		caption.add(cancelButton, DockPanel.EAST);
		cancelButton.addClickListener(this);

		okButton = new Button("Ok");
		caption.add(okButton, DockPanel.EAST);
		okButton.addClickListener(this);

		ScrollPanel panel = new ScrollPanel();
		panel.setPixelSize(width, height);

		Grid fileListTable = new Grid(1, 1);
		fileListTable.addStyleName("maintable");
		DOM.setElementAttribute(fileListTable.getElement(), "id", "maintable");

		DOM.setElementAttribute(fileListTable.getRowFormatter().getElement(0), "class", "shrink");

		Element fileList = DOM.createDiv();
		DOM.setElementAttribute(fileList, "id", "filelist");
		DOM.setElementAttribute(fileList, "class", "filelist");

		Element fileListCell = fileListTable.getCellFormatter().getElement(0, 0);
		DOM.setInnerHTML(fileListCell, null);

		DOM.setElementAttribute(fileListCell, "id", "filelistcell");
		DOM.setElementAttribute(fileListCell, "valign", "top");
		fileListTable.getCellFormatter().setStyleName(0, 0, "filelistcell");

		DOM.insertChild(fileListCell, fileList, 0);

		panel.add(fileListTable);

		DockPanel contents = new DockPanel();
		contents.add(panel, DockPanel.CENTER);
		contents.add(caption, DockPanel.NORTH);
		setWidget(contents);

	}

	public void show() {
		super.show();
		showFileDialogBox("filelist", "");
	}

	public SelectedFilesListener getSelectedFilesListener() {
		return selectedFilesListener;
	}

	public void setSelectedFilesListener(SelectedFilesListener selectedFilesListener) {
		this.selectedFilesListener = selectedFilesListener;
	}

	public void onClick(Widget widget) {
		if (widget.equals(okButton)) {
			String selectedFilesHairBall = getSelectedFiles();

			String[] eachSelectedFile = selectedFilesHairBall.split("\\n");
			final ServiceAsync fileFinderService = (ServiceAsync) GWT.create(Service.class);
			ServiceDefTarget endpoint = (ServiceDefTarget) fileFinderService;
			endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "Service");

			fileFinderService.findFiles(eachSelectedFile, new AsyncCallback<FileInfo[]>() {

				public void onFailure(Throwable throwable) {
					//TODO: implement me
				}

				public void onSuccess(FileInfo[] o) {
					selectedFilesListener.selectedFiles(o);
				}
			});

			this.hide();
		} else if (widget.equals(cancelButton)) {
			this.hide();
		}
	}

	/**
	 * Calling this method will load the old Swift 1.0 FileChooser.  When the dialog is closed then a List of String of the
	 * paths to the selected file will be returned.
	 *
	 * @return the List<String> denoting the paths to the selected files/folders.
	 */
	public static native void showFileDialogBox(String iframe, String basePath)/*-{
		$wnd.initDialog(iframe, null, "Load", basePath);

	}-*/;

	/**
	 * @return
	 */
	public static native String getSelectedFiles()/*-{
		var retFiles = "";

		var selectedFiles = $wnd.getSelectedFilesAndFolders();
		for (var i = 0; i < selectedFiles.length; i++) {
			retFiles += selectedFiles[i] + "\n";
		}

		return retFiles;
	}-*/;

}
