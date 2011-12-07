/*
 * Copyright 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.mprc.swift.ui.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A disclosure panel that does not make it's entire header clickable.
 * <p/>
 * This has to be copied because GWT's DisclosurePanel is final. :-(
 * <p/>
 * TODO This just a crude hacking of the GWT version; should be refined eventually.
 */
public final class CustomDisclosurePanel extends Composite implements
		HasWidgets {

	/**
	 * The default header widget used within a {@link DisclosurePanel}.
	 */
	private class DefaultHeader extends SimplePanel implements HasText,
			CustomDisclosureHandler {

		/**
		 * imageTD holds the image for the icon, not null. labelTD holds the text
		 * for the label.
		 */
		private final Element labelTD;

		private final Image iconImage;
		private final DisclosurePanelImages images;

		private DefaultHeader(DisclosurePanelImages images, String text) {
			super(DOM.createAnchor());
			this.images = images;

			Element elem = getElement();

			DOM.setElementProperty(elem, "href", "javascript:void(0);");
			// Avoids layout problems from having blocks in inlines.
			DOM.setStyleAttribute(elem, "display", "block");
			sinkEvents(Event.ONCLICK);
			setStyleName(STYLENAME_HEADER);

			iconImage = isOpen ? images.disclosurePanelOpen().createImage()
					: images.disclosurePanelClosed().createImage();

			// I do not need any Widgets here, just a DOM structure.
			Element root = DOM.createTable();
			Element tbody = DOM.createTBody();
			Element tr = DOM.createTR();
			final Element imageTD = DOM.createTD();
			labelTD = DOM.createTD();

			setElement(root);

			DOM.appendChild(root, tbody);
			DOM.appendChild(tbody, tr);
			DOM.appendChild(tr, imageTD);
			DOM.appendChild(tr, labelTD);

			// set image TD to be same width as image.
			DOM.setElementProperty(imageTD, "align", "center");
			DOM.setElementProperty(imageTD, "valign", "middle");
			DOM.setStyleAttribute(imageTD, "width", iconImage.getWidth() + "px");

			DOM.appendChild(imageTD, iconImage.getElement());

			setText(text);

			addEventHandler(this);
			setStyle();
		}

		public final String getText() {
			return DOM.getInnerText(labelTD);
		}

		public final void onClose(CustomDisclosureEvent event) {
			setStyle();
		}

		public final void onOpen(CustomDisclosureEvent event) {
			setStyle();
		}

		public final void setText(String text) {
			DOM.setInnerText(labelTD, text);
		}

		private void setStyle() {
			if (isOpen) {
				images.disclosurePanelOpen().applyTo(iconImage);
			} else {
				images.disclosurePanelClosed().applyTo(iconImage);
			}
		}

		public final void onBrowserEvent(Event event) {
			// no need to call super.
			if (DOM.eventGetType(event) == Event.ONCLICK) {
				// Prevent link default action.
				DOM.eventPreventDefault(event);
				setOpen(!isOpen);
			}
		}

	}

	// Stylename constants.
	private static final String STYLENAME_DEFAULT = "gwt-DisclosurePanel";

	private static final String STYLENAME_SUFFIX_OPEN = "open";

	private static final String STYLENAME_SUFFIX_CLOSED = "closed";

	private static final String STYLENAME_HEADER = "header";

	private static final String STYLENAME_CONTENT = "content";

	private static DisclosurePanelImages createDefaultImages() {
		return (DisclosurePanelImages) GWT.create(DisclosurePanelImages.class);
	}

	/**
	 * top level widget. The first child will be a panel containing the clickable and non-clickable parst of the header
	 * The second child will either not exist or be a non-null to reference to
	 * {@link #content}.
	 */
	private final VerticalPanel mainPanel = new VerticalPanel();
	private HorizontalPanel headerPanel = new HorizontalPanel();
	/**
	 * holds the clickable part of the header widget.
	 */
	private DefaultHeader clickableHeader;

	private SimplePanel staticHeader = new SimplePanel();

	/**
	 * the content widget, this can be null.
	 */
	private Widget content;

	private boolean isOpen = false;

	/**
	 * null until #{@link #addEventHandler(CustomDisclosureHandler)} is called (lazily
	 * initialized).
	 */
	private ArrayList /* <DisclosureHandler> */handlers;


	/**
	 * Creates a DisclosurePanel with the specified header text, an initial
	 * open/close state and a bundle of images to be used in the default header
	 * widget.
	 *
	 * @param images     a bundle that provides disclosure panel specific images
	 * @param headerText the text to be displayed in the header
	 * @param isOpen     the initial open/close state of the content panel
	 */
	public CustomDisclosurePanel(DisclosurePanelImages images, String headerText,
	                             boolean isOpen) {
		init(isOpen, images, headerText);
	}

	/**
	 * Creates a DisclosurePanel that will be initially closed using the specified
	 * text in the header.
	 *
	 * @param headerText the text to be displayed in the header.
	 */
	public CustomDisclosurePanel(String headerText) {
		this(createDefaultImages(), headerText, false);
	}

	/**
	 * Creates a DisclosurePanel with the specified header text and an initial
	 * open/close state.
	 *
	 * @param headerText the text to be displayed in the header
	 * @param isOpen     the initial open/close state of the content panel
	 */
	public CustomDisclosurePanel(String headerText, boolean isOpen) {
		this(createDefaultImages(), headerText, isOpen);
	}


	public void add(Widget w) {
		if (this.getContent() == null) {
			setContent(w);
		} else {
			throw new IllegalStateException(
					"A DisclosurePanel can only contain two Widgets.");
		}
	}

	/**
	 * Attaches an event handler to the panel to receive {@link DisclosureEvent}
	 * notification.
	 *
	 * @param handler the handler to be added (should not be null)
	 */
	public final void addEventHandler(CustomDisclosureHandler handler) {
		if (handlers == null) {
			handlers = new ArrayList();
		}
		handlers.add(handler);
	}

	public void clear() {
		setContent(null);
	}

	/**
	 * Gets the widget that was previously set in {@link #setContent(Widget)}.
	 *
	 * @return the panel's current content widget
	 */
	public final Widget getContent() {
		return content;
	}

	/**
	 * Gets the widget that is currently being used as a header.
	 *
	 * @return the widget currently being used as a header
	 */
	public final Widget getHeader() {
		return staticHeader;
	}

	public void setHeaderText(String text) {
		clickableHeader.setText(text);
	}

	/**
	 * Gets a {@link HasText} instance to provide access to the headers's text, if
	 * the header widget does provide such access.
	 *
	 * @return a reference to the header widget if it implements {@link HasText},
	 *         <code>null</code> otherwise
	 */
	public final HasText getHeaderTextAccessor() {
		Widget widget = staticHeader;
		return (widget instanceof HasText) ? (HasText) widget : null;
	}

	/**
	 * Determines whether the panel is open.
	 *
	 * @return <code>true</code> if panel is in open state
	 */
	public final boolean isOpen() {
		return isOpen;
	}

	// this has to be copied here because WidgetIterators is package access only

	private static Widget[] copyWidgetArray(final Widget[] widgets) {
		final Widget[] clone = new Widget[widgets.length];
		System.arraycopy(widgets, 0, clone, 0, widgets.length);
		return clone;
	}

	public static Iterator<Widget> createWidgetIterator(final HasWidgets container,
	                                                    final Widget[] contained) {
		return new Iterator<Widget>() {
			private int index = -1;
			private int last = -1;
			private boolean widgetsWasCopied = false;
			private Widget[] widgets = contained;

			{
				gotoNextIndex();
			}

			private void gotoNextIndex() {
				++index;
				while (index < contained.length) {
					if (contained[index] != null) {
						return;
					}
					++index;
				}
			}

			public boolean hasNext() {
				return (index < contained.length);
			}

			public Widget next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				last = index;
				final Widget w = contained[index];
				gotoNextIndex();
				return w;
			}

			public void remove() {
				if (last < 0) {
					throw new IllegalStateException();
				}

				if (!widgetsWasCopied) {
					widgets = copyWidgetArray(widgets);
					widgetsWasCopied = true;
				}

				container.remove(contained[last]);
				last = -1;
			}
		};
	}

	public Iterator<Widget> iterator() {
		return createWidgetIterator(this,
				new Widget[]{getContent()});
	}

	public boolean remove(Widget w) {
		if (w.equals(getContent())) {
			setContent(null);
			return true;
		}
		return false;
	}

	/**
	 * Removes an event handler from the panel.
	 *
	 * @param handler the handler to be removed
	 */
	public final void removeEventHandler(CustomDisclosureHandler handler) {
		if (handlers == null) {
			return;
		}
		handlers.remove(handler);
	}

	/**
	 * Sets the content widget which can be opened and closed by this panel. If
	 * there is a preexisting content widget, it will be detached.
	 *
	 * @param content the widget to be used as the content panel
	 */
	public final void setContent(Widget content) {
		final Widget currentContent = this.content;

		// Remove existing content widget.
		if (currentContent != null) {
			mainPanel.remove(currentContent);
			currentContent.removeStyleName(STYLENAME_CONTENT);
		}

		// Add new content widget if != null.
		this.content = content;
		if (content != null) {
			mainPanel.add(content);
			content.addStyleName(STYLENAME_CONTENT);
			setContentDisplay();
		}
	}

	/**
	 * Sets the widget used as the header for the panel.
	 *
	 * @param headerWidget the widget to be used as the header
	 */
	public final void setHeader(Widget headerWidget) {

		staticHeader.setWidget(headerWidget);
	}

	/**
	 * Changes the visible state of this <code>DisclosurePanel</code>.
	 *
	 * @param isOpen <code>true</code> to open the panel, <code>false</code>
	 *               to close
	 */
	public final void setOpen(boolean isOpen) {
		if (this.isOpen != isOpen) {
			this.isOpen = isOpen;
			setContentDisplay();
			fireEvent();
		}
	}

	private void fireEvent() {
		if (handlers == null) {
			return;
		}

		CustomDisclosureEvent event = new CustomDisclosureEvent(this);
		for (Iterator it = handlers.iterator(); it.hasNext();) {
			CustomDisclosureHandler handler = (CustomDisclosureHandler) it.next();
			if (isOpen) {
				handler.onOpen(event);
			} else {
				handler.onClose(event);
			}
		}
	}

	private void init(boolean isOpen, DisclosurePanelImages images, String headerText) {
		this.isOpen = isOpen;
		clickableHeader = new DefaultHeader(images, headerText);
		initWidget(mainPanel);
		headerPanel.add(clickableHeader);
		headerPanel.add(staticHeader);
		mainPanel.add(headerPanel);

		setStyleName(STYLENAME_DEFAULT);
		setContentDisplay();
	}

	private void setContentDisplay() {
		if (isOpen) {
			removeStyleDependentName(STYLENAME_SUFFIX_CLOSED);
			addStyleDependentName(STYLENAME_SUFFIX_OPEN);
		} else {
			removeStyleDependentName(STYLENAME_SUFFIX_OPEN);
			addStyleDependentName(STYLENAME_SUFFIX_CLOSED);
		}

		if (content != null) {
			content.setVisible(isOpen);
		}
	}
}
