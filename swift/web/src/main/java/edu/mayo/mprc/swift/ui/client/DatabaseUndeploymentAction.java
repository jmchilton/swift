package edu.mayo.mprc.swift.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import edu.mayo.mprc.swift.ui.client.dialogs.ProgressDialog;
import edu.mayo.mprc.swift.ui.client.rpc.ClientDatabaseUndeployerProgress;
import edu.mayo.mprc.swift.ui.client.rpc.ClientSequenceDatabase;
import edu.mayo.mprc.swift.ui.client.widgets.validation.DatabaseListBox;

public final class DatabaseUndeploymentAction implements ClickListener {

	private ProgressDialog progressDialog;
	private ServiceAsync serviceAsync;
	private DatabaseListBox dlb;

	public DatabaseUndeploymentAction(ServiceAsync serviceAsync, DatabaseListBox dlb) {
		this.serviceAsync = serviceAsync;
		this.dlb = dlb;
	}

	public void onClick(Widget widget) {
		ClientSequenceDatabase csd = (ClientSequenceDatabase) dlb.getSelected();

		progressDialog = new ProgressDialog("Database Undeployment Progress", true);
		progressDialog.enableOkButton(false);
		progressDialog.show();

		serviceAsync.undeployDatabase(csd.getShortName(), new AsyncCallback<ClientDatabaseUndeployerProgress>() {

			public void onFailure(Throwable caught) {
				showError(new RuntimeException("Error occurred while submitting undeployment request. " + caught.getMessage(), caught));
			}

			public void onSuccess(ClientDatabaseUndeployerProgress result) {
				showProgressMessage(result);
			}
		});
	}

	private void showProgressMessage(ClientDatabaseUndeployerProgress progressMessageClient) {
		progressDialog.appendText("\n" + progressMessageClient.getProgressMessage());

		if (!progressMessageClient.isLast()) {
			getNextProgressMessage(progressMessageClient.getDatabaseUndeployerTaskId());
		} else {
			progressDialog.enableOkButton(true);
		}
	}

	private void showError(Throwable t) {
		progressDialog.appendText("\nError message: " + t.getMessage());
		progressDialog.enableOkButton(true);
	}

	private void getNextProgressMessage(long taskId) {
		serviceAsync.getProgressMessageForDatabaseUndeployment(taskId, new AsyncCallback<ClientDatabaseUndeployerProgress>() {

			public void onFailure(Throwable caught) {
				showError(new RuntimeException("Error occurred while listening undeployment progress messages. " + caught.getMessage(), caught));
			}

			public void onSuccess(ClientDatabaseUndeployerProgress result) {
				showProgressMessage(result);
			}
		});
	}
}
