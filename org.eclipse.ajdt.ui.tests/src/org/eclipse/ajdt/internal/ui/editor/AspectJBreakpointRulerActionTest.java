/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Luzius Meisser  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests setting breakpoints.
 * TODO: only bug like 71718 that cause RuntimeExceptions are detected.
 * -> add check if really set.
 * 
 * @author Luzius Meisser
 */
public class AspectJBreakpointRulerActionTest extends TestCase {

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
	
	public void testSetBreakpointA(){
		IResource res = project.findMember("src/p1/Main.java");
		if (res == null)
			fail("Required file not found.");
		BreakpointSetTest((IFile)res);
	}
	
	public void testSetBreakpointB(){
		IResource res = project.findMember("src/p2/Aspect.aj");
		if (res == null)
			fail("Required file not found.");
		BreakpointSetTest((IFile)res);
	}
	
	public void testSetBreakpointC(){
		IResource res = project.findMember("src/AspectInDefaultPackage.aj");
		if (res == null)
			fail("Required file not found.");
		BreakpointSetTest((IFile)res);
	}
	
	public void BreakpointSetTest(IFile sourcefile){
		
		ITextEditor editorPart = (ITextEditor)Utils.openFileInEditor(sourcefile, false);

		//wait for annotation model to be created
		Utils.waitForJobsToComplete();
		
		//toggling breakpoint on line 17 should add it
		setBreakpoint(17, true, sourcefile, editorPart);
		
		Utils.waitForJobsToComplete();
		
		//toggling breakpoint on line 17 should remove it agin
		setBreakpoint(17, true, sourcefile, editorPart);
		
		Utils.waitForJobsToComplete();
		
		//toggling breakpoint on line 100 should not be possible
		setBreakpoint(100, false, sourcefile, editorPart);
		
		editorPart.close(false);
	}
	
	private void setBreakpoint(int line, boolean hasEffect, IFile file, ITextEditor editor){
		CompositeRuler rulerInfo= (CompositeRuler) editor.getAdapter(IVerticalRulerInfo.class);
		clickLine(line, rulerInfo);
		int numOfMarkers = getNumOfMarkers(file, editor);
		(new AspectJBreakpointRulerAction(rulerInfo, editor, editor)).run();
		Utils.waitForJobsToComplete();
		
		int newNumOfMarkers = getNumOfMarkers(file, editor);
		//hasBreakpointOnCurrentLine(file, editor);
		if ((numOfMarkers == newNumOfMarkers) == hasEffect)
			fail(hasEffect?"Could not toggle breakpoint.":"Could set breakpoint in illegal position.");
	}
	
	private void clickLine(int desiredline, CompositeRuler rulerInfo){
		int line;
		int inputline = desiredline;
		do{
			line = rulerInfo.toDocumentLineNumber(inputline);
			if (line==-1) {
				rulerInfo.setLocationOfLastMouseButtonActivity(0, inputline);
				return;
			}
			inputline++;// += desiredline - line;
		} while (line != desiredline);
		
		rulerInfo.setLocationOfLastMouseButtonActivity(0, inputline);
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
	
	protected boolean hasBreakpointOnCurrentLine(IResource resource, ITextEditor editor){
		return !getMarkers(resource, editor).isEmpty();
	}
	
	protected int getNumOfMarkers(IResource resource, ITextEditor editor) {
		
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
				JDIDebugUIPlugin.log(x.getStatus());
			}
		return 0;
	}
	
	protected List getMarkers(IResource resource, ITextEditor editor) {

		List breakpoints = new ArrayList();

		IDocumentProvider provider = editor.getDocumentProvider();
		IDocument document = provider.getDocument(editor.getEditorInput());
		
		AbstractMarkerAnnotationModel model = getAnnotationModel(editor);

		if (model != null) {
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
					IBreakpointManager breakpointManager = DebugPlugin
							.getDefault().getBreakpointManager();
					for (int i = 0; i < markers.length; i++) {
						IBreakpoint breakpoint = breakpointManager
								.getBreakpoint(markers[i]);
						if (breakpoint != null
								&& breakpointManager.isRegistered(breakpoint)
								&& includesRulerLine(model
										.getMarkerPosition(markers[i]),
										document, (IVerticalRulerInfo)editor.getAdapter(IVerticalRulerInfo.class)))
							breakpoints.add(markers[i]);
					}
				}
			} catch (CoreException x) {
				JDIDebugUIPlugin.log(x.getStatus());
			}
		}
		return breakpoints;
	}
	
	protected boolean includesRulerLine(Position position, IDocument document, IVerticalRulerInfo rulerInfo) {

		if (position != null) {
			try {
				int markerLine = document.getLineOfOffset(position.getOffset());
				int line = rulerInfo.getLineOfLastMouseButtonActivity();
				if (line == markerLine) {
					return true;
				}
			} catch (BadLocationException x) {
			}
		}

		return false;
	}

}
