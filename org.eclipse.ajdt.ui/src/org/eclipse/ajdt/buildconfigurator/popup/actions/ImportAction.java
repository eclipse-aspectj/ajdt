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
package org.eclipse.ajdt.buildconfigurator.popup.actions;

import org.eclipse.ajdt.buildconfigurator.BuildConfiguration;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurator;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * This action is triggered from a popup menu on a build configuration
 * file. It makes the target the current build config file for the project.
 */
public class ImportAction implements IWorkbenchWindowActionDelegate {

	private IFile currentlySelectedBuildFile = null;
	private BuildConfigurator buildConfigurator;

	public ImportAction(){
		buildConfigurator = BuildConfigurator.getBuildConfigurator();
	}
	
	
	/**
	 *  Executed when button clicked or popup menu "Select this build file" clicked
	 */
	public void run(IAction action) {

		if (currentlySelectedBuildFile != null){
			ProjectBuildConfigurator pbc = buildConfigurator.getProjectBuildConfigurator(currentlySelectedBuildFile.getProject());
			if (pbc != null){
				Shell shell = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
				new BuildConfiguration(currentlySelectedBuildFile, shell, pbc);
			}
		}
	}

	/**
	 * Selection has changed - if we've selected a build file then remember it
	 * as the new project build config.
	 */
	public void selectionChanged(IAction action, ISelection selection) {

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object first = structuredSelection.getFirstElement();
			if (first instanceof IFile) {
				currentlySelectedBuildFile = (IFile) first;
			}
		}
	}

	/**
	 * From IWorkbenchWindowActionDelegate
	 */
	public void dispose() {}

	/**
	 * From IWorkbenchWindowActionDelegate
	 */
	public void init(IWorkbenchWindow window) {
	}
}