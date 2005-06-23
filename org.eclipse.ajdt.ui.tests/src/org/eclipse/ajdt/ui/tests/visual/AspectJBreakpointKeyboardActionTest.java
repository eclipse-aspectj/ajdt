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

import junit.framework.TestCase;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests setting breakpoints with the Ctrl+Shift+B keyboard action
 */
public class AspectJBreakpointKeyboardActionTest extends TestCase {

	IProject project;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = Utils.createPredefinedProject("Simple AJ Project");
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		Utils.deleteProject(project);
	}
	
	public void testSetBreakpoint(){
		IResource res = project.findMember("src/p2/Aspect.aj");
		if (res == null)
			fail("Required file not found.");
		breakpointSetTest((IFile)res);
	}
	
	public void testSetBreakpointInDefaultPackage(){
		IResource res = project.findMember("src/AspectInDefaultPackage.aj");
		if (res == null)
			fail("Required file not found.");
		breakpointSetTest((IFile)res);
	}
	
	public void breakpointSetTest(IFile sourcefile){
		
		ITextEditor editorPart = (ITextEditor)Utils.openFileInDefaultEditor(sourcefile, false);

		//wait for annotation model to be created
		Utils.waitForJobsToComplete();
		
		//toggling breakpoint on line 18 should add it
		setBreakpoint(18, true, sourcefile, editorPart);		
		Utils.waitForJobsToComplete();
		
		//toggling breakpoint on line 18 should remove it agin
		setBreakpoint(18, true, sourcefile, editorPart);		
		Utils.waitForJobsToComplete();			
		
		editorPart.close(false);
	}
	
	private void setBreakpoint(int line, final boolean hasEffect, final IFile file, final ITextEditor editor){
		editor.setFocus();
		VisualTestUtils.gotoLine(line);
		final int numOfMarkers = getNumMarkers(file, editor);
		
		postCtrlShiftB(editor, new DisplayHelper() {
			protected boolean condition() {
				int newNumOfMarkers = getNumMarkers(file, editor);
				boolean ret = (numOfMarkers == newNumOfMarkers) != hasEffect;
				return ret;
			}
		
		});
	
		int newNumOfMarkers = getNumMarkers(file, editor);
		if ((numOfMarkers == newNumOfMarkers) == hasEffect)
			fail(hasEffect?"Could not toggle breakpoint.":"Could set breakpoint in illegal position.");
	}
	
	private void postCtrlShiftB(ITextEditor editor, DisplayHelper dh) {
		Display display = Display.getCurrent();
		
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = SWT.CTRL;
		display.post(event);
		
		event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = SWT.SHIFT;
		display.post(event);
		
		event = new Event();
		event.type = SWT.KeyDown;
		event.character = 'b';
		display.post(event);

		dh.waitForCondition(Display.getCurrent(), 5000);

		event = new Event();
		event.type = SWT.KeyUp;
		event.character = 'b';
		display.post(event);
						
		event = new Event();
		event.type = SWT.KeyUp;
		event.keyCode = SWT.SHIFT;
		display.post(event);
				
		event = new Event();
		event.type = SWT.KeyUp;
		event.keyCode = SWT.CTRL;
		display.post(event);		
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
	
	protected int getNumMarkers(IResource resource, ITextEditor editor) {
		
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
