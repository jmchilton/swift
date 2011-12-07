package edu.mayo.mprc.dbcurator.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

class MessagePopup extends PopupPanel {
	public MessagePopup(String msg, int xPosition, int yPosition) {
		super(true);
		this.setPopupPosition(xPosition, yPosition);
		this.setStyleName("curator-message-popup");
		setWidget(new Label(msg));
	}

	public void show(int msToDisplay) {
		super.show();
		if (msToDisplay > 0) {
			Timer hideTimer = new Timer() {
				public void run() {
					MessagePopup.this.hide();
				}
			};
			hideTimer.schedule(msToDisplay);
		}
	}

	public void show() {
		this.show(2500);
	}
}
