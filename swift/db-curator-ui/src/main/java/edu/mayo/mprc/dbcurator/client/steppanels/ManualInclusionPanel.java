package edu.mayo.mprc.dbcurator.client.steppanels;

import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.common.client.ExceptionUtilities;
import edu.mayo.mprc.dbcurator.client.curatorstubs.CurationStepStub;
import edu.mayo.mprc.dbcurator.client.curatorstubs.ManualInclusionStepStub;

/**
 * @author Eric Winter
 */
public final class ManualInclusionPanel extends AbstractStepPanel {

	private ManualInclusionStepStub containedStep;

	private TextBox txtHeader = new TextBox();
	private TextArea txtSequence = new TextArea();

	private static final String WELCOME_SEQUENCE = "Enter a sequence or a header and sequence.";
	private static final String WELCOME_HEADER = "Enter a FASTA header here, no need for right arrow";
	public static final String TITLE = "Paste Individual Sequence";

	public ManualInclusionPanel() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.add(new Label("FASTA Header:"));
		Panel tempPanel = new HorizontalPanel();
		tempPanel.setWidth("100%");
		Label arrow = new Label(">");
		tempPanel.add(arrow);
		txtHeader.setWidth("100%");
		txtHeader.setText(WELCOME_HEADER);
		txtHeader.addFocusListener(new FocusListener() {
			public void onFocus(Widget widget) {
				txtHeader.selectAll();
			}

			public void onLostFocus(Widget widget) {
				txtHeader.setText(stripHeaderChar(txtHeader.getText()));
			}
		});
		tempPanel.add(txtHeader);
		panel.add(tempPanel);
		panel.add(new Label("Sequence: "));
		txtSequence.setWidth("100%");
		txtSequence.setText(WELCOME_SEQUENCE);
		txtSequence.addFocusListener(new FocusListener() {
			public void onFocus(Widget widget) {
				txtSequence.selectAll();
			}

			public void onLostFocus(Widget widget) {
				if (!txtSequence.getText().equals(WELCOME_SEQUENCE)) {
					txtSequence.setText(cleanSequence(txtSequence.getText()));
				}
			}
		});
		txtSequence.setVisibleLines(5);
		panel.add(txtSequence);
		panel.setSpacing(5);
		this.setTitle(TITLE);
		initWidget(panel);
	}

	/**
	 * returns the step that this mainPanel will represent.  This is used to get generic step information such as the
	 * completion count, list of messages, etc.
	 *
	 * @return the step that this mainPanel represents
	 */
	public CurationStepStub getContainedStep() {
		this.containedStep.header = ">" + this.txtHeader.getText();
		this.containedStep.sequence = cleanSequence(this.txtSequence.getText());
		return this.containedStep;
	}

	/**
	 * Set the step associated with this StepPanel.
	 *
	 * @param step the step you want this mainPanel to represent
	 * @throws ClassCastException if the step passed in wasn't the type that the Panel can represent
	 */
	public void setContainedStep(CurationStepStub step) throws ClassCastException {
		if (!(step instanceof ManualInclusionStepStub)) {
			ExceptionUtilities.throwCastException(step, ManualInclusionStepStub.class);
			return;
		}
		this.containedStep = (ManualInclusionStepStub) step;
		update();
	}

	public String getStyle() {
		return "shell-header-manualinclusion";
	}

	public void update() {
		txtHeader.setText(stripHeaderChar(containedStep.header));
		txtSequence.setText(containedStep.sequence);
	}

	/**
	 * takes the header character (right arrow) out of the header if it exists.
	 *
	 * @param toStrip the string to try to strip the right arrow out of
	 * @return the string minus a leading header character if it had existed
	 */
	public String stripHeaderChar(String toStrip) {
		if (toStrip == null || toStrip.length() == 0) {
			return "";
		}
		return toStrip.replaceAll("^[>]*[\\s]*", "");

	}

	/**
	 * strips header (if included) and whitespace out of the sequence
	 *
	 * @param toClean the string you want to have changed
	 * @return the sequence with header and white space removed
	 */
	public String cleanSequence(String toClean) {
		if (toClean == null || toClean.length() == 0) {
			return toClean;
		}

		//if this includes a header then strip out between the header filter and th
		if (toClean.charAt(0) == '>') {
			int startChar = 1;
			int endChar = toClean.indexOf('\n', 1);
			if (endChar < 0) {
				endChar = startChar;
			}
			this.txtHeader.setText(stripHeaderChar(toClean.substring(startChar, endChar)));

			toClean = toClean.substring(endChar + 1);
		}

		//now take out any whitespace
		toClean = toClean.replaceAll("\\s", "");


		return toClean;
	}

	public String getImageURL() {
		return "images/step-icon-add.png";
	}

}
