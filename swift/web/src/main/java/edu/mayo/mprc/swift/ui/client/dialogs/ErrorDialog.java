package edu.mayo.mprc.swift.ui.client.dialogs;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.common.client.GWTServiceException;
import edu.mayo.mprc.swift.ui.client.rpc.ClientValidation;
import edu.mayo.mprc.swift.ui.client.widgets.validation.ValidationPanel;

/**
 * Simple dialog for showing errors.
 */
public final class ErrorDialog extends DialogBox {

	private VerticalPanel verticalPanel;
	private Widget widget;

	private ErrorDialog(Throwable t) {
		super(true);
		if (t instanceof GWTServiceException) {
			init(ClientValidation.SEVERITY_ERROR, t.getMessage(), ((GWTServiceException) t).getStackTraceAsString());
		} else {
			init(ClientValidation.SEVERITY_ERROR, t.getMessage(), null);
		}
		show();
	}

	private ErrorDialog(ClientValidation cv, Widget validationWidget) {
		super(true);
		init(cv.getSeverity(), cv.getMessage(), cv.getThrowableMessage());
		widget = validationWidget;
		setWidth(validationWidget.getOffsetWidth() + " px");
		setHeight("100px");
		setPopupPositionAndShow(new MyPositionCallback());
	}

	public static void show(ClientValidation cv, Widget validationWidget) {
		final ErrorDialog dialog = new ErrorDialog(cv, validationWidget);
		dialog.show();
	}

	public static void show(Throwable t) {
		final ErrorDialog dialog = new ErrorDialog(t);
		dialog.show();
	}

	private void init(int severity, String shortMessage, String detailedMessage) {
		setText(ValidationPanel.getSeverityName(severity));
		setStyleName("errorDialog");
		verticalPanel = new VerticalPanel();
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(ValidationPanel.getImageForSeverity(severity));
		hp.add(new Label(shortMessage));
		verticalPanel.add(hp);
		ScrollPanel pane = new ScrollPanel();
		pane.setSize("700px", "300px");
		pane.setStyleName("errorPane");
		HTML html = new HTML("<br/><pre>" + detailedMessage + "</pre>");
		pane.add(html);
		verticalPanel.add(pane);
		setWidget(verticalPanel);
	}

	class MyPositionCallback implements PositionCallback {
		public void setPosition(int width, int height) {
			int clientHeight = Window.getClientHeight();

			ErrorDialog.this.setPopupPosition(widget.getAbsoluteLeft(), widget.getAbsoluteTop() + widget.getOffsetHeight());
			if (height > clientHeight) {
				ErrorDialog.this.setHeight(clientHeight - ErrorDialog.this.getAbsoluteTop() + " px");
			}
		}
	}
}
