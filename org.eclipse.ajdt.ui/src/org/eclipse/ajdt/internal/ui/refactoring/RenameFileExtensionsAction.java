/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.ui.refactoring;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionDelegate;

/**
 * Wizard used to change the file extensions for classes and aspects in an
 * AspectJ project.
 */
public class RenameFileExtensionsAction implements IActionDelegate {
	
	private ISelection selection;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (selection instanceof StructuredSelection) {
			StructuredSelection struct = (StructuredSelection)selection;
			if (struct.size() > 0) {
				Object o = struct.getFirstElement();
				if (o instanceof IAdaptable) {
					IProject project = (IProject)((IAdaptable)o).getAdapter(IProject.class);
					if(project != null) {
						RenameFileExtensionsDialog dialog = new RenameFileExtensionsDialog(AspectJUIPlugin.getDefault().getDisplay().getActiveShell(), project);
						dialog.open();
					}
				}
				
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
