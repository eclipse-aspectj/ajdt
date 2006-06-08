/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sian January - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.markers;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class AJMarkersDialogAction implements IWorkbenchWindowActionDelegate {

	private ISelection fSelection;
		
	public void run(IAction action) {
		if(fSelection instanceof StructuredSelection) {
			Object element = ((StructuredSelection)fSelection).getFirstElement();
			if(element instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) element;
				IResource resource = (IResource) adaptable.getAdapter(IResource.class);
				if(resource != null) {
					Shell shell = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getShell();
					IProject project = resource.getProject();
					AJMarkersDialog dialog = new AJMarkersDialog(shell, project);
					dialog.open();
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = selection;
	}

	public void dispose() {
	
	}

	public void init(IWorkbenchWindow window) {

	}

}
