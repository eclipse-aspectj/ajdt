/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kris De Volder - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


/**
 * 
 * 
 * @author kdvolder
 */
public class EnableDisablePointcutDoctorAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	public EnableDisablePointcutDoctorAction() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		boolean enableTheDoctor = action.isChecked();
		PointcutDoctorUIPlugin.getDefault().setEnabled(enableTheDoctor);
		if (enableTheDoctor)
			MessageDialog.openInformation(window.getShell(), "PointcutDoctor Info",
					"PointcutDoctor is now enabled. \n" +
					"\n" +
					"Note however that the Doctor gathers joinpoint information during AspectJ  builds. " +
					"You will need to rebuild the projects you want information about!");
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow win) {
		this.window = win;
	}
}
