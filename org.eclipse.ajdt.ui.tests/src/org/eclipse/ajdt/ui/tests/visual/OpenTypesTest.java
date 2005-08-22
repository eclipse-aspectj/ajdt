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

import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.FileEditorInput;

/**
 *
 */
public class OpenTypesTest extends VisualTestCase {

	private IProject project;

	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.ui.tests.visual.VisualTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = createPredefinedProject("Spacewar Example");
		assertTrue("The Spacewar Example project should have been created", project != null);
	}
	
	public void testOpenTypesDialog() throws Exception {	
		IEditorReference[] editors = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		assertTrue("There should be no editors open at the start of the test", editors.length == 0);
		
		// 1. Test that a class in a .aj file is found by the JDT types dialog
		
		// Press Ctrl+Shift+T
		postKeyDown(SWT.CTRL);
		postKeyDown(SWT.SHIFT);
		postKey('t');
		postKeyUp(SWT.SHIFT);
		postKeyUp(SWT.CTRL);
		
		Runnable r = new Runnable(){
			public void run() {
				sleep();
				postString("Display");
				// Allow types to load
				sleep();
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		waitForJobsToComplete();
		
		editors = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		assertTrue("There should be one editor open", editors.length == 1);
		IFileEditorInput editorInput = (IFileEditorInput) editors[0].getEditor(false).getEditorInput();
		assertTrue("Display.aj should have been opened", editorInput.getFile().getName().equals("Display.aj"));
		
		
		// 2. Test that an aspect in a .aj file is not found by the JDT types dialog
		
		// Press Ctrl+Shift+T
		postKeyDown(SWT.CTRL);
		postKeyDown(SWT.SHIFT);
		postKey('t');
		postKeyUp(SWT.SHIFT);
		postKeyUp(SWT.CTRL);
		
		r = new Runnable(){
			public void run() {
				sleep();
				postString("GameSynchronization");
				sleep();
				postKey(SWT.CR);
				sleep();
				// If GameSynchronization wasn't found pressing enter will do nothing 
				// so we now need to press escape to exit the dialog
				postKey(SWT.ESC);
			}
		};
		new Thread(r).start();
		waitForJobsToComplete();
		
		editors = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		assertTrue("There should still only be one editor open", editors.length == 1);
		
		
		// 3. Test that an aspect in a .aj file is found by the AJDT types dialog
	
		// Press Ctrl+Alt+T
		postKeyDown(SWT.CTRL);
		postKeyDown(SWT.ALT);
		postKey('t');
		postKeyUp(SWT.ALT);
		postKeyUp(SWT.CTRL);
		
		r = new Runnable(){
			public void run() {
				sleep();
				// wait for searching
				sleep();
				postString("GameSynchronization");
				sleep();
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		waitForJobsToComplete();
		
		editors = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		assertTrue("There should be two editors open", editors.length == 2);
		editorInput = (IFileEditorInput) editors[1].getEditor(false).getEditorInput();
		assertTrue("GameSynchronization.aj should have been opened", editorInput.getFile().getName().equals("GameSynchronization.aj"));
		
		// 4. Test that a class in a .aj file is found only once by the AJDT types dialog
		
		// Press Ctrl+Alt+T
		postKeyDown(SWT.CTRL);
		postKeyDown(SWT.ALT);
		postKey('t');
		postKeyUp(SWT.ALT);
		postKeyUp(SWT.CTRL);
		
		r = new Runnable(){
			public void run() {
				sleep();
				postString("Display");
				sleep();
				// Select the item under Display
				postKey(SWT.TAB);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		waitForJobsToComplete();
		
		editors = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		assertTrue("There should be three editors open", editors.length == 3);
		editorInput = (IFileEditorInput) editors[2].getEditor(false).getEditorInput();
		assertTrue("Display1.aj should have been opened", editorInput.getFile().getName().equals("Display1.aj"));

			
		// 5. Test that an aspect that appears twice shows 2 options in the bottom box
	
		// Press Ctrl+Alt+T
		postKeyDown(SWT.CTRL);
		postKeyDown(SWT.ALT);
		postKey('t');
		postKeyUp(SWT.ALT);
		postKeyUp(SWT.CTRL);
		
		r = new Runnable(){
			public void run() {
				sleep();
				postString("SpaceObjectPainting");
				sleep();
				// Select Display2 in the bottom box
				postKey(SWT.TAB);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		waitForJobsToComplete();
		
		editors = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		assertTrue("There should be four editors open", editors.length == 4);
		editorInput = (IFileEditorInput) editors[3].getEditor(false).getEditorInput();
		assertTrue("Display2.aj should have been opened", editorInput.getFile().getName().equals("Display2.aj"));		
	}
	
	
	public void testNewAspectWizard () throws Exception {
		PackageExplorerPart packageExplorer = PackageExplorerPart.getFromActivePerspective();
		packageExplorer.setFocus();
		IResource folder = project.findMember("src/coordination");
		if (!(folder instanceof IFolder)) {
			fail("Folder \"src/coordination\" should have been found in the project");
		}
		packageExplorer.tryToReveal(folder);
		
		postKeyDown(SWT.ALT);
		postKeyDown(SWT.SHIFT);
		postKey('n');
		postKeyUp(SWT.SHIFT);
		postKeyUp(SWT.ALT);
		
		Runnable r = new Runnable(){
			public void run() {
				sleep();
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.CR);
				sleep();
				postString("A1");				
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(' ');
				sleep();
				postString("GameSynchronization");
				sleep();
				postKey(SWT.CR);
				sleep();
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		waitForJobsToComplete();
		
		IEditorReference[] editors = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		assertTrue("There should be one editor open", editors.length == 1);
		IFile newFile = ((FileEditorInput)editors[0].getEditor(false).getEditorInput()).getFile();
		IImportDeclaration[] imports = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(newFile).getImports();
		assertTrue("There should be one import in the new file", imports.length == 1);
		assertTrue("Should have imported GameSynchronization", imports[0].getElementName().equals("spacewar.GameSynchronization"));
		
	}
	
}
