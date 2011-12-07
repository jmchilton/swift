package edu.mayo.mprc.swift.ui.client.dialogs;

import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Dialog box that shows text messages. It allows appending text message
 * to current text.
 */
public final class ProgressDialog extends FrameDialog {

	private TextArea textArea;

	public ProgressDialog(String title, boolean modal) {
		super(title, true, false, false, modal);

		textArea = new TextArea();
		textArea.setSize("1000px", "600px");
		ScrollPanel scrollPanel = new ScrollPanel(textArea);
		setContent(scrollPanel);
		center();
	}

	public void setText(String text) {
		textArea.setText(text);
	}

	public void appendText(String text) {
		String currentText = textArea.getText();
		textArea.setText(currentText + text);
	}

	@Override
	public void enableOkButton(boolean enabled) {
		super.enableOkButton(enabled);
	}

	@Override
	public void enableCancelButton(boolean enabled) {
		super.enableCancelButton(enabled);
	}

	@Override
	protected void okay() {
		hide();
	}

	@Override
	protected void cancel() {
		//No cancel action is implemented.
	}
}
