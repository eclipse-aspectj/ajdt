/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.examples;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class AspectJExampleCreationWizard extends Wizard implements INewWizard, IExecutableExtension {

	public static final String PROJECT_SETUP_EXTENSION = "org.eclipse.ajdt.examples.projectsetup"; //$NON-NLS-1$

	private AspectJExampleCreationWizardPage[] fPages;
	private IConfigurationElement fConfigElement;

	public AspectJExampleCreationWizard() {
		super();
		setDialogSettings(AspectJExamplePlugin.getDefault().getDialogSettings());
		setWindowTitle(AspectJExampleMessages.getString("ExampleProjectCreationWizard.title"));		 //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}
	
	private void initializeDefaultPageImageDescriptor() {
		if (fConfigElement != null) {
			String banner= fConfigElement.getAttribute("banner"); //$NON-NLS-1$
			if (banner != null) {
				ImageDescriptor desc= AspectJExamplePlugin.getDefault().getImageDescriptor(banner);
				setDefaultPageImageDescriptor(desc);
			}
		}
	}

	/*
	 * @see Wizard#addPages
	 */	
	public void addPages() {
		super.addPages();

		String id = fConfigElement.getAttribute("id"); //$NON-NLS-1$
		IConfigurationElement child = getProjectSetupInfo(id);
		
		fPages=  new AspectJExampleCreationWizardPage[1];
		fPages[0]= new AspectJExampleCreationWizardPage(0, child);
		addPage(fPages[0]);
	}
	
	private IConfigurationElement getProjectSetupInfo(String matchingID) {
		IExtensionPoint exP = Platform.getExtensionRegistry()
				.getExtensionPoint(PROJECT_SETUP_EXTENSION);
		IExtension[] exs = exP.getExtensions();

		for (int i = 0; i < exs.length; i++) {
			IConfigurationElement[] ces = exs[i].getConfigurationElements();
			for (int j = 0; j < ces.length; j++) {
				String id = ces[j].getAttribute("id"); //$NON-NLS-1$
				if (id.equals(matchingID)) {
					return ces[j];
				}
			}
		}
		return null;
	}
	
	/*
	 * @see Wizard#performFinish
	 */		
	public boolean performFinish() {
		// Fix for 78263
		BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
		AspectJExampleCreationOperation runnable= new AspectJExampleCreationOperation(fPages, new ImportOverwriteQuery());
		
		IRunnableWithProgress op= new WorkspaceModifyDelegatingOperation(runnable);
		try {
			getContainer().run(false, true, op);
		} catch (InvocationTargetException e) {
			handleException(e.getTargetException());
			return false;
		} catch  (InterruptedException e) {
			return false;
		}
		IResource res= runnable.getElementToOpen();
		if (res != null) {
			openResource(res);
		}
		return true;
	}
	
	private void handleException(Throwable target) {
		String title= AspectJExampleMessages.getString("ExampleProjectCreationWizard.op_error.title"); //$NON-NLS-1$
		String message= AspectJExampleMessages.getString("ExampleProjectCreationWizard.op_error.message"); //$NON-NLS-1$
		if (target instanceof CoreException) {
			IStatus status= ((CoreException)target).getStatus();
			ErrorDialog.openError(getShell(), title, message, status);
			AspectJExamplePlugin.log(status);
		} else {
			MessageDialog.openError(getShell(), title, target.getMessage());
			AspectJExamplePlugin.log(target);
		}
	}
	
	private void openResource(final IResource resource) {
		if (resource.getType() != IResource.FILE) {
			return;
		}
		IWorkbenchWindow window= AspectJExamplePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		final IWorkbenchPage activePage= window.getActivePage();
		if (activePage != null) {
			final Display display= getShell().getDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					try {
						IDE.openEditor(activePage,(IFile)resource,true);
					} catch (PartInitException e) {
						AspectJExamplePlugin.log(e);
					}
				}
			});
			BasicNewResourceWizard.selectAndReveal(resource, activePage.getWorkbenchWindow());
		}
	}	
		
	/**
	 * Stores the configuration element for the wizard.  The config element will be used
	 * in <code>performFinish</code> to set the result perspective.
	 */
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		fConfigElement= cfig;
		
		initializeDefaultPageImageDescriptor();
	}
	
	// overwrite dialog
	
	private class ImportOverwriteQuery implements IOverwriteQuery {
		public String queryOverwrite(String file) {
			String[] returnCodes= { YES, NO, ALL, CANCEL};
			int returnVal= openDialog(file);
			return returnVal < 0 ? CANCEL : returnCodes[returnVal];
		}	
		
		private int openDialog(final String file) {
			final int[] result= { IDialogConstants.CANCEL_ID };
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					String title= AspectJExampleMessages.getString("ExampleProjectCreationWizard.overwritequery.title"); //$NON-NLS-1$
					String msg= AspectJExampleMessages.getFormattedString("ExampleProjectCreationWizard.overwritequery.message", file); //$NON-NLS-1$
					String[] options= {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.CANCEL_LABEL};
					MessageDialog dialog= new MessageDialog(getShell(), title, null, msg, MessageDialog.QUESTION, options, 0);
					result[0]= dialog.open();
				}
			});
			return result[0];
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}		
}
