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
package org.eclipse.ajdt.ui.visual.tests;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;

public class AJDocTest extends VisualTestCase {

	public void testGenerateAJDoc() throws Exception {
		IProject project = createPredefinedProject("Spacewar Example"); //$NON-NLS-1$

		IResource debug = project.findMember("debug.ajproperties"); //$NON-NLS-1$
		assertNotNull("Could not find debug.ajproperties", debug); //$NON-NLS-1$

		// select project and run AJDoc wizard
		selectInPackageExplorer(project);

		postKeyDown(SWT.ALT);
		postKey('p');
		postKeyUp(SWT.ALT);

		postKey('e'); // generate AJDoc

		Runnable r = new Runnable() {
			public void run() {
				sleep();

				// move to visibility section
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);

				postKey('i'); // select Private

				postKey('f'); // Finish

				sleep();

				postKey('a'); // answer All to dialog
			}
		};
		new Thread(r).start();

		waitForJobsToComplete();

		// now check the ajdoc operation was successful
		IResource doc = project.findMember("doc"); //$NON-NLS-1$
		assertNotNull("Could not find generated doc folder", doc); //$NON-NLS-1$
		assertTrue("Doc resource not a folder: " + doc, //$NON-NLS-1$
				doc.getType() == IResource.FOLDER);

		IFolder docFolder = (IFolder) doc;
		IResource reg = docFolder.findMember("spacewar/Registry.html"); //$NON-NLS-1$
		assertNotNull("Could not find Registry.html in generated docs", reg); //$NON-NLS-1$
		assertTrue("HTML resource not a file: " + reg, //$NON-NLS-1$
				reg.getType() == IResource.FILE);

		String contents = readFile((IFile) reg);
		String[] expected = {
				"Constructor Summary", //$NON-NLS-1$
				"Advised", //$NON-NLS-1$
				"spacewar.Debug.before(): allInitializationsCut" }; //$NON-NLS-1$
		for (int i = 0; i < expected.length; i++) {
			assertTrue(
					"Didn't find expected text in generated output: " + expected[i],  //$NON-NLS-1$
					contents.indexOf(expected[i]) != -1);
		}

	}
}
