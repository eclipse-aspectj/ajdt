/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui;


import org.aspectj.ajde.Ajde;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;

/**
 * This pulldown menu displays a version and other information about the 
 * AJDT plugin. This is where we display information we want the user
 * to include in any problem report.
 */
public class AboutMenuItem
	implements IWorkbenchWindowPulldownDelegate {

	IWorkbenchWindow window;

	/**
	 * Constructor for RunTestSuiteMenuSelection.
	 */
	public AboutMenuItem() {
		super();
	}


	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		// this method deliberately left blank
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		String ajdtVersion = AspectJUIPlugin.VERSION;
		String ajdeVersion = Ajde.getDefault().getVersion();
		IWorkbenchWindow iww = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow();
		if (iww != null) {
			Shell shell = iww.getShell();
				MessageDialog.openInformation(
							shell,
							UIMessages.ajAboutDialog,
							"AJDT version: " + ajdtVersion + 
							"\nAJDE version: " + ajdeVersion );
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// this method deliberately left blank
	}
	

	/**
	 * @see IWorkbenchWindowPulldownDelegate#getMenu(Control)
	 */
	public Menu getMenu(Control parent) {
		return null;
	}

}