/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.test.utils;

import org.eclipse.core.resources.IFolder;
import org.eclipse.ui.wizards.newresource.BasicNewFolderResourceWizard;

/**
 * A class which creates a new Folder via the new file wizard, simulating
 * user input.
 * 
 * @author hawkinsh
 */
public class BasicNewFolderResourceWizardExtension extends
		BasicNewFolderResourceWizard {
	
	private String newFolderName;
	private WizardNewFolderMainPageForTesting mainPage;
	private IFolder newFolder;
	private BlockingProgressMonitor blockingMonitor;

	
	public BasicNewFolderResourceWizardExtension() {
		super();
	}
	
	/**
	 * Used by the test suite to simulate user input - setting
	 * the new folder name - this must be called by the test suite.
	 * 
	 * @param name
	 */
	public void setNewFolderName(String name) {
		newFolderName = name;
	}

	public void setBlockingProgressMonitor(BlockingProgressMonitor monitor) {
		blockingMonitor = monitor;
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		mainPage = new WizardNewFolderMainPageForTesting("New Folder",getSelection()); 
		mainPage.setFolderName(newFolderName);
		mainPage.setBlockingProgressMonitor(blockingMonitor);
		addPage(mainPage);
	}

	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		IFolder folder = mainPage.createNewFolder();
		newFolder = folder; // AJDT: added this line so we can return the newly created folder

		if (folder == null)
			return false;

		selectAndReveal(folder);	
		
		return true;
	}

	/**
	 * Returns the newly created folder
	 * 
	 * @return
	 */
	public IFolder getNewFolder() {
		return newFolder;
	}

	
}
