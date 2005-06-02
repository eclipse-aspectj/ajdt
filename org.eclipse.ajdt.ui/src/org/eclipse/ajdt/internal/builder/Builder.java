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

import org.eclipse.ajdt.internal.core.AJLog;
import org.eclipse.ajdt.internal.ui.AspectJProjectNature;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;

/**
 * This is now the "old" builder, purely for compatibility with existing
 * projects which have the old builder id defined in their .project files. When
 * a build is requested, it first checks if the new builder id is also present.
 * If it is, no action is required. If not, the new builder is added, and the
 * old one optionally removed (depending on preference / user choice).
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
		final IProject project = getProject();
		
		if (AspectJProjectNature.hasNewBuilder(project)) {
			// don't need to do anything, the new builder will do the actual
			// build
			return null;
		}
		
		// adding this so we don't get the builder migration 
		// dialog until after we have run the migration wizard
		if(!AspectJPreferences.dontRunMigrationWizard()) {
		    return null;
		}

		boolean removeOldBuilder = true;
		if (AspectJPreferences.isAutoBuilderMigrationEnabled()) {
			// use previous answer
			removeOldBuilder = AspectJPreferences
					.isAutoBuilderMigrationSetToRemoveOldBuilder();
		} else {
			// prompt user
			final boolean[] settings = new boolean[2];
			AspectJUIPlugin.getDefault().getDisplay().syncExec(new Runnable() {
				public void run() {
					MessageDialogWithToggle dialog = MessageDialogWithToggle
							.openYesNoCancelQuestion(
									null,
									AspectJUIPlugin
											.getResourceString("Builder.migration.title"), //$NON-NLS-1$
									AspectJUIPlugin.getFormattedResourceString(
											"Builder.migration.message", //$NON-NLS-1$
											project.getName()),
									AspectJUIPlugin
											.getResourceString("Builder.migration.toggle"), //$NON-NLS-1$
									true, null, null);
//					System.out.println("ret: "+dialog.getReturnCode());
					settings[0] = (dialog.getReturnCode() == IDialogConstants.YES_ID);
					settings[1] = dialog.getToggleState();
				}
			});

			if (settings[1]) {
				// perform the same for all other projects without prompting
				AspectJPreferences
						.setAutoBuilderMigrationRemoveOldBuilder(settings[0]);
				AspectJPreferences.setAutoBuilderMigrationEnabled(true);
			}

			removeOldBuilder = settings[0];
		}

		// add new builder id
		AspectJProjectNature.addNewBuilder(project);

		if (!AspectJProjectNature.hasNewBuilder(project)) {
			// addition of new builder failed for some reason
			AJLog
					.log("AJDT migration builder: addition of new builder failed!");
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog
							.openError(
									null,
									AspectJUIPlugin
											.getResourceString("Builder.migration.failed.title"), //$NON-NLS-1$
									AspectJUIPlugin.getFormattedResourceString(
											"Builder.migration.failed.message", //$NON-NLS-1$
											project.getName()));
				}
			});
			return null;
		}

		if (removeOldBuilder) {
			AspectJProjectNature.removeOldBuilder(project);
		}

		// request a full build, which will then call the new builder.
		// we don't simply call the new builder directly, because the
		// eclipse build manager caches the build state against the builder,
		// so it would cache the state from the new builder against this
		// old builder.
		String jobName = AspectJUIPlugin.getFormattedResourceString(
				"Builder.migration.build", project.getName()); //$NON-NLS-1$
		Job buildJob = new Job(jobName) {
			public IStatus run(IProgressMonitor monitor) {
				try {
					project
							.build(IncrementalProjectBuilder.FULL_BUILD,
									monitor);
				} catch (CoreException e) {
				}
				return Status.OK_STATUS;
			}
		};
		buildJob.schedule();

		return null;
	}

}