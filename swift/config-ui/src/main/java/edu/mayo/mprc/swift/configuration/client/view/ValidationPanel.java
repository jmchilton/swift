package edu.mayo.mprc.swift.configuration.client.view;

import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.swift.configuration.client.FixTagActionListener;

/**
 * A panel that displays validation text. Has useful methods for parsing the &lt;fix&gt; tags.
 */
public final class ValidationPanel extends FlowPanel {

	private FocusWidget testButton;
	private Widget testProgress;
	private Widget successIndicator;

	/**
	 * Copy of {@link edu.mayo.mprc.config.ui.FixTag} values
	 */
	public static final String BEGINNINGTAG = "<fix";
	public static final String ENDTAG = "</fix>";
	public static final String ACTIONATTRIB = "action=\"";


	public ValidationPanel() {
		setVisible(false);
		addStyleName("validation-panel");
	}

	/**
	 * Empty the validation panel and hide it.
	 */
	public void empty() {
		clear();
		setVisible(false);
		validationReset();
	}

	public void addMessage(String message, FixTagActionListener listener) {
		fillPanel(this, message, listener);
		setVisible(true);
		validationReset();
	}

	public void addMessage(String message) {
		addMessage(message, null);
	}

	/**
	 * @param errorMessage Error message with fix tags.
	 * @param listener     listener for actions on the returned Widget.
	 * @return A widget that allows users to click on the fix tags defined in the specified string.
	 */
	public void fillPanel(Panel panel, String errorMessage, final FixTagActionListener listener) {
		if (errorMessage != null && errorMessage.trim().length() > 0) {
			String fixTag = null;
			int indexOfBeginningTag = errorMessage.indexOf(BEGINNINGTAG);
			int indexOfEndTag = errorMessage.indexOf(ENDTAG);

			if (indexOfBeginningTag != -1 && indexOfEndTag != -1) {
				fixTag = errorMessage.substring(indexOfBeginningTag, indexOfEndTag);
				int indexOfActionAttrib = fixTag.indexOf(ACTIONATTRIB);

				if (indexOfActionAttrib != -1) {
					final String action = fixTag.substring(indexOfActionAttrib + ACTIONATTRIB.length(), fixTag.indexOf("\">"));

					panel.add(new HTML(errorMessage.substring(0, errorMessage.indexOf(fixTag))));

					Hyperlink link = new Hyperlink(fixTag, true, null);
					link.addClickListener(new ClickListener() {
						public void onClick(Widget sender) {
							empty();
							validationStarted();
							listener.onFix(action, ValidationPanel.this);
						}
					});

					panel.add(link);
				}
			} else {
				panel.add(new HTML(errorMessage));
			}
		}
	}

	public void validationFailed(Throwable caught) {
		addMessage("The test failed: " + caught.getMessage(), null);
		validationReset();
	}

	public void showSuccess() {
		validationReset();
		if (successIndicator != null) {
			successIndicator.setVisible(true);
		}
	}

	private void validationReset() {
		if (successIndicator != null) {
			successIndicator.setVisible(false);
		}
		if (testButton != null) {
			testButton.setEnabled(true);
		}
		if (testProgress != null) {
			testProgress.setVisible(false);
		}
	}

	public void validationStarted() {
		validationReset();
		if (testButton != null) {
			testButton.setEnabled(false);
		}
		if (testProgress != null) {
			testProgress.setVisible(true);
		}
	}

	public FocusWidget getTestButton() {
		return testButton;
	}

	public void setTestButton(FocusWidget testButton) {
		this.testButton = testButton;
	}

	public Widget getTestProgress() {
		return testProgress;
	}

	public void setTestProgress(Widget testProgress) {
		this.testProgress = testProgress;
	}

	public Widget getSuccessIndicator() {
		return successIndicator;
	}

	public void setSuccessIndicator(Widget successIndicator) {
		this.successIndicator = successIndicator;
	}
}
