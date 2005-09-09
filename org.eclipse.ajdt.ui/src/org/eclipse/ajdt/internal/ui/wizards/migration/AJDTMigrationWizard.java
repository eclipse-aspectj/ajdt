/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards.migration;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.pde.internal.PDE;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * The wizard which handles the migration from AJDT version
 * pre 1.2.0 to AJDT 1.2.0 
 */
public class AJDTMigrationWizard extends Wizard {

	private IntroMigrationPage introPage;
	private RenameFileExtensionsPage fileExtensionsPage;
	private BuilderMigrationPage builderMigrationPage;
	private PluginDependencyMigrationPage pluginDependencyPage;
	private WorkbenchSettingsMigrationPage workbenchSettingsPage;
	private CrossCuttingViewMigrationPage crossCuttingViewPage;

	private List ajProjects = new ArrayList();
	private List ajPluginProjects = new ArrayList();
	
	/** 
	 * Adds the pages to the wizard
	 */
	public void addPages() {
	    setNeedsProgressMonitor(true);
	    findAJProjectsInCurrentWorkspace();
	    
		introPage = new IntroMigrationPage();
		fileExtensionsPage = new RenameFileExtensionsPage(ajProjects);
		builderMigrationPage = new BuilderMigrationPage(ajProjects);
		pluginDependencyPage = new PluginDependencyMigrationPage(ajPluginProjects);
		workbenchSettingsPage = new WorkbenchSettingsMigrationPage();
		crossCuttingViewPage = new CrossCuttingViewMigrationPage();
		
		addPage(introPage);
		addPage(fileExtensionsPage);
		addPage(builderMigrationPage);
		addPage(pluginDependencyPage);
		addPage(workbenchSettingsPage);
		addPage(crossCuttingViewPage);
	}
	
	/** 
	 * Set-up the title.
	 */
	public void init() {
		setWindowTitle(AspectJUIPlugin.getResourceString("AJDTMigrationWizard.title"));  //$NON-NLS-1$
	}
	
	/**
	 * Callback for the "Finish" button of this wizard.
	 * @return boolean Whether finish() for the single page of this wizard was 
	 * successful.
	 */
	public boolean performFinish() {
	   WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) {
			    int total = (27 * ajProjects.size()) + (2 * ajPluginProjects.size());
			    /*
			     * Why (27 * ajProjects.size()) + (2 * ajPluginProjects.size())?
			     * 
			     * builderMigrationPage gets 2 for each project
			     * workbenchSettingsPage gets 5 for each project
			     * fileExtensionsPage gets 20 for each project
			     * 		= 27 for each project 
			     * 
			     * In addition pluginDependencyPage gets 2 for each plug-in project
			     * 
			     */
			    monitor.setTaskName(AspectJUIPlugin.getResourceString("MigratingSettings")); //$NON-NLS-1$
			    monitor.beginTask("", total); //$NON-NLS-1$
			    monitor.subTask(AspectJUIPlugin.getResourceString("MigratingBuilderDependencies")); //$NON-NLS-1$                            		
			    builderMigrationPage.finishPressed(monitor);
			    if(monitor.isCanceled()) {
			    	return;
			    }
				monitor.subTask(AspectJUIPlugin.getResourceString("MigratingPluginDependencies")); //$NON-NLS-1$                            		
				pluginDependencyPage.finishPressed(monitor);
				 if(monitor.isCanceled()) {
			    	return;
			    }
				monitor.subTask(AspectJUIPlugin.getResourceString("MigratingWorkbenchSettings")); //$NON-NLS-1$
				workbenchSettingsPage.finishPressed(ajProjects,monitor);
				 if(monitor.isCanceled()) {
			    	return;
			    }
				monitor.subTask(AspectJUIPlugin.getResourceString("MigratingFileExtensions")); //$NON-NLS-1$
				fileExtensionsPage.finishPressed(monitor);                            		
				monitor.done();
				// Do this last because it interferes with the progress monitor.
				crossCuttingViewPage.finishPressed();
			}
	   };				

		try {
			PlatformUI.getWorkbench().getProgressService().runInUI(PlatformUI.getWorkbench().getProgressService(), op, AspectJPlugin.getWorkspace().getRoot());
		  
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}
		return true;
	}

	private void findAJProjectsInCurrentWorkspace() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if (AspectJPlugin.isAJProject(project)) {
				ajProjects.add(project);
				try {
	                if (project.hasNature(PDE.PLUGIN_NATURE)) {
	                    ajPluginProjects.add(project);
	                }
	            } catch (CoreException e) {
	            }
			}
		}	    
	}
	
	static void setAutoBuilding(boolean state) {
		IWorkspaceDescription workspaceDesc = AspectJPlugin.getWorkspace().getDescription();
		workspaceDesc.setAutoBuilding(state);
		try {
			AspectJPlugin.getWorkspace().setDescription(workspaceDesc);
		}
		catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchMessages.getString("Search.Error.setDescription.title"), SearchMessages.getString("Search.Error.setDescription.message")); //$NON-NLS-2$ //$NON-NLS-1$
		}
	}
	
}
