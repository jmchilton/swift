package edu.mayo.mprc.swift.ui.client.dialogs;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.swift.ui.client.Service;
import edu.mayo.mprc.swift.ui.client.ServiceAsync;
import edu.mayo.mprc.swift.ui.client.SimpleParamsEditorPanel;
import edu.mayo.mprc.swift.ui.client.rpc.ClientParamSet;
import edu.mayo.mprc.swift.ui.client.rpc.ClientUser;
import edu.mayo.mprc.swift.ui.client.rpc.ClientValidation;
import edu.mayo.mprc.swift.ui.client.widgets.validation.ValidationPanel;

/**
 * Prompt user to save the param set.
 */
public final class SaveDialog extends FrameDialog {
	public interface Callback {
		void saveCompleted(ClientParamSet paramSet);
	}

	private ServiceAsync service;
	private Callback cb;
	private ClientParamSet paramSet;
	private TextBox newName;
	private HorizontalPanel newNameValidation;
	private TextBox userNameTextBox;
	private ClientUser user;

	public SaveDialog(final ClientParamSet paramSet, final ServiceAsync service, final ClientUser user,
	                  final Callback cb) {
		super("Save", true, true);

		this.service = service;
		this.cb = cb;
		this.paramSet = paramSet;
		this.user = user;

		Label l;
		final Grid g = new Grid(3, 3);
		l = new Label("Save:");
		g.setWidget(0, 0, l);
		l.setStyleName("italic");
		g.setWidget(0, 1, new Label(paramSet.getName()));

		l = new Label("As:");
		g.setWidget(1, 0, l);
		l.setStyleName("italic");
		newName = new TextBox();
		g.setWidget(1, 1, newName);
		final KeyListener kl = new KeyListener();
		newName.addKeyboardListener(kl);

		l = new Label("Owner:");
		g.setWidget(2, 0, l);
		l.setStyleName("italic");

		userNameTextBox = new TextBox();
		g.setWidget(2, 1, userNameTextBox);
		userNameTextBox.setText(user.getName());
		userNameTextBox.setEnabled(false);

		newNameValidation = new HorizontalPanel();
		newNameValidation.addStyleName("invisible");
		newNameValidation.add(ValidationPanel.getImageForSeverity(ClientValidation.SEVERITY_WARNING));
		newNameValidation.add(new Label("That name is already in use"));
		g.setWidget(1, 2, newNameValidation);

		setContent(g);
		center();
		setValidStatus();
		show();
	}

	private boolean setValidStatus() {
		boolean enabled = true;
		if ("".equals(newName.getText())) {
			// is it actually necessary to show a valdation here;
			enabled = false;
		}

		enableOkButton(enabled);
		return enabled;
	}

	public final class KeyListener extends KeyboardListenerAdapter {
		public void onKeyUp(final Widget sender, final char keyCode, final int modifiers) {
			setValidStatus();
			super.onKeyUp(sender, keyCode, modifiers);
		}
	}

	protected void cancel() {
		hide();
	}

	protected void okay() {
		if (!setValidStatus()) {
			return; // TODO need better validation.
		}

		service.save(new Service.Token(true),
				paramSet,
				newName.getText(),
				user.getEmail(),
				user.getInitials(),
				true,
				new AsyncCallback<ClientParamSet>() {
					public void onFailure(final Throwable throwable) {
						hide();
						SimpleParamsEditorPanel.handleGlobalError(throwable);
					}

					public void onSuccess(final ClientParamSet o) {

						if (o != null) {
							cb.saveCompleted(o);
							hide();
						} else {
							setValidStatus();
						}
					}
				}
		);
	}
}
