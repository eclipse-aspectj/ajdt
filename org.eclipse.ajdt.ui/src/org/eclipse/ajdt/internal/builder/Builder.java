/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.builder;

import java.util.Map;

import org.eclipse.ajdt.internal.ui.AspectJProjectNature;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * This is now the "old" builder, purely for compatibility with existing
 * projects which have the old builder id defined in their .project files. When
 * a build is requested, it updates the builder entry to the new builder id, and
 * then delegates the build to the new builders. The migration to the new
 * builder should therefore be transparent to the user.
 */
public class Builder extends IncrementalProjectBuilder {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		// reset builder, to pick up the new id
		AspectJProjectNature projNature = new AspectJProjectNature();
		projNature.setProject(getProject());
		projNature.deconfigure();
		projNature.configure();

		// request a full build, which will then call the new builder.
		// we don't simply call the new builder directly, because the
		// eclipse build manager caches the build state against the builder,
		// so it would cache the state from the new builder against this
		// old builder.
		final IProject project = getProject();
		String jobName = AspectJUIPlugin.getFormattedResourceString(
				"migration.build", project.getName()); //$NON-NLS-1$
		Job buildJob = new Job(jobName) {
			public IStatus run(IProgressMonitor monitor) {
				try {
					project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
				} catch (CoreException e) {
				}
				return Status.OK_STATUS;
			}
		};
		buildJob.schedule();

		return null;
	}

}
