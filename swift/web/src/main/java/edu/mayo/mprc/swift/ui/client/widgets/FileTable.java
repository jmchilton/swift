package edu.mayo.mprc.swift.ui.client.widgets;

import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.swift.ui.client.rpc.ClientFileSearch;
import edu.mayo.mprc.swift.ui.client.rpc.ClientSearchEngine;
import edu.mayo.mprc.swift.ui.client.rpc.files.FileInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Roman Zenka
 */
public final class FileTable extends FlexTable implements SourcesChangeEvents, ChangeListener {
	private static final int SELECT_COLUMN = 0;
	private static final int FILE_COLUMN = 1;
	private static final int SIZE_COLUMN = 2;
	private static final int REMOVE_COLUMN = 3;
	private static final int FILE_COUNT_COLUMN = FILE_COLUMN;
	private static final int SAMPLE_COLUMN = 4;
	private static final int EXPERIMENT_COLUMN = 5;
	private static final int CATEGORY_COLUMN = 6;
	private static final int FIRST_ENGINE_COLUMN = 7;

	private static final int HEADER_ROW_INDEX = 1;
	private static final int FIRST_DATA_ROW_INDEX = 2;

	private static final String ROW_SELECTED_STYLE = "file-selected";
	private static final String ROW_DESELECTED_STYLE = "file-deselected";
	private static final String REMOVE_IMAGE = "images/delete.gif";

	private List<ClientSearchEngine> searchEngineList;
	private List<String> searchEnginesOnByDefault;
	private String searchTitle = ""; // Current title of the search

	private final FileTableColumn[] staticHeaders = {
			new FileTableColumn(SELECT_COLUMN, "", new CheckBox(), "button-column"),
			new FileTableColumn(FILE_COLUMN, "File", null),
			new FileTableColumn(SIZE_COLUMN, "Size", null),
			new FileTableColumn(REMOVE_COLUMN, "Remove", new PushButton(new Image(REMOVE_IMAGE)), "button-column"),
			new FileTableColumn(SAMPLE_COLUMN, "<img src=\"images/scaffold_column.gif\" style=\"vertical-align: middle;\">&nbsp;Biological Sample", null),
			new FileTableColumn(EXPERIMENT_COLUMN, "<img src=\"images/scaffold_icon.gif\" style=\"vertical-align: middle;\">&nbsp;.sfd", null),
			new FileTableColumn(CATEGORY_COLUMN, "Category", null),
	};

	FileTableColumn[] getHeaders() {
		FileTableColumn[] headers = new FileTableColumn[staticHeaders.length + searchEngineList.size()];
		System.arraycopy(staticHeaders, 0, headers, 0, staticHeaders.length);
		for (int i = 0; i < searchEngineList.size(); i++) {
			ClientSearchEngine info = searchEngineList.get(i);
			int column = i + FIRST_ENGINE_COLUMN;
			headers[column] = new FileTableColumn(column, info.getFriendlyName().replace(" ", "&nbsp;"), null, "engine-column");
		}
		return headers;
	}

	public int getFirstDataRow() {
		return FIRST_DATA_ROW_INDEX;
	}

	public int getHeaderRowIndex() {
		return HEADER_ROW_INDEX;
	}

	private static final class MutableInteger {
		public MutableInteger(int i) {
			this.i = i;
		}

		public int i;
	}

	public void onChange(Widget widget) {
		if (this.searchTypeList.equals(widget)) {
			setSearchType(this.searchTypeList.getSelectedSearchType());
		}
	}

	/**
	 * A hash map allowing us to translate a widget to its index.
	 * We use {@link MutableInteger} so we can re-index the widgets when
	 * lines are added/removed just by assigning a different index.
	 */
	private Map<Widget, MutableInteger> widgetIndices = new HashMap<Widget, MutableInteger>();

	private SearchTypeList searchTypeList;
	private Label fileCountLabel;

	private ChangeListenerCollection changeListeners = new ChangeListenerCollection();

	public FileTable(List<ClientSearchEngine> searchEngineList) {
		assert searchEngineList != null : "The list of search engines must not be null";

		searchTypeList = new SearchTypeList();
		searchTypeList.addSelectionChangeListener(this);

		setSearchEngineList(searchEngineList);

		this.fileCountLabel = new Label("", false);

		this.getRowFormatter().addStyleName(0, "info-row");
		this.getCellFormatter().addStyleName(0, FILE_COUNT_COLUMN, "file-count");
		this.setWidget(0, FILE_COUNT_COLUMN, fileCountLabel);

		this.getFlexCellFormatter().setColSpan(0, SAMPLE_COLUMN, 2);
		this.getCellFormatter().addStyleName(0, SAMPLE_COLUMN, "search-type");
		this.setWidget(0, SAMPLE_COLUMN, searchTypeList);

		FileTableColumn[] headers = getHeaders();
		for (FileTableColumn header : headers) {
			header.init(this);
		}

		((CheckBox) headers[SELECT_COLUMN].getWidget()).addClickListener(new ColumnSelectListener(SELECT_COLUMN, this));
		((PushButton) headers[REMOVE_COLUMN].getWidget()).addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				removeSelectedFiles();
			}
		});

		this.getRowFormatter().setStyleName(getHeaderRowIndex(), "table-header");

		// On every change, update count of selected files
		changeListeners.add(new ChangeListener() {
			public void onChange(Widget widget) {
				// List of files changed

				updateFileCount();
				updateMaxCommonPath();
				updateSizeDisplay();
			}
		});
		updateFileCount();
	}

	private void setSearchEngineList(List<ClientSearchEngine> searchEngineList) {
		this.searchEngineList = searchEngineList;
		searchEnginesOnByDefault = searchEnginesOnByDefault();
	}

	private void updateFileCount() {
		if (fileCountLabel != null) {
			int count = this.getRowCount() - getFirstDataRow();
			fileCountLabel.setText(count + (count == 1 ? " file" : " files"));
		}
	}

	public void addFiles(FileInfo[] fileInfos) {
		int lastRow = this.getRowCount();

		for (FileInfo info : fileInfos) {
			MutableInteger index = new MutableInteger(lastRow);

			CheckBox selection = new CheckBox();
			selection.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					setChecked(getWidgetRow(sender), SELECT_COLUMN, ((CheckBox) sender).isChecked());
				}
			});
			this.setWidget(lastRow, 0, selection);
			widgetIndices.put(selection, index);

			addNewLine(info, index, searchTitle, searchTypeList.getSelectedSearchType());

			lastRow++;
		}

		// fire change event
		changeListeners.fireChange(this);
	}

	public void setFiles(List<ClientFileSearch> inputFiles, SearchType searchType) {
		for (int i = getFirstDataRow(); i < getRowCount(); ) {
			removeTableRow(i);
		}

		this.searchTypeList.setSelectedSearchType(searchType, false/*This is coming from search load. Do not store the preference*/);
		setSearchType(searchType);

		int lastRow = this.getRowCount();

		for (final ClientFileSearch fileSearch : inputFiles) {
			MutableInteger index = new MutableInteger(lastRow);

			CheckBox selection = new CheckBox();
			selection.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					setChecked(getWidgetRow(sender), SELECT_COLUMN, ((CheckBox) sender).isChecked());
				}
			});
			this.setWidget(lastRow, 0, selection);
			widgetIndices.put(selection, index);

			addNewLine(
					fileSearch.getPath(),
					fileSearch.getFileSize(),
					index,
					fileSearch.getCategoryName(),
					fileSearch.getExperiment(),
					fileSearch.getBiologicalSample(),
					fileSearch.getEnabledEngineCodes());

			lastRow++;
		}

		changeListeners.fireChange(this);
	}

	/**
	 * The bars showing relative file size need to be updated when the list of files changes.
	 */
	private void updateSizeDisplay() {
		long maxSize = getMaxFileSize();
		for (int i = getFirstDataRow(); i < getRowCount(); i++) {
			FileSizeWidget fileSize = (FileSizeWidget) this.getWidget(i, SIZE_COLUMN);
			fileSize.setMaxSize(maxSize);
		}
	}

	/**
	 * @param info      Information about the file to add.
	 * @param lineIndex Index of this line.
	 * @param title     of the search
	 * @param type      type of the search (influences default experiment and sample names)
	 */
	private void addNewLine(FileInfo info, MutableInteger lineIndex, String title, SearchType type) {
		String path = info.getRelativePath();
		final long fileSize = info.getSize();
		String name = FilePathWidget.getFileNameWithoutExtension(path);
		final String sampleName = getDefaultSampleName(type, title, name);
		final String experimentName = getDefaultExperimentName(type, title, name);

		addNewLine(path, fileSize, lineIndex, "none", experimentName, sampleName, searchEnginesOnByDefault);
	}

	private void addNewLine(final String path, final long fileSize, final MutableInteger lineIndex, String categoryName, final String experimentName, final String sampleName, List<String> enabledSearchEngines) {
		FilePathWidget filePathWidget = new FilePathWidget(path);

		int rowNumber = lineIndex.i;

		this.setWidget(rowNumber, FILE_COLUMN, filePathWidget);
		this.setWidget(rowNumber, SIZE_COLUMN, new FileSizeWidget(fileSize));
		PushButton removeButton = new PushButton(new Image(REMOVE_IMAGE));
		removeButton.addClickListener(new RemoveButtonListener(lineIndex));
		this.setWidget(rowNumber, REMOVE_COLUMN, removeButton);

		EditableLabel sampleLabel = new EditableLabel(sampleName, new TextChangeListener(SAMPLE_COLUMN, this));
		sampleLabel.addStyleName("editable-label");
		widgetIndices.put(sampleLabel, lineIndex);
		this.setWidget(rowNumber, SAMPLE_COLUMN, sampleLabel);

		EditableLabel experimentLabel = new EditableLabel(experimentName, new TextChangeListener(EXPERIMENT_COLUMN, this));
		experimentLabel.addStyleName("editable-label");
		widgetIndices.put(experimentLabel, lineIndex);
		this.setWidget(rowNumber, EXPERIMENT_COLUMN, experimentLabel);

		EditableLabel categoryLabel = new EditableLabel(categoryName, new TextChangeListener(CATEGORY_COLUMN, this));
		categoryLabel.addStyleName("editable-label");
		widgetIndices.put(categoryLabel, lineIndex);
		this.setWidget(rowNumber, CATEGORY_COLUMN, categoryLabel);

		int engineColumnIndex = FIRST_ENGINE_COLUMN;
		for (ClientSearchEngine searchEngine : searchEngineList) {
			CheckBox engineCheckbox = new CheckBox();

			engineCheckbox.setChecked(searchEngineInList(searchEngine, enabledSearchEngines));
			engineCheckbox.addClickListener(new SearchEngineClickListener(this, engineColumnIndex));
			widgetIndices.put(engineCheckbox, lineIndex);
			this.setWidget(rowNumber, engineColumnIndex, engineCheckbox);
			engineColumnIndex++;
		}

		// By default the files are selected
		setChecked(rowNumber, SELECT_COLUMN, true);
	}

	private List<String> searchEnginesOnByDefault() {
		List<String> codeList = new ArrayList<String>(4);
		for (ClientSearchEngine engine : searchEngineList) {
			if (engine.isOnByDefault()) {
				codeList.add(engine.getCode());
			}
		}
		return codeList;
	}

	/**
	 * @return True if given search engine is listed.
	 */
	private static boolean searchEngineInList(ClientSearchEngine clientSearchEngine, List<String> engineCodeList) {
		for (String code : engineCodeList) {
			if (clientSearchEngine.getCode().equals(code)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the default name of the sample according to the search type, the search title and the file name.
	 *
	 * @param type  Type of search.
	 * @param title Title of the search.
	 * @param name  Name of the input file.
	 * @return Sample name.
	 */
	private static String getDefaultSampleName(SearchType type, String title, String name) {
		if (SearchType.ManyToOne.equals(type)) {
			return title;
		}
		return name;
	}

	/**
	 * Retrun the default name of the experiment according to the search type, the search title and the file name.
	 *
	 * @param type  Type of search.
	 * @param title Title of the search.
	 * @param name  Name of the input file.
	 * @return Sample name.
	 */
	private static String getDefaultExperimentName(SearchType type, String title, String name) {
		if (SearchType.ManyToOne.equals(type) || SearchType.ManyToSamples.equals(type)) {
			return title;
		}
		return name;
	}

	private void updateMaxCommonPath() {
		String[] maxCommonPath = null;
		int maxCommonPathLength = 0;
		for (int i = getFirstDataRow(); i < getRowCount(); i++) {
			FilePathWidget filePath = (FilePathWidget) this.getWidget(i, FILE_COLUMN);
			String path = filePath.getFullPath();
			String[] tokens = path.split("/");
			if (maxCommonPath == null) {
				maxCommonPath = tokens;
				maxCommonPathLength = tokens.length - 1;
			} else {
				for (int j = 0; j < maxCommonPathLength; j++) {
					if (!maxCommonPath[j].equals(tokens[j])) {
						maxCommonPathLength = j;
						break;
					}
				}

			}
		}

		StringBuilder pathPrefix = new StringBuilder();
		if (maxCommonPath != null) {
			for (int i = 0; i < maxCommonPathLength; i++) {
				pathPrefix.append(maxCommonPath[i]).append("/");
			}
		}

		for (int i = getFirstDataRow(); i < getRowCount(); i++) {
			FilePathWidget filePath = (FilePathWidget) this.getWidget(i, FILE_COLUMN);
			filePath.setPrefixPath(pathPrefix.toString());
		}
	}

	public void removeSelectedFiles() {
		for (int i = getFirstDataRow(); i < getRowCount(); ) {
			if (getSelectionCheckBox(i).isChecked()) {
				removeTableRow(i);
			} else {
				renumberTableRow(i);
				i++;
			}
		}

		changeListeners.fireChange(this);
	}

	public void removeFileAtRow(int row) {
		removeTableRow(row);
		for (int i = getFirstDataRow(); i < getRowCount(); i++) {
			renumberTableRow(i);
		}
		changeListeners.fireChange(this);
	}

	private void renumberTableRow(int i) {
		Widget ww = getWidget(i, SELECT_COLUMN);
		MutableInteger mi = widgetIndices.get(ww);
		if (mi != null) {
			mi.i = i;
		}
	}

	private void removeTableRow(int i) {
		for (int j = 0; j < getHeaders().length; ++j) {
			Widget ww = getWidget(i, j);
			if (ww != null) {
				widgetIndices.remove(ww);
			}
		}
		this.removeRow(i);
	}

	public void setSearchType(SearchType searchType) {
		if (!SearchType.Custom.equals(searchType)) {
			rewriteSamplesAndExperiments(searchType);
		}
	}

	/**
	 * The title has changed, update the table
	 */
	public void updateSearchTitle(String searchTitle) {
		this.searchTitle = searchTitle;
		SearchType selectedSearchType = this.searchTypeList.getSelectedSearchType();
		if (SearchType.ManyToOne.equals(selectedSearchType) || SearchType.ManyToSamples.equals(selectedSearchType)) {
			rewriteSamplesAndExperiments(selectedSearchType);
		}
	}

	private void rewriteSamplesAndExperiments(SearchType searchType) {
		for (int row = getFirstDataRow(); row < getRowCount(); row++) {
			EditableLabel w = (EditableLabel) this.getWidget(row, SAMPLE_COLUMN);
			String fileName = ((FilePathWidget) this.getWidget(row, FILE_COLUMN)).getFileNameWithoutExtension();
			w.setText(
					getDefaultSampleName(
							searchType,
							searchTitle,
							fileName));

			EditableLabel w2 = (EditableLabel) this.getWidget(row, EXPERIMENT_COLUMN);
			w2.setText(
					getDefaultExperimentName(
							searchType,
							searchTitle,
							fileName));
		}
	}

	private CheckBox getSelectionCheckBox(int row) {
		return ((CheckBox) this.getWidget(row, SELECT_COLUMN));
	}

	private long getMaxFileSize() {
		long max = 0;
		for (int i = getFirstDataRow(); i < this.getRowCount(); i++) {
			FileSizeWidget fileSize = (FileSizeWidget) this.getWidget(i, SIZE_COLUMN);
			long size = fileSize.getFileSize();
			if (size > max) {
				max = size;
			}
		}
		return max;
	}

	public void changeColumnText(int row, int column, String text) {
		// If the row is selected, change text for all selected rows
		if (getSelectionCheckBox(row).isChecked()) {
			for (int i = getFirstDataRow(); i < getRowCount(); i++) {
				if (getSelectionCheckBox(i).isChecked() && i != row) {
					EditableLabel label = (EditableLabel) getWidget(i, column);
					label.setText(text);
				}
			}
		}
	}

	public List<ClientFileSearch> getData() {
		List<ClientFileSearch> results = new ArrayList<ClientFileSearch>(this.getRowCount() - getFirstDataRow());

		for (int row = getFirstDataRow(); row < this.getRowCount(); row++) {

			EditableLabel w = (EditableLabel) this.getWidget(row, SAMPLE_COLUMN);
			String sampleName = w.getText();

			EditableLabel w2 = (EditableLabel) this.getWidget(row, EXPERIMENT_COLUMN);
			String experimentName = w2.getText();

			EditableLabel w3 = (EditableLabel) this.getWidget(row, CATEGORY_COLUMN);
			String categoryName = w3.getText();

			List<String> enabledEngines = new ArrayList<String>(searchEngineList.size());
			for (int i = 0; i < searchEngineList.size(); i++) {
				ClientSearchEngine engineInfo = searchEngineList.get(i);
				if (((CheckBox) this.getWidget(row, i + FIRST_ENGINE_COLUMN)).isChecked()) {
					enabledEngines.add(engineInfo.getCode());
				} else {
					enabledEngines.add("");
				}
			}
			results.add(new ClientFileSearch(
					((FilePathWidget) this.getWidget(row, FILE_COLUMN)).getFullPath(),
					sampleName,
					categoryName,
					experimentName,
					enabledEngines,
					null));
		}
		return results;
	}

	private class SearchEngineClickListener implements ClickListener {
		private final FileTable table;
		private final int column;

		private SearchEngineClickListener(FileTable table, int column) {
			this.table = table;
			this.column = column;
		}

		public void onClick(Widget sender) {
			// If we are on a selected row, propagate the change to all other selected checkboxes
			int r = getWidgetRow(sender);
			if (table.getSelectionCheckBox(r).isChecked()) {
				CheckBox checkBox = (CheckBox) sender;
				for (int row = getFirstDataRow(); row < table.getRowCount(); row++) {
					if (table.getSelectionCheckBox(row).isChecked()) {
						CheckBox rowCheckBox = (CheckBox) table.getWidget(row, column);
						rowCheckBox.setChecked(checkBox.isChecked());
					}
				}
			}
		}
	}

	private int getWidgetRow(Widget widget) {
		MutableInteger mi = widgetIndices.get(widget);
		if (mi == null) {
			throw new RuntimeException("Cant' find widget index for " + widget.toString());
		}
		return mi.i;
	}

	public void setChecked(int row, int column, boolean checked) {
		CheckBox rowCheckBox = (CheckBox) getWidget(row, column);
		rowCheckBox.setChecked(checked);
		// Select column also changes row style
		if (SELECT_COLUMN == column) {
			if (checked) {
				getRowFormatter().removeStyleName(row, ROW_DESELECTED_STYLE);
				getRowFormatter().addStyleName(row, ROW_SELECTED_STYLE);
			} else {
				getRowFormatter().removeStyleName(row, ROW_SELECTED_STYLE);
				getRowFormatter().addStyleName(row, ROW_DESELECTED_STYLE);
			}
		}
	}

	public void addChangeListener(ChangeListener changeListener) {
		changeListeners.add(changeListener);
	}

	public void removeChangeListener(ChangeListener changeListener) {
		changeListeners.add(changeListener);
	}

	private class TextChangeListener implements ChangeListener {
		private int _column;
		private FileTable _fileTable;

		public TextChangeListener(int column, FileTable fileTable) {
			_column = column;
			_fileTable = fileTable;
		}

		public void onChange(Widget widget) {
			EditableLabel label = (EditableLabel) widget;
			_fileTable.changeColumnText(getWidgetRow(widget), _column, label.getText());
			_fileTable.setSearchType(SearchType.Custom);
			_fileTable.searchTypeList.setSelectedSearchType(SearchType.Custom, true);
		}
	}

	private class RemoveButtonListener implements ClickListener {
		private MutableInteger rowIndex;

		public RemoveButtonListener(MutableInteger rowIndex) {
			this.rowIndex = rowIndex;
		}

		public void onClick(Widget sender) {
			removeFileAtRow(rowIndex.i);
		}
	}
}
