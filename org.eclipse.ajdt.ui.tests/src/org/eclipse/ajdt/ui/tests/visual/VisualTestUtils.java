/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests.visual;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

public class VisualTestUtils {

	public static void gotoLine(int line) {
		Display display = Display.getCurrent();
		
		// Go to beginning of file
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = SWT.CTRL;
		display.post(event);
		
		event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = SWT.HOME;
		display.post(event);
		
		event = new Event();
		event.type = SWT.KeyUp;
		event.keyCode = SWT.HOME;
		display.post(event);
		
		event = new Event();
		event.type = SWT.KeyUp;
		event.keyCode = SWT.CTRL;
		display.post(event);
		
		for (int i = 1; i < line; i++) {
			event = new Event();
			event.type = SWT.KeyDown;
			event.keyCode = SWT.ARROW_DOWN;
			display.post(event);
	
			event = new Event();
			event.type = SWT.KeyUp;
			event.keyCode = SWT.ARROW_DOWN;
			display.post(event);
		}
	}
	
	
	public static void moveCursorRight(int spaces) {
		Display display = Display.getCurrent();
		for (int i = 0; i < spaces; i++) {
			Event event = new Event();
			event.type = SWT.KeyDown;
			event.keyCode = SWT.ARROW_RIGHT;
			display.post(event);
	
			event = new Event();
			event.type = SWT.KeyUp;
			event.keyCode = SWT.ARROW_RIGHT;
			display.post(event);
		}
	}
	
	
	
	
	

}
