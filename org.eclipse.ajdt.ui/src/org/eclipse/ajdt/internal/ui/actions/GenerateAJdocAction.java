/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Luzius Meisser - adjusted for ajdoc 
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.actions;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.ajdt.ajdocexport.AJdocWizard;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


public class GenerateAJdocAction implements IWorkbenchWindowActionDelegate {

	private ISelection fSelection;
	private Shell fCurrentShell;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		fCurrentShell= window.getShell();
	}

	public void run(IAction action) {
		AJdocWizard wizard= new AJdocWizard();
		IStructuredSelection selection= null;
		if (fSelection instanceof IStructuredSelection) {
			selection= (IStructuredSelection)fSelection;
		} else {
			selection= new StructuredSelection();
		}
		AJdocWizard.openJavadocWizard(wizard, fCurrentShell, selection);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		fSelection= selection;
	}
}
