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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.contribution.xref.core.XReferenceAdapter;
import org.eclipse.contribution.xref.core.XReferenceProviderDefinition;
import org.eclipse.contribution.xref.internal.ui.actions.XReferenceCustomFilterAction;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaOutlinePage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class XReferenceViewTest extends VisualTestCase {
	
	private int inplaceSize;
	
	protected void setUp() throws Exception {	
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testBug92895() throws Exception {
		IProject project = createPredefinedProject("bug92895");
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
		final ITextEditor editorPart = (ITextEditor)openFileInAspectJEditor(ajFile, false);
		editorPart.setFocus();
		gotoLine(4);
		moveCursorRight(37);
		waitForJobsToComplete();
		
		XRefContainsInputDisplayHelper ds = new XRefContainsInputDisplayHelper();
		ds.waitForCondition(Display.getCurrent(), 5000);
		
		assertTrue("reference source for XRef view should exist",xrefSourceExists(xrefView));
		
		// remove the ":" from the pointcut definition
		postKey(SWT.DEL);
		postKey(SWT.DEL);
		// save file by using "Ctrl+S"
		postKeyDown(SWT.CTRL);
		postKey('s');
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

		ds.waitForCondition(Display.getCurrent(), 5000);
		
		// xref view should show the xreferences
		assertTrue("reference source for XRef view should exist",xrefSourceExists(xrefView));
	}

	public void testBug98319() throws Exception {
		IProject project = createPredefinedProject("bug98319");
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
		
		IResource res2 = project.findMember("test.txt");
		if (res2 == null || !(res2 instanceof IFile)) {
			fail("test.txt file not found.");
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
		
		XRefContainsInputDisplayHelper ds = new XRefContainsInputDisplayHelper();
		ds.waitForCondition(Display.getCurrent(), 5000);
		
		// The cross reference view should contain something
		assertTrue("reference source for XRef view should exist",xrefSourceExists(xrefView));
		
		// switch to the text file
		ITextEditor defaultEditorPart2 = (ITextEditor)openFileInDefaultEditor(textFile,true);
		defaultEditorPart2.setFocus();
		waitForJobsToComplete();
		
		// the cross reference view should be cleared
		XRefContainsNothingDisplayHelper ds2 = new XRefContainsNothingDisplayHelper();
		ds2.waitForCondition(Display.getCurrent(), 5000);
		// get hold of the xref view and check that it hasn't got
		// any contents - if it does have contents then the corresponding
		// source/resource should exist - this was the cause of bug 92895
		TreeViewer treeViewer = xrefView.getTreeViewer();
		assertNotNull("xref view should have non null treeviewer",treeViewer);
		Object obj = treeViewer.getInput();
		assertNull("tree viewer shouldn't contain anything",obj);
				
		defaultEditorPart.close(false);
		defaultEditorPart2.close(false);
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
	
	public void checkProvidersAgree(XReferenceCustomFilterAction xrefAction) {
		// If any providers return Lists from getCheckedFilters(), they should all agree on the stored Lists
		XReferenceProviderDefinition contributingProviderDefinition = null;
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			if (provider.getCheckedFilters() != null || provider.getCheckedInplaceFilters() != null) {
				if (contributingProviderDefinition == null){
					contributingProviderDefinition = provider;
//					inplaceSize = contributingProviderDefinition.getCheckedInplaceFilters().size();
				} else {
					assertTrue("Provider 'checked' Lists do not match",
							provider.getCheckedFilters().equals(contributingProviderDefinition.getCheckedFilters()));
					assertTrue("Provider 'checkedInplace' Lists do not match",
							provider.getCheckedInplaceFilters().equals(contributingProviderDefinition.getCheckedInplaceFilters())
							&& provider.getCheckedInplaceFilters().size() == inplaceSize);
				}
			} else {
				contributingProviderDefinition = provider;
			}
		}		
	}

	public void testSelectAll() throws CoreException {
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing");
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		XReferenceCustomFilterAction xrefAction = (XReferenceCustomFilterAction)xrefView.getCustomFilterAction();
		waitForJobsToComplete();
		
		checkProvidersAgree(xrefAction);

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

		checkProvidersAgree(xrefAction);
		

		TreeViewer treeViewer = xrefView.getTreeViewer();
//		assertTrue("xref view should have non null treeviewer",treeViewer.getInput());
		Object obj = treeViewer.getInput();
		//if (obj != null) {
		//	assertTrue("IAspectJElement should exist",xrefSourceExists(xrefView));
		assertNull("tree viewer shouldn't contain anything",obj);
		
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				// Comparing the number of selected items with the populating list at this point is ok because repeated entries
				// in the populating list are removed in the constructor of the action
				assertTrue("The number of checked Filtes should equal the number of items in the list", xrefAction.getPopulatingList().size() == provider.getCheckedFilters().size());
			}
		}
	}
	
	public void testDeselectAll() throws CoreException {
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing");
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		XReferenceCustomFilterAction xrefAction = (XReferenceCustomFilterAction)xrefView.getCustomFilterAction();
		waitForJobsToComplete();
		
		checkProvidersAgree(xrefAction);

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

		checkProvidersAgree(xrefAction);
		
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				assertTrue("provider.getCheckedFilters() should be of size() == 0", provider.getCheckedFilters().size() == 0);
			}
		}
		testSelectAll();
	}
	
	public void testRestoreDefaults() throws CoreException {
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing");
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		XReferenceCustomFilterAction xrefAction = (XReferenceCustomFilterAction)xrefView.getCustomFilterAction();
		waitForJobsToComplete();
		
		checkProvidersAgree(xrefAction);

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

		checkProvidersAgree(xrefAction);
		
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				assertTrue("provider.getCheckedFilters() should be of size() == 0", provider.getCheckedFilters().size() == 0);
			}
		}
	}

	// CheckedList should now be empty
	public void testChecking() throws CoreException {
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing");
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		XReferenceCustomFilterAction xrefAction = (XReferenceCustomFilterAction)xrefView.getCustomFilterAction();
		waitForJobsToComplete();
		
		checkProvidersAgree(xrefAction);
		
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

		checkProvidersAgree(xrefAction);

		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				assertTrue("provider.getCheckedFilters() should be of size() == 3", provider.getCheckedFilters().size() == 3);
			}
		}
	}
	
	// CheckedList should now have first three items checked.  Uncheck these...
	public void testUnChecking() throws CoreException {
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing");
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		XReferenceCustomFilterAction xrefAction = (XReferenceCustomFilterAction)xrefView.getCustomFilterAction();
		waitForJobsToComplete();
		
		checkProvidersAgree(xrefAction);
		
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

		checkProvidersAgree(xrefAction);

		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				assertTrue("provider.getCheckedFilters() should be of size() == 0", provider.getCheckedFilters().size() == 0);
			}
		}
	}
	

	// CheckedList should now be empty
	public void testCancelDoesNotUpdate() throws CoreException {
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(XReferenceView.ID);
		if (view == null || !(view instanceof XReferenceView)) {
			fail("xrefView should be showing");
		}
		final XReferenceView xrefView = (XReferenceView)view;
		
		XReferenceCustomFilterAction xrefAction = (XReferenceCustomFilterAction)xrefView.getCustomFilterAction();
		waitForJobsToComplete();
		
		checkProvidersAgree(xrefAction);
		
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

		checkProvidersAgree(xrefAction);

		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				assertTrue("provider.getCheckedFilters() should be of size() == 0", provider.getCheckedFilters().size() == 0);
			}
		}
	}
}
