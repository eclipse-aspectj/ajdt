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
import org.eclipse.contribution.xref.internal.ui.inplace.XReferenceInplaceDialog;
import org.eclipse.contribution.xref.internal.ui.providers.TreeObject;
import org.eclipse.contribution.xref.internal.ui.providers.TreeParent;
import org.eclipse.contribution.xref.internal.ui.utils.XRefUIUtils;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
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
	
}
