/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.visual;

import java.util.ArrayList;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.contribution.xref.core.XReferenceAdapter;
import org.eclipse.contribution.xref.internal.ui.actions.ToggleLinkingAction;
import org.eclipse.contribution.xref.internal.ui.providers.TreeParent;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class XReferenceViewBuildingTest extends VisualTestCase {
	
	private IProject project;
	private IViewPart view;
	private XReferenceView xrefView;
	
	protected void setUp() throws Exception {	
		super.setUp();
		project = createPredefinedProject("bug84317"); //$NON-NLS-1$
		view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing"); //$NON-NLS-1$
		}
		xrefView = (XReferenceView)view;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/** 
	 * Linking is enabled: then the XRef view contains information about the
	 * currently open editor/file. This means that the view can respond to selections
	 * in the currently open editor (and after builds, update according to the
	 * currently opened file)
	 **/
	public void testLinkingEnabledAndViewUpdating() throws Exception {
		// ensure linking is enabled
		assertTrue("link with editor should be enabled by default",xrefView.isLinkingEnabled()); //$NON-NLS-1$
		
		IResource res = project.findMember("src/pack/C.java"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/C.java file not found."); //$NON-NLS-1$
		} 
		IFile classFile = (IFile)res;

		// open C.java and select the first method
		final ITextEditor editorPart = (ITextEditor)openFileInDefaultEditor(classFile, true);
		editorPart.setFocus();
		gotoLine(6);
		waitForJobsToComplete();

		XRefVisualTestUtils.waitForXRefViewToContainXRefs(2);
		
		// get the contents of the xref view and check that it has
		// one reference source with 2 different cross references
		ArrayList originalContents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,originalContents.size()); //$NON-NLS-1$
		TreeParent originalParentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)originalContents.get(0));
		assertEquals("There should be 2 cross references shown",2,originalParentNode.getChildren().length); //$NON-NLS-1$

		// comment out the currently selected line and save
		postKey('/');
		postKey('/');
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		waitForJobsToComplete();
		
		XRefVisualTestUtils.waitForXRefViewToContainXRefs(1);

		// get the contents of the xref view and check that it has
		// one reference source with 1 cross references
		ArrayList newContents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,newContents.size()); //$NON-NLS-1$
		TreeParent newParentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)newContents.get(0));
		assertEquals("There should be 1 cross reference shown",1,newParentNode.getChildren().length); //$NON-NLS-1$
	}


	/**
	 * Same test as testLinkingEnabledAndViewUpdating(), however
	 * with an aspect changing rather than a class
	 */
	public void testLinkingEnabledAndViewUpdating2() throws Exception {
		// ensure linking is enabled
		assertTrue("link with editor should be enabled by default",xrefView.isLinkingEnabled()); //$NON-NLS-1$
		
		IResource res = project.findMember("src/pack/A.aj"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found."); //$NON-NLS-1$
		} 
		IFile ajFile = (IFile)res;

		// open A.aj and select the pointcut
		final ITextEditor editorPart = (ITextEditor)openFileInDefaultEditor(ajFile, true);
		editorPart.setFocus();
		gotoLine(8);
		waitForJobsToComplete();

		XRefVisualTestUtils.waitForXRefViewToContainXRefs(2);
		
		// get the contents of the xref view and check that it has
		// one reference source with 2 different cross references
		ArrayList originalContents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,originalContents.size()); //$NON-NLS-1$
		TreeParent originalParentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)originalContents.get(0));
		assertEquals("There should be 2 cross references shown",2,originalParentNode.getChildren().length); //$NON-NLS-1$

		
		
		// comment out the currently selected line and save
		postKey('/');
		postKey('/');
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		waitForJobsToComplete();
		
		// HELEN XXX having to force build here because the incremental build doesn't pick everything up
		project.build(IncrementalProjectBuilder.FULL_BUILD,null);
		waitForJobsToComplete();
		
		XRefVisualTestUtils.waitForXRefViewToContainXRefs(1);
		
		// get the contents of the xref view and check that it has
		// one reference source with 1 cross references
		ArrayList newContents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,newContents.size()); //$NON-NLS-1$
		TreeParent newParentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)newContents.get(0));
		assertEquals("There should be 1 cross reference shown",1,newParentNode.getChildren().length); //$NON-NLS-1$
	}
	
	/**
	 * Linking is disabled and the xref view contains info about the currently open
	 * editor/file:
	 * 
	 *    the contents of a method changes, or a piece of advise is deleted (that
	 *    is the current selection): 
	 *    In this case, the view can be refreshed by using the existing contents
	 *    of the view (the changes are picked up via calls to the ContentProvider)
	 *    
	 *    A class is the open file.
	 *    
	 */
	public void testLinkingDisabledAndUpdating1() throws Exception {
		// ensure linking is enabled
		assertTrue("link with editor should be enabled by default",xrefView.isLinkingEnabled()); //$NON-NLS-1$
		
		IResource res = project.findMember("src/pack/C.java"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/C.java file not found."); //$NON-NLS-1$
		} 
		IFile classFile = (IFile)res;

		// open C.java and select the first method
		final ITextEditor editorPart = (ITextEditor)openFileInDefaultEditor(classFile, true);
		editorPart.setFocus();
		gotoLine(6);
		waitForJobsToComplete();

		XRefVisualTestUtils.waitForXRefViewToContainXRefs(2);
		
		// get the contents of the xref view and check that it has
		// one reference source with 2 different cross references
		ArrayList originalContents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,originalContents.size()); //$NON-NLS-1$
		TreeParent originalParentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)originalContents.get(0));
		assertEquals("There should be 2 cross references shown",2,originalParentNode.getChildren().length); //$NON-NLS-1$

		// disable link with editor
		ToggleLinkingAction linkWithEditorAction = (ToggleLinkingAction)xrefView.getToggleLinkWithEditorAction();
		linkWithEditorAction.setChecked(false);
		linkWithEditorAction.run();	
		assertFalse("link with editor should not be enabled",xrefView.isLinkingEnabled()); //$NON-NLS-1$
		
		// comment out the currently selected line and save
		postKey('/');
		postKey('/');
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		waitForJobsToComplete();
		
		XRefVisualTestUtils.waitForXRefViewToContainXRefs(1);

		// get the contents of the xref view and check that it has
		// one reference source with 1 cross references
		ArrayList newContents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,newContents.size()); //$NON-NLS-1$
		TreeParent newParentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)newContents.get(0));
		assertEquals("There should be 1 cross reference shown",1,newParentNode.getChildren().length); //$NON-NLS-1$
		
		// set linking enabled (which is the default)
		linkWithEditorAction.setChecked(true);
		linkWithEditorAction.run();	
		assertTrue("link with editor should now be enabled",xrefView.isLinkingEnabled()); //$NON-NLS-1$
			
	}
	
	/**
	 * Linking is disabled and the xref view contains info about the currently open
	 * editor/file:
	 * 
	 *    the contents of a method changes, or a piece of advise is deleted (that
	 *    is the current selection): 
	 *    In this case, the view can be refreshed by using the existing contents
	 *    of the view (the changes are picked up via calls to the ContentProvider)
	 *    
	 * An aspect is the open file.    
	 *    
	 */
	public void testLinkingDisabledAndUpdating2() throws Exception {
		IResource res = project.findMember("src/pack/A.aj"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found."); //$NON-NLS-1$
		} 
		IFile ajFile = (IFile)res;

		// open A.aj and select the pointcut
		final ITextEditor editorPart = (ITextEditor)openFileInDefaultEditor(ajFile, true);
		editorPart.setFocus();
		gotoLine(8);
		waitForJobsToComplete();

		XRefVisualTestUtils.waitForXRefViewToContainXRefs(2);
		
		// get the contents of the xref view and check that it has
		// one reference source with 2 different cross references
		ArrayList originalContents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,originalContents.size()); //$NON-NLS-1$
		TreeParent originalParentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)originalContents.get(0));
		assertEquals("There should be 2 cross references shown",2,originalParentNode.getChildren().length); //$NON-NLS-1$

		// disable link with editor
		ToggleLinkingAction linkWithEditorAction = (ToggleLinkingAction)xrefView.getToggleLinkWithEditorAction();
		linkWithEditorAction.setChecked(false);
		linkWithEditorAction.run();	
		assertFalse("link with editor should not be enabled",xrefView.isLinkingEnabled()); //$NON-NLS-1$
		
		// comment out the currently selected line and save
		postKey('/');
		postKey('/');
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		waitForJobsToComplete();
		
		XRefVisualTestUtils.waitForXRefViewToContainXRefs(1);

		// get the contents of the xref view and check that it has
		// one reference source with 1 cross references
		ArrayList newContents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,newContents.size()); //$NON-NLS-1$
		TreeParent newParentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)newContents.get(0));
		assertEquals("There should be 1 cross reference shown",1,newParentNode.getChildren().length); //$NON-NLS-1$
		
		// set linking enabled (which is the default)
		linkWithEditorAction.setChecked(true);
		linkWithEditorAction.run();	
		assertTrue("link with editor should now be enabled",xrefView.isLinkingEnabled()); //$NON-NLS-1$
	}
	
	/**
	 * Linking is disabled and the xref view contains info about the currently open
	 * editor/file:
	 * 
	 *    the contents of a method changes or a piece of advise is deleted (that
     *    isn't the current selection):
     *    In this case the view should just refresh the current selection, which
     *    didn't change, so the contents of the view shouldn't change	
	 *    
	 *    A class is the open file.
	 *    
	 */
	public void testLinkingDisabledAndUpdating3() throws Exception {
		// ensure linking is enabled
		assertTrue("link with editor should be enabled by default",xrefView.isLinkingEnabled()); //$NON-NLS-1$
		
		IResource res = project.findMember("src/pack/C.java"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/C.java file not found."); //$NON-NLS-1$
		} 
		IFile classFile = (IFile)res;

		// open C.java and select the first method
		ITextEditor editorPart = (ITextEditor)openFileInDefaultEditor(classFile, true);
		editorPart.setFocus();
		gotoLine(6);
		waitForJobsToComplete();

		XRefVisualTestUtils.waitForXRefViewToContainXRefs(2);
		
		// get the contents of the xref view and check that it has
		// one reference source with 2 different cross references
		ArrayList originalContents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,originalContents.size()); //$NON-NLS-1$
		TreeParent originalParentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)originalContents.get(0));
		assertEquals("There should be 2 cross references shown",2,originalParentNode.getChildren().length); //$NON-NLS-1$

		// disable link with editor
		ToggleLinkingAction linkWithEditorAction = (ToggleLinkingAction)xrefView.getToggleLinkWithEditorAction();
		linkWithEditorAction.setChecked(false);
		linkWithEditorAction.run();	
		assertFalse("link with editor should not be enabled",xrefView.isLinkingEnabled()); //$NON-NLS-1$

		
		// go to another method in the file and comment out
		// a sysout statement
		gotoLine(11);
		postKey('/');
		postKey('/');
		waitForJobsToComplete();
		// need to wait here for the xref view to clear otherwise we're saving
		// too quickly
		XRefVisualTestUtils.waitForXRefViewToEmpty();
		
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		waitForJobsToComplete();
		
		XRefVisualTestUtils.waitForXRefViewToContainXRefs(2);

		// get the contents of the xref view and check that it has
		// one reference source with 2 cross references (since changing the
		// contents of a method currently not showing in the xref view shouldn't
		// change what's showing in the xref view).
		ArrayList newContents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,newContents.size()); //$NON-NLS-1$
		TreeParent newParentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)newContents.get(0));
		assertEquals("There should be 2 cross reference shown",2,newParentNode.getChildren().length); //$NON-NLS-1$
		
		// uncomment line 11
		gotoLine(11);
		postKey(SWT.DEL);
		waitForJobsToComplete();
		gotoLine(11);
		postKey(SWT.DEL);
		waitForJobsToComplete();
		
		// need to wait here for the xref view to clear otherwise we're saving
		// too quickly
		XRefVisualTestUtils.waitForXRefViewToEmpty();
		
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		waitForJobsToComplete();
		
		// set linking enabled (which is the default)
		linkWithEditorAction.setChecked(true);
		linkWithEditorAction.run();	
		assertTrue("link with editor should now be enabled",xrefView.isLinkingEnabled()); //$NON-NLS-1$
		editorPart.setFocus();
		waitForJobsToComplete();
		
		XRefVisualTestUtils.waitForXRefViewToContainXRefs(1);

		// get the contents of the xref view and check that it has
		// one reference source with 1 cross reference (since xref view should
		// now update to current selection).
		ArrayList newContents2 = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,newContents2.size()); //$NON-NLS-1$
		TreeParent newParentNode2 = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)newContents2.get(0));
		assertEquals("There should be 1 cross reference shown",1,newParentNode2.getChildren().length); //$NON-NLS-1$
		
	}
	
	/**
	 * Linking is disabled and the xref view contains info about the currently open
	 * editor/file:
	 * 
	 *    the contents of a method changes or a piece of advise is deleted (that
     *    isn't the current selection):
     *    In this case the view should just refresh the current selection, which
     *    didn't change, so the contents of the view shouldn't change	
	 *    
	 * An aspect is the open file.  
	 */
	public void testLinkingDisabledAndUpdating4() throws Exception {
		// ensure linking is enabled
		assertTrue("link with editor should be enabled by default",xrefView.isLinkingEnabled()); //$NON-NLS-1$
		
		IResource res = project.findMember("src/pack/A.aj"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found."); //$NON-NLS-1$
		} 
		IFile ajFile = (IFile)res;

		// open C.java and select the first method
		ITextEditor editorPart = (ITextEditor)openFileInDefaultEditor(ajFile, true);
		editorPart.setFocus();
		gotoLine(8);
		moveCursorRight(13);
		waitForJobsToComplete();

		XRefVisualTestUtils.waitForXRefViewToContainXRefs(1);
		
		// get the contents of the xref view and check that it has
		// one reference source with 2 different cross references
		ArrayList originalContents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,originalContents.size()); //$NON-NLS-1$
		assertEquals("the reference source should be:  declare warning: \"There should be no printlns\"", //$NON-NLS-1$
				"declare warning: \"There should be no printlns\"", //$NON-NLS-1$
				XRefVisualTestUtils.getReferenceSourceNames(originalContents).get(0));
		TreeParent originalParentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)originalContents.get(0));
		assertEquals("There should be 1 cross references shown",1,originalParentNode.getChildren().length); //$NON-NLS-1$
		assertEquals("declare warning should affect 4 places", //$NON-NLS-1$
				4,
				XRefVisualTestUtils.getNumberOfAffectedPlacesForRel(originalParentNode,0));
		
		// disable link with editor
		ToggleLinkingAction linkWithEditorAction = (ToggleLinkingAction)xrefView.getToggleLinkWithEditorAction();
		linkWithEditorAction.setChecked(false);
		linkWithEditorAction.run();
		waitForJobsToComplete();
		assertFalse("link with editor should not be enabled",xrefView.isLinkingEnabled()); //$NON-NLS-1$
		assertFalse("link with editor action should not be checked", linkWithEditorAction.isChecked()); //$NON-NLS-1$
		
		// go to the advice and comment it out
		gotoLine(12);
		postKey('/');
		postKey('/');
		waitForJobsToComplete();
		gotoLine(13);
		postKey('/');
		postKey('/');
		waitForJobsToComplete();
		gotoLine(14);
		postKey('/');
		postKey('/');
		waitForJobsToComplete();
		// need to wait here for the xref view to clear otherwise we're saving
		// too quickly
		XRefVisualTestUtils.waitForXRefViewToEmpty();
		
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		waitForJobsToComplete();
		
		XRefVisualTestUtils.waitForXRefViewToContainXRefs(2);

		// get the contents of the xref view and check that it has
		// one reference source with 2 cross references (since changing the
		// contents of a method currently not showing in the xref view shouldn't
		// change what's showing in the xref view).
		ArrayList newContents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,newContents.size()); //$NON-NLS-1$
		assertEquals("the reference source should be:  declare warning: \"There should be no printlns\"", //$NON-NLS-1$
				"declare warning: \"There should be no printlns\"", //$NON-NLS-1$
				XRefVisualTestUtils.getReferenceSourceNames(newContents).get(0));
		TreeParent newParentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)newContents.get(0));
		assertEquals("There should be 1 cross reference shown",1,newParentNode.getChildren().length); //$NON-NLS-1$
		assertEquals("declare warning should now affect 3 places", //$NON-NLS-1$
				3,
				XRefVisualTestUtils.getNumberOfAffectedPlacesForRel(newParentNode,0));
		
		// uncomment the advice
		gotoLine(12);
		postKey(SWT.DEL);
		waitForJobsToComplete();
		gotoLine(12);
		postKey(SWT.DEL);
		waitForJobsToComplete();
		gotoLine(13);
		postKey(SWT.DEL);
		waitForJobsToComplete();
		gotoLine(13);
		postKey(SWT.DEL);
		waitForJobsToComplete();
		gotoLine(14);
		postKey(SWT.DEL);
		waitForJobsToComplete();
		gotoLine(14);
		postKey(SWT.DEL);
		waitForJobsToComplete();
		
		// need to wait here for the xref view to clear otherwise we're saving
		// too quickly
		XRefVisualTestUtils.waitForXRefViewToEmpty();
		
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		waitForJobsToComplete();
		
		XRefVisualTestUtils.waitToContainNumberOfAffectedPlacesForNode(0,0,4);

		// get the contents of the xref view and check that it has
		// one reference source with 2 cross references (since changing the
		// contents of a method currently not showing in the xref view shouldn't
		// change what's showing in the xref view).
		ArrayList newContents1 = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,newContents1.size()); //$NON-NLS-1$
		assertEquals("the reference source should be:  declare warning: \"There should be no printlns\"", //$NON-NLS-1$
				"declare warning: \"There should be no printlns\"", //$NON-NLS-1$
				XRefVisualTestUtils.getReferenceSourceNames(newContents1).get(0));
		TreeParent newParentNode1 = XRefVisualTestUtils.getTopLevelNodeInXRefView
				(xrefView,(XReferenceAdapter)newContents1.get(0));
		assertEquals("There should be 1 cross reference shown",1,newParentNode1.getChildren().length); //$NON-NLS-1$
		assertEquals("declare warning should now affect 4 places", //$NON-NLS-1$
				4,
				XRefVisualTestUtils.getNumberOfAffectedPlacesForRel(newParentNode1,0));

		// set linking enabled (which is the default)
		linkWithEditorAction.setChecked(true);
		linkWithEditorAction.run();	
		assertTrue("link with editor should now be enabled",xrefView.isLinkingEnabled()); //$NON-NLS-1$
		editorPart.setFocus();
		waitForJobsToComplete();
		
		XRefVisualTestUtils.waitForXRefViewToContainXRefs(1);

		// get the contents of the xref view and check that it has
		// one reference source with 1 cross reference (since xref view should
		// now update to current selection).
		ArrayList newContents2 = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,newContents2.size()); //$NON-NLS-1$
		System.out.println("TEST: name = " + XRefVisualTestUtils.getReferenceSourceNames(newContents2).get(0)); //$NON-NLS-1$
		assertEquals("the reference source should be:  before", //$NON-NLS-1$
				"before", //$NON-NLS-1$
				XRefVisualTestUtils.getReferenceSourceNames(newContents2).get(0));
		assertEquals("declare warning should now affect 4 places", //$NON-NLS-1$
				4,
				XRefVisualTestUtils.getNumberOfAffectedPlacesForRel(newParentNode1,0));
		TreeParent newParentNode2 = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)newContents2.get(0));
		assertEquals("There should be 2 cross references shown",2,newParentNode2.getChildren().length); //$NON-NLS-1$
		
	}
	

	/**
	 * Linking is disabled and the xref view does not contain info about the currently open
	 * editor/file:
	 * 
	 *    the contents of a method changes or a piece of advise is deleted 
     *    In this case the view should just refresh by using the existing contents
     *    of the view (the changes are picked up via calls to the ContentProvider
	 *    
	 * The xref view shows the contents of a class
	 */
	public void testLinkingIsDisabledAndUpdatingOtherEditor1() throws Exception {
		
		assertTrue("link with editor should be enabled by default",xrefView.isLinkingEnabled()); //$NON-NLS-1$

		IResource res = project.findMember("src/pack/C.java"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/C.java file not found."); //$NON-NLS-1$
		} 
		IFile classFile = (IFile)res;

		// open C.java and navigate to contents of method1()
		final ITextEditor editorPart = (ITextEditor)openFileInDefaultEditor(classFile, true);
		editorPart.setFocus();
		gotoLine(6);
		waitForJobsToComplete();

		XRefVisualTestUtils.waitForXRefViewToContainXRefs(2);
		
		// xref view should contain
		//  - method1()
		//      - (..) field-get(java.io.PrintStream)
		//         - matches declare
		//            - A.declare warning:
        //      - (..) method-call(void pack.C..)
		//         - advised by
		//            - A.before(): p
		ArrayList originalContents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,originalContents.size()); //$NON-NLS-1$
		TreeParent originalParentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)originalContents.get(0));
		assertEquals("There should be 2 cross references shown",2,originalParentNode.getChildren().length); //$NON-NLS-1$
				
		// disable link with editor
		ToggleLinkingAction linkWithEditorAction = (ToggleLinkingAction)xrefView.getToggleLinkWithEditorAction();
		linkWithEditorAction.setChecked(false);
		linkWithEditorAction.run();
		waitForJobsToComplete();
		assertFalse("link with editor should not be enabled",xrefView.isLinkingEnabled()); //$NON-NLS-1$
		assertFalse("link with editor action should not be checked", linkWithEditorAction.isChecked()); //$NON-NLS-1$

		
		// navigate to declare warning in A.aj (via the xref view)
		AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().activate(xrefView);
		waitForJobsToComplete();

		// press "down" three times and then return
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.CR);
		
		// wait for the new editor to be opened
		new DisplayHelper() {

			protected boolean condition() {
				IEditorPart activeEditor = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				boolean ret = (activeEditor != null && !activeEditor.equals(editorPart));
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		final IEditorPart newEditor = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		assertTrue("A.aj should have been opened in the editor",!(newEditor.equals(editorPart)));  //$NON-NLS-1$
		
		// xref view should still contain contents of method1(), however just wait a bit to 
		// give it a chance to change if its going to
		XRefVisualTestUtils.waitForXRefViewToContainSomethingNew(xrefView.getTreeViewer().getInput());
		
		ArrayList newContents1 = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1, //$NON-NLS-1$
				newContents1.size());
		TreeParent newParentNode1 = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,
				(XReferenceAdapter)newContents1.get(0));
		assertEquals("There should be 2 cross references shown",2,newParentNode1.getChildren().length); //$NON-NLS-1$
		
		// comment out the declare warning statement and save		
		newEditor.setFocus();
		gotoLine(8);
		waitForJobsToComplete();
		postKey('/');
		postKey('/');
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		waitForJobsToComplete();
		
		// HELEN XXX having to force build here because the incremental build doesn't pick everything up
		project.build(IncrementalProjectBuilder.FULL_BUILD,null);
		waitForJobsToComplete();
		
		XRefVisualTestUtils.waitToContainNumberOfAffectedPlacesForNode(0,0,1);
		
		// xref view should now contain:
		//  - method1()
		//      - (..) method-call(void pack.C..)
		//         - advised by
		//            - A.before(): p
		ArrayList newContents2 = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view" //$NON-NLS-1$
				,1,newContents2.size());
		TreeParent newParentNode2 = XRefVisualTestUtils.getTopLevelNodeInXRefView(
				xrefView,(XReferenceAdapter)newContents2.get(0));
		assertEquals("There should be 1 cross reference shown",1 //$NON-NLS-1$
				,newParentNode2.getChildren().length);
		
		// uncomment the declare warning statement and save
		gotoLine(8);
		waitForJobsToComplete();
		postKey(SWT.DEL);
		waitForJobsToComplete();
		gotoLine(8);
		waitForJobsToComplete();
		postKey(SWT.DEL);
		waitForJobsToComplete();
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		waitForJobsToComplete();
				
		XRefVisualTestUtils.waitForXRefViewToContainXRefs(2);
		
		// xref view should contain what it originally contained
		ArrayList newContents3 = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1, //$NON-NLS-1$
				newContents3.size());
		TreeParent newParentNode3 = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,
				(XReferenceAdapter)newContents3.get(0));
		assertEquals("There should be 2 cross references shown",2,newParentNode3.getChildren().length); //$NON-NLS-1$

		// set linking enabled (which is the default)
		linkWithEditorAction.setChecked(true);
		linkWithEditorAction.run();	
		assertTrue("link with editor should now be enabled",xrefView.isLinkingEnabled()); //$NON-NLS-1$

		assertEquals("xref view should be showing the xrefs for A", //$NON-NLS-1$
				"A",XRefVisualTestUtils.getReferenceSourceNamesInXRefView(xrefView).get(0)); //$NON-NLS-1$

	}
	
	/**
	 * Linking is disabled and the xref view does not contain info about the currently open
	 * editor/file:
	 * 
	 *    the contents of a method changes or a piece of advise is deleted 
     *    In this case the view should just refresh by using the existing contents
     *    of the view (the changes are picked up via calls to the ContentProvider
	 *    
	 * The xref view shows the contents of an aspect 
	 */	
	public void testLinkingIsDisabledAndUpdatingOtherEditor2() throws Exception {
		
		assertTrue("link with editor should be enabled by default",xrefView.isLinkingEnabled()); //$NON-NLS-1$

		IResource res = project.findMember("src/pack/A.aj"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found."); //$NON-NLS-1$
		} 
		IFile classFile = (IFile)res;

		// open A.aj and navigate to before advice
		final ITextEditor editorPart = (ITextEditor)openFileInDefaultEditor(classFile, true);
		editorPart.setFocus();
		gotoLine(13);
		waitForJobsToComplete();

		XRefVisualTestUtils.waitForXRefViewToContainXRefs(2);
		
		// xref view should contain
		//  - before()
		//      - advises
		//         - (..) C: method-call(void ..)
        //      - (..) field-get(java.io.PrintStream)
		//         - matches declare
		//            - A.declare warning:
		ArrayList originalContents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1,originalContents.size()); //$NON-NLS-1$
		TreeParent originalParentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)originalContents.get(0));
		assertEquals("There should be 2 cross references shown",2,originalParentNode.getChildren().length); //$NON-NLS-1$
				
		// disable link with editor
		ToggleLinkingAction linkWithEditorAction = (ToggleLinkingAction)xrefView.getToggleLinkWithEditorAction();
		linkWithEditorAction.setChecked(false);
		linkWithEditorAction.run();
		waitForJobsToComplete();
		assertFalse("link with editor should not be enabled",xrefView.isLinkingEnabled()); //$NON-NLS-1$
		assertFalse("link with editor action should not be checked", linkWithEditorAction.isChecked()); //$NON-NLS-1$

		
		// navigate to the C: method-call in C.java (via the xref view)
		AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().activate(xrefView);
		waitForJobsToComplete();

		// press "down" twice and then return
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.CR);
		
		// wait for the new editor to be opened
		new DisplayHelper() {

			protected boolean condition() {
				IEditorPart activeEditor = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				boolean ret = (activeEditor != null && !activeEditor.equals(editorPart));
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		final IEditorPart newEditor = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		assertTrue("C.java should have been opened in the editor",!(newEditor.equals(editorPart)));  //$NON-NLS-1$
		
		// xref view should still contain contents of before(), however, first wait for
		// a bit to see if the contents does change
		XRefVisualTestUtils.waitForXRefViewToContainSomethingNew(xrefView.getTreeViewer().getInput());
		
		ArrayList newContents1 = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1, //$NON-NLS-1$
				newContents1.size());
		TreeParent newParentNode1 = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,
				(XReferenceAdapter)newContents1.get(0));
		assertEquals("There should be 2 cross references shown",2,newParentNode1.getChildren().length); //$NON-NLS-1$
		
		// comment out the call to method2() and save	
		newEditor.setFocus();
		gotoLine(7);
		waitForJobsToComplete();
		postKey('/');
		postKey('/');
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		waitForJobsToComplete();
		
		// HELEN XXX having to force build here because the incremental build doesn't pick everything up
		project.build(IncrementalProjectBuilder.FULL_BUILD,null);
		waitForJobsToComplete();
		
		XRefVisualTestUtils.waitToContainNumberOfAffectedPlacesForNode(0,0,1);
		
		// xref view should now contain:
		//  - before()
        //      - (..) field-get(java.io.PrintStream)
		//         - matches declare
		//            - A.declare warning:
		ArrayList newContents2 = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view" //$NON-NLS-1$
				,1,newContents2.size());
		TreeParent newParentNode2 = XRefVisualTestUtils.getTopLevelNodeInXRefView(
				xrefView,(XReferenceAdapter)newContents2.get(0));
		assertEquals("There should be 1 cross reference shown",1 //$NON-NLS-1$
				,newParentNode2.getChildren().length);
		
		// uncomment the call to method2()
		gotoLine(7);
		waitForJobsToComplete();
		postKey(SWT.DEL);
		waitForJobsToComplete();
		gotoLine(7);
		waitForJobsToComplete();
		postKey(SWT.DEL);
		waitForJobsToComplete();
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		waitForJobsToComplete();
				
		XRefVisualTestUtils.waitForXRefViewToContainXRefs(2);
		
		// xref view should contain what it originally contained
		ArrayList newContents3 = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("there should be one reference source featured in the xref view",1, //$NON-NLS-1$
				newContents3.size());
		TreeParent newParentNode3 = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,
				(XReferenceAdapter)newContents3.get(0));
		assertEquals("There should be 2 cross references shown",2,newParentNode3.getChildren().length); //$NON-NLS-1$

		// set linking enabled (which is the default)
		linkWithEditorAction.setChecked(true);
		linkWithEditorAction.run();	
		assertTrue("link with editor should now be enabled",xrefView.isLinkingEnabled()); //$NON-NLS-1$

		assertEquals("xref view should be showing the xrefs for method1()", //$NON-NLS-1$
				"method1",XRefVisualTestUtils.getReferenceSourceNamesInXRefView(xrefView).get(0)); //$NON-NLS-1$
			
	}
	
}
