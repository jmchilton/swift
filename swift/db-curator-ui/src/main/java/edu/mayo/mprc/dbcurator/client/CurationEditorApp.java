package edu.mayo.mprc.dbcurator.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.HashMap;

/**
 * Wrapper application for CurationEditor
 */
public final class CurationEditorApp implements EntryPoint {

	private static final boolean TESTING = false;

	/**
	 * this is called when we loadPanel the module (kicks off the application)
	 */
	public void onModuleLoad() {
		if (TESTING) {
			final RootPanel rootPanel = RootPanel.get("db-curator");
			HashMap<String, String> userMap = new HashMap<String, String>(3);
			userMap.put("test@test.com", "Tester Testovich Testinov");
			CurationEditor editor = new CurationEditor(0, "test@test.com", userMap, new EditorCloseCallback() {
				@Override
				public void editorClosed(Integer openCurationID) {

				}
			});
			rootPanel.add(editor);
		}
	}
}
