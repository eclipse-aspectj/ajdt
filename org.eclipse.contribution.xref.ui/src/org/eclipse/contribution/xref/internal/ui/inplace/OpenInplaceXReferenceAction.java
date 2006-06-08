/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.internal.ui.inplace;

import org.eclipse.contribution.xref.internal.ui.utils.XRefUIUtils;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Class to activate the inplace Cross Reference view, via
 * the key binding defined in the plugin.xml.
 */
public class OpenInplaceXReferenceAction implements IWorkbenchWindowActionDelegate {

	XReferenceInplaceDialog xrefDialog;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		xrefDialog = null;
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
		Shell parent = JavaPlugin.getActiveWorkbenchShell();
		xrefDialog = new XReferenceInplaceDialog(parent);

		xrefDialog.setLastSelection(XRefUIUtils.getCurrentSelection());
		xrefDialog.setWorkbenchPart(JavaPlugin.getActiveWorkbenchWindow().getActivePage().getActivePart());		
		xrefDialog.open();		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// Have selected something in the editor - therefore
		// want to close the inplace view if haven't already done so
		if (xrefDialog != null && xrefDialog.isOpen()) {
			xrefDialog.dispose();
			xrefDialog = null; 
		}
	}
	
}
