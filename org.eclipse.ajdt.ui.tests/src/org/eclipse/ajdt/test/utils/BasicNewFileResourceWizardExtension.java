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

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;

/**
 * A class which creates a new File via the new file wizard, simulating
 * user input.
 * 
 * @author hawkinsh
 */
public class BasicNewFileResourceWizardExtension extends BasicNewFileResourceWizard{

	private String newFileName;
	private WizardNewFileCreationPageForTesting mainPage;
	private IFile newFile;
	private BlockingProgressMonitor blockingMonitor;
	
	public BasicNewFileResourceWizardExtension() {
		super();
	}
	
	/**
	 * Used by the test suite to simulate user input - setting
	 * the new file name - this must be called by the test suite.
	 * 
	 * @param name
	 */
	public void setNewFileName(String name) {
		newFileName = name;
	}

	public void setBlockingProgressMonitor(BlockingProgressMonitor monitor) {
		blockingMonitor = monitor;
	}
	
	/**
	 * Overwrites addPages to add simulate user input.
	 */	
	public void addPages() {
		// This is mostly the same as super.addPages() except that we
		// don't call "super.addPages()" and we make the extra call to
		// "mainPage.setFileName(newFileName)" to simulate the user input
		mainPage = new WizardNewFileCreationPageForTesting("newFilePage1", getSelection());
		mainPage.setTitle("New File Wizard"); 
		mainPage.setDescription("New File Wizard");
		mainPage.setFileName(newFileName); 
		mainPage.setBlockingProgressMonitor(blockingMonitor);
		addPage(mainPage);
	}

	/**
	 * Overwrites performFinish to add simulate user input.
	 * This is the same as super.addPages() - except for the
	 * lines marked. The reason we need it here is to user our 
	 * WizardNewFileCreationPage which has the simulated user input.
	 */	
	public boolean performFinish() {
		IFile file = mainPage.createNewFile();
		newFile = file; // AJDT: added this line so we can return the newly created file
		if (file == null)
			return false;

		selectAndReveal(file);

		// Open editor on new file.
		IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
		try {
			if (dw != null) {
				IWorkbenchPage page = dw.getActivePage();
				if (page != null) {
					IDE.openEditor(page, file, true);
				}
			}
		} catch (PartInitException e) {
			DialogUtil.openError(
				dw.getShell(),
				"ERROR occured!",
				e.getMessage(),
				e);
		}
				
		return true;
	}
	
	/**
	 * Returns the newly created file
	 * 
	 * @return
	 */
	public IFile getNewFile() {
		return newFile;
	}
	
}
