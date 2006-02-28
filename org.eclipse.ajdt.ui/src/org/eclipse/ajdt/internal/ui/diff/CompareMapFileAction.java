/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.diff;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

/**
 * Handles the selection of .ajmap files in the package explorer
 */
public class CompareMapFileAction implements IWorkbenchWindowActionDelegate {

	private ISelection fSelection;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (fSelection == null) {
			return;
		}
		try {
			IViewPart view = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(ChangesView.CROSSCUTTING_VIEW_ID);
			if (view instanceof ChangesView) {
				ChangesView changesView = (ChangesView)view;
				if (fSelection instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection) fSelection;
					if (ss.size() == 1) {
						// compare with current build
						Object element = ss.getFirstElement();
						if (element instanceof IFile) {
							IFile file = (IFile)element;
							IProject project = file.getProject();
							String fromName = file.getName();
							String toName = ChangesView.CURRENT_BUILD;
							changesView.compareProjects(project, fromName, project, toName);
						}
					} else {
						// compare with each other
						Object[] elements = ss.toArray();
						if (elements.length == 2) {
							if ((elements[0] instanceof IFile) &&
									(elements[1] instanceof IFile)) {
								IFile fromFile = (IFile)elements[0];
								IFile toFile = (IFile)elements[1];
								
								// we want to compare the older file with the newer one
								if (fromFile.getLocalTimeStamp() > toFile.getLocalTimeStamp()) {
									// swap files
									IFile tmp = fromFile;
									fromFile = toFile;
									toFile = tmp;
								}
								
								IProject fromProject = fromFile.getProject();
								IProject toProject = toFile.getProject();
								String fromName = fromFile.getName();
								String toName = toFile.getName();
								changesView.compareProjects(fromProject, fromName, toProject, toName);
							}
						}
					}
				}
				changesView.setFocus();
			}
		} catch (PartInitException e) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = selection;
	}
}
