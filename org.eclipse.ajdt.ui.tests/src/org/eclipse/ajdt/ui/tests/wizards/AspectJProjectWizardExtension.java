/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ian McGrath - initial version
 * 	Sian January - updated when wizard was 
 * 		updated to new Java project wizard style (bug 78264)
 ******************************************************************************/

package org.eclipse.ajdt.ui.tests.wizards;

import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.internal.ui.wizards.AspectJProjectWizard;
import org.eclipse.jdt.internal.ui.wizards.JavaProjectWizardSecondPage;
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
		fFirstPage.setTitle(UIMessages.NewAspectJProject_CreateAnAspectJProject);
		fFirstPage.setDescription(UIMessages.NewAspectJProject_CreateAnAspectJProjectDescription);
        addPage(fFirstPage);
        fSecondPage= new JavaProjectWizardSecondPage(fFirstPage);
        fSecondPage.setTitle(UIMessages.NewAspectJProject_BuildSettings);
        fSecondPage.setDescription(UIMessages.NewAspectJProject_BuildSettingsDescription);
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
