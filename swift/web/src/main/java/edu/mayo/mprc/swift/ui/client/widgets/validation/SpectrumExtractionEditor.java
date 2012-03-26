package edu.mayo.mprc.swift.ui.client.widgets.validation;

import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.common.client.ExceptionUtilities;
import edu.mayo.mprc.swift.ui.client.rpc.ClientExtractMsnSettings;
import edu.mayo.mprc.swift.ui.client.rpc.ClientValue;
import edu.mayo.mprc.swift.ui.client.widgets.HelpPopupButton;

import java.util.List;

public final class SpectrumExtractionEditor extends Composite implements Validatable, ChangeListener {
	private ClientExtractMsnSettings extractMsnSettings;
	private ChangeListenerCollection changeListenerCollection = new ChangeListenerCollection();
	private HorizontalPanel panel;
	private Label engineName;
	private TextBox settings;
	private HelpPopupButton help;

	public SpectrumExtractionEditor() {
		panel = new HorizontalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		engineName = new Label("extract_msn");
		settings = new TextBox();
		settings.setVisibleLength(50);
		settings.addChangeListener(this);
		help = new HelpPopupButton("<pre> extract_msn v.3, Copyright 1997-2005\n" +
				"\n" +
				" extract_msn usage:  extract_msn [options] [datafile]\n" +
				" options = -Fnum     where num is an INT specifying the first scan\n" +
				"           -Lnum     where num is an INT specifying the last scan\n" +
				"           -Bnum     where num is a FLOAT specifying the bottom MW for datafile creation\n" +
				"           -Tnum     where num is a FLOAT specifying the top MW for datafile creation\n" +
				"           -Mnum     where num is a FLOAT specifying the precursor mass\n" +
				"                       tolerance for grouping (default=1.4)\n" +
				"           -Snum     where num is an INT specifying the number of allowed\n" +
				"                       different intermediate scans for grouping. (default=1)\n" +
				"           -Cnum     where num is an INT specifying the charge state to use\n" +
				"           -Gnum     where num is an INT specifying the minimum # of related\n" +
				"                       grouped scans needed for a .dta file (default=2)\n" +
				"           -Inum     where num is an INT specifying the minimum # of ions\n" +
				"                       needed for a .dta file (default=0)\n" +
				"           -Dstring  where string is a path name\n" +
				"           -U        Use a unified search file\n" +
				"           -Ystring  where string is a subsequence\n" +
				"           -Z        Controls whether the zta files are written\n" +
				"           -K        Controls whether the charge calculations are performed\n" +
				"           -Ostring  where string is the path of a template file\n" +
				"                       [Default name is chgstate.tpl]\n" +
				"           -Astring  where the string can contain any of the options\n" +
				"                       T: use template          F: use discrete Fourier transform\n" +
				"                       E: use Eng's algorithm   H: use scan header\n" +
				"                       M: use MSMS count\n" +
				"                       O: override header charge state\n" +
				"                       S: create summary file   L: create log file\n" +
				"                       D: create both files     C: create MSMS count file\n" +
				"                       A: find CS even for nonzero headers\n" +
				"                       tfehm: include algorithm output in summary file even if not called\n" +
				"                       [NOTE: This version of the program has a default string of -AHTFEMAOSC,\n" +
				"                       but if -A option is used all desired parameters must be specified]\n" +
				"\n" +
				"           -Rnum     where num is a FLOAT specifying the minimum signal-to-noise value\n" +
				"                       needed for a peak to be written to a .dta file (default=3)\n" +
				"           -rnum     where num is an INT specifying the minimum number of major peaks\n" +
				"                       (peaks above S/N threshold) needed for a .dta file (default=5)\n" +
				"</pre>");
		panel.add(engineName);
		panel.add(settings);
		panel.add(help);
		this.initWidget(panel);
	}

	public ClientValue getClientValue() {
		return extractMsnSettings;
	}

	public void setValue(final ClientValue value) {
		if (!(value instanceof ClientExtractMsnSettings)) {
			ExceptionUtilities.throwCastException(value, ClientExtractMsnSettings.class);
			return;
		}
		extractMsnSettings = (ClientExtractMsnSettings) value;
		settings.setText(extractMsnSettings.getCommandLineSwitches());
	}

	public void focus() {
		settings.setFocus(true);
	}

	public void setValidationSeverity(final int validationSeverity) {
		ValidationController.setValidationSeverity(validationSeverity, this);
	}

	public String getAllowedValuesParam() {
		return null;
	}

	public void setAllowedValues(final List<? extends ClientValue> values) {
		// Not supported
	}

	public void setEnabled(final boolean enabled) {
		settings.setEnabled(enabled);
	}

	public void addChangeListener(final ChangeListener changeListener) {
		changeListenerCollection.add(changeListener);
	}

	public void removeChangeListener(final ChangeListener changeListener) {
		changeListenerCollection.remove(changeListener);
	}

	public void onChange(final Widget widget) {
		extractMsnSettings.setCommandLineSwitches(settings.getText().trim());
		changeListenerCollection.fireChange(this);
	}
}
