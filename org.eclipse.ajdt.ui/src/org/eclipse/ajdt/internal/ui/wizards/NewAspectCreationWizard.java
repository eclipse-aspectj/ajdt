/**********************************************************************
 Copyright (c) 2003, 2005 IBM Corporation and others.
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
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.ui.INewWizard;

public class NewAspectCreationWizard extends NewElementWizard implements
		INewWizard {
	private NewAspectWizardPage fPage;

	public NewAspectCreationWizard() {
		super();
		setDefaultPageImageDescriptor(AspectJImages.W_NEW_ASPECT
				.getImageDescriptor());
		setDialogSettings(AspectJUIPlugin.getDefault().getDialogSettings());
		setWindowTitle(UIMessages.NewAspectCreationWizard_title);
	}

	/**
	 * Adds the NewAspectWizardCreationPage
	 */
	public void addPages() {
		setNeedsProgressMonitor(true);
		fPage = new NewAspectWizardPage();
		addPage(fPage);
		fPage.init(getSelection());
	}

	/**
	 * Complete generation of the new file.
	 */
	public boolean performFinish() {
		boolean res = super.performFinish();
		if (res) {
			IResource resource = fPage.getModifiedResource();
			if (resource != null) {
				selectAndReveal(resource);
				openResource((IFile) resource);
			}
		}
		return res;
	}

	protected void finishPage(IProgressMonitor monitor)
			throws InterruptedException, CoreException {
		fPage.createType(monitor); // use the full progress monitor
	}

	public IJavaElement getCreatedElement() {
		return fPage.getCreatedType();
	}
}