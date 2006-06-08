/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.visual;

import org.eclipse.ajdt.core.EclipseVersion;
import org.eclipse.ajdt.ui.tests.AllUITests;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * Abstract superclass for Visual tests
 */
public abstract class VisualTestCase extends UITestCase {

	protected void setUp() throws Exception {
		super.setUp();
		AllUITests.setupAJDTPlugin();
	}

	protected Display display = Display.getCurrent();

	protected boolean runningEclipse31 = EclipseVersion.MINOR_VERSION == 1
			&& EclipseVersion.MAJOR_VERSION == 3;

	protected void gotoLine(final int line) {
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = SWT.CTRL;
		display.post(event);

		event = new Event();
		event.type = SWT.KeyDown;
		event.character = 'l';
		display.post(event);

		event = new Event();
		event.character = 'l';
		event.type = SWT.KeyUp;
		display.post(event);

		event = new Event();
		event.type = SWT.KeyUp;
		event.keyCode = SWT.CTRL;
		display.post(event);

		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				
				char[] chars = String.valueOf(line).toCharArray();
				Event event;
				for (int i = 0; i < chars.length; i++) {
					char c = chars[i];
					event = new Event();
					event.type = SWT.KeyDown;
					event.character = c;
					display.post(event);

					event = new Event();
					event.character = c;
					event.type = SWT.KeyUp;
					display.post(event);
				}

				event = new Event();
				event.type = SWT.KeyDown;
				event.character = SWT.CR;
				display.post(event);

				event = new Event();
				event.type = SWT.KeyUp;
				event.character = SWT.CR;
				display.post(event);

			}
		};
		new Thread(r).start();
		waitForJobsToComplete();
	}

	protected void moveCursorRight(int spaces) {
		for (int i = 0; i < spaces; i++) {
			Event event = new Event();
			event.type = SWT.KeyDown;
			event.keyCode = SWT.ARROW_RIGHT;
			display.post(event);

			event.type = SWT.KeyUp;
			display.post(event);
		}
	}

	/**
	 * Post a key event (equivalent to posting a key down event then a key up
	 * event)
	 * 
	 * @param c -
	 *            the character to post
	 */
	protected void postKey(char c) {
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.character = c;
		display.post(event);

		event.type = SWT.KeyUp;
		display.post(event);
	}

	/**
	 * Post a key down event
	 * 
	 * @param c -
	 *            the character to post
	 */
	protected void postKeyDown(char c) {
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.character = c;
		display.post(event);
	}

	/**
	 * Post a key up event
	 * 
	 * @param c -
	 *            the character to post
	 */
	protected void postKeyUp(char c) {
		Event event = new Event();
		event.type = SWT.KeyUp;
		event.character = c;
		display.post(event);
	}

	/**
	 * Post a key event (equivalent to posting a key down event then a key up
	 * event)
	 * 
	 * @param keyCode -
	 *            one of the key codes defined int he SWT class
	 */
	protected void postKey(int keyCode) {
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = keyCode;
		display.post(event);

		event.type = SWT.KeyUp;
		display.post(event);
	}

	/**
	 * Post a key down event
	 * 
	 * @param keyCode -
	 *            one of the key codes defined int he SWT class
	 */
	protected void postKeyDown(int keyCode) {
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = keyCode;
		display.post(event);
	}

	/**
	 * Post a key up event
	 * 
	 * @param keyCode -
	 *            one of the key codes defined int he SWT class
	 */
	protected void postKeyUp(int keyCode) {
		Event event = new Event();
		event.type = SWT.KeyUp;
		event.keyCode = keyCode;
		display.post(event);
	}

	/**
	 * Post a whole string (equivalent to posting key up and then key down
	 * events for each character in turn)
	 * 
	 * @param string
	 */
	protected void postString(String string) {
		char[] characters = string.toCharArray();
		for (int i = 0; i < characters.length; i++) {
			char c = characters[i];
			if (Character.isUpperCase(c)) {
				c = Character.toLowerCase(c);
				postKeyDown(SWT.SHIFT);
				postKey(c);
				postKeyUp(SWT.SHIFT);
			} else {
				postKey(c);
			}
		}

	}

	protected void sleep() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
	}
}
