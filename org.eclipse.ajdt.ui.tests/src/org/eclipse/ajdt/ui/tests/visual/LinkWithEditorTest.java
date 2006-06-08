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

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests for the package explorer's link with editor feature
 */
public class LinkWithEditorTest extends VisualTestCase {

	private IProject project;
	private PackageExplorerPart packageExplorer;
	private boolean linkWithEditorSet;
	
	protected void setUp() throws Exception {	
		super.setUp();
		project = createPredefinedProject("Simple AJ Project"); //$NON-NLS-1$
		packageExplorer = PackageExplorerPart.getFromActivePerspective();
		assertNotNull("The Package Explorer View should be open", packageExplorer); //$NON-NLS-1$
		linkWithEditorSet = PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.LINK_PACKAGES_TO_EDITOR);
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		// reset link with editor to what it was when test started
		packageExplorer.setLinkingEnabled(linkWithEditorSet);
	}
	
	public void testPackageExplorerSelection() {
		IResource res = project.findMember("src/p2/Aspect.aj"); //$NON-NLS-1$
		IResource res2 = project.findMember("src/ClassInDefaultPackage.java"); //$NON-NLS-1$
		if (res == null || res2 == null)
			fail("Required files not found."); //$NON-NLS-1$
		
		// Open an aj file in the editor then open a java file on top
		final ITextEditor editor1 = (ITextEditor)openFileInDefaultEditor((IFile)res, true);
		openFileInDefaultEditor((IFile)res2, true);
		assertTrue("Editor for Aspect.aj should not be on top", !isActiveEditor(editor1)); //$NON-NLS-1$
				
		packageExplorer.setFocus();
		
		// Enable link with editor
		packageExplorer.setLinkingEnabled(true);

		// Select the aj file in the package explorer
		IFileEditorInput fInput = (IFileEditorInput) editor1.getEditorInput();
		AJCompilationUnit ajc = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(fInput.getFile());
		packageExplorer.tryToReveal(ajc);	
		waitForJobsToComplete();
	
		// Check that the editor for the aj file is now on top
		assertTrue("Editor for Aspect.aj should be on top", isActiveEditor(editor1)); //$NON-NLS-1$
		// need to close the AJ editor until bug 98261 is fixed
		// otherwise testBug100018 will fail
		editor1.close(false);
	}
	
	public void testEditorSelection() {
		IResource res = project.findMember("src/p2/Aspect.aj"); //$NON-NLS-1$
		IResource res2 = project.findMember("src/ClassInDefaultPackage.java"); //$NON-NLS-1$
		if (res == null || res2 == null)
			fail("Required files not found."); //$NON-NLS-1$
		
		// Open an aj file in the editor then open a java file on top
		final ITextEditor editor1 = (ITextEditor)openFileInDefaultEditor((IFile)res, true);
		openFileInDefaultEditor((IFile)res2, true);
		assertTrue("Editor for Aspect.aj should not be on top", !isActiveEditor(editor1)); //$NON-NLS-1$
		IFileEditorInput fInput = (IFileEditorInput) editor1.getEditorInput();
		AJCompilationUnit ajc = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(fInput.getFile());
				
		// Enable link with editor
		packageExplorer.setLinkingEnabled(true);
		
		if(packageExplorer.getTreeViewer().getSelection() instanceof StructuredSelection) {
			StructuredSelection s = (StructuredSelection)packageExplorer.getTreeViewer().getSelection();
			assertTrue("Aspect should not be selected", !(ajc.equals(s.getFirstElement()))); //$NON-NLS-1$
		} 

		// Bring the aj editor to the front
		IWorkbenchWindow window = editor1.getSite().getWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		page.activate(editor1);
		editor1.setFocus();
		assertTrue("Editor for Aspect.aj should be on top", isActiveEditor(editor1));		 //$NON-NLS-1$
		
		
		if(packageExplorer.getTreeViewer().getSelection() instanceof StructuredSelection) {
			StructuredSelection s = (StructuredSelection)packageExplorer.getTreeViewer().getSelection();
			assertTrue("Aspect.aj should be selected", ajc.equals(s.getFirstElement())); //$NON-NLS-1$
		} else {
			fail("Nothing was selected in the package explorer"); //$NON-NLS-1$
		}
	
		// Check that the editor for the aj file is now on top
		assertTrue("Editor for Aspect.aj should be on top", isActiveEditor(editor1));		 //$NON-NLS-1$
		
		// need to close the AJ editor until bug 98261 is fixed
		// otherwise testBug100018 will fail
		editor1.close(false);
	}

	/**
	 * Is the given editor on top?
	 * @param editor
	 * @return
	 */
	private boolean isActiveEditor(IEditorPart editor) {
		IWorkbenchWindow window = editor.getSite().getWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return false;
		IEditorPart activeEditor = page.getActiveEditor();
		return activeEditor != null && activeEditor.equals(editor);
	}


}
