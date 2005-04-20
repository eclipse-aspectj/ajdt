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


import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.pde.internal.PDE;

/**
 * The wizard which handles the migration from AJDT version
 * pre 1.2.0 to AJDT 1.2.0 
 */
public class AJDTMigrationWizard extends Wizard {

	private IntroMigrationPage introPage;
	private RenameFileExtensionsPage fileExtensionsPage;
	private BuilderMigrationPage1 builderMigrationPage1;
	private BuilderMigrationPage2 builderMigrationPage2;
	private PluginDependencyMigrationPage pluginDependencyPage;
	private WorkbenchSettingsMigrationPage workbenchSettingsPage;
	private CrossCuttingViewMigrationPage crossCuttingViewPage;

	private List ajProjects = new ArrayList();
	private List ajPluginProjects = new ArrayList();
	
	/** 
	 * Adds the pages to the wizard
	 */
	public void addPages() {
	    findAJProjectsInCurrentWorkspace();
	    
		introPage = new IntroMigrationPage();
		fileExtensionsPage = new RenameFileExtensionsPage(ajProjects);
		builderMigrationPage1 = new BuilderMigrationPage1(ajProjects);
		builderMigrationPage2 = new BuilderMigrationPage2();
		pluginDependencyPage = new PluginDependencyMigrationPage(ajPluginProjects);
		workbenchSettingsPage = new WorkbenchSettingsMigrationPage();
		crossCuttingViewPage = new CrossCuttingViewMigrationPage();
		
		addPage(introPage);
		addPage(fileExtensionsPage);
		addPage(builderMigrationPage1);
		addPage(builderMigrationPage2);
		addPage(pluginDependencyPage);
		addPage(workbenchSettingsPage);
		addPage(crossCuttingViewPage);
	}
	
	/** 
	 * Set-up the title.
	 */
	public void init() {
		setWindowTitle(AspectJUIPlugin.getResourceString("AJDTMigrationWizard.title")); 
	}
	
	/**
	 * Callback for the "Finish" button of this wizard.
	 * @return boolean Whether finish() for the single page of this wizard was 
	 * successful.
	 */
	public boolean performFinish() {
		builderMigrationPage1.finishPressed();
		builderMigrationPage2.finishPressed(ajProjects);
		pluginDependencyPage.finishPressed();
		workbenchSettingsPage.finishPressed();
		crossCuttingViewPage.finishPressed();
		fileExtensionsPage.finishPressed();
		AspectJUIPlugin.setMigrationWizardHasRun(true);
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

}
