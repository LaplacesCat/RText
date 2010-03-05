/*
 * 02/14/2010
 *
 * TasksPrefs.java - Preferences for the tasks plugin.
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This program is a part of RText.
 *
 * RText program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * RText program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.rtext.plugins.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import javax.swing.KeyStroke;

import org.fife.ui.app.Prefs;
import org.fife.ui.dockablewindows.DockableWindow;


/**
 * Preferences for the tasks plugin.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class TasksPrefs extends Prefs {

	/**
	 * Whether the GUI plugin window is active (visible).
	 */
	public boolean windowVisible;

	/**
	 * The location of the dockable tasks window.
	 */
	public int windowPosition;

	/**
	 * Key stroke that toggles the task window's visibility.
	 */
	public KeyStroke windowVisibilityAccelerator;

	/**
	 * The task identifiers.
	 */
	public String taskIdentifiers;

	private static final Pattern TASK_IDENTIFIERS_PATTERN =
		Pattern.compile("^$|^[\\p{Alpha}\\?]+(?:\\|[\\p{Alpha}\\?]+)*$");

	static final String DEFAULT_TASK_IDS	= "FIXME|TODO|HACK";


	/**
	 * Overridden to validate the task identifiers value.
	 */
	public void load(InputStream in) throws IOException {

		super.load(in);

		// Ensure task ID's is proper format - "letter+(|letter+)*"
		if (!TASK_IDENTIFIERS_PATTERN.matcher(taskIdentifiers).matches()) {
			System.out.println("Invalid task identifiers: " + taskIdentifiers);
			taskIdentifiers = DEFAULT_TASK_IDS;
		}

		// Ensure window position is valid.
		if (!DockableWindow.isValidPosition(windowPosition)) {
			windowPosition = DockableWindow.BOTTOM;
		}

	}


	/**
	 * {@inheritDoc}
	 */
	public void setDefaults() {
		windowVisible = true;
		windowPosition = DockableWindow.BOTTOM;
		windowVisibilityAccelerator = null;
		taskIdentifiers = DEFAULT_TASK_IDS;
	}


}