/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Ian McGrath - initial version
 * 	Sian January - updated when wizard was 
 * 		updated to new Java project wizard style (bug 78264)
 ******************************************************************************/

package org.eclipse.ajdt.internal.ui.wizards;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.swt.widgets.Composite;


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
	 * Overridden to use JaveProjectWizardFirstPageExtension instead of JavaProjectWizardFirstPage 
	 */
	
	public void addPages() {
        fFirstPage= new JavaProjectWizardFirstPageExtension();
		fFirstPage.setTitle(AspectJUIPlugin.getResourceString("NewAspectJProject.CreateAnAspectJProject"));
		fFirstPage.setDescription(AspectJUIPlugin.getResourceString("NewAspectJProject.CreateAnAspectJProjectDescription"));
        addPage(fFirstPage);
        fSecondPage= new AspectJProjectWizardSecondPage(fFirstPage);
        fSecondPage.setTitle(AspectJUIPlugin.getResourceString("NewAspectJProject.BuildSettings"));
        fSecondPage.setDescription(AspectJUIPlugin.getResourceString("NewAspectJProject.BuildSettingsDescription"));
        addPage(fSecondPage);
    }
	
	/**
	 * Overridden to add simulated user input
	 */
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		 fFirstPage.setName(projectDefaultName);
	     ((JavaProjectWizardFirstPageExtension)fFirstPage).fLocationGroup.fWorkspaceRadio.setSelection(true);
	     ((JavaProjectWizardFirstPageExtension)fFirstPage).fLayoutGroup.fStdRadio.setSelection(true);
	}

	
}
