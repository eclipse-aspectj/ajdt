/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.buildconfigurator;

import org.eclipse.ajdt.buildconfigurator.menu.DynamicBuildConfigurationMenu;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * @author Luzius Meisser
 *
 * Adds the menu and our selection listener to all workbench windows.
 */
public class BCWorkbenchWindowInitializer implements IWindowListener, IStartup {
	
	private BuildConfigurator buildConfigurator;
	private IWorkbench workbench;

	
	/**
	 * @param workbench
	 */
	public BCWorkbenchWindowInitializer() {
		this.workbench = AspectJUIPlugin.getDefault().getWorkbench();
		buildConfigurator = BuildConfigurator.getBuildConfigurator();
		
		workbench.addWindowListener(this);
		IWorkbenchWindow[] wins = workbench.getWorkbenchWindows();
		for (int i=0; i<wins.length; i++){
			this.windowOpened(wins[i]);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowActivated(IWorkbenchWindow window) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowDeactivated(IWorkbenchWindow window) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowClosed(IWorkbenchWindow window) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowOpened(IWorkbenchWindow window) {
		window.getSelectionService().addSelectionListener(buildConfigurator);
		IMenuManager imm = ((WorkbenchWindow)window).getMenuManager();
		imm = imm.findMenuUsingPath("project");
		//in 3M6: immSub.insertAfter("rebuildAll", dbcm);
		imm.insertAfter("buildProject", new DynamicBuildConfigurationMenu());
	}

	public void earlyStartup() {
	}
}
