/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.visual;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.contribution.xref.core.XReferenceAdapter;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class XReferenceViewTest extends VisualTestCase {
	
	protected void setUp() throws Exception {	
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testBug92895() throws Exception {
		IProject project = Utils.createPredefinedProject("bug92895");
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing");
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		IResource res = project.findMember("src/pack/A.aj");
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found.");
		} 
		IFile ajFile = (IFile)res;

		// open A.aj and select the pointcut
		ITextEditor editorPart = (ITextEditor)Utils.openFileInAspectJEditor(ajFile, false);
		editorPart.setFocus();
		gotoLine(4);
		moveCursorRight(37);
		Utils.waitForJobsToComplete();
		
		XRefContainsInputDisplayHelper ds = new XRefContainsInputDisplayHelper();
		ds.waitForCondition(Display.getCurrent(), 5000);
		
		assertTrue("reference source for XRef view should exist",xrefSourceExists(xrefView));
		
		// remove the ":" from the pointcut definition
		postKey(SWT.DEL);
		postKey(SWT.DEL);
		// save file by using "Ctrl+S"
		postKeyDown(SWT.CTRL);
		postCharacterKey('s');
		postKeyUp(SWT.CTRL);
		
		XRefContainsNothingDisplayHelper ds2 = new XRefContainsNothingDisplayHelper();
		ds2.waitForCondition(Display.getCurrent(), 5000);
		// get hold of the xref view and check that it hasn't got
		// any contents - if it does have contents then the corresponding
		// source/resource should exist - this was the cause of bug 92895
		TreeViewer treeViewer = xrefView.getTreeViewer();
		assertNotNull("xref view should have non null treeviewer",treeViewer);
		Object obj = treeViewer.getInput();
		//if (obj != null) {
		//	assertTrue("IAspectJElement should exist",xrefSourceExists(xrefView));
		assertNull("tree viewer shouldn't contain anything",obj);
		
		// put the ":" back
		postKeyDown(SWT.SHIFT);
		postCharacterKey(':');
		postKeyUp(SWT.SHIFT);
		
		// save file by using "Ctrl+S"
		postKeyDown(SWT.CTRL);
		postCharacterKey('s');
		postKeyUp(SWT.CTRL);

		ds.waitForCondition(Display.getCurrent(), 5000);
		
		// xref view should show the xreferences
		assertTrue("reference source for XRef view should exist",xrefSourceExists(xrefView));
		
		Utils.deleteProject(project);
	}
	
	private boolean xrefSourceExists(XReferenceView xrefView) {
		TreeViewer treeViewer = xrefView.getTreeViewer();
		assertNotNull("xref view should have non null treeviewer",treeViewer);
		Object obj = treeViewer.getInput();
		assertNotNull("input of xref view should not be null",obj);
		if (!(obj instanceof ArrayList)) {
			fail("input of xrefview should be an arraylist");
		}
		ArrayList al = (ArrayList)obj;
		Object o = al.get(0);
		assertNotNull("contents of xref view should not be null",o);
		if (!(o instanceof XReferenceAdapter)) {
			fail("input should be a XReferenceAdapter");
		}
		Object o1 = ((XReferenceAdapter)o).getReferenceSource();
		assertNotNull("reference source should not be null",o1);
		if (!(o1 instanceof IJavaElement)) {
			fail("input should be an IJavaElement");
		}
		return ((IJavaElement)o1).exists();
	}
	
	private class XRefContainsInputDisplayHelper extends DisplayHelper {		
		protected boolean condition() {
			IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(XReferenceView.ID);
			if (view == null || !(view instanceof XReferenceView)) {
				fail("xrefView should be showing and not null");
			}
			XReferenceView xrefView = (XReferenceView)view;
			TreeViewer tv = xrefView.getTreeViewer();
			Object o = tv.getInput();
			boolean ret = (o != null);
			return ret;
		}		
	}
	
	private class XRefContainsNothingDisplayHelper extends DisplayHelper {		
		protected boolean condition() {
			IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(XReferenceView.ID);
			if (view == null || !(view instanceof XReferenceView)) {
				fail("xrefView should be showing and not null");
			}
			XReferenceView xrefView = (XReferenceView)view;
			TreeViewer tv = xrefView.getTreeViewer();
			Object o = tv.getInput();
			boolean ret = (o == null);
			return ret;
		}		
	}
}
