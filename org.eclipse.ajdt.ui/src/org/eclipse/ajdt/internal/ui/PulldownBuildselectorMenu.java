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
package org.eclipse.ajdt.internal.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.bc.BuildConfiguration;
import org.eclipse.ajdt.internal.ui.ajde.CompilerMonitor;
import org.eclipse.ajdt.internal.ui.ajde.ErrorHandler;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.buildconfig.DefaultBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IProjectBuildConfigurator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * This pulldown menu displays a list of the .lst files available in the current
 * project. It highlights the current 'selected' .lst file with a checkbox. By
 * selecting one of the other options, the user can change the selected .lst
 * file for the project. If the button in the toolbar is operated, rather than
 * the pulldown arrow next to it, then it displays the current selected build
 * file for the project in a message dialog.
 */
public class PulldownBuildselectorMenu implements
	IWorkbenchWindowActionDelegate, SelectionListener {

	private IAction buildAction;

	public PulldownBuildselectorMenu(){
		super();
	}
	
	/**
	 * Project for which the pulldown menu instance is being built.
	 */
	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	public void run(IAction action) {
		IProject project = AspectJPlugin.getDefault().getCurrentProject();
		if (project != null) {
			build(project);
		}
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		if (buildAction == null) {
			buildAction = action;
		}
	}

	/**
	 * Handle selection of an item in the menu.
	 */
	public void widgetDefaultSelected(SelectionEvent se) {
		chooseBuild(se);
	}

	/**
	 * Handle selection of an item in the menu.
	 */
	public void widgetSelected(SelectionEvent se) {
		chooseBuild(se);
	}

	/**
	 * When an item is selected from the pulldown menu, this function pulls the
	 * IResource from the menu item selected and makes that IResource the
	 * current build config for the project (remembered in the AspectJPlugin).
	 */
	public void chooseBuild(SelectionEvent se) {
		Object src = se.getSource();
		if (src instanceof MenuItem) {
			MenuItem w = (MenuItem) src;
			BuildConfiguration bc = (BuildConfiguration) w.getData();
			IProjectBuildConfigurator pbc = DefaultBuildConfigurator
					.getBuildConfigurator().getActiveProjectBuildConfigurator();
			if (pbc != null) {
				pbc.setActiveBuildConfiguration(bc);
			}
		}
	}

	/**
	 * Once the button has been clicked, or a config selected, then perform a
	 * build immediately
	 */
	private void build(final IProject project) {
		Shell activeShell = AspectJUIPlugin.getDefault()
				.getActiveWorkbenchWindow().getShell();
		IRunnableWithProgress op = new IRunnableWithProgress() {

			void doLocalBuild(int buildType, IProgressMonitor pm)
					throws CoreException {
				CompilerMonitor.isLocalBuild = true;
				// Related to bug #40868 - Do not just call the aspectj builder,
				// invoke *all* the builders defined.
				project.build(IncrementalProjectBuilder.FULL_BUILD, pm);
				CompilerMonitor.isLocalBuild = false;
			}

			public void run(IProgressMonitor pm) {
				try {
					doLocalBuild(IncrementalProjectBuilder.FULL_BUILD, pm);
				} catch (CoreException cEx) {
					ErrorHandler.handleAJDTError(
							UIMessages.PulldownBuildselectorMenu_build_error, cEx);
				} catch (NullPointerException npe) {
					AJLog.log("Unexpected NullPointerException during build processing (eclipse bug?): Your task view will be temporarily out of step with compilation:" //$NON-NLS-1$
									+ npe);
				} catch (OperationCanceledException e) {
					AJLog.log("Build was cancelled."); //$NON-NLS-1$
				}
			}
		};

		try {
			new ProgressMonitorDialog(activeShell).run(true, true, op);

		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}
	}

}