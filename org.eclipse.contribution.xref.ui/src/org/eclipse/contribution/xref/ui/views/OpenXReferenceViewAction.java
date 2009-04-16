/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.contribution.xref.ui.views;

import org.eclipse.contribution.xref.internal.ui.utils.XRefUIUtils;
import org.eclipse.contribution.xref.ui.XReferenceUIPlugin;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.contentoutline.ContentOutline;

/**
 * Class to activate the Cross References View via the context menu
 * in the outline view and the Navigate/Show In workbench menu
 */
public class OpenXReferenceViewAction implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {

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
        try {
        	IWorkbenchPart activePart = JavaPlugin.getActiveWorkbenchWindow().getActivePage().getActivePart();
    		action.setEnabled(activePart instanceof ContentOutline || activePart instanceof IEditorPart);
        } catch (NullPointerException e) {
            // see bug 271605
            // ignore...workspace is shutting down
        }
	}

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
}
