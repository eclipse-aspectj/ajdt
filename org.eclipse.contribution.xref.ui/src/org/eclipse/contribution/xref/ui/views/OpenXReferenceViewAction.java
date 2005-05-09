/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.contribution.xref.ui.views;

import org.eclipse.contribution.xref.internal.ui.XReferenceUIPlugin;
import org.eclipse.contribution.xref.ui.utils.XRefUIUtils;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

/**
 * Class to activate the Cross References View via the context menu
 * in the outline view.
 */
public class OpenXReferenceViewAction implements IObjectActionDelegate {

    /* (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
 		try {
		    ISelection currentSelection = XRefUIUtils.getCurrentSelection();
		    IWorkbenchPart workbenchPart = JavaPlugin.getActiveWorkbenchWindow().getActivePage().getActivePart();
			XReferenceUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(XReferenceView.ID);
			XReferenceView xrefView = XReferenceUIPlugin.xrefView;
			xrefView.selectionChanged(workbenchPart,currentSelection);
		} catch (PartInitException e) {
		}        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }
}
