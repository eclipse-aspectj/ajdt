/**********************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Matt Chapman - initial version
...
**********************************************************************/

package org.eclipse.ajdt.internal.ui.wizards;

import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewAspectWizard
	extends Wizard
	implements INewWizard {
	private IStructuredSelection selection;
	private IWorkbench workbench;
	private NewAspectCreationWizardPage mainPage;

	/** 
	 * Adds the NewAspectWizardCreationPage
	 */
	public void addPages() {
		setNeedsProgressMonitor(true);
		mainPage = new NewAspectCreationWizardPage("page", workbench, selection); //$NON-NLS-1$
		addPage(mainPage);
	}
	
	/** 
	 * Set-up the title and icon.
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;

		setDefaultPageImageDescriptor(AspectJImages.W_NEW_ASPECT.getImageDescriptor());
		setWindowTitle(UIMessages.NewAspectCreationWizard_title);
	}
	/** 
	 * Complete generation of the new file.
	 */
	public boolean performFinish() {
		return mainPage.finish();
	}
}