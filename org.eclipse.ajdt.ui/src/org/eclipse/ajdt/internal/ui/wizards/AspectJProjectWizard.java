/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
Ian McGrath - added ability to use existing project structures
...
**********************************************************************/

package org.eclipse.ajdt.internal.ui.wizards;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPage;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Wizard to create a new AspectJ project
 */
public class AspectJProjectWizard
	extends BasicNewProjectResourceWizard
	implements IExecutableExtension {

	protected NewJavaProjectWizardPage fJavaPage;
	protected AspectJWizardNewProjectCreationPage fMainPage;
	private IConfigurationElement fConfigElement;

	/**
	 * Create a new wizard
	 */
	public AspectJProjectWizard() {
		super();
		setDefaultPageImageDescriptor(AspectJImages.W_NEW_AJ_PRJ.getImageDescriptor());
		setDialogSettings(AspectJUIPlugin.getDefault().getDialogSettings());
		setWindowTitle(AspectJUIPlugin.getResourceString("NewAspectjProjectCreationWizard.title"));
	}

	/*
	 * @see Wizard#addPages
	 */
	public void addPages() {
		
		//super.addPages();
		fMainPage = new AspectJWizardNewProjectCreationPage("NewAspectjProjectCreationWizard");
		fMainPage.setTitle(
			AspectJUIPlugin.getResourceString("NewAspectjProjectCreationWizard.MainPage.title"));
		fMainPage.setDescription(
			AspectJUIPlugin.getResourceString("NewAspectjProjectCreationWizard.MainPage.description"));
		fMainPage.setImageDescriptor(AspectJImages.W_NEW_AJ_PRJ.getImageDescriptor());
		
		addPage(fMainPage);
		IWorkspaceRoot root = AspectJUIPlugin.getWorkspace().getRoot();
		fJavaPage = new NewJavaProjectWizardPage(root, fMainPage);
		addPage(fJavaPage);
	}

	/*
	 * @see Wizard#performFinish
	 */
	public boolean performFinish() {

		IProject project = fJavaPage.getNewJavaProject().getProject();
		boolean useExisting = fMainPage.useExistingProjectStructure();
		
		if(!useExisting) {	//overwrite any existing structure
			IRunnableWithProgress op =
				new WorkspaceModifyDelegatingOperation(fJavaPage.getRunnable());
			try {
				getContainer().run(false, true, op);
			} catch (InvocationTargetException e) {
				String title = AspectJUIPlugin.getResourceString("NewAspectjProjectCreationWizard.op_error.title");
				String message = AspectJUIPlugin.getResourceString("NewAspectjProjectCreationWizard.op_error.message");
				ExceptionHandler.handle(e, getShell(), title, message);
				return false;
				
			} catch (InterruptedException e) {
				return false;
			}
			
			project = fJavaPage.getNewJavaProject().getProject();
		}

		else {	//use existing structure
			
			if(!fMainPage.hasCorrectCapitalisation()) { //auto correct the users capitalization
				//To correct the name of the project a separate wizard is required so the text field
				//can be set
				IWorkspaceRoot root = AspectJUIPlugin.getWorkspace().getRoot();
				AspectJWizardNewProjectCreationPage TempMainPage =
					new AspectJWizardNewProjectCreationPage("NewAspectjProjectCreationWizard");
				TempMainPage.setInitialProjectName(fMainPage.getCorrectCapitalization());
				NewJavaProjectWizardPage TempJavaPage = new NewJavaProjectWizardPage(root, TempMainPage);
				project = TempJavaPage.getNewJavaProject().getProject();
				
				//disable the buttons of the wizard
				fMainPage.setPageComplete(false);
				fJavaPage.setPageComplete(false);
			}
			
			IProgressMonitor monitor = new NullProgressMonitor();
			try {
				project.create(monitor);
				project.open(monitor);
			} catch (CoreException e) {	
		        String title = AspectJUIPlugin.getResourceString("NewAspectjProjectCreationWizard.build_error.title");
		        String message = AspectJUIPlugin.getResourceString("NewAspectjProjectCreationWizard.build_error.message");
		        ExceptionHandler.handle(e, getShell(), title, message);
		        return false;
			}
		}
		BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
		boolean completed = finalizeNewProject(project, useExisting);
		return completed;
	}

	
	/**
	 * Builds and adds the necessary properties to the new project and updates the workspace view
	 */
	private boolean finalizeNewProject(IProject project, boolean alreadyExists) {
		
//		BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
		
        // Bugzilla 46271
        // Force a build of the new AspectJ project using the Java builder
        // so that project state can be created. The creation of project 
        // state means that Java projects can reference this project on their
        // build path and successfully continue to build.

		final IProject thisProject = project;
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try {
			
			// The nature to add is the PluginID+NatureID - it is not the
			// name of the class implementing IProjectNature !!
			// When the nature is attached, the project will be driven through
			// INatureProject.configure() which will replace the normal javabuilder
			// with the aspectj builder.
			if(!alreadyExists) {
				AJDTUtils.addAspectJNature(project);
			}
			
			else {
				dialog.run(true, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException {
						monitor.beginTask("", 2);
						try {
							monitor
									.setTaskName(AspectJUIPlugin
											.getResourceString("OptionsConfigurationBlock.buildproject.taskname"));
							thisProject.build(
									IncrementalProjectBuilder.FULL_BUILD,
									new SubProgressMonitor(monitor, 2));
						} catch (CoreException e) {
						} finally {
							monitor.done();
						}
					}
				});
			}
		} catch(InterruptedException e) {
			// build cancelled by user
			return false;
		} catch(InvocationTargetException e) {
			String title =
				AspectJUIPlugin.getResourceString("NewAspectjProjectCreationWizard.op_error.title");
			String message =
				AspectJUIPlugin.getResourceString("NewAspectjProjectCreationWizard.op_error.message");
			ExceptionHandler.handle(e, getShell(), title, message);
		} catch(CoreException e) {
		}
		
		project = thisProject;
		AspectJUIPlugin.getDefault().setCurrentProject( project );
		selectAndReveal(project);
		AJDTEventTrace.newProjectCreated( project );
		return true;
	}
	
	/*
	 * Stores the configuration element for the wizard.  The config element will be used
	 * in <code>performFinish</code> to set the result perspective.
	 */
	public void setInitializationData(
		IConfigurationElement cfig,
		String propertyName,
		Object data) {
		fConfigElement = cfig;
	}
}