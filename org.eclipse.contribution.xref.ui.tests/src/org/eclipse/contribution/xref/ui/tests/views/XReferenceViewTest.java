/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.ui.tests.views;

import junit.framework.TestCase;

import org.eclipse.contribution.xref.core.XReferenceAdapter;
import org.eclipse.contribution.xref.core.tests.AdaptableObject;
import org.eclipse.contribution.xref.internal.ui.providers.TreeObject;
import org.eclipse.contribution.xref.internal.ui.providers.TreeParent;
import org.eclipse.contribution.xref.internal.ui.providers.XReferenceContentProvider;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * @author hawkinsh
 *  
 */
public class XReferenceViewTest extends TestCase {

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// helper method to return the current active page
	private IWorkbenchPage getPage() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		return window.getActivePage();
	}

	// simple test to ensure view has appeared in workspace
	public void testShowHide() throws PartInitException {
		IViewPart view =
			getPage().showView(
				XReferenceView.ID);
		getPage().hideView(view);
	}

	// tests for XReferenceView.TreeObject
	// pretty basic stuff...
	public void testTreeObject() {
		TreeObject to = new TreeObject("name"); //$NON-NLS-1$

		assertEquals("name", to.getName()); //$NON-NLS-1$
		assertNull(to.getAdapter(Object.class));
		to.setData(this);
		assertEquals(this, to.getData());

		TreeParent tp = new TreeParent("pname"); //$NON-NLS-1$
		to.setParent(tp);
		assertEquals(tp, to.getParent());
	}

	// test for TreeParent, basic stuff again
	public void testTreeParent() {
		TreeParent tp = new TreeParent("pname"); //$NON-NLS-1$
		assertFalse(tp.hasChildren());

		TreeObject to = new TreeObject("name"); //$NON-NLS-1$

		tp.addChild(to);
		assertTrue(tp.hasChildren());
		assertEquals(tp, to.getParent());
		TreeObject[] children = tp.getChildren();
		assertEquals(1, children.length);
		assertEquals(to, children[0]);

		TreeObject to2 = new TreeObject("name2"); //$NON-NLS-1$
		tp.addChild(to2);
		children = tp.getChildren();
		assertEquals(2, children.length);

		tp.removeChild(to);
		children = tp.getChildren();
		assertEquals(1, children.length);
		assertEquals(to2, children[0]);
	}

	public void testViewContentProvider() {
		XReferenceContentProvider viewContentProvider =
			new XReferenceContentProvider();

		TreeParent parent =
			new TreeParent("parent-name"); //$NON-NLS-1$
		TreeObject child1 =
			new TreeObject("child1-name"); //$NON-NLS-1$
		TreeObject child2 =
			new TreeObject("child2-name"); //$NON-NLS-1$

		parent.addChild(child1);
		parent.addChild(child2);

		Object[] elements = viewContentProvider.getElements(parent);
		assertEquals(2, elements.length);
		assertTrue(
			((elements[0] == child1) && (elements[1] == child2))
				|| ((elements[1] == child1) && (elements[0] == child2)));

		elements = viewContentProvider.getElements(child1);
		assertEquals(0, elements.length);

		assertNull(viewContentProvider.getParent(parent));
		assertEquals(parent, viewContentProvider.getParent(child2));
		assertTrue(viewContentProvider.hasChildren(parent));
		assertFalse(viewContentProvider.hasChildren(child1));

		Object[] children = viewContentProvider.getChildren(parent);
		assertEquals(2, children.length);
		assertTrue(
			((children[0] == child1) && (children[1] == child2))
				|| ((children[1] == child1) && (children[0] == child2)));

		children = viewContentProvider.getElements(child1);
		assertEquals(0, children.length);
	}

	// test view content provider - real input
	public void testViewContentProviderLive() {
		XReferenceContentProvider viewContentProvider =
			new XReferenceContentProvider();

		// as soon as we set some input, the tree contents is live
		assertEquals(0, viewContentProvider.getElements(null).length);
		viewContentProvider.inputChanged(null, null, null);
		assertEquals(0, viewContentProvider.getElements(null).length);

		// if the input is not an IXReferenceAdapter, we ignore it
		viewContentProvider.inputChanged(null, null, new String("aha!")); //$NON-NLS-1$
		assertEquals(0, viewContentProvider.getElements(null).length);

		// add a XReference adapter with no XReference
		AdaptableObject source = new AdaptableObject();
		XReferenceAdapter xra = new XReferenceAdapter(source);
		viewContentProvider.inputChanged(null, null, xra);
		Object[] es = viewContentProvider.getElements(xra);
		assertEquals(1, es.length);
		assertEquals(source, ((TreeParent) es[0]).getData());
		assertFalse(viewContentProvider.hasChildren(es[0]));

		// add a XReference provider with attributes
		TestXRefClass testClass = new TestXRefClass();
		XReferenceAdapter xra2 = new XReferenceAdapter(testClass);
		assertEquals(2, xra2.getXReferences().size());
		viewContentProvider.inputChanged(null, xra, xra2);
		es = viewContentProvider.getElements(xra2);
		assertEquals(1, es.length);
		assertEquals(testClass, ((TreeParent) es[0]).getData());
		assertTrue(viewContentProvider.hasChildren(es[0]));
		assertEquals(2, viewContentProvider.getChildren(es[0]).length);
		Object[] ch = viewContentProvider.getChildren(es[0]);
		String n1 = ((TreeParent) ch[0]).getName();
		String n2 = ((TreeParent) ch[1]).getName();
		assertTrue(
			(n1.equals("extends") && n2.equals("implements")) //$NON-NLS-1$ //$NON-NLS-2$
				|| (n2.equals("extends") && n1.equals("implements"))); //$NON-NLS-1$ //$NON-NLS-2$
		assertNull(
			"No data associated with a XReference node", //$NON-NLS-1$
			((TreeParent) ch[0]).getData());
		assertNull(
			"No data associated with a XReference node", //$NON-NLS-1$
			((TreeParent) ch[1]).getData());
		assertFalse(viewContentProvider.hasChildren(ch[0]));
		assertFalse(viewContentProvider.hasChildren(ch[1]));

		// add a XReference provider with attributes and
		// some related entities
		TestXRefClassWithEntities testClass2 = new TestXRefClassWithEntities();
		XReferenceAdapter xra3 = new XReferenceAdapter(testClass2);
		viewContentProvider.inputChanged(null, xra2, xra3);
		es = viewContentProvider.getElements(xra3);
		ch = viewContentProvider.getChildren(es[0]);
		assertTrue(
			viewContentProvider.hasChildren(ch[0])
				|| viewContentProvider.hasChildren(ch[1]));
		if (((TreeParent) ch[0]).getName().equals("extends")) { //$NON-NLS-1$
			ch = viewContentProvider.getChildren(ch[0]);
		} else {
			ch = viewContentProvider.getChildren(ch[1]);
		}
		assertEquals(
			"test associate", //$NON-NLS-1$
			((TreeObject) ch[0]).getData());
	}

}
