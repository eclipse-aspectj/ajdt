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
package org.eclipse.ajdt.internal.buildconfig.actions;

import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.ui.buildconfig.DefaultBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IBuildConfiguration;
import org.eclipse.ajdt.ui.buildconfig.IBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IProjectBuildConfigurator;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * This action is triggered from a popup menu on a build configuration
 * file. It makes the target the current build config file for the project.
 */
public class ExportAction implements IWorkbenchWindowActionDelegate {


	private IFile currentlySelectedBuildFile = null;
	private IBuildConfigurator buildConfigurator;

	public ExportAction(){
		buildConfigurator = DefaultBuildConfigurator.getBuildConfigurator();
	}
	
	
	/**
	 *  Executed when button clicked or popup menu "Select this build file" clicked
	 */
	public void run(IAction action) {

		if (currentlySelectedBuildFile != null){
			IProjectBuildConfigurator pbc = buildConfigurator.getProjectBuildConfigurator(currentlySelectedBuildFile.getProject());
			if (pbc != null){
				IBuildConfiguration bc = pbc.getBuildConfiguration(currentlySelectedBuildFile);
				if (bc != null){
					((BuildConfiguration)bc).writeLstFile();
				}
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