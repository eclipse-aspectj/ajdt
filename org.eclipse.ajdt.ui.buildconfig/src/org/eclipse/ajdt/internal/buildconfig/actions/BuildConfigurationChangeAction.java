/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.buildconfig.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.IBuildConfigurationChangedListener;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.buildconfig.DefaultBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IBuildConfiguration;
import org.eclipse.ajdt.ui.buildconfig.IBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IProjectBuildConfigurator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Luzius Meisser
 *  
 */
public abstract class BuildConfigurationChangeAction implements
		IObjectActionDelegate, IBuildConfigurationChangedListener {

	protected IAction myAction;

	protected IStructuredSelection strucSel;

	protected IBuildConfigurator buildConfigurator;

	protected String actionText;

	/**
	 * Constructor for Action1.
	 */
	public BuildConfigurationChangeAction() {
		super();
		buildConfigurator = DefaultBuildConfigurator.getBuildConfigurator();
		((BuildConfigurator)buildConfigurator).addBuildConfigurationChangedListener(this);
	}

	ArrayList changedBCs;

	/**
	 * @see IActionDelegate#run(IAction)
	 */

	public void run(IAction action) {
		changedBCs = new ArrayList();
		Object mySel;
		IProjectBuildConfigurator pbc = buildConfigurator
				.getActiveProjectBuildConfigurator();

		if (pbc != null) {
			IProjectBuildConfigurator resourcepbc;
			Iterator mySelIter = strucSel.iterator();
			ArrayList itemsToManipulate = new ArrayList();
			while (mySelIter.hasNext()) {
				mySel = mySelIter.next();

				if (mySel instanceof IJavaElement) {
					try {
						mySel = ((IJavaElement) mySel)
								.getCorrespondingResource();
					} catch (JavaModelException e) {
						mySel = null;
					}
				}
				if (mySel instanceof IResource) {
					IResource res = (IResource) mySel;
					resourcepbc = buildConfigurator
							.getProjectBuildConfigurator(res.getProject());
					if (pbc == resourcepbc) {
						itemsToManipulate.add(res);
					}
				}
			}
			doMySpecificAction(pbc.getActiveBuildConfiguration(),
					itemsToManipulate);
		}

	}
	
	protected abstract Job getJob(final IBuildConfiguration bc, final List fileList);

	private void doMySpecificAction(IBuildConfiguration bc, List fileList) {
		Job job = getJob(bc, fileList);
		job.setPriority(Job.SHORT);
		job.setRule(bc.getFile().getProject());
		job.schedule();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {

	}

	public void buildConfigurationChanged(final IProjectBuildConfigurator pbc) {
		if (pbc != null) {
			myAction.setEnabled(true);
			Job job = new Job(UIMessages.buildConfigurationChangeAction_job_name) {
				protected IStatus run(IProgressMonitor monitor) {
					IBuildConfiguration bc = pbc.getActiveBuildConfiguration();
					myAction.setText(actionText.replaceAll("%bcname", bc //$NON-NLS-1$
							.getName()));
					return Status.OK_STATUS;
				}
			};
			job.setPriority(Job.SHORT);
			job.setRule(pbc.getJavaProject().getProject());
			job.schedule();
		} else {
			myAction.setEnabled(false);
		}
	}

	abstract boolean isObjectApplicable(Object o);

	protected boolean isApplicable(Object sel) {
		if (sel instanceof IPackageFragment) {
			try {
				IPackageFragment pack = ((IPackageFragment) sel);
				Object[] children = pack.getChildren();
				for (int i = 0; i < children.length; i++) {
					if (isApplicable(children[i]))
						return true;
				}

				children = pack.getNonJavaResources();
				for (int i = 0; i < children.length; i++) {
					if (isApplicable(children[i]))
						return true;
				}

			} catch (CoreException e) {
				//if this function fails, include/exlude files will fail as
				// well so deactivate these acitons
				return false;
			}
		} else {
			return isObjectApplicable(sel);
		}
		return false;
	}

	protected boolean isApplicable(IStructuredSelection ssel) {
		Iterator mySelIter = ssel.iterator();
		while (mySelIter.hasNext()) {
			Object sel = mySelIter.next();
			if (isApplicable(sel))
				return true;
		}
		return false;
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (myAction == null) {
			myAction = action;
			IProjectBuildConfigurator pbc = buildConfigurator
					.getActiveProjectBuildConfigurator();
			if (pbc != null) {
				myAction.setText(actionText.replaceAll("%bcname", pbc //$NON-NLS-1$
						.getActiveBuildConfiguration().getName()));
			}
		}
		if (selection instanceof IStructuredSelection) {
			strucSel = (IStructuredSelection) selection;
			action.setEnabled(isApplicable(strucSel));
		} else {
			strucSel = null;
			action.setEnabled(false);
		}
	}

}