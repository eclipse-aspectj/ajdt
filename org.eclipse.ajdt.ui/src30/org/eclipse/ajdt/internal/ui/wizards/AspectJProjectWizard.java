/**********************************************************************
 * Copyright (c) 2002 - 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * Contributors:
 * 		Adrian Colyer, Andy Clement, Tracy Gardner - initial version
 * 		Ian McGrath - added ability to use existing project structures
 * 		Sian January - updated to look like 3.0 new Java Project wizard
 * 		...
**********************************************************************/

package org.eclipse.ajdt.internal.ui.wizards;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.internal.utils.AJDTEventTrace;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.wizards.JavaProjectWizardFirstPage;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

/**
 * Wizard to create a new AspectJ project
 */
public class AspectJProjectWizard extends NewElementWizard implements IExecutableExtension {
    
    protected JavaProjectWizardFirstPage fFirstPage;
    protected AspectJProjectWizardSecondPage fSecondPage;
    
    private IConfigurationElement fConfigElement;
    
    public AspectJProjectWizard() {
		setDefaultPageImageDescriptor(AspectJImages.W_NEW_AJ_PRJ.getImageDescriptor());
		setDialogSettings(AspectJUIPlugin.getDefault().getDialogSettings());
		setWindowTitle(AspectJUIPlugin.getResourceString("NewAspectjProjectCreationWizard.title"));
   }

    /*
     * @see Wizard#addPages
     */	
    public void addPages() {
        super.addPages();
        fFirstPage= new JavaProjectWizardFirstPage();
        addPage(fFirstPage);
        fFirstPage.setTitle(AspectJUIPlugin.getResourceString("NewAspectJProject.CreateAnAspectJProject"));
		fFirstPage.setDescription(AspectJUIPlugin.getResourceString("NewAspectJProject.CreateAnAspectJProjectDescription"));
        fSecondPage= new AspectJProjectWizardSecondPage(fFirstPage);
        fSecondPage.setTitle(AspectJUIPlugin.getResourceString("NewAspectJProject.BuildSettings"));
        fSecondPage.setDescription(AspectJUIPlugin.getResourceString("NewAspectJProject.BuildSettingsDescription"));
        addPage(fSecondPage);
    }		
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#finishPage(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
    	fSecondPage.performFinish(monitor); // use the full progress monitor
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		boolean res= super.performFinish();
		if (res) {
			// Fix for 78263
	        BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
	 		IProject project = fSecondPage.getJavaProject().getProject();
	 		selectAndReveal(project);
			boolean completed = finalizeNewProject(project, fFirstPage.getDetect());
			res = completed;
		}
		return res;
	}
    
    protected void handleFinishException(Shell shell, InvocationTargetException e) {
        String title= NewWizardMessages.getString("JavaProjectWizard.op_error.title"); //$NON-NLS-1$
        String message= NewWizardMessages.getString("JavaProjectWizard.op_error_create.message");			 //$NON-NLS-1$
        ExceptionHandler.handle(e, getShell(), title, message);
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
		AspectJPlugin.getDefault().setCurrentProject( project );
		selectAndReveal(project);
		AJDTEventTrace.newProjectCreated( project );
		return true;
	}


   
    /*
     * Stores the configuration element for the wizard.  The config element will be used
     * in <code>performFinish</code> to set the result perspective.
     */
    public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
        fConfigElement= cfig;
    }
    
    /* (non-Javadoc)
     * @see IWizard#performCancel()
     */
    public boolean performCancel() {
        fSecondPage.performCancel();
        return super.performCancel();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#canFinish()
     */
    public boolean canFinish() {
        return super.canFinish();
    }

}