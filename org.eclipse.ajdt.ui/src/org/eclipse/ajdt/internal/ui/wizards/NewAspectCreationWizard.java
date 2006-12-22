/**********************************************************************
 Copyright (c) 2003, 2005 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
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
	private NewAspectWizardPage fPage = null;

	public NewAspectCreationWizard() {
		super();
		setDefaultPageImageDescriptor(AspectJImages.W_NEW_ASPECT
				.getImageDescriptor());
		setDialogSettings(AspectJUIPlugin.getDefault().getDialogSettings());
		setWindowTitle(UIMessages.NewAspectCreationWizard_title);
	}

	/**
	 * Adds the NewAspectWizardCreationPage. If the page has already been added,
	 * this method does nothing (extra pages can be added by calling
	 * <code> addPage()
	 * </code>
	 */
	public void addPages() {
		setNeedsProgressMonitor(true);

		// Only initialse if the member is currently null - necessary for
		// testing - spyoung
		if (fPage == null) {
			fPage = new NewAspectWizardPage();
			addPage(fPage);
			fPage.init(getSelection());
		}
	}

	/**
	 * Complete generation of the new file, open it in the associated editor,
	 * and open the Cross References view, if desired.
	 */
	public boolean performFinish() {
		boolean result = super.performFinish();
		if (result) {

			IResource resource = fPage.getModifiedResource();
			if (resource != null) {
				selectAndReveal(resource);
				openResource((IFile) resource);
			}
		}

		return result;
	}

	protected void finishPage(IProgressMonitor monitor)
			throws InterruptedException, CoreException {
		fPage.createType(monitor); // use the full progress monitor
	}

	public IJavaElement getCreatedElement() {
		return fPage.getCreatedType();
	}
}