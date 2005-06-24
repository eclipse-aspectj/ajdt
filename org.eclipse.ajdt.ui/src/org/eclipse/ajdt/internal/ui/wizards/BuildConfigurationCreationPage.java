/**********************************************************************
Copyright (c) 2002, 2005 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
Sian January - updated for new style build configurations
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui.wizards;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.utils.AJDTEventTrace;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;

/**
 * This class is the only page of the ".lst" file resource creation wizard.  
 * It subclasses the standard file resource creation page class, 
 * and consequently inherits the file resource creation functionality.
 */
public class BuildConfigurationCreationPage extends WizardNewFileCreationPage {
	private IWorkbench workbench;

	// widgets
	private Button openFileCheckbox;
	private Button makeActiveCheckbox;

	// constants
	
	private IStructuredSelection selection;
		
	
	/**
	 * Creates the page for the build configuration file creation wizard.
	 *
	 * @param workbench  the workbench on which the page should be created
	 * @param selection  the current selection
	 */
	public BuildConfigurationCreationPage(
		IWorkbench workbench,
		IStructuredSelection selection) {
		super("CreateBuildConfigurationFilePage1", selection);
		this.setTitle( AspectJUIPlugin.getResourceString( "BuildConfig.createLstFile" ) ); 
		this.setDescription( AspectJUIPlugin.getResourceString( "BuildConfig.createLstDesc" ) );
		this.workbench = workbench;
		this.selection = selection;
	}
	
	/**
     * Build the GUI representation of the page.
	 */
	public void createControl(Composite parent) {
		// inherit default container and name specification widgets
		super.createControl(parent);
		Composite composite = (Composite) getControl();
		
        //ASCFIXME: Add help!
		//WorkbenchHelp.setHelp(composite, new String[] {IReadmeConstants.CREATION_WIZARD_PAGE_CONTEXT});

		this.setFileName(getFreeFileName() + "." + BuildConfiguration.EXTENSION);

		// open file for editing checkbox
		openFileCheckbox = new Button(composite, SWT.CHECK);
		openFileCheckbox.setText(AspectJUIPlugin.getResourceString( "BuildConfig.openForEdit" ) );
		openFileCheckbox.setSelection(true);
		
		makeActiveCheckbox = new Button(composite, SWT.CHECK);
		makeActiveCheckbox.setText(AspectJUIPlugin.getResourceString( "BuildConfig.activate" ) );
		makeActiveCheckbox.setSelection(true);

		setPageComplete(validatePage());

	}
	
	/**
	 * Get a suitable and unused filename (without file extension)
	 * @return
	 */
	private String getFreeFileName() {
		IProject project = null;
		if(selection.size() > 0) {
			Object element = selection.getFirstElement();
			if(element instanceof IResource) {
				project = ((IResource)element).getProject(); 
			} else if (element instanceof IJavaElement) {
				project = ((IJavaElement)element).getJavaProject().getProject();
			}
		}
		return BuildConfigurator.getFreeFileName(project);
	}

	/*
	 * Override because linking to a file in the file system doesn't make a lot
	 * of sense in this case
	 */
	protected void createAdvancedControls(Composite parent){}
	
	/*
	 * Override because we overrode createAdvancedControls(..) 
	 */
	protected IStatus validateLinkedResource() {
		return new Status(IStatus.OK, AspectJUIPlugin.PLUGIN_ID, IStatus.OK, "", null);
	}
	
	/*
	 * Override because we overrode createAdvancedControls(..) 
	 */	
	protected void createLinkTarget() {}

	/**
	 * Creates a new file resource as requested by the user. If everything
	 * is OK then answer true. If not, false will cause the dialog
	 * to stay open.
	 *
	 * @return whether creation was successful
	 * @see ReadmeCreationWizard#performFinish()
	 */
	public boolean finish() {
		// create the new file resource
		
		IFile newFile = createNewFile();

		
		if (newFile == null)
			return false; // ie.- creation was unsuccessful

		// Since the file resource was created fine, open it for editing
		// if requested by the user
		IProject project = newFile.getProject();
		ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator().getProjectBuildConfigurator(project);
		new BuildConfiguration(newFile, pbc, makeActiveCheckbox.getSelection());
		try {
			if (openFileCheckbox.getSelection()) {
				IWorkbenchWindow dwindow = workbench.getActiveWorkbenchWindow();
				IWorkbenchPage page = dwindow.getActivePage();
				if (page != null) IDE.openEditor(page,newFile,true);
			}
		} catch (PartInitException e) {
			return false;
		} 
		AJDTEventTrace.newConfigFileCreated( newFile );
		return true;
	}
	

	/**
	 * Validate the page
	 */
	protected boolean validatePage() {
		if(super.validatePage()) {
			if(!isProjectSelected()) {
				setErrorMessage(AspectJUIPlugin.getResourceString("BuildConfig.needToSelectProject"));
				return false;
			} else {
				return true;
			}			
		} else {
			return false;
		}
	}
	

	/**
	 * Test to see whether the selected parent is a project
	 * @return
	 */
	private boolean isProjectSelected() {
		IPath containerPath = getContainerFullPath();
		IProject[] projects = AspectJPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			IPath path = project.getFullPath();
			if(path.equals(containerPath)) {
				return true;
			}
		}
		return false;
	}

	/** (non-Javadoc)
	 * Method declared on WizardNewFileCreationPage.
	 */
	protected String getNewFileLabel() {
		return AspectJUIPlugin.getResourceString( "BuildConfig.newLstFile" );
	}

}