package edu.mayo.mprc.swift.ui.client.widgets;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;

public final class ProgressDialogBox extends DialogBox {

	private double widthFraction = 0.5;
	private double heightFraction = 0.5;
	private int minWidth = 200;
	private int minHeight = 150;
	private static final String PROGRESS_STYLE = "submit-progress";
	private static final double MIN_FRACTION = 0.001;
	private static final double MAX_FRACTION = 1;

	public ProgressDialogBox() {
		super(false, true);
	}

	public void setProgressMessage(String text) {
		this.addStyleName(PROGRESS_STYLE);
		this.setText(text);
		Button cancel = new Button("Cancel");
		cancel.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				hide();
			}
		});
		setWidget(cancel);
	}

	public void setRelativeSize(double widthFraction, double heightFraction) {
		assert widthFraction >= MIN_FRACTION && widthFraction <= MAX_FRACTION : "The width fraction must be in [" + MIN_FRACTION + ", " + MAX_FRACTION + "] range";
		assert heightFraction >= MIN_FRACTION && heightFraction <= MAX_FRACTION : "The height fraction must be in [" + MIN_FRACTION + ", " + MAX_FRACTION + "] range";

		this.widthFraction = widthFraction;
		this.heightFraction = heightFraction;
	}

	public void setMinimumSize(int minWidth, int minHeight) {
		this.minWidth = minWidth;
		this.minHeight = minHeight;
	}

	public void showModal() {
		positionDialog();
//		final LightBox lightBox = new LightBox(this);
//		try {
//			lightBox.show();
//		} catch (Exception ignore) {
		this.show();
//		}
	}

	private void positionDialog() {
		int clientWidth = Window.getClientWidth();
		int clientHeight = Window.getClientHeight();
		int popupWidth = (int) Math.max(clientWidth * widthFraction, minWidth);
		int popupHeight = (int) Math.max(clientHeight * heightFraction, minHeight);
		int posX = (clientWidth - popupWidth) / 2;
		int posY = (clientHeight - popupHeight) / 2;
		this.setSize(String.valueOf(popupWidth) + "px", String.valueOf(popupHeight) + "px");
		this.setPopupPosition(posX, posY);
	}
}
