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
package org.eclipse.ajdt.internal.ui.wizards;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
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
	private IStructuredSelection selection;
	private IWorkbench workbench;
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
		this.selection = selection;
		setWindowTitle( AspectJUIPlugin.getResourceString( "newConfig" ) ); 
		//setDefaultPageImageDescriptor( AspectJImages.getImageDescriptor( AspectJImages.FILE_LST ) );
	}
	/** 
	 * Complete generation of the .lst file.
	 */
	public boolean performFinish() {
		return mainPage.finish();
	}
}