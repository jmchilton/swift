package edu.mayo.mprc.swift.configuration.client.view;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * A delete button that support confirmation.
 */
public final class DeleteButton extends PushButton {
	private String deleteMessage;

	public DeleteButton(String deleteMessage) {
		super();
		this.addStyleName("delete-button");
		this.deleteMessage = deleteMessage;
	}

	public String getDeleteMessage() {
		return deleteMessage;
	}

	public void setDeleteMessage(String deleteMessage) {
		this.deleteMessage = deleteMessage;
	}

	public void addClickListener(ClickListener listener) {
		super.addClickListener(new ConfirmationClickListener(listener));
	}

	private class ConfirmationClickListener implements ClickListener {
		private ClickListener wrappedClickListener;

		private ConfirmationClickListener(ClickListener wrappedClickListener) {
			this.wrappedClickListener = wrappedClickListener;
		}

		public void onClick(Widget sender) {
			if (Window.confirm(deleteMessage)) {
				wrappedClickListener.onClick(sender);
			}
		}
	}
}
