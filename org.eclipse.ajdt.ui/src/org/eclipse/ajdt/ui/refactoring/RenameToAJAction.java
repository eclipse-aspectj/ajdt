/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.ui.refactoring;

import java.util.Iterator;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.internal.corext.refactoring.changes.RenameResourceChange;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Rename the file extension of a file to .aj.
 */
public class RenameToAJAction implements IObjectActionDelegate {

	private ISelection selection;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
	 *      org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (selection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) selection;
			for (Iterator iter = sel.iterator(); iter.hasNext();) {
				Object object = iter.next();
				if (object instanceof IAdaptable) {

					IResource file = (IResource) ((IAdaptable) object)
							.getAdapter(IResource.class);
					if (file != null) {
						String name = file.getName();
						name = name.substring(0, name.indexOf('.')); //$NON-NLS-1$
						RenameResourceChange change = new RenameResourceChange(
								file, name + ".aj"); //$NON-NLS-1$
						IProgressMonitor monitor = new NullProgressMonitor();
						try {
							change.perform(monitor);
						} catch (CoreException e) {
							AspectJUIPlugin
									.getDefault()
									.getErrorHandler()
									.handleError(
											AspectJUIPlugin
													.getResourceString("Refactoring.ErrorRenamingResource"), //$NON-NLS-1$
											e);
						}
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;

	}

}
