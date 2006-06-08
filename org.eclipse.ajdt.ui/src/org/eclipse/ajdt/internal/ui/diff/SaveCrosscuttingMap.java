/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.diff;

import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJProjectModel;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Action to save out the current structure model in a file
 */
public class SaveCrosscuttingMap implements IWorkbenchWindowActionDelegate {
	
	private IProject currentProject;
	
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
		if(currentProject == null) {
			return;
		}		
		String title = UIMessages.savemap_dialog_title;
		String msg = UIMessages.savemap_dialog_message;
		String defaultFileName = UIMessages.savemap_as_default;
		String initial = AJDTUtils.getFreeFileName(
				currentProject, defaultFileName,
				ChangesView.MAP_FILE_EXT);
	
		InputDialog input = new InputDialog(null, title, msg, initial, null);
		input.setBlockOnOpen(true);
		if (input.open() == Window.OK) {
			String mapName = input.getValue();
			if ((mapName != null) && (mapName.length() > 0)) {
				if (!mapName.endsWith(ChangesView.DOT_MAP_FILE_EXT)) {
					mapName += ChangesView.DOT_MAP_FILE_EXT;
				}
				IPath file = currentProject.getFile(mapName).getLocation();
				if (file.toFile().exists()) {
					if (!askUserOverwrite(mapName)) {
						return;
					}
				}
				AJProjectModel pm = AJModel.getInstance().getModelForProject(currentProject);
				pm.saveModel(file);
				try {
					currentProject.getFile(mapName).refreshLocal(0,null);
				} catch (CoreException e) {
				}
			}
		}
	}

	private boolean askUserOverwrite(String fileName) {
		String[] options = {
				UIMessages.BCDialog_Overwrite_yes,
				UIMessages.BCDialog_Overwrite_no };
		String title = UIMessages.BCDialog_Overwrite_title;
		String msg = UIMessages.BCDialog_Overwrite_message.replaceAll("%fileName", fileName); //$NON-NLS-1$

		MessageDialog mdiag = new MessageDialog(null, title, null, msg,
				MessageDialog.QUESTION, options, 1);
		return (mdiag.open() == 0);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		currentProject = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object first = structuredSelection.getFirstElement();
			if (first instanceof IProject) {
				currentProject = (IProject) first;
			} else if (first instanceof IAdaptable) {
				currentProject = (IProject) ((IAdaptable)first).getAdapter(IProject.class);
			}
		}
	}
}
