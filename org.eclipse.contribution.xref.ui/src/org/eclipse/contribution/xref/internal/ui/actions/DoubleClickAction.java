/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.contribution.xref.core.IDeferredXReference;
import org.eclipse.contribution.xref.core.IXReferenceNode;
import org.eclipse.contribution.xref.internal.ui.XReferenceUIPlugin;
import org.eclipse.contribution.xref.internal.ui.inplace.XReferenceInformationControl;
import org.eclipse.contribution.xref.internal.ui.providers.TreeObject;
import org.eclipse.contribution.xref.internal.ui.providers.XReferenceContentProvider;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;

/**
 * The class which responds to double clicking on a node in the XReferenceView.
 */
public class DoubleClickAction extends Action {

	private Shell shell;
	private TreeViewer viewer;
	private Object typeOfView;
	private XReferenceInformationControl infoControl;
	private XReferenceView view;
	
	public DoubleClickAction(Shell shell, TreeViewer viewer, Object typeOfView) {
		this.shell = shell;
		this.viewer = viewer;
		this.typeOfView = typeOfView;
		
		if (typeOfView instanceof XReferenceInformationControl) {
			infoControl = (XReferenceInformationControl) typeOfView;
		} else if (typeOfView instanceof XReferenceView) {
			view = (XReferenceView) typeOfView;
		}
	}
	
	public void run() {
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object sel =
				((IStructuredSelection) selection).getFirstElement();
			Object data = ((TreeObject) sel).getData();
			if (data != null) {
			    if (data instanceof IXReferenceNode) {
			        revealInEditor(((IXReferenceNode)data).getJavaElement());  
			    } else if (data instanceof IJavaElement) {
					revealInEditor((IJavaElement) data);
				} else if (data instanceof IDeferredXReference) {
					evaluateXReferences((IDeferredXReference) data);
				} else if (data instanceof IResource) {
					revealInEditor((IResource) data);
				}
			}
		}
	}

	private void revealInEditor(IJavaElement j) {
		try {
			IEditorPart p = JavaUI.openInEditor(j);
			JavaUI.revealInEditor(p, j);
			// HELEN - commenting out for now.....
//			if (view != null) {
//				if (view.isLinkingEnabled()) {
//					view.getNavigationHistoryActionGroup().nowLeaving(viewer.getInput());
//					viewer.setInput(j.getAdapter(IXReferenceAdapter.class));	
//				}
//			} else if (infoControl != null){
//				viewer.setInput(j.getAdapter(IXReferenceAdapter.class));
//				// ommenting out inplace stuff
//				//infoControl.dispose();
//			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// revealInEditor(IResource) might not work for inplace view
	private void revealInEditor(IResource r) {
		IMarker m;
		try {
			m = r.createMarker(IMarker.MARKER);
			IDE.openEditor(getActiveWorkbenchWindow().getActivePage(), m, true);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	// evaluateXReferences might not work for inplace view 
	private void evaluateXReferences(IDeferredXReference xr) {
		try {
			new ProgressMonitorDialog(shell).run(true, true, xr);
			if (!(viewer.getContentProvider() instanceof XReferenceContentProvider)) {
				return;
			}
			((XReferenceContentProvider)viewer.getContentProvider()).refresh();
			viewer.refresh();
			viewer.expandToLevel(3);
		} catch (InterruptedException intEx) {
			// user cancelled - this is ok...
		} catch (InvocationTargetException invEx) {
			System.err.println(
					"Something nasty here, "
					+ xr
					+ " could not be evaluated: "
					+ invEx);
		}
	}
	
	private IWorkbenchWindow getActiveWorkbenchWindow() {
		return XReferenceUIPlugin
		.getDefault()
		.getWorkbench()
		.getActiveWorkbenchWindow();
	}
}