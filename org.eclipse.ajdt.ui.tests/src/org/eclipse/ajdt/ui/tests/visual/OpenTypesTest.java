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

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;

/**
 *
 */
public class OpenTypesTest extends VisualTestCase {

	public void testOpenTypesDialog() throws Exception {
		IProject project = Utils.createPredefinedProject("Spacewar Example");
		assertTrue("The Spacewar Example project should have been created", project != null);
		try {
			IEditorReference[] editors = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
			assertTrue("There should be no editors open at the start of the test", editors.length == 0);
			
			// 1. Test that a class in a .aj file is found by the JDT types dialog
			
			// Press Ctrl+Shift+T
			postKeyDown(SWT.CTRL);
			postKeyDown(SWT.SHIFT);
			postCharacterKey('t');
			postKeyUp(SWT.SHIFT);
			postKeyUp(SWT.CTRL);
			
			Runnable r = new Runnable(){
				public void run() {
					sleep();
					postString("Display");
					sleep();
					postCharacterKey(SWT.CR);
				}
			};
			new Thread(r).start();
			Utils.waitForJobsToComplete();
			
			editors = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
			assertTrue("There should be one editor open", editors.length == 1);
			IFileEditorInput editorInput = (IFileEditorInput) editors[0].getEditorInput();
			assertTrue("Display.aj should have been opened", editorInput.getFile().getName().equals("Display.aj"));
			
			
			// 2. Test that an aspect in a .aj file is not found by the JDT types dialog
			
			// Press Ctrl+Shift+T
			postKeyDown(SWT.CTRL);
			postKeyDown(SWT.SHIFT);
			postCharacterKey('t');
			postKeyUp(SWT.SHIFT);
			postKeyUp(SWT.CTRL);
			
			r = new Runnable(){
				public void run() {
					sleep();
					postString("GameSynchronization");
					sleep();
					postCharacterKey(SWT.CR);
					sleep();
					// If GameSynchronization wasn't found pressing enter will do nothing 
					// so we now need to press escape to exit the dialog
					postCharacterKey(SWT.ESC);
				}
			};
			new Thread(r).start();
			Utils.waitForJobsToComplete();
			
			editors = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
			assertTrue("There should still only be one editor open", editors.length == 1);
			
			
			// 3. Test that an aspect in a .aj file is found by the AJDT types dialog
		
			// Press Ctrl+Alt+T
			postKeyDown(SWT.CTRL);
			postKeyDown(SWT.ALT);
			postCharacterKey('t');
			postKeyUp(SWT.ALT);
			postKeyUp(SWT.CTRL);
			
			r = new Runnable(){
				public void run() {
					sleep();
					sleep();
					postString("GameSynchronization");
					sleep();
					postCharacterKey(SWT.CR);
				}
			};
			new Thread(r).start();
			Utils.waitForJobsToComplete();
			
			editors = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
			assertTrue("There should be two editors open", editors.length == 2);
			editorInput = (IFileEditorInput) editors[1].getEditorInput();
			assertTrue("GameSynchronization.aj should have been opened", editorInput.getFile().getName().equals("GameSynchronization.aj"));
			
		
		} finally {
			Utils.deleteProject(project);
		}
	}
	
}
