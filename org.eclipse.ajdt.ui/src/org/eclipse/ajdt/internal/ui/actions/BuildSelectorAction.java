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
package org.eclipse.ajdt.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.ajde.CompilerMonitor;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * This action is triggered from a popup menu on a build configuration
 * file. It makes the target the current build config file for the project.
 */
public class BuildSelectorAction implements IWorkbenchWindowActionDelegate {

	private IFile currentlySelectedBuildFile = null;

	/**
	 *  Executed when button clicked or popup menu "Select this build file" clicked
	 */
	public void run(IAction action) {
		final IProject buildFileProject = currentlySelectedBuildFile.getProject();
		
		if (!AspectJPlugin.isAJProject(buildFileProject)) {
			return;
		}
		
		AspectJUIPlugin.setBuildConfigurationFile(
			buildFileProject,
			currentlySelectedBuildFile);
		AspectJPlugin.getDefault().setCurrentProject( buildFileProject );

		doFullBuild(buildFileProject);
	}

	//do full build with progress monitor
	//no effect if called by non-UI thread
	public static void doFullBuild(final IProject project) {
		//getActiveWorkbenchWindow returns null if called by a non-UI thread -> test necessary
		IWorkbenchWindow wbwin = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow();
		Shell activeShell;
		if (wbwin != null){
			activeShell = wbwin.getShell();
		} else {
			return;
		}
		IRunnableWithProgress op = new IRunnableWithProgress( ) {
            
            void doLocalBuild(IProgressMonitor pm) throws CoreException {
            	CompilerMonitor.isLocalBuild = true;
                // Related to bug #40868 - Do not just call the aspectj builder, invoke *all* the builders defined.
                project.build(IncrementalProjectBuilder.FULL_BUILD, pm);
                CompilerMonitor.isLocalBuild = false;
            }
            
			public void run( IProgressMonitor pm ) {
				try {
                    doLocalBuild(pm);
				} catch ( CoreException cEx ) {
					AspectJUIPlugin.getDefault().getErrorHandler().handleError( 
						"Build on select error", cEx );	 //$NON-NLS-1$
				} catch (Exception e) {
				}
			}
		};
		try {
			new ProgressMonitorDialog( activeShell).run( true, true, op );
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}
	}
	
	/**
	 * Selection has changed - if we've selected a build file then remember it
	 * as the new project build config.
	 */
	public void selectionChanged(IAction action, ISelection selection) {

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object first = structuredSelection.getFirstElement();
			if (first instanceof IFile) {
				currentlySelectedBuildFile = (IFile) first;
			}
		}
	}

	/**
	 * From IWorkbenchWindowActionDelegate
	 */
	public void dispose() {}

	/**
	 * From IWorkbenchWindowActionDelegate
	 */
	public void init(IWorkbenchWindow window) {
	}
}