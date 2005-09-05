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

/**
 * ToDos
 * 1) Work out how to operate under the AspectJ menu
 * 
 */
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.IBuildConfigurationChangedListener;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.ui.ajde.CompilerMonitor;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;

/**
 * This pulldown menu displays a list of the .lst files available in the current
 * project. It highlights the current 'selected' .lst file with a checkbox. By
 * selecting one of the other options, the user can change the selected .lst
 * file for the project. If the button in the toolbar is operated, rather than
 * the pulldown arrow next to it, then it displays the current selected build
 * file for the project in a message dialog.
 */
public class PulldownBuildselectorMenu implements
		IWorkbenchWindowPulldownDelegate, SelectionListener, IBuildConfigurationChangedListener{

	private IAction buildAction;

	public PulldownBuildselectorMenu(){
		super();
		BuildConfigurator.getBuildConfigurator().addBuildConfigurationChangedListener(this);
	}
	/**
	 * Project for which the pulldown menu instance is being built.
	 */
	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	/**
	 * Return the location of the workspace, for example c:\eclipse\workspace
	 */
	public String getWorkspaceDirectory() {
		IProject currentProject = AspectJPlugin.getDefault()
				.getCurrentProject();
		// If there are no AspectJ projects in the workspace, return null
		if (currentProject == null)
			return null;
		IPath trimPath = currentProject.getLocation();
		trimPath = trimPath.removeLastSegments(1);
		return new String(trimPath.toOSString() + File.separator);
	}

	/**
	 * Called when the pulldown tab on the menu is clicked, this produces a menu
	 * that contains an up to date list of the current .lst files. 'this' is
	 * registered as the selection listener for each of those .lst files - so
	 * that if the user clicks any of the options on the menu then we are called
	 * back.
	 */
	public Menu getMenu(Control c) {
		Menu m = new Menu(c);
		ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator()
				.getActiveProjectBuildConfigurator();

		if (pbc == null) {
			// There are no AspectJ projects, so make a special
			// menu to inform the user.
			MenuItem defaultLstItem = new MenuItem(m, SWT.CHECK);
			defaultLstItem.setText(UIMessages.PulldownBuildselectorMenu_no_open_ajproject);
			defaultLstItem.setData(null);
			return m;
		}

		BuildConfiguration[] bcs = (BuildConfiguration[]) pbc
				.getBuildConfigurations().toArray(new BuildConfiguration[0]);
		Util.sort(bcs);

		// For each .lst file, add an item to the menu
		MenuItem mi = null;
		BuildConfiguration activeBC = pbc.getActiveBuildConfiguration();
		for (int i = 0; i < bcs.length; i++) {
			BuildConfiguration bc = bcs[i];
			mi = new MenuItem(m, SWT.CHECK);
			mi.setText(bc.getName());
			mi.addSelectionListener(this);
			mi.setData(bc);
			if (bc == activeBC) {
				mi.setSelection(true);
			}
		}

		return m;
	}

	public void run(IAction action) {

		ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator()
				.getActiveProjectBuildConfigurator();
		if (pbc != null) {
			build(pbc.getJavaProject().getProject());
		}
	}
	
	public void buildConfigurationChanged(ProjectBuildConfigurator pbc){
		if (pbc == null) {
			buildAction.setEnabled(false);
		} else {
			buildAction.setEnabled(true);
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
			ProjectBuildConfigurator pbc = BuildConfigurator
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

					AspectJUIPlugin.getDefault().getErrorHandler().handleError(
							"Build pulldown error", cEx); //$NON-NLS-1$
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
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(
					"Auto build on select failed", e); //$NON-NLS-1$
		} catch (InterruptedException e) {
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(
					"Auto build on select failed", e); //$NON-NLS-1$

		}
	}

}