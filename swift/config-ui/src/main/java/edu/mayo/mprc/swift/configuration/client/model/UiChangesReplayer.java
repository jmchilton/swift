package edu.mayo.mprc.swift.configuration.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class UiChangesReplayer implements Serializable {
	public static final String SET_PROPERTY = "setProperty";
	public static final String DISPLAY_PROPERTY_ERROR = "displayPropertyError";
	private static final long serialVersionUID = 3812490472211147945L;

	private ArrayList<String> commands;

	public UiChangesReplayer() {
	}

	public UiChangesReplayer(final ArrayList<String> commands) {
		this.commands = commands;
	}

	public void replay(final UiChanges uiChanges) {
		final Iterator<String> iterator = commands.iterator();
		while (iterator.hasNext()) {
			final String command = iterator.next();
			if (SET_PROPERTY.equals(command)) {
				uiChanges.setProperty(iterator.next(), iterator.next(), iterator.next());
			} else if (DISPLAY_PROPERTY_ERROR.equals(command)) {
				uiChanges.displayPropertyError(iterator.next(), iterator.next(), iterator.next());
			} else {
				throw new RuntimeException("Unknown command " + command);
			}
		}
	}
}
