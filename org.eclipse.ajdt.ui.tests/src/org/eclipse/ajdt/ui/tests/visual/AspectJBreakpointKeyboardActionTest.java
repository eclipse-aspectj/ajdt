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

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests setting breakpoints with the Ctrl+Shift+B keyboard action
 */
public class AspectJBreakpointKeyboardActionTest extends VisualTestCase {

	IProject project;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = createPredefinedProject("Simple AJ Project"); //$NON-NLS-1$
	}
	
	public void testSetBreakpoint(){
		IResource res = project.findMember("src/p2/Aspect.aj"); //$NON-NLS-1$
		if (res == null)
			fail("Required file not found."); //$NON-NLS-1$
		breakpointSetTest((IFile)res);
	}
	
	public void testSetBreakpointInDefaultPackage(){
		IResource res = project.findMember("src/AspectInDefaultPackage.aj"); //$NON-NLS-1$
		if (res == null)
			fail("Required file not found."); //$NON-NLS-1$
		breakpointSetTest((IFile)res);
	}
	
	public void breakpointSetTest(IFile sourcefile){
		
		ITextEditor editorPart = (ITextEditor)openFileInDefaultEditor(sourcefile, false);

		//wait for annotation model to be created
		waitForJobsToComplete();
		
		//toggling breakpoint on line 18 should add it
		setBreakpoint(18, true, sourcefile, editorPart);		
		waitForJobsToComplete();
		
		//toggling breakpoint on line 18 should remove it again
		setBreakpoint(18, true, sourcefile, editorPart);		
		waitForJobsToComplete();			

		//toggling breakpoint on line 21 should have no effect
		setBreakpoint(21, false, sourcefile, editorPart);		
		waitForJobsToComplete();
		
		editorPart.close(false);
	}
	
	private void setBreakpoint(int line, final boolean hasEffect, final IFile file, final ITextEditor editor){
		editor.setFocus();
		gotoLine(line);
		final int numOfMarkers = getNumMarkers(file);
		
		postCtrlShiftB(new DisplayHelper() {
			protected boolean condition() {
				int newNumOfMarkers = getNumMarkers(file);
				boolean ret = (numOfMarkers == newNumOfMarkers) != hasEffect;
				return ret;
			}
		
		});
	
		int newNumOfMarkers = getNumMarkers(file);
		if ((numOfMarkers == newNumOfMarkers) == hasEffect)
			fail(hasEffect?"Could not toggle breakpoint.":"Could set breakpoint in illegal position."); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private void postCtrlShiftB(DisplayHelper dh) {
		postKeyDown(SWT.CTRL);
		postKeyDown(SWT.SHIFT);
		postKeyDown('b');
		dh.waitForCondition(Display.getCurrent(), 5000);
		postKeyUp('b');
		postKeyUp(SWT.SHIFT);
		postKeyUp(SWT.CTRL);	
	}

	protected AbstractMarkerAnnotationModel getAnnotationModel(ITextEditor editor) {
		IDocumentProvider provider = editor.getDocumentProvider();
		IAnnotationModel model = provider.getAnnotationModel(editor
				.getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel) {
			return (AbstractMarkerAnnotationModel) model;
		}
		return null;
	}
	
	protected int getNumMarkers(IResource resource) {
		
			try {

				IMarker[] markers = null;
				if (resource instanceof IFile)
					markers = resource.findMarkers(
							IBreakpoint.BREAKPOINT_MARKER, true,
							IResource.DEPTH_INFINITE);
				else {
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
							.getRoot();
					markers = root.findMarkers(IBreakpoint.BREAKPOINT_MARKER,
							true, IResource.DEPTH_INFINITE);
				}
				
				if (markers != null) {
					return markers.length;
				}
			} catch (CoreException x) {
				AspectJUIPlugin.getDefault().getLog().log(x.getStatus());
			}
		return 0;
	}

}
