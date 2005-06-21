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
package org.eclipse.ajdt.ui.tests.testutils;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.jdt.internal.ui.wizards.NewPackageCreationWizard;

/**
 * A class which creates a new package via the new file wizard, simulating
 * user input.
 * 
 * @author hawkinsh
 */
public class NewPackageCreationWizardExtension extends NewPackageCreationWizard {

	private String newPackageName;
	private NewPackageWizardPageForTesting fPage;
	private IFolder newPackage;
	private BlockingProgressMonitor blockingMonitor;

	
	public NewPackageCreationWizardExtension() {
		super();
	}

	/**
	 * Used by the test suite to simulate user input - setting
	 * the new package name - this must be called by the test suite.
	 * 
	 * @param name
	 */
	public void setNewPackageName(String name) {
		newPackageName = name;
	}

	public void setBlockingProgressMonitor(BlockingProgressMonitor monitor) {
		blockingMonitor = monitor;
	}

	
	public void addPages() {
		fPage= new NewPackageWizardPageForTesting();
		fPage.init(getSelection());
		fPage.setPackageText(newPackageName,true);
		fPage.setBlockingProgressMonitor(blockingMonitor);
		addPage(fPage);
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#finishPage(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
		//fPage.createPackage(monitor); // use the full progress monitor
		blockingMonitor.reset();
		fPage.createPackage(blockingMonitor);
		blockingMonitor.waitForCompletion();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		boolean res= performTestFinish();
		if (res) {
			selectAndReveal(fPage.getModifiedResource());
		}
		return res;
	}

	/*
	 * @see Wizard#performFinish
	 */		
	private boolean performTestFinish() {
		IWorkspaceRunnable op= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
				try {
					finishPage(monitor);
				} catch (InterruptedException e) {
					throw new OperationCanceledException(e.getMessage());
				}
			}
		};
		try {
			getContainer().run(canRunForked(), true, new WorkbenchRunnableAdapter(op, getSchedulingRule()));
		} catch (InvocationTargetException e) {
			handleFinishException(getShell(), e);
			return false;
		} catch  (InterruptedException e) {
			return false;
		}
		return true;
	}
	
	
}
