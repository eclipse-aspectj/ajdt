/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.buildconfig.wizards;

import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * This class implements the interface required by the desktop
 * for all 'New' wizards.  This wizard creates Aspect#J Build
 * configuration files.
 */
public class BuildConfigurationFileWizard
	extends Wizard
	implements INewWizard {
	protected IStructuredSelection selection;
	protected IWorkbench workbench;
	private BuildConfigurationCreationPage mainPage;

	/** 
	 * Adds the BuildConfigurationCreationPage
	 */
	public void addPages() {
		mainPage = new BuildConfigurationCreationPage(workbench, selection);
		addPage(mainPage);
	}
	
	/** 
	 * Set-up the title and icon.
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		if(selection instanceof StructuredSelection) {
			if(((StructuredSelection)selection).size() > 0) {
				Object firstObject = ((StructuredSelection)selection).getFirstElement();
				if(firstObject instanceof IResource) {
					IProject project = ((IResource)firstObject).getProject();
					StructuredSelection newSelection = new StructuredSelection(project);
					this.selection = newSelection;
				} else if(firstObject instanceof IJavaElement) {
					IJavaProject project = ((IJavaElement)firstObject).getJavaProject();
					StructuredSelection newSelection = new StructuredSelection(project);
					this.selection = newSelection;
				}
			}
		}
		if(this.selection == null) {
			this.selection = selection;
		}
		setWindowTitle(UIMessages.newConfig); 
		//setDefaultPageImageDescriptor( AspectJImages.getImageDescriptor( AspectJImages.FILE_LST ) );
	}

	
	/** 
	 * Complete generation of the build configuration file.
	 */
	public boolean performFinish() {
		return mainPage.finish();
	}
}