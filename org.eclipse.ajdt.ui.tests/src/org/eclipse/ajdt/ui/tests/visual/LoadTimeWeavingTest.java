/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.visual;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;

public class LoadTimeWeavingTest extends VisualTestCase {

	private String outputStringStart = "about to call a main method"; //$NON-NLS-1$

	public void testLTWLaunchConfig() throws CoreException {
		createPredefinedProject("MyAspectLibrary"); //$NON-NLS-1$
		createPredefinedProject("LoadTimeWeaveMe"); //$NON-NLS-1$

		postKeyDown(SWT.ALT);
		postKey('r');
		postKeyUp(SWT.ALT);

		postKey('n'); // Run...
		
		Runnable r = new Runnable() {
			public void run() {
				sleep();
				String ajload = "AspectJ Load"; //$NON-NLS-1$
				postString(ajload);
				sleep();
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(' ');
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(' ');
				postKey(SWT.TAB);
				for (int i = 0; i < ajload.length(); i++) {
					postKey(SWT.BS);					
				}
				postString("New"); //$NON-NLS-1$
				sleep();
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				sleep();
				postKeyDown(SWT.ALT);
				postKey('b'); // browse for project
				postKeyUp(SWT.ALT);
				sleep();
				postKey(SWT.CR); // select first one
				postKeyDown(SWT.ALT);
				postKey('s'); // search for main type
				postKeyUp(SWT.ALT);
				sleep();
				postKey(SWT.CR); // select first one
				
				// go back to Main tab
				postKeyDown(SWT.SHIFT);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKeyUp(SWT.SHIFT);
				
				// now along to LTW tab
				postKey(SWT.ARROW_RIGHT);
				postKey(SWT.ARROW_RIGHT);
				postKey(SWT.ARROW_RIGHT);
				postKey(SWT.ARROW_RIGHT);
				
				// select user entries
				postKey(SWT.TAB);
				postKey(' ');
				
				postKeyDown(SWT.ALT);
				postKey('s'); // Add projects
				postKeyUp(SWT.ALT);
				
				sleep();
				postKey(' '); // tick first one (MyAspectLibrary)
				postKey(SWT.CR);
				
				// finally, run the launch config
				postKeyDown(SWT.ALT);
				postKey('r');
				postKeyUp(SWT.ALT);
			}
		};
		new Thread(r).start();
		
		waitForJobsToComplete();

		String output = getConsoleViewContents();
		assertNotNull(output);
		assertTrue("program did not run correctly", output.indexOf(outputStringStart) != -1); //$NON-NLS-1$
	}
}
