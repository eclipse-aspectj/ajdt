/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Ian McGrath - initial version
 ******************************************************************************/

package org.eclipse.ajdt.internal.ui.wizards;

import org.eclipse.ajdt.internal.core.resources.AspectJImages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPage;

public class AspectJProjectWizardExtension extends AspectJProjectWizard {

	private String projectDefaultName;
	
	/**
	 * Used by the test suite to simulate user input to the dialog pages
	 */
	public AspectJProjectWizardExtension() {
		super();
	}

	public void setProjectDefaultName(String name) {
		projectDefaultName = name;
	}
	
	/**
	 * Overwrites addPages to add simulate user input.
	 * The contents is exactly the same as super.addPages()
	 * except from the lines indicated
	 */
	
	public void addPages() {
		fMainPage = new AspectJWizardNewProjectCreationPage("NewAspectjProjectCreationWizard");
		
		fMainPage.setTitle(
			AspectJUIPlugin.getResourceString("NewAspectjProjectCreationWizard.MainPage.title"));
		fMainPage.setDescription(
			AspectJUIPlugin.getResourceString("NewAspectjProjectCreationWizard.MainPage.description"));
		fMainPage.setImageDescriptor(AspectJImages.W_NEW_AJ_PRJ.getImageDescriptor());
		fMainPage.setInitialProjectName(projectDefaultName);						// This line has been added
		addPage(fMainPage);
		IWorkspaceRoot root = AspectJUIPlugin.getWorkspace().getRoot();
		fJavaPage = new NewJavaProjectWizardPage(root, fMainPage);
		//fJavaPage.setDefaultOutputFolder(AspectJPlugin.getWorkspace().getRoot()			// This line has been added
		//.getLocation().append("WizardTestProject" + IPath.SEPARATOR + "OutputFolder"));	// This line has been added
		addPage(fJavaPage);
	}
}
