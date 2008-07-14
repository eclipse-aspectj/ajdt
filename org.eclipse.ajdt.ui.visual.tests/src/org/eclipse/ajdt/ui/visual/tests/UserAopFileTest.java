/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Ford  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.visual.tests;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;

public class UserAopFileTest extends VisualTestCase {

	public void testUserAopXmlFile() throws Exception {

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
				postKey('w');
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
				postKey(' '); // tick first one
				postKey(SWT.CR);

				// finally, run the launch config
				postKeyDown(SWT.ALT);
				postKey('r');
				postKeyUp(SWT.ALT);
			}
		};
		new Thread(r).start();

		waitForJobsToComplete();
		String output = getConsoleViewContents().trim();
		System.out.println(output);
		assertTrue("aspect was not woven", output //$NON-NLS-1$
				.indexOf("Before advise called") != -1); //$NON-NLS-1$
		assertTrue("user aop.xml file was not read.", output //$NON-NLS-1$
				.indexOf("[WeavingURLClassLoader]") != -1); //$NON-NLS-1$

	}

	protected void setUp() throws Exception {
		super.setUp();
		IProject project = createPredefinedProject("user.aop.xml.weave"); //$NON-NLS-1$
		assertTrue(
				"The user.aop.xml.weave project should have been created", project != null); //$NON-NLS-1$
		IProject project2 = createPredefinedProject("WeaveMe2"); //$NON-NLS-1$		
		assertTrue(
				"The WeaveMe2 project should have been created", project2 != null); //$NON-NLS-1$
	}
}
