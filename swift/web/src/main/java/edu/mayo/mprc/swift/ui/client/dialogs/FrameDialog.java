package edu.mayo.mprc.swift.ui.client.dialogs;

import com.google.gwt.user.client.ui.*;

/**
 * A floating dialog with a frame, and a close box.
 */
public abstract class FrameDialog extends DialogBox {
	private SimplePanel contentPanel;
	private Button okayButton;
	private Button cancelButton;
	private Label dialogTitleLabel;

	protected FrameDialog(String title, boolean enableOk, boolean enableCancel, boolean clickOutsideCancels, boolean modal) {
		// Closed by clicking outside only if we do not provide closing buttons
		super(clickOutsideCancels, modal);

		VerticalPanel vp = new VerticalPanel();
		vp.setStyleName("frameDialog");
		DockPanel dp = new DockPanel();
		dialogTitleLabel = new Label(title);
		dp.add(dialogTitleLabel, DockPanel.CENTER);
		// TODO: close box.
		vp.add(dp);
		contentPanel = new SimplePanel();
		vp.add(contentPanel);
		dp.setStyleName("frameDialogHeader");

		FlowPanel buttonPanel = new FlowPanel();
		buttonPanel.addStyleName("ok-cancel-button-panel");

		if (enableOk) {
			okayButton = new Button("OK");
			buttonPanel.add(okayButton);
			okayButton.addClickListener(new ClickListener() {
				public void onClick(Widget widget) {
					okay();
				}
			});
		}

		if (enableCancel) {
			cancelButton = new Button("Cancel");
			buttonPanel.add(cancelButton);
			cancelButton.addClickListener(new ClickListener() {
				public void onClick(Widget widget) {
					cancel();
				}
			});
		}

		if (enableOk || enableCancel) {
			vp.add(buttonPanel);
		}

		setWidget(vp);
	}

	protected FrameDialog(boolean enableOk, boolean enableCancel, boolean clickOutsideCancels) {
		this("", enableOk, enableCancel, clickOutsideCancels, false);
	}

	protected FrameDialog(String title, boolean enableOk, boolean enableCancel) {
		this(title, enableOk, enableCancel, !enableOk && !enableCancel, false);
	}

	protected FrameDialog(boolean enableOk, boolean enableCancel) {
		this("", enableOk, enableCancel, !enableOk && !enableCancel, false);
	}

	public String getDialogTitle() {
		return dialogTitleLabel.getText();
	}

	public void setDialogTitle(String title) {
		dialogTitleLabel.setText(title);
	}

	public void setContent(Widget contentPanel) {
		this.contentPanel.setWidget(contentPanel);
	}

	protected void enableOkButton(boolean enabled) {
		okayButton.setEnabled(enabled);
	}

	protected void enableCancelButton(boolean enabled) {
		cancelButton.setEnabled(enabled);
	}


	public void setOkListener(ClickListener listener) {
		this.okayButton.addClickListener(listener);
	}

	protected abstract void okay();

	protected abstract void cancel();
}
