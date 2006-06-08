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
package org.eclipse.contribution.xref.internal.ui.actions;

import org.eclipse.contribution.xref.core.IXReferenceNode;
import org.eclipse.contribution.xref.internal.ui.providers.TreeObject;
import org.eclipse.contribution.xref.internal.ui.utils.XRefUIUtils;
import org.eclipse.contribution.xref.ui.IDeferredXReference;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;

/**
 * The class which responds to double clicking on a node in the XReferenceView.
 */
public class DoubleClickAction extends Action {

	private Shell shell;
	private TreeViewer viewer;
	
	public DoubleClickAction(Shell shell, TreeViewer viewer) {
		this.shell = shell;
		this.viewer = viewer;
	}
	
	public void run() {
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object sel =
				((IStructuredSelection) selection).getFirstElement();
			if (sel instanceof TreeObject) {
				Object data = ((TreeObject) sel).getData();
				if (data != null) {
				    if (data instanceof IXReferenceNode) {
				        XRefUIUtils.revealInEditor(((IXReferenceNode)data).getJavaElement());  
				    } else if (data instanceof IJavaElement) {
				    	XRefUIUtils.revealInEditor((IJavaElement) data);
					} else if (data instanceof IDeferredXReference) {
						XRefUIUtils.evaluateXReferences((IDeferredXReference) data, viewer, shell);
					} else if (data instanceof IResource) {
						XRefUIUtils.revealInEditor((IResource) data);
					}
				}
			}
			
		}
	}
	
}