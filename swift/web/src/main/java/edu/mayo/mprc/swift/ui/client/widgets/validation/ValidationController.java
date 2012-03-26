package edu.mayo.mprc.swift.ui.client.widgets.validation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.swift.ui.client.HidesPageContentsWhileLoading;
import edu.mayo.mprc.swift.ui.client.Service;
import edu.mayo.mprc.swift.ui.client.ServiceAsync;
import edu.mayo.mprc.swift.ui.client.SimpleParamsEditorPanel;
import edu.mayo.mprc.swift.ui.client.rpc.*;
import edu.mayo.mprc.swift.ui.client.widgets.Callback;
import edu.mayo.mprc.swift.ui.client.widgets.ParamSetSelectionController;
import edu.mayo.mprc.swift.ui.client.widgets.ParamSetSelectionListener;

import java.util.*;

/**
 * Controller object responsible for delivering events to and receiving events from child widgets,
 * and associating those widgets and events with Params objects from the server.  This
 * is intended to separate out the logic of handling updates from the physical
 * layout of the various widgets on the screen.
 */

public final class ValidationController implements ChangeListener, SourcesChangeEvents, ParamSetSelectionListener {

	private ClientParamSet paramSet;
	private ClientParamSetValues values;
	private ServiceAsync s;
	private ParamSetSelectionController selector;
	private HidesPageContentsWhileLoading contentsHiding;

	/**
	 * Maps parameter id strings one-to-one onto registrations.
	 */
	private Map<java.lang.String, ValidationController.Registration> byParam = new HashMap();

	/**
	 * Maps widgets to registrations.
	 */
	private Map<Validatable, ValidationController.Registration> byWidget = new HashMap();

	private Map<ValidationPanel, ValidationController.Registration> byValidationPanel = new HashMap();

	/**
	 * List of all the registrations with invalid (error or worse) settings.
	 */
	private HashSet<ValidationController.Registration> invalid = new HashSet();
	private ChangeListenerCollection listeners = new ChangeListenerCollection();
	private Registration awaitingUpdate = null;
	private ClientValue awaitingUpdateValue = null;

	/**
	 * Each widget has a registration that associates the various pieces together.
	 */
	private static final class Registration {
		private Validatable v;
		private ValidationPanel validationPanel;
		private ClientValidationList cv;
		private String param;
		private String mappingData; // mappingData that was passed to AbstractParam in order to get below allowedValues
		private List<? extends ClientValue> allowedValues;

		private Registration(final Validatable v, final String param, final ValidationPanel validationPanel) {
			this.v = v;
			this.validationPanel = validationPanel;
			this.param = param;
		}

		public Validatable getV() {
			return v;
		}

		public ValidationPanel getValidationPanel() {
			return validationPanel;
		}

		public ClientValidationList getCv() {
			return cv;
		}

		public String getParam() {
			return param;
		}

		public String getMappingData() {
			return mappingData;
		}

		public List<? extends ClientValue> getAllowedValues() {
			return allowedValues;
		}
	}


	public ValidationController(final ServiceAsync serviceAsync, final ParamSetSelectionController selector) {
		this.s = serviceAsync;
		this.selector = selector;
		selector.addParamSetSelectionListener(this);
	}

	public HidesPageContentsWhileLoading getContentsHiding() {
		return contentsHiding;
	}

	public void setContentsHiding(final HidesPageContentsWhileLoading contentsHiding) {
		this.contentsHiding = contentsHiding;
	}

	/**
	 * Register the given Validatable widget as responding to updates for the given param.
	 *
	 * @param v          The widget to register for.
	 * @param param      The param to associate this Validatable with.
	 * @param validation The ValidationPanel in which to place errors/warnings received for the given param.
	 */
	public void add(final Validatable v, final String param, final ValidationPanel validation) {
		final Registration reg = new Registration(v, param, validation);
		byParam.put(param, reg);
		byWidget.put(v, reg);
		byValidationPanel.put(validation, reg);
		v.addChangeListener(this);
	}

	public void update(final String paramId, final ClientValidationList cv) {
		update(paramId, null, cv, new HashSet<Validatable>());
	}

	/**
	 * Update the widget associated with the given param.  The given value is used
	 * in preference to the value in the ClientValidation.
	 *
	 * @param paramId
	 * @param value
	 * @param ccv
	 * @param visited HashSet of validatables which have been visited during this
	 *                round of updates.  Validatables which appear in this hash don't have their
	 *                validations cleared in case there are multiple validations for a given
	 *                Validatable.
	 */
	public void update(final String paramId,
	                   ClientValue value,
	                   final ClientValidationList ccv,
	                   final HashSet<Validatable> visited) {
		final Registration rr = byParam.get(paramId);
		if (value == null && ccv != null) {
			value = ccv.getValue();
		}
		if (rr == null) {
			throw new RuntimeException("Can't find registration for " + paramId);
		}
		// if we haven't yet seen this Validatable yet for this update, then
		// remove all the existing validations for it.
		if (!visited.contains(rr.getV())) {
			rr.getValidationPanel().removeValidationsFor(rr.getV());
			visited.add(rr.getV());
		}
		if (ccv != null) {
			rr.cv = ccv;
			final int sev = ccv.getWorstSeverity();
			rr.getV().setValidationSeverity(sev);
			if (sev > ClientValidation.SEVERITY_WARNING) {
				invalid.add(rr);
			} else {
				invalid.remove(rr);
			}
			for (final ClientValidation v : ccv) {
				if (v.getSeverity() != ClientValidation.SEVERITY_NONE) {
					rr.getValidationPanel().addValidation(v.shallowCopy(), rr.getV());
				}
			}
		} else {
			rr.getV().setValidationSeverity(ClientValidation.SEVERITY_NONE);
			invalid.remove(rr);
		}
		if (rr.getAllowedValues() != null) {
			rr.getV().setAllowedValues(rr.getAllowedValues());
		}
		final boolean validationDefinesNullValue = value == null && (ccv != null && ccv.size() > 0);
		final boolean validationDefinesValue = validationDefinesNullValue || value != null;
		if (rr.getV().getClientValue() == null || (validationDefinesValue && !rr.getV().getClientValue().equals(value))) {
			rr.getV().setValue(value);
		}
	}

	public void onChange(final Widget widget) {
		final Validatable v = (Validatable) widget;
		if (v == null) {
			throw new RuntimeException("ValidationController received change event for non Validatable widget");
		}
		final Registration r = (Registration) byWidget.get(v);
		if (r == null) {
			throw new RuntimeException("ValidationController received changed event for unregistered Validatable");
		}

		if (awaitingUpdate != null) {
			// ignore duplicate events while we're creating the temporary.
			return;
		}

		if (r.getV().getClientValue() == null) {
			if (r.getCv() != null) {  // TODO how to deal with locally generated validations?
				//ignore
				return;
			} else {
				throw new RuntimeException("Widget returned null value for " + r.getParam());
			}
		}

		// determine whether we need to make a temporary.

		if (!paramSet.isTemporary()) {
			setEnabled(false); //prevent users from making more changes while we're doing the
			// complex machinations of making the temporary paramset.
			awaitingUpdate = r;

			// must cache the users's requested change so it will be applied to the
			// correct ParamSet.
			awaitingUpdateValue = r.getV().getClientValue();
			createTemporary(r, r.getV().getClientValue());
		} else {
			doUpdate(r, r.getV().getClientValue());
		}
	}

	private void createTemporary(final Registration r, final ClientValue value) {
		s.save(new Service.Token(true), paramSet, null, null, null,
				false, new AsyncCallback<ClientParamSet>() {
			public void onFailure(final Throwable throwable) {
				SimpleParamsEditorPanel.handleGlobalError(throwable);
			}

			public void onSuccess(final ClientParamSet newParamSet) {
				selector.refresh(new ParamSetSelectionController.Callback() {
					public void refreshed() {
						// we match ClientParamSets by pointer, so always use the one from the list.
						ClientParamSet selectme = null;
						final List<ClientParamSet> list = selector.getClientParamSets();
						for (final ClientParamSet aList : list) {
							if (aList.equals(newParamSet)) {
								selectme = aList;
								break;
							}
						}
						if (selectme == null) {
							throw new RuntimeException("Temporary param set isn't in list");
						}
						selector.select(selectme);
					}
				});
			}
		});
	}

	private void doUpdate(final Registration r, final ClientValue value) {
		s.update(new Service.Token(true), paramSet, r.getParam(), value, new UpdateCallback(r));
	}

	private class UpdateCallback implements AsyncCallback<ClientParamsValidations> {
		private Registration r;

		UpdateCallback(final Registration r) {
			this.r = r;
		}

		public void onFailure(final Throwable throwable) {
			r.getValidationPanel().addValidation(new ClientValidation(throwable.toString()), r.getV());
		}

		public void onSuccess(final ClientParamsValidations o) {
			final HashSet<Validatable> visited = new HashSet<Validatable>();
			// Update all validations to clear them
			for (final Registration r : byWidget.values()) {
				final String paramId = r.getParam();
				final ClientValidationList cv = o.getValidationMap().get(paramId);
				if (cv == null) {
					update(paramId, null);
				} else {
					update(paramId, cv.getValue(), cv, visited);
				}
			}
			// Update all others
			for (final Map.Entry<String, ClientValidationList> entry : o.getValidationMap().entrySet()) {
				final String paramId = entry.getKey();
				final ClientValidationList cv = entry.getValue();
				update(paramId, cv.getValue(), cv, visited);
			}
			finishedUpdating();
		}
	}

	public void updateDependent(final Callback cb) {

		fetchAllowedValues(new Callback() {

			public void done() {
				final List<ClientParam> vals = values.getValues();
				final HashSet<Validatable> visited = new HashSet<Validatable>();
				for (final ClientParam val : vals) {
					update(val.getParamId(), val.getValue(), val.getValidationList(), visited);
				}
				finishedUpdating();
				cb.done();
			}
		});

	}

	public boolean isValid() {
		return invalid.size() == 0;
	}

	public void getAllowedValuesForValidatable(final Validatable v, final Callback cb) {
		final Registration r = byWidget.get(v);
		s.getAllowedValues(new Service.Token(true),
				paramSet,
				new String[]{r.getParam()},
				new String[]{null}, new AsyncCallback<List<List<ClientValue>>>() {

			public void onFailure(final Throwable throwable) {
				SimpleParamsEditorPanel.handleGlobalError(throwable);
			}

			public void onSuccess(final List<List<ClientValue>> allowedValues) {
				if (allowedValues.size() != 1) {
					throw new RuntimeException("Incorrect number of allowed values returned.");
				}
				r.allowedValues = allowedValues.get(0);
				final HashSet<Validatable> visited = new HashSet<Validatable>();
				update(r.getParam(), null, r.getCv(), visited);
				if (cb != null) {
					cb.done();
				}
			}
		});

	}

	private void fetchAllowedValues(final Callback cb) {
		final List<String> params = new ArrayList<String>();
		final List<String> mappingDatas = new ArrayList<String>();
		for (final Object o : byWidget.values()) {
			final Registration r = (Registration) o;
			final String newMappingData = r.getV().getAllowedValuesParam();
			if (newMappingData != null && ((!newMappingData.equals(r.getMappingData())) || r.getAllowedValues() == null)) {
				params.add(r.getParam());
				mappingDatas.add(newMappingData);
			}
		}

		if (params.size() == 0) {
			if (cb != null) {
				cb.done();
			}
			return;
		}

		s.getAllowedValues(new Service.Token(true),
				paramSet,
				params.toArray(new String[params.size()]),
				mappingDatas.toArray(new String[mappingDatas.size()]), new AsyncCallback<List<List<ClientValue>>>() {

			public void onFailure(final Throwable throwable) {
				SimpleParamsEditorPanel.handleGlobalError(throwable);
			}

			public void onSuccess(final List<List<ClientValue>> allowedValues) {
				if (allowedValues.size() != params.size()) {
					throw new RuntimeException("Incorrect number of allowed values returned.");
				}
				final HashSet<Validatable> visited = new HashSet<Validatable>();
				for (int i = 0; i < params.size(); ++i) {
					final Registration r = byParam.get(params.get(i));
					r.allowedValues = allowedValues.get(i);
					r.mappingData = mappingDatas.get(i);
					update(r.getParam(), null, r.getCv(), visited);
				}

				if (cb != null) {
					cb.done();
				}
			}
		});
	}

	/**
	 * Forces the refetch of given allowed values.
	 */
	public void getAllowedValues(final Validatable v, final Callback cb) {
		final Registration r = byWidget.get(v);
		r.allowedValues = null;
		fetchAllowedValues(cb);
	}

	public void addChangeListener(final ChangeListener changeListener) {
		listeners.add(changeListener);
	}

	public void removeChangeListener(final ChangeListener changeListener) {
		listeners.remove(changeListener);
	}

	/**
	 * Set the styles on a widget based on a validation severity.
	 * <p/>
	 * TODO this probably doesn't belong here, but where to put it?
	 */
	static void setValidationSeverity(final int validationSeverity, final UIObject o) {
		switch (validationSeverity) {
			case ClientValidation.SEVERITY_ERROR:
				o.addStyleName("severity-Error");
				o.removeStyleName("severity-Warning");
				break;
			case ClientValidation.SEVERITY_WARNING:
				o.addStyleName("severity-Warning");
				o.removeStyleName("severity-Error");
				break;
			default:
				o.removeStyleName("severity-Warning");
				o.removeStyleName("severity-Error");
		}
	}

	public void setEnabled(final boolean enabled) {
		for (final Registration r : byWidget.values()) {
			r.getV().setEnabled(enabled);
		}
	}

	private void finishedUpdating() {
		listeners.fireChange(null);
		setEnabled(true);
	}

	/**
	 * Fired whenever the selection changes.
	 */
	public void selected(final ClientParamSet selection) {
		final ClientParamSet sel = selection;
		if (paramSet == null || !paramSet.equals(sel)) {
			setEnabled(false);
			if (awaitingUpdate != null) {
				// we've just created a temporary and we need to install the value that
				// the user changed that caused us to request the temporary.
				paramSet = sel;
				doUpdate(awaitingUpdate, awaitingUpdateValue);
				awaitingUpdate = null;
				awaitingUpdateValue = null;

			} else {

				if (contentsHiding != null) {
					contentsHiding.hidePageContentsWhileLoading();
				}
				s.getParamSetValues(new Service.Token(true), sel, new AsyncCallback<ClientParamSetValues>() {
					public void onFailure(final Throwable throwable) {
						contentsHiding.showPageContents();
						SimpleParamsEditorPanel.handleGlobalError(throwable);
					}

					public void onSuccess(final ClientParamSetValues newValues) {
						try {
							if (newValues == null) {
								throw new RuntimeException("Didn't get a ClientParamSet");
							}
							values = newValues;
							paramSet = sel;
							updateDependent(new Callback() {
								public void done() {
									if (contentsHiding != null) {
										contentsHiding.showPageContentsAfterLoad();
									}
								}
							}
							);
						} catch (Exception ignore) {
							if (contentsHiding != null) {
								contentsHiding.showPageContents();
							}
						}
					}
				});
			}
		} else {
			setEnabled(true);
		}
	}
}
