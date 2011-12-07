package edu.mayo.mprc.dbcurator.client.steppanels;

import com.allen_sauer.gwt.dnd.client.*;
import com.allen_sauer.gwt.dnd.client.drop.IndexedDropController;
import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.dbcurator.client.curatorstubs.CurationStepStub;

import java.util.ArrayList;
import java.util.List;

/**
 * A mainPanel that will hold the StepPanels
 */
public final class StepPanelContainer extends Composite {


	/**
	 * the underlying stepOrganizer where the widgets will go
	 */
	private VerticalPanel stepOrganizer = new VerticalPanel();

	/**
	 * the controller that we need to register widgets on to make them draggable when they are added to the container
	 */
	private PickupDragController dragController = null;

	/**
	 * the list of steps that are represented in this stepOrganizer
	 */
	private List<CurationStepStub> containedSteps = new ArrayList<CurationStepStub>();

	private IndexedDropController dropController = null;

	private AbsolutePanel boundaryPanel = new AbsolutePanel();

	/**
	 * takes a list of steps that will be updated througout the life of this container to reflect the order of the widgets
	 * in this mainPanel.
	 *
	 * @param curationStepStubs the list of steps to represent in this mainPanel
	 */
	public StepPanelContainer(List<CurationStepStub> curationStepStubs) {

		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setAlwaysShowScrollBars(true);
		scrollPanel.add(this.stepOrganizer);

		scrollPanel.addStyleName("stepeditor-scrollpanel");
		boundaryPanel.add(scrollPanel);
		boundaryPanel.addStyleName("stepeditor-dndboundarypanel");

		initWidget(boundaryPanel);

		//all dragging to occur through whole page but dropping can only happen in the container and
		dragController = new PickupDragController( /*dropBoundary*/ boundaryPanel, /*allowDroppingOnBoundaryPanel*/ false);

		dragController.addDragHandler(new DragHandler() {

			public void onDragEnd(DragEndEvent event) {
				StepPanelContainer.this.updateStepOrderFromUI();
			}

			public void onDragStart(DragStartEvent event) {
			}

			public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
			}

			public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {
			}
		});

		dropController = new IndexedDropController(stepOrganizer);
		dragController.registerDropController(dropController);

		//insert the respective widgets for each one of the respective steps

		for (CurationStepStub curationStepStub : curationStepStubs) {
			this.add(curationStepStub);
		}

	}

	public void setModificationEnabled(boolean enabled) {

		for (CurationStepStub containedStep : this.containedSteps) {
			containedStep.setEditable(enabled);
		}

		if (enabled) {
			dragController.registerDropController(dropController);
		} else {
			dragController.unregisterDropController(dropController);
		}

	}

	private void updateStepOrderFromUI() {
		List<CurationStepStub> newOrder = new ArrayList<CurationStepStub>();
		int widgetCount = this.stepOrganizer.getWidgetCount();
		for (int i = 0; i < widgetCount; i++) {
			StepPanelShell shell = (StepPanelShell) stepOrganizer.getWidget(i);
			newOrder.add(shell.getContainedStepPanel().getContainedStep());
		}
		this.containedSteps = newOrder;
		this.refresh();
	}

	/**
	 * remove a widget from this container
	 *
	 * @param toRemove the widget you want to remove
	 * @return true if a widget was removed else false
	 */
	public boolean remove(Widget toRemove) {
		return this.remove(stepOrganizer.getWidgetIndex(toRemove));
	}

	/**
	 * remove a widget at a specified index
	 *
	 * @param indexToRemove the index of the widget you want to remove
	 * @return true if a widget wsa removed
	 */
	public boolean remove(int indexToRemove) {
		boolean wasRemoved = stepOrganizer.remove(indexToRemove);
		if (wasRemoved) {
			this.containedSteps.remove(indexToRemove);
		}
		return wasRemoved;
	}

	/**
	 * takes a curation step and adds it to the end of the curation
	 *
	 * @param stepToInsert the step that you want to insert
	 */
	public void add(CurationStepStub stepToInsert) {
		if (this.containedSteps.size() == 0) {
			this.stepOrganizer.clear();
		}
		this.containedSteps.add(stepToInsert);
		StepPanelShell shell = stepToInsert.getStepPanel().getShell(this);

		//if the step has not been run allow drag and drop
		if (shell.getContainedStepPanel().getContainedStep().getCompletionCount() == null) {
			dragController.makeDraggable(shell, shell.getStepTitle());
		}

		stepOrganizer.add(shell);

		shell.update();
	}

	/**
	 * get the index of a certain widget
	 *
	 * @param w the widget to get an index of
	 * @return the index of widget w
	 */
	public int getWidgetIndex(Widget w) {
		return stepOrganizer.getWidgetIndex(w);
	}

	/**
	 * use this to insert a widget into a certain position
	 *
	 * @param toInsert the widget to insert
	 * @param index    where you want to insert the widget
	 */
	public void insert(Widget toInsert, int index) {
		if (this.containedSteps.size() == 0) {
			this.stepOrganizer.clear();
		}

		StepPanelShell shell = (StepPanelShell) toInsert;

		//if the step has not been run then make it draggable
		if (shell.getContainedStepPanel().getContainedStep().getCompletionCount() == null) {
			dragController.makeDraggable(shell, shell.getStepTitle());
		}

		this.containedSteps.add(index, ((StepPanelShell) toInsert).getContainedStepPanel().getContainedStep());

		this.stepOrganizer.insert(toInsert, index);
	}

	/**
	 * gets the number of steps in this container
	 *
	 * @return the number of steps in the container
	 */
	public int getStepCount() {
		return this.containedSteps.size();
	}

	/**
	 * Updates each of the steps in the container and returns the list of step
	 *
	 * @return the list of curation steps in the container
	 */
	public List<CurationStepStub> getContainedSteps() {

		List<CurationStepStub> retList = new ArrayList();

		for (int i = 0; i < this.stepOrganizer.getWidgetCount(); i++) {
			retList.add((((StepPanelShell) stepOrganizer.getWidget(i)).getContainedStepPanel()).getContainedStep());
		}

		return retList;
	}

	/**
	 * tell this container to refresh itself
	 *
	 * @param steps the steps that should be refreshed.  It is assumed the step haven't been changed so that the refresh will not have any problems
	 */
	public void refresh(List<CurationStepStub> steps) {
		//have each step update itself to reflect the current state of the widget
		for (int i = 0; i < this.stepOrganizer.getWidgetCount(); i++) {
			((StepPanelShell) this.stepOrganizer.getWidget(i)).update();
		}

		//if the number of steps has changed for some reason then remove them all and re-add them
		if (this.containedSteps.size() != steps.size()) {
			for (int i = 0; i < this.containedSteps.size(); i++) {
				this.remove(i);
			}
			this.containedSteps.clear();

			for (CurationStepStub step : steps) {
				this.add(step);
			}
		} else {
			//update to refect new state
			for (int i = 0; i < steps.size(); i++) {
				StepPanelShell shell = (StepPanelShell) this.stepOrganizer.getWidget(i);
				CurationStepStub stub = steps.get(i);
				shell.update(stub);
			}
		}
	}

	public void refresh() {
		this.refresh(this.containedSteps);
	}
}