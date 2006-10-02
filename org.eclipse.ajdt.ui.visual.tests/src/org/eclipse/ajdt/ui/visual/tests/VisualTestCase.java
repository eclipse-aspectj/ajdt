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
package org.eclipse.ajdt.ui.visual.tests;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.AllUITests;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.internal.console.ConsoleView;
import org.eclipse.ui.internal.console.IOConsolePage;
import org.eclipse.ui.progress.UIJob;

/**
 * Abstract superclass for Visual tests
 */
public abstract class VisualTestCase extends UITestCase {

	protected void setUp() throws Exception {
		super.setUp();
		AllUITests.setupAJDTPlugin();
	}

	protected Display display = Display.getCurrent();

	protected void gotoLine(final int line) {
		postKeyDown(SWT.CTRL);
		postKey('l');
		postKeyUp(SWT.CTRL);

		new UIJob("post key job") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				sleep();
				char[] chars = String.valueOf(line).toCharArray();
				for (int i = 0; i < chars.length; i++) {
					char c = chars[i];
					postKey(c);
				}				
				postKey(SWT.CR);
				return Status.OK_STATUS;
			}
		}.schedule(1000);
		waitForJobsToComplete();
	}

	protected void moveCursorRight(int spaces) {
		for (int i = 0; i < spaces; i++) {
			Event event = new Event();
			event.type = SWT.KeyDown;
			event.keyCode = SWT.ARROW_RIGHT;
			display.post(event);
			sleep(10);
			event.type = SWT.KeyUp;
			display.post(event);
			sleep(10);
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
		postKeyDown(c);
		postKeyUp(c);
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
		sleep(10);
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
		sleep(10);
	}

	/**
	 * Post a key event (equivalent to posting a key down event then a key up
	 * event)
	 * 
	 * @param keyCode -
	 *            one of the key codes defined int he SWT class
	 */
	protected void postKey(int keyCode) {
		postKeyDown(keyCode);
		postKeyUp(keyCode);
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
		sleep(10);
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
		sleep(10);
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

	protected void sleep(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
		}
	}

	protected static void selectInPackageExplorer(Object element) {
		PackageExplorerPart packageExplorer = PackageExplorerPart
				.getFromActivePerspective();
		packageExplorer.setFocus();
		packageExplorer.selectAndReveal(element);
	}

	protected static String getConsoleViewContents() {
		ConsoleView cview = null;
		IViewReference[] views = AspectJUIPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getViewReferences();
		for (int i = 0; i < views.length; i++) {
			if (views[i].getView(false) instanceof ConsoleView) {
				cview = (ConsoleView) views[i].getView(false);
			}
		}
		assertNotNull("Console view should be open", cview); //$NON-NLS-1$
		IOConsolePage page = (IOConsolePage) cview.getCurrentPage();
		TextViewer viewer = page.getViewer();
		return viewer.getDocument().get();
	}
}
