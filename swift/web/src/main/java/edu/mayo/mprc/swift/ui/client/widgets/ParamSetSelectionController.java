package edu.mayo.mprc.swift.ui.client.widgets;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;
import edu.mayo.mprc.swift.ui.client.HidesPageContentsWhileLoading;
import edu.mayo.mprc.swift.ui.client.Service;
import edu.mayo.mprc.swift.ui.client.ServiceAsync;
import edu.mayo.mprc.swift.ui.client.SimpleParamsEditorPanel;
import edu.mayo.mprc.swift.ui.client.rpc.ClientParamSet;
import edu.mayo.mprc.swift.ui.client.rpc.ClientParamSetList;

import java.util.*;

/**
 * Handles changes to the primary selector.
 */
public final class ParamSetSelectionController implements ChangeListener {
	private ClientParamSetList list;
	private ServiceAsync service;
	private HidesPageContentsWhileLoading contentsHiding;

	private ParamsSelector selector;

	/**
	 * Keeps mapping between ClientParamSet and integer index in list.
	 */
	private Map<ClientParamSet, Integer> paramSetIndices = new HashMap<ClientParamSet, Integer>();

	/**
	 * Keeps list of ParamSetSelectionListeners.
	 */
	private List<ParamSetSelectionListener> listeners = new ArrayList<ParamSetSelectionListener>();

	/**
	 * Last user-selected parameter set id {@link ClientParamSet#getId()}.
	 */
	private static final String PARAM_COOKIE = "param";

	public ParamSetSelectionController(final ServiceAsync service) {
		this.service = service;
	}

	public HidesPageContentsWhileLoading getContentsHiding() {
		return contentsHiding;
	}

	public void setContentsHiding(final HidesPageContentsWhileLoading contentsHiding) {
		this.contentsHiding = contentsHiding;
	}

	public void setSelector(final ParamsSelector selector) {
		this.selector = selector;
		if (list != null) {
			selector.update(list.getList(), getDefaultParamSetId());
		}
		selector.addChangeListener(this);
	}

	public ClientParamSet getSelectedParamSet() {
		return selector.getSelectedParamSet();
	}

	public List<ClientParamSet> getClientParamSets() {
		return list.getList();
	}

	public void refresh() {
		refresh(null);
	}

	public static Date getCookieExpirationDate() {
		final Date expires = new Date();
		long expiresLong = expires.getTime();
		expiresLong += 1000L * 60 * 60 * 24 * 30; //30 days
		expires.setTime(expiresLong);
		return expires;
	}

	public interface Callback {
		void refreshed();
	}

	public void refresh(final Callback cb) {
		if (null != contentsHiding) {
			contentsHiding.hidePageContentsWhileLoading();
		}
		service.getParamSetList(new Service.Token(true), new AsyncCallback<ClientParamSetList>() {
			public void onFailure(final Throwable throwable) {
				if (null != contentsHiding) {
					contentsHiding.showPageContents();
				}
				SimpleParamsEditorPanel.handleGlobalError(throwable);
			}

			public void onSuccess(final ClientParamSetList o) {
				try {
					setParamSetList(o);
					if (cb != null) {
						cb.refreshed();
					}
				} finally {
					if (null != contentsHiding) {
						contentsHiding.showPageContentsAfterLoad();
					}
				}
			}
		});
	}

	public void setParamSetList(final ClientParamSetList newList) {
		final boolean prime = list == null;
		list = newList;
		final List<ClientParamSet> arr = list.getList();
		paramSetIndices.clear();
		for (int i = 0; i < arr.size(); ++i) {
			paramSetIndices.put(arr.get(i), i);
		}
		selector.update(list.getList(), getDefaultParamSetId());
		if (prime) {
			int index = 0;
			final int defaultParamSetId = getDefaultParamSetId();
			// Find param set with the default id
			for (int i = 0; i < arr.size(); i++) {
				if (arr.get(i).getId() == defaultParamSetId) {
					index = i;
					break;
				}
			}
			select(arr.get(index));  // bootstrap by selecting the default value.
		}
	}

	/**
	 * Store default parameter set in a cookie.
	 *
	 * @param id {@link ClientParamSet#getId()} for the param set to use as a default.
	 */
	private void setDefaultParamSetId(final int id) {
		Cookies.setCookie(PARAM_COOKIE, String.valueOf(id), getCookieExpirationDate(), null, "/", false);
	}

	/**
	 * @return The user-specified default parameter set id - {@link ClientParamSet#getId()}.
	 */
	public int getDefaultParamSetId() {
		final String paramCookie = Cookies.getCookie(PARAM_COOKIE);
		int id = 0;
		if (paramCookie != null) {
			try {
				id = Integer.parseInt(paramCookie);
			} catch (Exception ignore) {
				// SWALLOWED - we ignore malformed cookies
			}
		}
		return id;
	}

	public void select(final ClientParamSet paramSet) {
		Integer index = paramSetIndices.get(paramSet);
		if (index == null) {
			index = addNewClientParamSet(paramSet);
		}
		selector.select(index);
		fireSelectionEvent();
	}

	/**
	 * This typically happens when a previous search is loaded and new parameter sets,
	 * unseen before, appear.
	 * <p/>
	 * Add the new parameter set to our data structures and update all the selectors.
	 */
	private int addNewClientParamSet(final ClientParamSet paramSet) {
		// New client parameter set
		final int newSetId = paramSetIndices.size();
		paramSetIndices.put(paramSet, newSetId);
		final List<ClientParamSet> sets = list.getList();
		final List<ClientParamSet> newSets = new ArrayList<ClientParamSet>(sets.size() + 1);
		final int newSetIndex = sets.size();
		newSets.addAll(sets);
		newSets.add(paramSet);
		selector.update(newSets, newSetIndex);
		return newSetIndex;
	}

	public void onChange(final Widget widget) {
		// The primary selector's changes get preserved
		setDefaultParamSetId(selector.getSelectedParamSet().getId());
		fireSelectionEvent();
	}

	public void addParamSetSelectionListener(final ParamSetSelectionListener listener) {
		listeners.add(listener);
	}

	public void removeParamSetSelectionListener(final ParamSetSelectionListener listener) {
		listeners.remove(listener);
	}

	public void fireSelectionEvent() {
		for (final ParamSetSelectionListener listener1 : listeners) {
			final ParamSetSelectionListener listener = (ParamSetSelectionListener) listener1;
			final ClientParamSet paramSet = getSelectedParamSet();
			listener.selected(paramSet);
		}
	}

}
