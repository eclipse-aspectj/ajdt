/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards;

import org.eclipse.jface.wizard.Wizard;

import org.eclipse.jdt.internal.ui.wizards.AbstractOpenWizardAction;

public class OpenAJProjectWizardAction extends AbstractOpenWizardAction {

	public OpenAJProjectWizardAction() {
		//WorkbenchHelp.setHelp(this, IJavaHelpContextIds.OPEN_PROJECT_WIZARD_ACTION);
	}
	
	public OpenAJProjectWizardAction(String label, Class[] acceptedTypes) {
		super(label, acceptedTypes, true);
		//WorkbenchHelp.setHelp(this, IJavaHelpContextIds.OPEN_PROJECT_WIZARD_ACTION);
	}
	
	protected Wizard createWizard() { 
		return new AspectJProjectWizard(); 
	}	
	/*
	 * @see AbstractOpenWizardAction#showWorkspaceEmptyWizard()
	 */
	protected boolean checkWorkspaceNotEmpty() {
		return true;
	}

}
