package edu.mayo.mprc.swift.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.swift.ui.client.rpc.*;
import edu.mayo.mprc.swift.ui.client.rpc.files.FileInfo;
import edu.mayo.mprc.swift.ui.client.widgets.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * * Entry point classes define <code>onModuleLoad()</code>.
 */
public final class SwiftApp implements EntryPoint, HidesPageContentsWhileLoading {

	private static final String SELECT_USER_STRING = "<Select User>";

	private ListBox users = new ListBox();
	private TextBox title = new TextBox();
	private HTML messageDisplay = new HTML("");

	private FileTable files;

	private TextBox output;
	private PushButton runButton;
	private PushButton addFileButton;
	private SimpleParamsEditorPanel paramsEditor;
	private ToggleButton editorToggle;
	private SpectrumQaSetupPanel spectrumQaSetupPanel;
	private ReportSetupPanel reportSetupPanel;
	private AdditionalSettingsPanel additionalSettingsPanel;

	private Map<String, ClientUser> userInfo = new HashMap<String, ClientUser>();

	/**
	 * This will get incremented with every RPC call made on the page load, and decremented when the load
	 * finishes/fails. When the counter drops back to zero, the page contents will become visible by
	 * removing the class="hidden" from #hidden-until-loaded div.
	 */
	private int hiddenWhileLoadingCounter = 0;

	private String outputPath;
	private boolean outputPathChangeEnabled = true;

	// The output path control is showing a special value - not really a path, more of an informative string
	private boolean outputPathSpecial = false;
	// The user changed the output path, do not update it automatically
	private boolean outputPathUserSpecified = false;
	// The querystring parameter specifying what search definition to load initially
	private static final String LOAD_SEARCH_DEFINITION_ID = "load";
	private SwiftApp.TitleChangeListener titleChangeListener;
	private int previousSearchRunId;

	/**
	 * This is the entry point method. Creates all the elements and hooks up some
	 * event listeners.
	 */
	public void onModuleLoad() {
		try {
			DOM.setStyleAttribute(DOM.getElementById("shown_while_loading"), "display", "block");
			DOM.setStyleAttribute(DOM.getElementById("hidden_while_loading"), "display", "none");
			hidePageContentsWhileLoading();

			initFileTable();
			initSpectrumQa();
			initReport();
			initPublicMgfs();
			initTitleEditor();
			initParamsEditor();
			initEditorToggle();
			initUserList();
			initAddFilesButton();
			initOutputLocation();
			initRunButton();
			initMessage();
			loadPreviousSearch();
		} catch (Exception t) {
			throw new RuntimeException(t);
		} finally {
			loadCompleted();
			showPageContentsAfterLoad();
		}
	}

	public synchronized void hidePageContentsWhileLoading() {
		hiddenWhileLoadingCounter++;
	}

	public synchronized void showPageContents() {
		DOM.setStyleAttribute(DOM.getElementById("shown_while_loading"), "display", "none");
		DOM.setStyleAttribute(DOM.getElementById("hidden_while_loading"), "display", "block");
	}

	public synchronized void showPageContentsAfterLoad() {
		hiddenWhileLoadingCounter--;
		if (hiddenWhileLoadingCounter == 0) {
			showPageContents();
		}
	}

	private void loadCompleted() {
		// The load completed, enable buttons
		this.runButton.setEnabled(true);
		this.addFileButton.setEnabled(true);
	}

	private void initFileTable() {
		hidePageContentsWhileLoading();
		ServiceConnection.instance().listSearchEngines(
				new AsyncCallback<List<ClientSearchEngine>>() {
					public void onFailure(final Throwable throwable) {
						// SWALLOWED: No search engines. We just create filetable that does not let user to enable any search engine
						finalizeFileTableInit(new ArrayList<ClientSearchEngine>());
					}

					public void onSuccess(final List<ClientSearchEngine> o) {
						finalizeFileTableInit(o);
					}
				});
	}

	private void finalizeFileTableInit(final List<ClientSearchEngine> o) {
		files = new FileTable(o);
		final RootPanel filePanel = RootPanel.get("fileTable");
		filePanel.add(files);
		connectOutputLocationAndFileTable();
		showPageContentsAfterLoad();
	}

	private void initSpectrumQa() {
		hidePageContentsWhileLoading();
		ServiceConnection.instance().listSpectrumQaParamFiles(new AsyncCallback<List<SpectrumQaParamFileInfo>>() {
			public void onFailure(final Throwable throwable) {
				// SWALLOWED: No msmsEval available. Do not even create the spectrum QA UI
				finalizeSpectrumQa(null);
			}

			public void onSuccess(final List<SpectrumQaParamFileInfo> o) {
				finalizeSpectrumQa(o);
			}
		});
	}

	private void initPublicMgfs() {
		hidePageContentsWhileLoading();
		additionalSettingsPanel = new AdditionalSettingsPanel();
		final RootPanel panel = RootPanel.get("publicResults");
		panel.add(additionalSettingsPanel);
		showPageContentsAfterLoad();
	}

	private void finalizeSpectrumQa(final List<SpectrumQaParamFileInfo> list) {
		if (list == null || list.size() == 0) {
			DOM.setStyleAttribute(DOM.getElementById("spectrumQaRow"), "display", "none");
		} else {
			DOM.setStyleAttribute(DOM.getElementById("spectrumQaRow"), "display", "");
			spectrumQaSetupPanel = new SpectrumQaSetupPanel(list);
			final RootPanel rootSpectrumQaPanel = RootPanel.get("spectrumQa");
			rootSpectrumQaPanel.add(spectrumQaSetupPanel);
		}
		showPageContentsAfterLoad();
	}

	private void initTitleEditor() {
		final RootPanel titlePanel = RootPanel.get("title");
		title.setVisibleLength(30);
		titlePanel.add(title);
		title.addKeyboardListener(new KeyboardListenerAdapter() {
			public void onKeyUp(final Widget sender, final char keyCode, final int modifiers) {
				// TODO Validation
				updateOutputLocation();
			}
		});
		titleChangeListener = new TitleChangeListener();
		title.addChangeListener(titleChangeListener);
	}

	private void initReport() {
		ServiceConnection.instance().isScaffoldReportEnabled(new AsyncCallback<Boolean>() {
			public void onFailure(final Throwable throwable) {
				DOM.setStyleAttribute(DOM.getElementById("reportingRow"), "display", "none");
			}

			public void onSuccess(final Boolean reportEnabled) {
				if (reportEnabled) {
					DOM.setStyleAttribute(DOM.getElementById("reportingRow"), "display", "");
					reportSetupPanel = new ReportSetupPanel();
					final RootPanel rootPanel = RootPanel.get("report");
					rootPanel.add(reportSetupPanel);
				} else {
					DOM.setStyleAttribute(DOM.getElementById("reportingRow"), "display", "none");
				}
			}
		});
	}

	private void initParamsEditor() {
		ParamsEditorApp.onModuleLoad(this, userInfo);
		paramsEditor = ParamsEditorApp.getPanel();
	}

	private void initEditorToggle() {
		final RootPanel togglePanel = RootPanel.get("paramsToggleButton");
		editorToggle = new ToggleButton(new Image("images/triright.png"), new Image("images/tridown.png"));
		editorToggle.setDown(false);
		editorToggle.addStyleName("toggle-button");
		togglePanel.add(editorToggle);
		editorToggle.addClickListener(new ClickListener() {
			public void onClick(final Widget sender) {
				paramsEditor.setEditorExpanded(editorToggle.isDown());
			}
		});
	}

	private void userChanged() {
		if (users == null) {
			return;
		}
		ClientUser user = null;
		if (users.getSelectedIndex() > 0) {
			final String email = users.getValue(users.getSelectedIndex());
			if (userInfo.containsKey(email)) {
				user =
						userInfo.get(email);
			}

			Cookies.setCookie("email", users.getValue(users.getSelectedIndex()), ParamSetSelectionController.getCookieExpirationDate(), null, "/", false);
		}

		// Figure out whether this user can edit parameters or not
		final boolean editorEnabled = user != null && user.isParameterEditorEnabled();
		if (paramsEditor != null) {
			paramsEditor.setEditorEnabled(editorEnabled, user);
		}
		setOutputPathChangeEnabled(user != null && user.isOutputPathChangeEnabled());
	}

	private void setOutputPathChangeEnabled(final boolean enabled) {
		if (output != null && enabled != this.outputPathChangeEnabled) {
			this.outputPathChangeEnabled = enabled;
			if (!outputPathChangeEnabled) {
				outputPathUserSpecified = false;
			}
			output.setReadOnly(!enabled);
			// Update the output location, when changes are disabled, the field gets filled automatically
			updateOutputLocation();
		}
	}

	private static String wrapDisplayMessage(final String message) {
		return "<div class=\"user-message\">" + message + "</div>";
	}

	private void initMessage() {
		final RootPanel messagePanel = RootPanel.get("messagePlaceholder");
		messageDisplay.setVisible(false);
		messagePanel.add(messageDisplay);
		updateUserMessage();
	}

	/**
	 * returns specific query-string parameter
	 *
	 * @param name Query string parameter name
	 * @return Value of the parameter
	 */
	private static String getQueryString(final String name) {
		return Window.Location.getParameter(name);
	}

	/**
	 * Check query string to see if the user wants to load previous search data into the forms.
	 */
	private void loadPreviousSearch() {
		hidePageContentsWhileLoading();
		final String searchRunIdString = getQueryString(LOAD_SEARCH_DEFINITION_ID);
		if (searchRunIdString == null) {
			showPageContentsAfterLoad();
			return;
		}
		int searchRunId = -1;
		try {
			searchRunId = Integer.parseInt(searchRunIdString);
			previousSearchRunId = searchRunId;
			ServiceConnection.instance().loadSearch(new Service.Token(true), searchRunId, new AsyncCallback<ClientLoadedSearch>() {
				public void onFailure(final Throwable caught) {
					// SWALLOWED: not a big deal when a load fails
					// Clear the search run id - load did not work
					previousSearchRunId = 0;
					showPageContentsAfterLoad();
				}

				public void onSuccess(final ClientLoadedSearch result) {
					final ClientSwiftSearchDefinition definition = result.getDefinition();
					displayMessage("Loaded previous search " + definition.getSearchTitle());

					if (result.getClientParamSetList() != null) {
						// We transfered a new parameter set list together with search definition
						// Update the parameter editor.
						paramsEditor.setParamSetList(result.getClientParamSetList());
					}

					paramsEditor.setSelectedParamSet(result.getDefinition().getParamSet());

					// Determine search type
					SearchType searchType = null;
					for (final ClientFileSearch clientFileSearch : definition.getInputFiles()) {
						final String fileNamefileNameWithoutExtension = FilePathWidget.getFileNameWithoutExtension(clientFileSearch.getPath());
						final SearchType newSearchType = SearchTypeList.getSearchTypeFromSetup(
								definition.getSearchTitle(),
								fileNamefileNameWithoutExtension,
								clientFileSearch.getExperiment(),
								clientFileSearch.getBiologicalSample());
						if (searchType == null) {
							searchType = newSearchType;
						} else {
							if (!searchType.equals(newSearchType)) {
								searchType = SearchType.Custom;
								break;
							}
						}
					}

					final String title = definition.getSearchTitle();
					setTitleText(title);
					if (spectrumQaSetupPanel != null) {
						spectrumQaSetupPanel.setParameters(definition.getSpectrumQa());
					}
					if (reportSetupPanel != null) {
						reportSetupPanel.setParameters(definition.getPeptideReport());
					}
					final List<ClientFileSearch> inputFiles = definition.getInputFiles();

					files.setFiles(inputFiles, searchType);

					output.setText(definition.getOutputFolder());
					selectUser(definition.getUser().getEmail());

					additionalSettingsPanel.setPublicMgfs(definition.isPublicMgfFiles());
					additionalSettingsPanel.setPublicSearchFiles(definition.isPublicSearchFiles());

					showPageContentsAfterLoad();
				}
			});
		} catch (Exception t) {
			throw new RuntimeException("Invalid search definition id: " + searchRunId, t);
		}
	}

	private void updateUserMessage() {
		ServiceConnection.instance().getUserMessage(new AsyncCallback<String>() {
			public void onFailure(final Throwable throwable) {
				displayMessage("");
			}

			public void onSuccess(final String message) {
				displayMessage(message);
			}
		});
	}

	private void displayMessage(final String message) {
		if (message != null && message.length() > 0) {
			messageDisplay.setHTML(wrapDisplayMessage(message));
			messageDisplay.setVisible(true);
		} else {
			messageDisplay.setVisible(false);
		}
	}

	private void initUserList() {
		// The user listing is downloaded by an async call.
		final RootPanel userPanel = RootPanel.get("email");
		userPanel.add(users);
		users.addChangeListener(new ChangeListener() {
			public void onChange(final Widget widget) {
				userChanged();
			}
		});
		hidePageContentsWhileLoading();
		ServiceConnection.instance().listUsers(new AsyncCallback<ClientUser[]>() {
			public void onFailure(final Throwable throwable) {
				showPageContents();
			}

			public void onSuccess(final ClientUser[] list) {
				users.addItem(SELECT_USER_STRING, "");
				userInfo.clear();
				for (final ClientUser user : list) {
					users.addItem(user.getName(), user.getEmail());
					userInfo.put(user.getEmail(), user);
				}

				// Select the user according to the cookie stored
				final String userEmail = Cookies.getCookie("email");
				selectUser(userEmail);

				showPageContentsAfterLoad();
			}
		});
	}

	private void selectUser(final String userEmail) {
		if (userEmail != null) {
			for (int i = 0; i < users.getItemCount(); i++) {
				if (users.getValue(i).equalsIgnoreCase(userEmail)) {
					users.setSelectedIndex(i);
					break;
				}
			}
		}
		userChanged();
	}

	private void initAddFilesButton() {
		// Add files button produces a dialog
		addFileButton = new PushButton("", new ClickListener() {
			public void onClick(final Widget sender) {
				final int clientWidth = Window.getClientWidth();
				final int clientHeight = Window.getClientHeight();
				final int popupWidth = clientWidth * 3 / 4;
				final int popupHeight = clientHeight * 3 / 4;
				final int posX = (clientWidth - popupWidth) / 2;
				final int posY = (clientHeight - popupHeight) / 2;
				final FileTreeDialog dialog = new FileTreeDialog(popupWidth, popupHeight);
				dialog.setPopupPosition(posX, posY);
				dialog.setSelectedFilesListener(new SelectedFilesListener() {
					public void selectedFiles(final FileInfo[] info) {
						files.addFiles(info);
					}
				});
//				try {
//					LightBox lightBox = new LightBox(dialog);
//					lightBox.show();
//				} catch (Exception ignore) {
				// The lightbox failed.
				dialog.show();
//				}
			}
		});
		addFileButton.setEnabled(false);
		RootPanel.get("addFileButton").add(addFileButton);
	}

	private void initOutputLocation() {
		// currently, the save location is populated deterministically by the combination of
		// the users's input

		final RootPanel outputPanel = RootPanel.get("output");
		output = new TextBox();
		output.setVisibleLength(150);
		output.addChangeListener(new ChangeListener() {
			public void onChange(final Widget widget) {
				outputPathUserSpecified = true;
				updateOutputLocation();
			}
		});
		outputPanel.add(output);
		connectOutputLocationAndFileTable();
		// Fire userChanged to get the output location updated
		userChanged();
		updateOutputLocation();
	}

	// Called after either the output location or the file table get initialized.

	private void connectOutputLocationAndFileTable() {
		if (files != null) {
			files.addChangeListener(new ChangeListener() {
				public void onChange(final Widget widget) {
					updateOutputLocation();
				}
			});
		}
	}


	private void initRunButton() {
		// Run Button ////////////////////////////////////////////
		runButton = new PushButton();
		RootPanel.get("runButton").add(runButton);
		runButton.setEnabled(false);
		// Make params editor disable the runWithCallback button when it is invalid
		if (paramsEditor != null) {
			paramsEditor.getValidationController().addChangeListener(new ChangeListener() {
				public void onChange(final Widget sender) {
					runButton.setEnabled(paramsEditor.isValid());
				}
			});
		}

		runButton.addClickListener(new RunClickListener(this));
	}

	/**
	 * Update the output location as appropriate.
	 */
	public void updateOutputLocation() {
		if (!outputPathChangeEnabled || !outputPathUserSpecified) {
			// The user is not able to or chose not to influence output path, it gets set automatically
			final List<ClientFileSearch> fileSearches = files != null ? files.getData() : null;
			if (files == null || (fileSearches == null) || (fileSearches.size() == 0)) {
				if (!outputPathChangeEnabled) {
					output.setText("<No Files Selected>");
					outputPathSpecial = true;
				} else {
					output.setText("");
				}
				outputPath = null;
			} else if ((getTitleText() == null) || getTitleText().equals("")) {
				if (!outputPathChangeEnabled) {
					output.setText("<No Title>");
					outputPathSpecial = true;
				} else {
					output.setText("");
				}
				outputPath = null;
			} else {
				outputPathSpecial = false;
				outputPath = fileSearches.get(0).getPath();
				if (!outputPath.endsWith("/")) {
					final int i = outputPath.lastIndexOf('/');
					if (i < 0) {
						outputPath = "";
					} else {
						outputPath = outputPath.substring(0, i + 1);
					}
				}
				outputPath += pathify(getTitleText());
				output.setText(outputPath);
			}
		} else {
			// The user can influence output path. Keep whatever was the previous setting, unless
			// it is a special value like "<No Files Selected>" or "<No Title>" - wipe that one.
			if (outputPathSpecial) {
				outputPathSpecial = false;
			}
		}
		// TODO update validation state;
	}

	/**
	 * When the title changes, we need to fire onChange event on its listener.
	 */
	private void setTitleText(final String title) {
		this.title.setText(title);
		titleChangeListener.onChange(this.title);
	}

	/**
	 * @return Contents of the Title field, trimmed.
	 */
	private String getTitleText() {
		return title.getText() == null ? null : title.getText().trim();
	}

	static String pathify(final String userProvided) {
		final String s = userProvided.replaceAll("\\.\\.", "_");
		return s.replaceAll("/", "_");
	}

	//redirect the browser to the given url

	public static native void redirect(String url)/*-{
		$wnd.location = url;
	}-*/;

	private class RunClickListener implements ClickListener {
		private final SwiftApp swiftApp;

		public RunClickListener(final SwiftApp app) {
			swiftApp = app;
		}

		public void onClick(final Widget sender) {
			updateUserMessage();
			updateOutputLocation();

			final String effectiveOutputPath = outputPathUserSpecified ? output.getText() : outputPath;
			final String user = users.getValue(users.getSelectedIndex());

			if ((effectiveOutputPath == null) || user.equals("") || user.equals(SELECT_USER_STRING) || files == null) {
				Window.alert("No files, no title, or no user specified");
				return;
			}

			final ProgressDialogBox dialog = new ProgressDialogBox();
			dialog.setProgressMessage("Submitting search...");
			dialog.setRelativeSize(0.25, 0.25);
			dialog.setMinimumSize(200, 200);
			dialog.showModal();

			try {
				final List<ClientFileSearch> entries = files.getData();

				final ClientSpectrumQa clientSpectrumQa;
				if (spectrumQaSetupPanel != null) {
					clientSpectrumQa = spectrumQaSetupPanel.getParameters();
				} else {
					clientSpectrumQa = new ClientSpectrumQa();
				}
				final ClientSwiftSearchDefinition def = new ClientSwiftSearchDefinition(
						getTitleText(),
						userInfo.get(users.getValue(users.getSelectedIndex())),
						effectiveOutputPath,
						paramsEditor.getSelectedParamSet(),
						entries,
						clientSpectrumQa,
						reportSetupPanel == null ? new ClientPeptideReport(false) : reportSetupPanel.getParameters(),
						additionalSettingsPanel.isPublicMgfs(),
						additionalSettingsPanel.isPublicSearchFiles(),
						previousSearchRunId
				);
				def.setFromScratch(additionalSettingsPanel.isFromScratch());

				ServiceConnection.instance().startSearch(new Service.Token(true), def, new AsyncCallback<Void>() {
					public void onFailure(final Throwable throwable) {
						dialog.hide();
						SimpleParamsEditorPanel.handleGlobalError(throwable);
					}

					public void onSuccess(final Void o) {
						dialog.hide();
						redirect("/report/report.jsp");
					}
				});
			} catch (Exception e) {
				dialog.hide();
				SimpleParamsEditorPanel.handleGlobalError(e);
			}
		}
	}

	private class TitleChangeListener implements ChangeListener {
		public void onChange(final Widget sender) {
			files.updateSearchTitle(getTitleText());
		}
	}
}
