/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.visual;

import org.eclipse.ajdt.ui.tests.AllUITests;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.actions.RenameAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class Bug100018Test extends VisualTestCase {

	public void testBug100018() throws Exception {
		AllUITests.setupAJDTPlugin();
		// must be a Java project
		IProject project = Utils.createPredefinedProject("project.java.Y");
		try {
			IFile javaFile = (IFile) project
					.findMember("src/internal/stuff/MyBuilder.java");
			IEditorPart editor = Utils.openFileInDefaultEditor(javaFile, true);
			int numEditors = countOpenEditors();
			assertTrue("There should only be one open editor", numEditors == 1);

			IJavaProject jp = JavaCore.create(project);
			assertNotNull("Java project is null", jp);
			IJavaElement elem = jp.findElement(new Path(
					"internal/stuff/MyBuilder.java"), null);
			assertNotNull("Couldn't find IJavaElement for MyBuilder.java", elem);

			Runnable r = new Runnable() {
				public void run() {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
					}
					postKey(SWT.ARROW_RIGHT); // deselect hilighted text
					postCharacterKey('2'); // new name should now be MyBuilder2
					postCharacterKey(SWT.CR);
				}
			};
			new Thread(r).start();

			// create the rename action, and pass it the IJavaElement
			RenameAction rename = new RenameAction(editor.getSite());
			StructuredSelection selection = new StructuredSelection(elem);
			rename.run(selection);

			// bug 100018: the rename operation caused the editor to close
			numEditors = countOpenEditors();
			assertFalse("Bug 100018: Rename operation caused editor to close",
					numEditors == 0);
			assertTrue("Wrong number of open editors: expected 1, found "
					+ numEditors, numEditors == 1);
		} finally {
			Utils.deleteProject(project);
		}
	}

	private int countOpenEditors() {
		int count = 0;
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
				.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			for (int x = 0; x < pages.length; x++) {
				IEditorReference[] editors = pages[x].getEditorReferences();
				for (int z = 0; z < editors.length; z++) {
					IEditorPart editor = editors[z].getEditor(true);
					if (editor != null) {
						count++;
					}
				}
			}
		}
		return count;
	}
}
