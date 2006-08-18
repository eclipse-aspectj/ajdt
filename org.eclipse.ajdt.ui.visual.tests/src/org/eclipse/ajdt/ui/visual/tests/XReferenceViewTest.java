/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.ui.visual.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.contribution.xref.core.IXReferenceAdapter;
import org.eclipse.contribution.xref.core.XReferenceAdapter;
import org.eclipse.contribution.xref.core.XReferenceProviderDefinition;
import org.eclipse.contribution.xref.internal.ui.actions.ToggleLinkingAction;
import org.eclipse.contribution.xref.internal.ui.actions.XReferenceCustomFilterAction;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.JavaOutlinePage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class XReferenceViewTest extends VisualTestCase {
		
	protected void setUp() throws Exception {	
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testBug92895() throws Exception {
		IProject project = createPredefinedProject("bug92895"); //$NON-NLS-1$
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing"); //$NON-NLS-1$
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		IResource res = project.findMember("src/pack/A.aj"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found."); //$NON-NLS-1$
		} 
		IFile ajFile = (IFile)res;

		// open A.aj and select the pointcut
		final ITextEditor editorPart = (ITextEditor)openFileInAspectJEditor(ajFile, false);
		editorPart.setFocus();
		gotoLine(4);
		moveCursorRight(37);
		waitForJobsToComplete();

		XRefVisualTestUtils.waitForXRefViewToContainSomething();
		
		assertTrue("reference source for XRef view should exist",xrefSourceExists(xrefView)); //$NON-NLS-1$
		
		// remove the ":" from the pointcut definition
		postKey(SWT.DEL);
		postKey(SWT.DEL);
		// save file by using "Ctrl+S"
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		
		XRefVisualTestUtils.waitForXRefViewToEmpty();

		// get hold of the xref view and check that it hasn't got
		// any contents - if it does have contents then the corresponding
		// source/resource should exist - this was the cause of bug 92895
		TreeViewer treeViewer = xrefView.getTreeViewer();
		assertNotNull("xref view should have non null treeviewer",treeViewer); //$NON-NLS-1$
		Object obj = treeViewer.getInput();
		//if (obj != null) {
		//	assertTrue("IAspectJElement should exist",xrefSourceExists(xrefView));
		assertNull("tree viewer shouldn't contain anything",obj); //$NON-NLS-1$
		
		// put the ":" back
		// Use a Runnable due to problems introduced with the fix for 98547
		Runnable r = new Runnable() {
			public void run() {
				postKeyDown(SWT.SHIFT);
				postKey(':');
				postKeyUp(SWT.SHIFT);
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ie){}
			}
		};
		new Thread(r).start();
		waitForJobsToComplete();
		
		new DisplayHelper() {
			public boolean condition() {
		
				IContentOutlinePage outlinePage = (IContentOutlinePage) editorPart.getAdapter(IContentOutlinePage.class);
				if(outlinePage instanceof JavaOutlinePage) {
					outlinePage.setFocus();
					postKey(SWT.TAB);
					postKey(SWT.TAB);
					ISelection selection = ((JavaOutlinePage)outlinePage).getSelection();
					return !selection.isEmpty();
				} 
				return false;
			}
		}.waitForCondition(Display.getCurrent(), 5000);
		
		editorPart.setFocus();
		// save file by using "Ctrl+S"
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);

		XRefVisualTestUtils.waitForXRefViewToContainSomething();
		
		// xref view should show the xreferences
		assertTrue("reference source for XRef view should exist",xrefSourceExists(xrefView)); //$NON-NLS-1$
	}

	public void testBug92895WithLinkingDisabled() throws Exception {
		
		IProject project = createPredefinedProject("bug92895"); //$NON-NLS-1$
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing"); //$NON-NLS-1$
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		// ensure linking is enabled
		assertTrue("link with editor should be enabled by default",xrefView.isLinkingEnabled()); //$NON-NLS-1$

		IResource res = project.findMember("src/pack/A.aj"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found."); //$NON-NLS-1$
		} 
		IFile ajFile = (IFile)res;

		// open A.aj and select the pointcut
		final ITextEditor editorPart = (ITextEditor)openFileInAspectJEditor(ajFile, false);
		editorPart.setFocus();
		gotoLine(4);
		moveCursorRight(37);
		waitForJobsToComplete();

		XRefVisualTestUtils.waitForXRefViewToContainSomething();
		
		assertTrue("reference source for XRef view should exist",xrefSourceExists(xrefView)); //$NON-NLS-1$
		
		// disable link with editor
		ToggleLinkingAction linkWithEditorAction = (ToggleLinkingAction)xrefView.getToggleLinkWithEditorAction();
		linkWithEditorAction.setChecked(false);
		linkWithEditorAction.run();	
		assertFalse("link with editor should not be enabled",xrefView.isLinkingEnabled()); //$NON-NLS-1$
		
		// remove the ":" from the pointcut definition
		postKey(SWT.DEL);
		postKey(SWT.DEL);
		// save file by using "Ctrl+S"
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		
		XRefVisualTestUtils.waitForXRefViewToEmpty();

		// get hold of the xref view and check that it hasn't got
		// any contents - if it does have contents then the corresponding
		// source/resource should exist - this was the cause of bug 92895
		TreeViewer treeViewer = xrefView.getTreeViewer();
		assertNotNull("xref view should have non null treeviewer",treeViewer); //$NON-NLS-1$
		Object obj = treeViewer.getInput();
		//if (obj != null) {
		//	assertTrue("IAspectJElement should exist",xrefSourceExists(xrefView));
		assertNull("tree viewer shouldn't contain anything",obj); //$NON-NLS-1$
		
		// put the ":" back
		// Use a Runnable due to problems introduced with the fix for 98547
		Runnable r = new Runnable() {
			public void run() {
				postKeyDown(SWT.SHIFT);
				postKey(':');
				postKeyUp(SWT.SHIFT);
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ie){}
			}
		};
		new Thread(r).start();
		waitForJobsToComplete();
		
		new DisplayHelper() {
			public boolean condition() {
		
				IContentOutlinePage outlinePage = (IContentOutlinePage) editorPart.getAdapter(IContentOutlinePage.class);
				if(outlinePage instanceof JavaOutlinePage) {
					outlinePage.setFocus();
					postKey(SWT.TAB);
					postKey(SWT.TAB);
					ISelection selection = ((JavaOutlinePage)outlinePage).getSelection();
					return !selection.isEmpty();
				} 
				return false;
			}
		}.waitForCondition(Display.getCurrent(), 5000);
		
		editorPart.setFocus();
		// save file by using "Ctrl+S"
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);

		XRefVisualTestUtils.waitForXRefViewToContainSomething();
		
		// xref view should show the xreferences
		assertTrue("reference source for XRef view should exist",xrefSourceExists(xrefView)); //$NON-NLS-1$
		
		// set linking enabled (which is the default)
		linkWithEditorAction.setChecked(true);
		linkWithEditorAction.run();	
		assertTrue("link with editor should now be enabled",xrefView.isLinkingEnabled()); //$NON-NLS-1$

	}
	
	public void testBug98319() throws Exception {
		IProject project = createPredefinedProject("bug98319"); //$NON-NLS-1$
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing"); //$NON-NLS-1$
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		IResource res = project.findMember("src/pack/A.aj"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found."); //$NON-NLS-1$
		} 
		IFile ajFile = (IFile)res;
		
		IResource res2 = project.findMember("test.txt"); //$NON-NLS-1$
		if (res2 == null || !(res2 instanceof IFile)) {
			fail("test.txt file not found."); //$NON-NLS-1$
		} 
		IFile textFile = (IFile)res2;
		
		// open the text file
		ITextEditor defaultEditorPart = (ITextEditor)openFileInDefaultEditor(textFile,true);
		defaultEditorPart.setFocus();
		waitForJobsToComplete();
		
		// open A.aj and select the pointcut
		final ITextEditor editorPart = (ITextEditor)openFileInAspectJEditor(ajFile, false);
		editorPart.setFocus();
		gotoLine(4);
		moveCursorRight(37);
		waitForJobsToComplete();
		
		XRefVisualTestUtils.waitForXRefViewToContainSomething();
		
		// The cross reference view should contain something
		assertTrue("reference source for XRef view should exist",xrefSourceExists(xrefView)); //$NON-NLS-1$
		
		// switch to the text file
		ITextEditor defaultEditorPart2 = (ITextEditor)openFileInDefaultEditor(textFile,true);
		defaultEditorPart2.setFocus();
		waitForJobsToComplete();
		
		// the cross reference view should be cleared
		XRefVisualTestUtils.waitForXRefViewToEmpty();
		
		// get hold of the xref view and check that it hasn't got
		// any contents - if it does have contents then the corresponding
		// source/resource should exist - this was the cause of bug 92895
		TreeViewer treeViewer = xrefView.getTreeViewer();
		assertNotNull("xref view should have non null treeviewer",treeViewer); //$NON-NLS-1$
		Object obj = treeViewer.getInput();
		assertNull("tree viewer shouldn't contain anything",obj); //$NON-NLS-1$
				
		defaultEditorPart.close(false);
		defaultEditorPart2.close(false);
	}

	/**
	 * Test selection before the aspect declaration and ensure that
	 * the xref view contains the xrefs for the entire file.
	 */
	public void testBug96313() throws Exception {
		IProject project = createPredefinedProject("bug92895"); //$NON-NLS-1$
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing"); //$NON-NLS-1$
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		IResource res = project.findMember("src/pack/A.aj"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found."); //$NON-NLS-1$
		} 
		IFile ajFile = (IFile)res;

		// open A.aj and select the pointcut
		ITextEditor editorPart = (ITextEditor)openFileInAspectJEditor(ajFile, false);
		editorPart.setFocus();
		gotoLine(2);
		waitForJobsToComplete();
		
		// add a space and save
		postKey(' ');
		// save file by usng "Ctrl+S"
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		waitForJobsToComplete();
		
		new DisplayHelper() {
			public boolean condition() {
				ArrayList contentsOfView = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
				boolean ret = false;
				if (contentsOfView != null) {
					ret = (contentsOfView.size() == 2);
				}
				return ret;
				
			}
		}.waitForCondition(Display.getCurrent(), 5000);
		
		assertTrue("reference source for XRef view should exist",xrefSourceExists(xrefView)); //$NON-NLS-1$

		ArrayList contentsOfView = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
		assertEquals("xref view should be showing two high level nodes",2,contentsOfView.size()); //$NON-NLS-1$

		// check that the top level nodes are "A" and "C1"
		for (Iterator iter = contentsOfView.iterator(); iter.hasNext();) {
			IXReferenceAdapter element = (IXReferenceAdapter) iter.next();
			Object o = element.getReferenceSource();
			assertTrue("reference source should be an IJavaElement, instead it was a " + o.toString(),o instanceof IJavaElement); //$NON-NLS-1$
			boolean correctName = ((IJavaElement)o).getElementName().equals("A") || ((IJavaElement)o).getElementName().equals("C1");  //$NON-NLS-1$//$NON-NLS-2$
			assertTrue("reference source should be either A or C1, it is " + ((IJavaElement)o).getElementName(), correctName); //$NON-NLS-1$
		}
		
		editorPart.close(false);
	}

	
	private boolean xrefSourceExists(XReferenceView xrefView) {
		TreeViewer treeViewer = xrefView.getTreeViewer();
		assertNotNull("xref view should have non null treeviewer",treeViewer); //$NON-NLS-1$
		Object obj = treeViewer.getInput();
		assertNotNull("input of xref view should not be null",obj); //$NON-NLS-1$
		if (!(obj instanceof ArrayList)) {
			fail("input of xrefview should be an arraylist"); //$NON-NLS-1$
		}
		ArrayList al = (ArrayList)obj;
		Object o = al.get(0);
		assertNotNull("contents of xref view should not be null",o); //$NON-NLS-1$
		if (!(o instanceof XReferenceAdapter)) {
			fail("input should be a XReferenceAdapter"); //$NON-NLS-1$
		}
		Object o1 = ((XReferenceAdapter)o).getReferenceSource();
		assertNotNull("reference source should not be null",o1); //$NON-NLS-1$
		if (!(o1 instanceof IJavaElement)) {
			fail("input should be an IJavaElement"); //$NON-NLS-1$
		}
		return ((IJavaElement)o1).exists();
	}
	
	private Set getCheckedList(XReferenceCustomFilterAction xrefAction) {
		Set checkedList = new TreeSet();
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter
				.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter
					.next();
			List checked = provider.getCheckedFilters();;
			if (checked != null) {
				checkedList.addAll(checked);
			}
		}
		return checkedList;
	}
	
	
	// -------------------- tests for the filter ---------------------
	
	public void testSelectAll() throws CoreException {
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing"); //$NON-NLS-1$
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		XReferenceCustomFilterAction xrefAction = (XReferenceCustomFilterAction)xrefView.getCustomFilterAction();
		waitForJobsToComplete();
		
		Set first = getCheckedList(xrefAction);

		Runnable r = new Runnable() {
			public void run() {
				sleep();
				postKey(SWT.TAB);
				postKey(SWT.CR);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		xrefAction.run();
		waitForJobsToComplete();

		Set second = getCheckedList(xrefAction);
		assertTrue("Select all did not increase the number of selected items", second.size() > first.size()); //$NON-NLS-1$

		TreeViewer treeViewer = xrefView.getTreeViewer();
//		assertTrue("xref view should have non null treeviewer",treeViewer.getInput());
		Object obj = treeViewer.getInput();
		//if (obj != null) {
		//	assertTrue("IAspectJElement should exist",xrefSourceExists(xrefView));
		assertNull("tree viewer shouldn't contain anything",obj); //$NON-NLS-1$
		
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				// Comparing the number of selected items with the populating list at this point is ok because repeated entries
				// in the populating list are removed in the constructor of the action
				assertTrue("The number of checked Filters should equal the number of items in the list", xrefAction.getPopulatingList().size() == provider.getCheckedFilters().size()); //$NON-NLS-1$
			}
		}
	}
	
	public void testDeselectAll() throws CoreException {
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing"); //$NON-NLS-1$
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		XReferenceCustomFilterAction xrefAction = (XReferenceCustomFilterAction)xrefView.getCustomFilterAction();
		waitForJobsToComplete();
		
		Set first = getCheckedList(xrefAction);

		Runnable r = new Runnable() {
			public void run() {
				sleep();
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.CR);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		xrefAction.run();
		waitForJobsToComplete();

		Set second = getCheckedList(xrefAction);
		assertTrue("Deselect all did not decrease the number of selected items", second.size() < first.size()); //$NON-NLS-1$
		
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				assertTrue("provider.getCheckedFilters() should be of size() == 0", provider.getCheckedFilters().size() == 0); //$NON-NLS-1$
			}
		}
	}
	
	public void testRestoreDefaults() throws CoreException {
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing"); //$NON-NLS-1$
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		XReferenceCustomFilterAction xrefAction = (XReferenceCustomFilterAction)xrefView.getCustomFilterAction();
		waitForJobsToComplete();
		
		Set first = getCheckedList(xrefAction);

		Runnable r = new Runnable() {
			public void run() {
				sleep();
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.CR);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		xrefAction.run();
		waitForJobsToComplete();

		Set second = getCheckedList(xrefAction);
		assertEquals("Restore defaults changed the number of selected items", second.size(), first.size()); //$NON-NLS-1$
		
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				assertTrue("provider.getCheckedFilters() should be of size() == 0", provider.getCheckedFilters().size() == 0); //$NON-NLS-1$
			}
		}
	}

	// CheckedList should now be empty
	public void testChecking() throws CoreException {
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing"); //$NON-NLS-1$
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		XReferenceCustomFilterAction xrefAction = (XReferenceCustomFilterAction)xrefView.getCustomFilterAction();
		waitForJobsToComplete();
		
		Set first = getCheckedList(xrefAction);
		
		Runnable r = new Runnable() {
			public void run() {
				sleep();
				postKey(' ');
				postKey(SWT.ARROW_DOWN);
				postKey(' ');
				postKey(SWT.ARROW_DOWN);
				postKey(' ');
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		
		xrefAction.run();
		
		waitForJobsToComplete();

		Set second = getCheckedList(xrefAction);
		assertTrue("Selecting 3 items all did not increase the number of selected items by 3", (second.size() - first.size()) == 3); //$NON-NLS-1$

		doUnChecking();
	}
	
	// CheckedList should now have first three items checked.  Uncheck these...
	private void doUnChecking() throws CoreException {
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing"); //$NON-NLS-1$
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		XReferenceCustomFilterAction xrefAction = (XReferenceCustomFilterAction)xrefView.getCustomFilterAction();
		waitForJobsToComplete();
		
		Set first = getCheckedList(xrefAction);
		
		Runnable r = new Runnable() {
			public void run() {
				sleep();
				postKey(' ');
				postKey(SWT.ARROW_DOWN);
				postKey(' ');
				postKey(SWT.ARROW_DOWN);
				postKey(' ');
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		
		xrefAction.run();
		
		waitForJobsToComplete();

		Set second = getCheckedList(xrefAction);
		assertTrue("Deselecting 3 items all did not decrease the number of selected items by 3", (first.size() - second.size()) == 3); //$NON-NLS-1$
	}
	

	// CheckedList should now be empty
	public void testCancelDoesNotUpdate() throws CoreException {
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing"); //$NON-NLS-1$
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		XReferenceCustomFilterAction xrefAction = (XReferenceCustomFilterAction)xrefView.getCustomFilterAction();
		waitForJobsToComplete();
		
		Set first = getCheckedList(xrefAction);
		
		Runnable r = new Runnable() {
			public void run() {
				sleep();
				postKey(' ');
				postKey(SWT.ARROW_DOWN);
				postKey(' ');
				postKey(SWT.ARROW_DOWN);
				postKey(' ');

				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		
		xrefAction.run();
		
		waitForJobsToComplete();

		Set second = getCheckedList(xrefAction);
		assertEquals("Pressing Cancel changed the number of selected items", second.size(), first.size()); //$NON-NLS-1$

		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				assertTrue("provider.getCheckedFilters() should be of size() == 0", provider.getCheckedFilters().size() == 0); //$NON-NLS-1$
			}
		}
	}
}
