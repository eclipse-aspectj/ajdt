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
import org.eclipse.ajdt.internal.ui.ajde.ErrorHandler;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * @author Luzius Meisser
 *  
 */
public class ExcludeAction extends BuildConfigurationChangeAction {
	public ExcludeAction() {
		super();
		actionText = UIMessages.BCLabels_ExcludeAction;
	}

	protected Job getJob(final BuildConfiguration bc,
			final List fileList) {
		return new Job(UIMessages.excludeAction_job_name) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					bc.excludeFiles(fileList);
				} catch (CoreException e) {
					ErrorHandler.handleAJDTError(
									UIMessages.buildConfig_exceptionIncluding,
									e);
				}
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
					if (pbc != null)
						return pbc.getActiveBuildConfiguration().isIncluded(
								file);
				}
			}
		} catch (CoreException e) {
			// assume non-aj-project
		}
		return false;
	}

}