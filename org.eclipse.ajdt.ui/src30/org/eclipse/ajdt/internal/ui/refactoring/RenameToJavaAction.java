/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.ui.refactoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.corext.refactoring.changes.RenameResourceChange;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

/**
 * Rename the file extension of a file to .java
 */
public class RenameToJavaAction implements IActionDelegate {

	private ISelection selection;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (selection instanceof StructuredSelection) {
			AspectJUIPlugin.getDefault().disableBuildConfiguratorResourceChangeListener();		
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					StructuredSelection sel = (StructuredSelection) selection;
					for (Iterator iter = sel.iterator(); iter.hasNext();) {
						Object object = iter.next();
						if (object instanceof IAdaptable) {

							IResource file = (IResource) ((IAdaptable) object)
									.getAdapter(IResource.class);
							IProject project = file.getProject();
							if (file != null) {
								String name = file.getName();
								name = name.substring(0, name.indexOf('.')); //$NON-NLS-1$
								RenameResourceChange change = new RenameResourceChange(
										file, name + ".java"); //$NON-NLS-1$
								try {
									change.perform(monitor);
								} catch (CoreException e) {
									AspectJUIPlugin
											.getDefault()
											.getErrorHandler()
											.handleError(
													AspectJUIPlugin
															.getResourceString("Refactoring.ErrorRenamingResource"), //$NON-NLS-1$
													e);
								}
								if (project != null) {
									updateBuildConfigs(monitor, project, name);
								}
							}
						}
					}
				}
			};

			IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(
					runnable);
			try {
				new ProgressMonitorDialog(AspectJUIPlugin.getDefault()
						.getDisplay().getActiveShell()).run(true, true, op);
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
			}
			AspectJUIPlugin.getDefault().enableBuildConfiguratorResourceChangeListener();
		}
	}

	/**
	 * Update the build configurations with the new name
	 * @param monitor
	 * @param project
	 * @param name
	 */
	private void updateBuildConfigs(IProgressMonitor monitor, IProject project,
			String name) {
		ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator()
				.getProjectBuildConfigurator(project);
		IFile[] buildConfigs = pbc.getConfigurationFiles();
		for (int i = 0; i < buildConfigs.length; i++) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(buildConfigs[i]
						.getContents()));
			} catch (CoreException e) {
				continue;
			}
			StringBuffer sb = new StringBuffer();
			try {
				String line = br.readLine();
				while (line != null) {
					line = line.replaceAll(name + ".aj", name + ".java"); //$NON-NLS-1$
					sb.append(line);
					sb.append(System.getProperty("line.separator")); //$NON-NLS-1$
					line = br.readLine();
				}
				StringReader reader = new StringReader(sb.toString());
				buildConfigs[i].setContents(new ReaderInputStream(reader), true, true, monitor);
			} catch (IOException ioe) {
			} catch (CoreException e) {
			} finally {
				try {
					br.close();
				} catch (IOException ioe) {
				}
			}
			Collection c = pbc.getBuildConfigurations();
			for (Iterator iter = c.iterator(); iter.hasNext();) {
				BuildConfiguration config = (BuildConfiguration) iter.next();
				config.update(true);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}