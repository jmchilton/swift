package edu.mayo.mprc.dbcurator.client.steppanels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.common.client.ExceptionUtilities;
import edu.mayo.mprc.dbcurator.client.curatorstubs.CurationStepStub;
import edu.mayo.mprc.dbcurator.client.curatorstubs.NewDatabaseInclusionStub;
import edu.mayo.mprc.dbcurator.client.services.CommonDataRequester;
import edu.mayo.mprc.dbcurator.client.services.CommonDataRequesterAsync;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Eric Winter
 */
public final class NewDatabaseInclusionPanel extends AbstractStepPanel {

	private NewDatabaseInclusionStub containedStep = new NewDatabaseInclusionStub();

	private TextBox url = new TextBox();
	private ListBox lstCommonSites = new ListBox();
	private Map<String, String> commonSites = new HashMap<String, String>();
	public static final String TITLE = "Download Sequence Database";

	public NewDatabaseInclusionPanel() {
		VerticalPanel panel = new VerticalPanel();

		lstCommonSites.addItem("Manual Entry");
		this.getCommonSites(); //generate the list of common sites
		lstCommonSites.addChangeListener(new ChangeListener() {
			public void onChange(Widget widget) {
				ListBox source = (ListBox) widget;
				String selection = source.getItemText(source.getSelectedIndex());
				if (!selection.equalsIgnoreCase("Manual Entry")) {
					NewDatabaseInclusionPanel.this.url.setText(NewDatabaseInclusionPanel.this.commonSites.get(selection));
				} else {
					NewDatabaseInclusionPanel.this.url.setText("ftp://");
				}
			}
		});

		panel.add(new Label("Choose a database to include..."));
		panel.add(lstCommonSites);

		panel.setWidth("100%");
		url.setWidth("100%");
		panel.add(url);

		panel.setSpacing(5);
		this.setTitle(TITLE);

		initWidget(panel);
	}

	public CurationStepStub getContainedStep() {
		this.containedStep.url = this.url.getText();
		return this.containedStep;
	}

	public void setContainedStep(CurationStepStub step) throws ClassCastException {
		if (!(step instanceof NewDatabaseInclusionStub)) {
			ExceptionUtilities.throwCastException(step, NewDatabaseInclusionStub.class);
			return;
		}
		this.containedStep = (NewDatabaseInclusionStub) step;
		update();
	}

	public String getStyle() {
		return "shell-header-newdb";
	}

	/**
	 * This site currently is static but it will eventually use a call to the server to get a list of sites that are both hard coded
	 * and may also go to the database to find the top 5 ftp paths that were manually entered.
	 *
	 * @return a list of sites that are commonly available
	 */
	private void getCommonSites() {

		if (commonSites.size() < 2) {

			//then make an rpc call to the server requesting the list of common transforms
			final CommonDataRequesterAsync dataRequester = (CommonDataRequesterAsync) GWT.create(CommonDataRequester.class);
			ServiceDefTarget endpoint = (ServiceDefTarget) dataRequester;
			endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "CommonDataRequester");
			dataRequester.getFTPDataSources(new AsyncCallback<Map<String, String>>() {

				public void onFailure(Throwable throwable) {
					//do nothing we just can't add common formatters
				}

				public void onSuccess(Map<String, String> trans) {
					commonSites.putAll(trans);

					for (String s : commonSites.keySet()) {
						lstCommonSites.addItem(s);
					}
				}
			});
		}

		for (String s : commonSites.keySet()) {
			lstCommonSites.addItem(s);
		}

	}

	public void update() {
		url.setText(containedStep.url);
	}

	public String getImageURL() {
		return "images/step-icon-add.png";
	}
}
