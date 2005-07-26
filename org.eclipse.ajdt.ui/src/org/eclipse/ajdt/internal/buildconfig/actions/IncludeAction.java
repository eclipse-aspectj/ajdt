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

import java.util.List;

import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Luzius Meisser
 *  
 */
public class IncludeAction extends BuildConfigurationChangeAction {

	public IncludeAction() {
		super();
		actionText = UIMessages.BCLabels_IncludeAction;
	}

	protected Job getJob(final BuildConfiguration bc,
			final List fileList) {
		return new Job("BuildConfiguration include action") {
			protected IStatus run(IProgressMonitor monitor) {
				bc.includeFiles(fileList);
				return Status.OK_STATUS;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ajdt.buildconfigurator.popup.actions.BuildConfigurationChangeAction#isApplicable(java.lang.Object)
	 */
	boolean isObjectApplicable(Object o) {
		try {
			if (o instanceof ICompilationUnit) {
				o = ((ICompilationUnit) o).getCorrespondingResource();
			}
			if (o instanceof IFile) {
				IFile file = (IFile) o;
				if (CoreUtils.ASPECTJ_SOURCE_FILTER.accept(file
						.getName())) {
					ProjectBuildConfigurator pbc = buildConfigurator
						.getProjectBuildConfigurator(file.getProject());
					if ((pbc!=null) && pbc.getActiveBuildConfiguration().isIncluded(
							file)) {
						return false;
					}
					
					// the file is not currently included but we only give the
					// option to include it if it is contained in a source folder
					IJavaProject jp = JavaCore.create(file.getProject());
					IClasspathEntry[] cpes = jp.getRawClasspath();
					IPath fp = file.getFullPath();
					for (int i = 0; i < cpes.length; i++) {
						if (cpes[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
							IPath p = cpes[i].getPath();
							if (p.isPrefixOf(fp)) {
								return true;
							}
						}
					}
				}
			}
		} catch (CoreException e) {
			// assume non-aj-project, do nothing
			// can be ignored
		}
		return false;
	}
}