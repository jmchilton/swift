package edu.mayo.mprc.swift.ui.client.widgets.validation;

import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.common.client.StringUtilities;
import edu.mayo.mprc.swift.ui.client.CompareClientModSpecificity;
import edu.mayo.mprc.swift.ui.client.rpc.ClientModSpecificity;
import edu.mayo.mprc.swift.ui.client.rpc.ClientModSpecificitySet;
import edu.mayo.mprc.swift.ui.client.rpc.ClientValue;

import java.util.*;

/**
 * Implementation of the GWT part of the Modification Selection Editor
 * Layout of this composite widget has 4 subpanels 1 flow and 3 horizontal
 * <p/>
 * <ui> Search Area
 * <ui> Titles
 * <ui> Modification Selection lists
 * <ui> Long Description
 */
public final class ModificationSelectionEditor extends Composite implements SourcesChangeEvents, Validatable {
	private static final String VARIABLE_MODS_TITLE = "Variable Modifications :";
	private static final String FIXED_MODS_TITLE = "Fixed Modifications :";
	public static final String VARIABLE_MOD_TYPE = "variable";
	public static final String FIXED_MOD_TYPE = "fixed";
	public static final String FIXED_PARAM_NAME = "modifications.fixed";
	public static final String VARIABLE_PARAM_NAME = "modifications.variable";
	private static final String AVAILABLE_TITLE = "Available :";
	private static final String SELECTED_TITLE = "Selected :";
	private TextBox modsSearchDefinition;
	private ModificationListBox modsAvailable;
	private ModificationListBox modsSelected;
	private Label modsDescriptionTitle;
	private HTML modsDescription;

	private String title;
	private String param;


	/**
	 * This is the container for all the contents
	 */
	private Panel container;

	private ChangeListenerCollection changeListeners;
	public static final List<ClientValue> EMPTY_CLIENT_VALUES = Collections.emptyList();
	public static final List<ClientModSpecificity> EMPTY_CLIENT_MOD_SPECIFICITY = Collections.emptyList();

	/**
	 * creates the modification selection editor object
	 *
	 * @param param - the modifications parameter
	 * @param type  - the type of modification (variable or fixed)
	 */
	public ModificationSelectionEditor(String param, String type) {
		this.param = param;
		if (type.equals(VARIABLE_MOD_TYPE)) {
			this.title = VARIABLE_MODS_TITLE;
		} else {
			this.title = FIXED_MODS_TITLE;
		}
		createModsSelectionEditor();

	}

	private void createModsSelectionEditor() {
		this.createContainer();
		this.initWidget(this.container);
	}


	/**
	 * this is used to perform a modifications search
	 */
	class SearchKeyboardListener implements KeyboardListener {
		private ModificationListBox available;
		private ModificationListBox selected;
		private HTML description;
		private boolean first;
		private ModificationSearch searcher;

		public SearchKeyboardListener(ModificationListBox available, ModificationListBox selected, HTML description) {
			this.available = available;
			this.selected = selected;
			this.description = description;
			this.first = true;
		}

		private void doSearch(Widget widget) {
			// do a search for words or whatever
			if (first || searcher == null) {
				first = false;
				List<? extends ClientValue> modSpecs = available.getAllValues();
				List<ClientModSpecificity> specs = new ArrayList<ClientModSpecificity>(modSpecs.size());
				for (ClientValue modSpec : modSpecs) {
					specs.add((ClientModSpecificity) modSpec);
				}
				searcher = ModificationSearch.createInstance(specs);
			}
			List<ClientModSpecificity> values = searcher.search(((TextBox) widget).getText());
			// copy results to the available list
			if (values == null) {
				values = EMPTY_CLIENT_MOD_SPECIFICITY;
			}
			this.available.setAllowedValues(values);
			updateModsDescriptionText(this.available, this.selected, this.description);
		}

		public void onKeyDown(Widget widget, char c, int i) {

		}

		public void onKeyPress(Widget widget, char c, int i) {

		}

		public void onKeyUp(Widget widget, char c, int i) {
			// want the enter Key
			//if (c == (char) KEY_ENTER) {
			doSearch(widget);
			//}
		}
	}

	private static final class SearchDefinitionClickListener implements ClickListener {
		private boolean first = true;

		public void onClick(Widget sender) {

			sender.setStyleName("mods-search-definition-enter");
			if (first) {
				first = false;
				((TextBox) sender).setText("");
			}
		}
	}

	private Panel createSearchArea() {
		HorizontalPanel searchArea = new HorizontalPanel();
		Label _modsTitle = new Label(title);
		_modsTitle.setStyleName("mods-label-title");
		_modsTitle.setWordWrap(false);

		modsSearchDefinition = new TextBox();
		modsSearchDefinition.setMaxLength(80);
		modsSearchDefinition.setStyleName("mods-search-definition");
		modsSearchDefinition.setText("Search By Name or Mass");
		modsSearchDefinition.addClickListener(new SearchDefinitionClickListener());
		// add ToolTip
		String ToolTip = "Mass Search use <mass>-<precision>, ex 40.0-1.1\n";
		ToolTip += "Word Search use <word> is NOT case sensitive, ex Acetyl\n";
		ToolTip += "Record Search use <record id>, ex 125";
		modsSearchDefinition.setTitle(ToolTip);
		searchArea.add(_modsTitle);
		Panel pad = new HorizontalPanel();
		pad.setWidth("5em");
		searchArea.add(pad);
		searchArea.add(modsSearchDefinition);
		return searchArea;
	}

	private Panel createCombinedTitlesModsSelectionArea() {
		// 3 vertical panels inside a Horizontal Panel
		Panel container = new HorizontalPanel();

		VerticalPanel col_1 = new VerticalPanel();
		Label _modsAvailableTitle = new Label(AVAILABLE_TITLE);
		_modsAvailableTitle.setStyleName("mods-label");

		this.modsAvailable = new ModificationListBox(param, true);
		this.modsAvailable.setWidth("20em");
		this.modsAvailable.setVisibleItemCount(20);
		col_1.add(_modsAvailableTitle);
		col_1.add(this.modsAvailable);

		// this has to be created before command panel

		this.modsSelected = new ModificationListBox(param, true);
		this.modsSelected.setWidth("20em");
		this.modsSelected.setVisibleItemCount(20);
		this.modsSelected.addChangeListener(new ModificationsChange(this.modsAvailable, this.modsSelected, this.modsDescription));
		this.modsAvailable.addChangeListener(new ModificationsChange(this.modsAvailable, this.modsSelected, this.modsDescription));

		VerticalPanel col_2 = new VerticalPanel();

		HorizontalPanel spacer = new HorizontalPanel();
		spacer.setWidth("25px");

		col_2.add(spacer);
		Panel commandPanel = createCommandPanel();
		col_2.add(commandPanel);


		VerticalPanel col_3 = new VerticalPanel();
		Label _modsSelectedTitle = new Label(SELECTED_TITLE);
		_modsSelectedTitle.setStyleName("mods-label");
		_modsSelectedTitle.setWordWrap(false);
		col_3.add(_modsSelectedTitle);


		col_3.add(this.modsSelected);

		container.add(col_1);
		container.add(col_2);
		container.add(col_3);

		return container;
	}

	/**
	 * return the selected values in the 'selected' listbox
	 *
	 * @return the allowed values as a @ClientModSpecificitySet
	 */
	public ClientValue getClientValue() {
		List<? extends ClientValue> selected = this.modsSelected.getAllValues();
		int length = 0;
		if (selected != null) {
			length = selected.size();
		}
		List<ClientModSpecificity> specs = new ArrayList<ClientModSpecificity>(length);

		if (selected != null) {
			for (ClientValue value : selected) {
				specs.add(ClientModSpecificity.cast(value));
			}
		}

		return new ClientModSpecificitySet(specs);
	}

	/**
	 * used to support the Add button functionality
	 * copies selected values from the list of available mods to the list of selected mods
	 */
	private static final class AddOnClick implements ClickListener {
		private ModificationListBox available;
		private ModificationListBox selected;

		public AddOnClick(ModificationListBox available, ModificationListBox selected) {
			this.available = available;
			this.selected = selected;
		}

		public void onClick(Widget sender) {
			// copy contents of the availabe list to the selected list
			ClientValue items = available.getClientValue();
			// each item is a ClientModSpecificity
			selected.addValue(items, new CompareClientModSpecificity());
		}
	}

	/**
	 * used to remove items from the selected list
	 */
	class RemoveOnClick implements ClickListener {

		private ModificationListBox available;
		private ModificationListBox selected;
		private HTML description;

		public RemoveOnClick(ModificationListBox available, ModificationListBox selected, HTML description) {
			this.available = available;
			this.selected = selected;
			this.description = description;
		}

		public void onClick(Widget sender) {
			// remove the selected items in the selected list from the selected list
			ClientValue items = selected.getClientValue();
			if (items != null) {
				selected.removeValue(items, new CompareClientModSpecificity());

				updateModsDescriptionText(this.available, this.selected, this.description);

			}
		}
	}

	/**
	 * mirrors items described in the descriptions area
	 */
	private HashSet<ClientValue> allSelected = new HashSet<ClientValue>();

	/*
			Some umimod info, from unimod.xml ...

			<umod:mod title="Acetyl" full_name="Acetylation" username_of_poster="unimod"
					  group_of_poster="admin"
					  date_time_posted="2002-08-19 19:17:11"
					  date_time_modified="2006-10-15 19:52:06"
					  approved="1"
					  record_id="1">
				<umod:specificity hidden="1" site="C" position="Anywhere" classification="Post-translational"
								  spec_group="3"/>
				<umod:specificity hidden="0" site="N-term" position="Protein N-term"
								  classification="Post-translational"
								  spec_group="5"/>
				<umod:specificity hidden="1" site="S" position="Anywhere" classification="Post-translational"
								  spec_group="4"/>
				<umod:specificity hidden="0" site="N-term" position="Any N-term" classification="Multiple"
								  spec_group="2">
					<umod:misc_notes>GIST acetyl light</umod:misc_notes>
				</umod:specificity>
				<umod:specificity hidden="0" site="K" position="Anywhere" classification="Multiple"
								  spec_group="1">
					<umod:misc_notes>PT and GIST acetyl light</umod:misc_notes>
				</umod:specificity>
				<umod:delta mono_mass="42.010565" avge_mass="42.0367" composition="H(2) C(2) O">
					<umod:element symbol="H" number="2"/>
					<umod:element symbol="C" number="2"/>
					<umod:element symbol="O" number="1"/>
				</umod:delta>
		*/

	/**
	 * when new mods selected in the mods area, need to update the description text area if there
	 * is a real change
	 */
	class ModificationsChange implements ChangeListener {
		private ModificationListBox available;
		private ModificationListBox selected;
		private HTML description;

		public ModificationsChange(ModificationListBox available, ModificationListBox selected, HTML description) {
			this.available = available;
			this.selected = selected;
			this.description = description;
		}

		public void onChange(Widget widget) {
			updateModsDescriptionText(this.available, this.selected, this.description);
		}
	}

	public void updateModsDescriptionText(ModificationListBox available, ModificationListBox selected, HTML description) {

		// find all the selected items and see if they are different than allSelected
		// if so then change the content of the text area
		List<? extends ClientValue> availableValues = EMPTY_CLIENT_VALUES;
		if (available.getClientValue() != null) {
			availableValues = available.unbundle(available.getClientValue());
		}
		List<? extends ClientValue> selectedValues = EMPTY_CLIENT_VALUES;
		if (selected.getClientValue() != null) {
			selectedValues = selected.unbundle(selected.getClientValue());
		}
		// add them to a hashset to enure uniqueness then move them to the array
		HashSet<ClientValue> h = new HashSet<ClientValue>();
		if (availableValues != null) {
			for (ClientValue availableValue : availableValues) {
				if (!h.contains(availableValue)) {
					h.add(availableValue);
				}
			}
		}
		if (selectedValues != null) {
			for (ClientValue selectedValue : selectedValues) {
				if (!h.contains(selectedValue)) {
					h.add(selectedValue);
				}
			}
		}
		List<ClientValue> clientValues = Arrays.asList(h.toArray(new ClientValue[h.size()]));
		Collections.sort(clientValues, new CompareClientModSpecificity());
		ClientValue[] c = clientValues.toArray(new ClientValue[h.size()]);

		boolean notfound = false;
		for (ClientValue item : c) {
			notfound = !allSelected.contains(item);
			if (notfound) {
				// clear allSelected
				allSelected.clear();
				allSelected.addAll(Arrays.asList(c));
				break;
			}
		}

		if (!notfound) {
			// see if something was removed
			if (allSelected.size() > c.length) {
				notfound = true;
			}
		}

		// if not found then rebuild the content
		if (notfound) {
			resetContent(c);
		}

	}

	/**
	 * Fill in the content of the HTML panel for the description
	 * If there is one item fill in the details table, otherwise fill in the list
	 *
	 * @param values - list of items making up the content
	 */
	private void resetContent(ClientValue[] values) {
		if (values != null && values.length == 1) {
			ClientModSpecificity spec = (ClientModSpecificity) values[0];
			// change the title to the name
			this.modsDescriptionTitle.setText(spec.getName());
			this.resetContentDescriptionPanel(spec);
		} else {
			int length = 0;
			if (values != null) {
				length = values.length;
			}
			// get the names
			String[] names = new String[length];
			for (int i = 0; i < length; i++) {
				names[i] = ((ClientModSpecificity) values[i]).getName();
			}
			this.modsDescriptionTitle.setText("");

			resetDescriptionList(names);
		}
	}


	private Panel createCommandPanel() {

		VerticalPanel newPanel = new VerticalPanel();

		Hyperlink _cmdAdd = new Hyperlink("Add-->", "Add");
		_cmdAdd.setTitle("To create a new empty curation for you editing.");
		_cmdAdd.setStyleName("command-link");
		_cmdAdd.setWidth("7em");
		_cmdAdd.addClickListener(new AddOnClick(this.modsAvailable, this.modsSelected));


		_cmdAdd.addStyleName("spaceAfter");
		Panel spacer = new HorizontalPanel();
		spacer.setHeight("1em");
		newPanel.add(spacer);
		newPanel.add(_cmdAdd);

		Hyperlink _cmdRemove = new Hyperlink("<--Remove", "Remove");
		_cmdRemove.setTitle("To make a copy of the currently displayed curation for you own editing.");
		_cmdRemove.setStyleName("command-link");
		_cmdRemove.addClickListener(new RemoveOnClick(this.modsAvailable, this.modsSelected, this.modsDescription));
		_cmdAdd.addStyleName("spaceAfter");
		newPanel.add(_cmdRemove);

		return newPanel;
	}


	private Panel createDescriptionArea() {
		// has a text area and a title
		VerticalPanel newPanel = new VerticalPanel();
		this.modsDescriptionTitle = new Label("Description");
		this.modsDescriptionTitle.setStyleName("mods-label-description");
		this.modsDescription = this.createDescriptionPanel();
		this.modsDescription.setHeight("10em");
		this.modsDescription.setWidth("100%");
		newPanel.add(this.modsDescriptionTitle);
		newPanel.add(this.modsDescription);
		return newPanel;
	}

	private static final String UNIMOD_PATH = "http://www.unimod.org";
	private static final String UNIMOD_RECORD_PATH = UNIMOD_PATH + "/modifications_view.php?editid1=";

	private String getUnimodLinkContent(int recordId) {
		if (recordId < 0) {
			return "";
		} else {
			return "<a target=\"_unimod1\" href=\"" + UNIMOD_RECORD_PATH + recordId + "\">" + "unimod" + "</a>";
		}
	}

	private void createContainer() {
		this.container = new VerticalPanel();
		this.container.add(this.createSearchArea());
		Panel description = createDescriptionArea();
		this.container.add(createCombinedTitlesModsSelectionArea());
		this.container.add(description);

		modsSearchDefinition.addKeyboardListener(new SearchKeyboardListener(this.modsAvailable, this.modsSelected, this.modsDescription));
	}

	public void addChangeListener(ChangeListener listener) {
		if (this.changeListeners == null) {
			changeListeners = new ChangeListenerCollection();
		}
		changeListeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		if (this.changeListeners == null) {
			return;
		}
		for (int i = 0; i < changeListeners.size(); i++) {
			if (changeListeners.get(i).equals(listener)) {
				changeListeners.remove(i);
			}
		}
	}

	/**
	 * clear the selected mods
	 */
	public void setValueClear() {
		this.modsSelected.clearValues();
	}

	/**
	 * puts value in the 'selected' mods list box
	 *
	 * @param value - the value(s) that want to be selected
	 */
	public void setValue(ClientValue value) {
		this.modsSelected.addValueWithoutSelecting(value, new CompareClientModSpecificity());
		// need to update the description area also
		this.updateModsDescriptionText(this.modsAvailable, this.modsSelected, this.modsDescription);
	}

	public void focus() {
		this.modsAvailable.focus();
	}

	public void setValidationSeverity(int validationSeverity) {
		this.modsAvailable.setValidationSeverity(validationSeverity);
	}

	public String getAllowedValuesParam() {
		return "all";
	}

	public void setAllowedValues(List<? extends ClientValue> values) {
		this.modsAvailable.setAllowedValues(values);
	}

	public void setEnabled(boolean enabled) {
		// enable each of the controls
		this.modsAvailable.setEnabled(enabled);
		this.modsSelected.setEnabled(enabled);
		this.modsSearchDefinition.setEnabled(true);

	}

	/**
	 * these are added to provide control of the html description area for css etc. They are not used at this point.
	 */
	private static final String DESC_ALT_NAMES = "desc_alt_names";
	private static final String DESC_ALT_NAMES_VALUE = "desc_alt_names_value";

	private static final String DESC_COMPOSITION = "desc_composition";
	private static final String DESC_COMPOSITION_VALUE = "desc_composition_value";

	private static final String DESC_SPECIFICITY = "desc_specificity";
	private static final String DESC_SPECIFICITY_VALUE = "desc_specificity_value";

	private static final String DESC_COMMENTS = "desc_comments";
	private static final String DESC_COMMENTS_VALUE = "desc_comments_value";

	private static final String ALT_NAMES_TITLE = "Alt Names:";
	private static final String COMPOSITION_TITLE = "Composition:";
	private static final String SPECIFICITY_TITLE = "Specificity:";
	private static final String COMMENTS_TITLE = "Comments:";

	private static final String FULL_SPEC_DESCRIPTION = "full_spec_description";
	private static final String LIST_SPEC_DESCRIPTION = "list_spec_description";

	/**
	 * create the place holders for the description table
	 */
	private HTML createDescriptionPanel() {


		String html =
				"<div id='" + FULL_SPEC_DESCRIPTION + "'>" +
						"<table> " +
						"<tr><td><div id='" + DESC_ALT_NAMES + "'></div></td><td><div id='" + DESC_ALT_NAMES_VALUE + "'></div></td>" +
						"<tr><td><div id='" + DESC_COMPOSITION + "'></div></td><td><div id='" + DESC_COMPOSITION_VALUE + "'></div></td>" +
						"<tr><td><div id='" + DESC_SPECIFICITY + "'></div></td><td><div id='" + DESC_SPECIFICITY_VALUE + "'></div></td>" +
						"<tr><td><div id='" + DESC_COMMENTS + "'></div></td><td><div id='" + DESC_COMMENTS_VALUE + "'></div></td>" +
						"</table>" +
						"</div>" +
						"<div id='" + LIST_SPEC_DESCRIPTION + "'>" +
						"</div>";

		return new HTML(html);

	}

	private void resetContentDescriptionPanel(ClientModSpecificity spec) {
		String altNames = StringUtilities.join(spec.getAltNames(), ",");
		String comments = spec.getComments();
		if (comments == null) {
			comments = "";
		}
		// some comments start with 'null;', strip it  TODO find the root cause
		comments = comments.replaceAll("null;", "");
		String html = this.getDescriptionPanelContent(altNames, spec.getComposition() + "; Monoisotopic: " + spec.getMonoisotopic(), spec.getSite() + " " + "(" + spec.getTerm() + ")" + "; " + spec.getClassification(), comments, spec.getRecordID());
		this.modsDescription.setHTML(html);
	}

	private String getDescriptionPanelContent(String altNames, String composition, String specificity, String comments, int recordId) {
		String html =
				"<div id='" + FULL_SPEC_DESCRIPTION + "'>" +
						"<table> " +
						"<tr><td><div id='" + DESC_ALT_NAMES + "'>" + ALT_NAMES_TITLE + "</div></td><td><div id='" + DESC_ALT_NAMES_VALUE + "'>" + altNames + "</div></td>" +
						"<tr><td><div id='" + DESC_COMPOSITION + "'>" + COMPOSITION_TITLE + "</div></td><td><div id='" + DESC_COMPOSITION_VALUE + "'>" + composition + "</div></td>" +
						"<tr><td><div id='" + DESC_SPECIFICITY + "'>" + SPECIFICITY_TITLE + "</div></td><td><div id='" + DESC_SPECIFICITY_VALUE + "'>" + specificity + "</div></td>" +
						"<tr><td><div id='" + DESC_COMMENTS + "'>" + COMMENTS_TITLE + "</div></td><td><div id='" + DESC_COMMENTS_VALUE + "'>" + comments + "</div></td>" +
						"</table>" +
						"</div>";
		return html + this.getUnimodLinkContent(recordId);
	}

	private static String getDescriptionPanelContent(String[] names) {
		StringBuilder html = new StringBuilder("<div id='" + LIST_SPEC_DESCRIPTION + "'>");
		if (names != null) {
			for (String name : names) {
				html.append("<br>").append(name);
			}
		}
		html.append("</div>");
		return html.toString();
	}

	private void resetDescriptionList(String[] names) {
		this.modsDescription.setHTML(getDescriptionPanelContent(names));
	}
}
