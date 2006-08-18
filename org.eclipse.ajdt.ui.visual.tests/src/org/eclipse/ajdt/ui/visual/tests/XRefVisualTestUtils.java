/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.ui.visual.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.contribution.xref.core.IXReferenceAdapter;
import org.eclipse.contribution.xref.core.XReferenceAdapter;
import org.eclipse.contribution.xref.internal.ui.inplace.XReferenceInplaceDialog;
import org.eclipse.contribution.xref.internal.ui.providers.TreeObject;
import org.eclipse.contribution.xref.internal.ui.providers.TreeParent;
import org.eclipse.contribution.xref.internal.ui.utils.XRefUIUtils;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;

public class XRefVisualTestUtils {

	public static void waitForXRefViewToContainSomething() {
		new DisplayHelper() {

			protected boolean condition() {
				IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(XReferenceView.ID);
				if (view == null || !(view instanceof XReferenceView)) {
					return false;
				}
				XReferenceView xrefView = (XReferenceView)view;
				TreeViewer tv = xrefView.getTreeViewer();
				Object o = tv.getInput();
				boolean ret = (o != null);
				return ret;
			}	
		
		}.waitForCondition(Display.getCurrent(), 5000);
	}
		
	public static void waitForXRefViewToContainSomethingNew(Object oldTreeInput) {
		final Object oldInput = oldTreeInput;
		new DisplayHelper() {

			protected boolean condition() {
				IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(XReferenceView.ID);
				if (view == null || !(view instanceof XReferenceView)) {
					return false;
				}
				XReferenceView xrefView = (XReferenceView)view;
				TreeViewer tv = xrefView.getTreeViewer();
				Object o = tv.getInput();
				boolean ret = (o != null && !o.equals(oldInput));
				return ret;
			}	
		
		}.waitForCondition(Display.getCurrent(), 5000);
	}
	
	/**
	 * Wait for the Cross References View to contain the given number of 
	 * xrefs from the first top level node. 
	 */
	public static void waitForXRefViewToContainXRefs(int numberOfExpectedXRefs) {
		final int n = numberOfExpectedXRefs;
		new DisplayHelper() {

			protected boolean condition() {
				IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(XReferenceView.ID);
				if (view == null || !(view instanceof XReferenceView)) {
					return false;
				}
				XReferenceView xrefView = (XReferenceView)view;
				ArrayList contents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);
				boolean ret = false;
				if (contents != null) {
					TreeParent parentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)contents.get(0));
					if (parentNode != null) {
						ret = (parentNode.getChildren().length == n);
					}
				}
				return ret;
			}	
		
		}.waitForCondition(Display.getCurrent(), 5000);
	}
	
	public static void waitToContainNumberOfAffectedPlacesForNode(int nodeNumber, int relationship, int numberOfAffectedPlaces) {
		final int r = relationship;
		final int a = numberOfAffectedPlaces;
		final int n = nodeNumber;
		new DisplayHelper() {

			protected boolean condition() {
				IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(XReferenceView.ID);
				if (view == null || !(view instanceof XReferenceView)) {
					return false;
				}
				XReferenceView xrefView = (XReferenceView)view;
				ArrayList contents = XRefVisualTestUtils.getContentsOfXRefView(xrefView);

				boolean ret = false;
				if (contents != null && n < contents.size() ) {
					TreeParent parentNode = XRefVisualTestUtils.getTopLevelNodeInXRefView(xrefView,(XReferenceAdapter)contents.get(n));
					if (parentNode != null && parentNode.getChildren().length > r) {
						TreeObject o = parentNode.getChildren()[r];
						if (o instanceof TreeParent) {
							ret = (((TreeParent)o).getChildren().length == a);
						}
					}
				}
				return ret;
			}	
		
		}.waitForCondition(Display.getCurrent(), 5000);
	}
	
	public static void waitForXRefViewToEmpty() {
		new DisplayHelper() {

			protected boolean condition() {
				IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(XReferenceView.ID);
				if (view == null || !(view instanceof XReferenceView)) {
					return false;
				}
				XReferenceView xrefView = (XReferenceView)view;
				TreeViewer tv = xrefView.getTreeViewer();
				Object o = tv.getInput();
				boolean ret = (o == null);
				return ret;
			}		
		
		}.waitForCondition(Display.getCurrent(), 5000);
	}
	
	/**
	 * Get the list of IXReferenceAdapters which correspond to the
	 * top level nodes in the XReference view. Will only return null
	 * if there has been a problem, therefore users should use an
	 * assertNotNull(..) statement with what is returned from this method.
	 * 
	 * @param xrefView
	 * @return an ArrayList of the contents of the xref view. Each entry 
	 * in the ArrayList is an IXReferenceAdapter
	 */
	public static ArrayList getContentsOfXRefView(XReferenceView xrefView) {
		TreeViewer treeViewer = xrefView.getTreeViewer();
		if (treeViewer == null) {
			return null;
		}
		Object obj = treeViewer.getInput();
		if (!(obj instanceof ArrayList)) {
			return null;
		}
		return (ArrayList) obj;
	}
	
	/**
	 * Get the TreeParent corresponding to the given XReferenceAdapter
	 * Will only return null if there has been a problem, therefore 
	 * users should use an assertNotNull(..) statement with what is 
	 * returned from this method.
	 * 
	 * @param xrefView 
	 * @param xreferenceAdapter
	 * @return TreeParent.
	 */
	public static TreeParent getTopLevelNodeInXRefView(XReferenceView view, XReferenceAdapter adapter) {
		if (!(adapter.getReferenceSource() instanceof JavaElement)) {
			return null;
		}
		JavaElement element = (JavaElement)adapter.getReferenceSource();	
		TreeObject to = XRefUIUtils.getTreeObjectForJavaElement(view.getTreeViewer().getTree().getItems(),element);		
		if (!(to instanceof TreeParent)) {
			return null;
		}
		return (TreeParent)to;
	}
	
	/**
	 * Get the list of IXReferenceAdapters which correspond to the
	 * top level nodes in the XReference view. Will only return null
	 * if there has been a problem, therefore users should use an
	 * assertNotNull(..) statement with what is returned from this method.
	 * 
	 * @param xrefView
	 * @return an ArrayList of the contents of the xref view. Each entry 
	 * in the ArrayList is an IXReferenceAdapter
	 */
	public static ArrayList getContentsOfXRefInplaceDialog(XReferenceInplaceDialog inplaceDialog) {
		TreeViewer treeViewer = inplaceDialog.getTreeViewer();
		if (treeViewer == null) {
			return null;
		}
		Object obj = treeViewer.getInput();
		if (!(obj instanceof ArrayList)) {
			return null;
		}
		return (ArrayList) obj;
	}
	
	/**
	 * Get the TreeParent corresponding to the given XReferenceAdapter
	 * Will only return null if there has been a problem, therefore 
	 * users should use an assertNotNull(..) statement with what is 
	 * returned from this method.
	 * 
	 * @param xrefView 
	 * @param xreferenceAdapter
	 * @return TreeParent.
	 */
	public static TreeParent getTopLevelNodeInInplaceDialog(XReferenceInplaceDialog inplaceDialog, XReferenceAdapter adapter) {
		if (!(adapter.getReferenceSource() instanceof JavaElement)) {
			return null;
		}
		JavaElement element = (JavaElement)adapter.getReferenceSource();	
		TreeObject to = XRefUIUtils.getTreeObjectForJavaElement(inplaceDialog.getTreeViewer().getTree().getItems(),element);		
		if (!(to instanceof TreeParent)) {
			return null;
		}
		return (TreeParent)to;
	}

	public static int getNumberOfAffectedPlacesForRel(TreeParent parentNode, int i) {
		TreeObject[] children = parentNode.getChildren();
		if (children[i] instanceof TreeParent) {
			TreeParent relationship = (TreeParent)children[i];
			return relationship.getChildren().length;
		}
		return 0;
	}
	
	public static ArrayList /* String */ getReferenceSourceNamesInXRefView(XReferenceView xrefView) {
		ArrayList names = new ArrayList();
		ArrayList contents = getContentsOfXRefView(xrefView);
		for (Iterator iter = contents.iterator(); iter.hasNext();) {
			IXReferenceAdapter element = (IXReferenceAdapter) iter.next();
			if (element.getReferenceSource() instanceof JavaElement) {
				names.add(((JavaElement)element.getReferenceSource()).getElementName());
			}
		}
		
		return names;
	}
	
	
	public static List /*String */ getReferenceSourceNames(List /*IXReferenceAdapter */ xrefAdapterList) {
		if (xrefAdapterList == null) {
			return new ArrayList();
		}
		List names = new ArrayList();
		for (Iterator iter = xrefAdapterList.iterator(); iter.hasNext();) {
			IXReferenceAdapter element = (IXReferenceAdapter) iter.next();
			Object o = element.getReferenceSource();
			if (o instanceof IJavaElement) {
				names.add(((IJavaElement)o).getElementName());

			}
		}
		return names;
	}
}
