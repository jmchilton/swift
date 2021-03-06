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


/**
 * Event handler interface for {@link com.google.gwt.user.client.ui.DisclosureEvent}.
 *
 * @see com.google.gwt.user.client.ui.DisclosurePanel
 */
public interface CustomDisclosureHandler {
	/**
	 * Fired when the panel is closed.
	 *
	 * @param event event representing this action.
	 */
	void onClose(CustomDisclosureEvent event);

	/**
	 * Fired when the panel is opened.
	 *
	 * @param event event representing this action.
	 */
	void onOpen(CustomDisclosureEvent event);
}