/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.internal.buildconfig;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.buildconfig.IBuildConfiguration;
import org.eclipse.ajdt.ui.buildconfig.IBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IProjectBuildConfigurator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class UpdateJob extends Job {
	public static final int BUILD_CONFIG_CHANGED = 1;

	public static final int BUILD_CONFIG_REMOVED = 2;

	public static final int CLASSPATH_CHANGED = 3;

	public static final int SOURCE_ADDED = 4;

	public static final int FILE_MOVED = 5;

	private int type;

	private IResource myRes;

	private IResource myRes2;

	private final IBuildConfigurator myBCor;

	/**
	 *  
	 */
	public UpdateJob(IBuildConfigurator myBCor, int type, IResource myRes) {
		super(UIMessages.UpdateJob_name);
		setRule(myRes.getProject());
		setSystem(true);
		setPriority(Job.SHORT);
		this.myBCor = myBCor;
		this.type = type;
		this.myRes = myRes;
	}

	public UpdateJob(IBuildConfigurator myBCor, int type, IResource myRes,
			IResource myRes2) {
		super(UIMessages.UpdateJob_name);
		setRule(myRes.getProject());
		setSystem(true);
		setPriority(Job.SHORT);
		this.myBCor = myBCor;
		this.type = type;
		this.myRes = myRes;
		this.myRes2 = myRes2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		switch (type) {
		case BUILD_CONFIG_CHANGED:
			buildConfigChanged();
			break;
		case BUILD_CONFIG_REMOVED:
			buildConfigRemoved();
			break;
		case CLASSPATH_CHANGED:
			classpathChanged();
			break;
		case SOURCE_ADDED:
			sourceAdded();
			break;
		case FILE_MOVED:
			fileMoved();
			break;
		}
		return Status.OK_STATUS;
	}

	/**
	 *  
	 */
	private void sourceAdded() {
		IProjectBuildConfigurator pbc = myBCor.getProjectBuildConfigurator(myRes
				.getProject());
		if (pbc != null) {
			IBuildConfiguration bc = pbc.getActiveBuildConfiguration();
			if (bc != null) {
				if (!bc.isIncluded(myRes))
					bc.update(false);
			}
		}
	}

	/**
	 *  
	 */
	private void classpathChanged() {
		IProjectBuildConfigurator pbc = myBCor.getProjectBuildConfigurator(myRes
				.getProject());
		if (pbc != null) {
			try {
				IJavaProject jp = JavaCore.create(myRes.getProject());
				IClasspathEntry[] cpes = jp.getRawClasspath();
				ArrayList sourcePathes = new ArrayList();
				for (int i = 0; i < cpes.length; i++) {
					if (cpes[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						//make path project relative by
						// removing first segment
						sourcePathes.add(cpes[i].getPath().removeFirstSegments(
								1));
					}
				}
				Iterator iter = pbc.getBuildConfigurations().iterator();
				while (iter.hasNext()) {
					((BuildConfiguration) iter.next())
							.updateSourceFolders(sourcePathes);
				}
			} catch (JavaModelException e) {
			}
		}
	}

	/**
	 *  
	 */
	private void buildConfigRemoved() {
		IProjectBuildConfigurator pbc = myBCor.getProjectBuildConfigurator(myRes
				.getProject());
		if (pbc != null) {
			((ProjectBuildConfigurator)pbc).removeBuildConfiguration((IFile) myRes);
		}
	}

	/**
	 *  
	 */
	private void buildConfigChanged() {
		IProjectBuildConfigurator pbc = myBCor.getProjectBuildConfigurator(myRes
				.getProject());
		if (pbc != null) {
			IBuildConfiguration bc = pbc.getBuildConfiguration((IFile) myRes);
			if (bc != null) {
				bc.update(true);
				((ProjectBuildConfigurator)pbc).configurationChanged(bc);
			}
		}
	}

	private void fileMoved() {
		if ((myRes2 == null) || (myRes == null))
			return;
		if (CoreUtils.ASPECTJ_SOURCE_FILTER.accept(myRes2.getName())
				&& CoreUtils.ASPECTJ_SOURCE_FILTER.accept(myRes
						.getName())) {
			IProjectBuildConfigurator pbc = myBCor
					.getProjectBuildConfigurator(myRes.getProject());
			if (pbc != null) {
				IBuildConfiguration bc = pbc.getActiveBuildConfiguration();
				// reread build config file to update the include/exclude status
				bc.update(true);
			}
		}
	}
}