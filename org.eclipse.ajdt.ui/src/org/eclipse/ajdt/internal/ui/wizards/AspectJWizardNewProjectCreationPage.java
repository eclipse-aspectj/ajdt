/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ian McGrath - added warning message for using existing project structures
 *******************************************************************************/

package org.eclipse.ajdt.internal.ui.wizards;

import java.io.File;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Standard main page for the wizard that creates an AspectJ project resource.
 */
public class AspectJWizardNewProjectCreationPage extends WizardNewProjectCreationPage {
	
	private boolean exactProjectMatch = false;
	private boolean reconstructProject = false;
	private String correctCapitalization;
	
	public AspectJWizardNewProjectCreationPage(String pageName) {
		super(pageName);
	}
	/**
	 * Returns whether this page's controls currently all contain valid 
	 * values and checks if the project structure already exists.
	*/
	protected boolean validatePage() {

		IPath projectSource = getLocationPath();
		IWorkspaceRoot wRoot = AspectJUIPlugin.getWorkspace().getRoot();
		IPath x = wRoot.getLocation();
		exactProjectMatch = false;
		reconstructProject = false;
		
		//Use the default methods if the user has changed the project contents directory
		if(!projectSource.toString().equals(wRoot.getRawLocation().toString())) {
			return super.validatePage();
		}
		
		String projectName = getProjectName();
		correctCapitalization = projectName;
		File projectFile = wRoot.getLocation().append(projectName).toFile();
		File classpath = wRoot.getLocation().append(projectName + IPath.SEPARATOR + ".classpath").toFile();
		File projectDetailsFile = wRoot.getLocation().append(projectName + IPath.SEPARATOR + ".project").toFile();
		File projectDirectory = wRoot.getLocation().toFile();
		boolean projectFolderExists = projectFile.isDirectory();
		boolean classpathExists = classpath.isFile();
		boolean projectFileExists = projectDetailsFile.isFile();
		boolean ProjectInWorkspace = false;

		if(OpSystemIsCaseSensitive()) {
			ProjectInWorkspace = !(wRoot.findMember(projectName) == null);
		}
		else {
			IResource[] workspaceMembers = null;
			try {
				workspaceMembers = wRoot.members();
			} catch(CoreException e) {
				AspectJUIPlugin.logException(e);				
			}
			
			for(int i= 0; i<workspaceMembers.length; i++) {
				if(workspaceMembers[i].getName().equalsIgnoreCase(projectName)) {
					ProjectInWorkspace = true;
				}
			}
		}

		//There is enough data to reconstruct the project
		if (projectFolderExists && classpathExists && projectFileExists && !ProjectInWorkspace) {
			reconstructProject= true;
			setErrorMessage(null);
			setMessage(AspectJUIPlugin.getResourceString("NewAspectjProjectCreationWizard.MainPage.project_exists"),
			        IMessageProvider.WARNING);
			boolean errorSet = true;
			boolean errorValue = true;		
			File nonExactProjectFile = null;
			
			//check if the existing project is an exact match (capitalization of name)
			File workspaceProjects[] = projectDirectory.listFiles();
			for(int i= 0; i<workspaceProjects.length && !exactProjectMatch; i++) {
				if(workspaceProjects[i].getName().equals(projectName) && workspaceProjects[i].isDirectory()) {
					exactProjectMatch = true;
					return exactProjectMatch;	// case sensitive operating systems will always return here
				}
				
				if(workspaceProjects[i].equals(projectFile) && workspaceProjects[i].isDirectory()) {
					nonExactProjectFile = workspaceProjects[i]; // should only ever match once
					correctCapitalization = nonExactProjectFile.getName();
				}
			}
			
			//The user has entered incorrect capitalization
			setErrorMessage(null);
			setMessage(AspectJUIPlugin.getResourceString("NewAspectjProjectCreationWizard.MainPage.project_exists.capitalization"),
					IMessageProvider.WARNING);			
			return true;
		}
		
		// ammends the always case insensitive method of detecting existing projects in the super
		// implementation of validatePage()
		if(ProjectInWorkspace && !projectName.equals("")) {
			setErrorMessage(null);
			setErrorMessage(IDEWorkbenchMessages.getString("WizardNewProjectCreationPage.projectExistsMessage"));
			return false;
		}
		
		return super.validatePage();
		
	}
	
	public boolean hasCorrectCapitalisation() {
		return exactProjectMatch;
	}
	
	public boolean useExistingProjectStructure() {
		return reconstructProject;
	}
	
	public String getCorrectCapitalization() {
		return correctCapitalization;
		
	}
	
	private boolean OpSystemIsCaseSensitive() {
		File testFile1 = AspectJUIPlugin.getWorkspace().getRoot().getLocation().append("testlocation").toFile();
		File testFile2 = AspectJUIPlugin.getWorkspace().getRoot().getLocation().append("TESTLOCATION").toFile();		
		if(testFile1.equals(testFile2))
			return false;
		else
			return true;
	}
	
}
