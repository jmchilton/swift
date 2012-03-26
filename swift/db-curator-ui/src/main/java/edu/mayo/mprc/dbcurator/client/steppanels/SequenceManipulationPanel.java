package edu.mayo.mprc.dbcurator.client.steppanels;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import edu.mayo.mprc.common.client.ExceptionUtilities;
import edu.mayo.mprc.dbcurator.client.curatorstubs.CurationStepStub;
import edu.mayo.mprc.dbcurator.client.curatorstubs.SequenceManipulationStepStub;

import java.util.Date;

/**
 * @author Eric Winter
 */
public final class SequenceManipulationPanel extends AbstractStepPanel {

	private SequenceManipulationStepStub containedStep;

	private RadioButton radioReversal;
	private RadioButton radioScramble;

	private CheckBox chkRetainOriginal = new CheckBox("Retain original sequence?");
	public static final String TITLE = "Make Decoy Database";

	public SequenceManipulationPanel() {
		final VerticalPanel panel = new VerticalPanel();

		final String radioGroup = String.valueOf(new Date().getTime());

		this.radioReversal = new RadioButton(radioGroup, "Sequence Reversal");
		this.radioScramble = new RadioButton(radioGroup, "Sequence Scramble");
		final HorizontalPanel modePanel = new HorizontalPanel();
		//radioReversal.setChecked(true);

		modePanel.add(radioReversal);
		modePanel.add(radioScramble);
		modePanel.setSpacing(5);

		panel.add(modePanel);

		panel.add(chkRetainOriginal);


		panel.setSpacing(5);

		this.setTitle(TITLE);

		initWidget(panel);


	}


	/**
	 * returns the containedStep that this mainPanel will represent.  This is used to get generic containedStep information such as the
	 * completion count, list of messages, etc.
	 *
	 * @return the containedStep that this mainPanel represents
	 */
	public CurationStepStub getContainedStep() {
		this.containedStep.manipulationType = (this.radioReversal.isChecked() ? SequenceManipulationStepStub.REVERSAL : SequenceManipulationStepStub.SCRAMBLE);
		this.containedStep.overwrite = !chkRetainOriginal.isChecked();
		return this.containedStep;
	}

	/**
	 * Set the containedStep associated with this StepPanel.
	 *
	 * @param step the containedStep you want this mainPanel to represent
	 * @throws ClassCastException if the containedStep passed in wasn't the type that the Panel can represent
	 */
	public void setContainedStep(final CurationStepStub step) throws ClassCastException {
		if (!(step instanceof SequenceManipulationStepStub)) {
			ExceptionUtilities.throwCastException(step, SequenceManipulationStepStub.class);
			return;
		}
		this.containedStep = (SequenceManipulationStepStub) step;
		update();
	}

	/**
	 * return the CSS style name that should be used in conjunction with this stepOrganizer
	 *
	 * @return "shell-header-manipulation"
	 */
	public String getStyle() {
		return "shell-header-manipulation";
	}


	/**
	 * call this method when this mainPanel should look for updates in its contained step
	 */
	public void update() {

		if (containedStep.manipulationType.equalsIgnoreCase(SequenceManipulationStepStub.SCRAMBLE)) {
			radioScramble.setChecked(true);
		} else {
			radioReversal.setChecked(true);
		}
		chkRetainOriginal.setChecked(!containedStep.overwrite);
	}

	public String getImageURL() {
		return "images/step-icon-manip.png";
	}
}
