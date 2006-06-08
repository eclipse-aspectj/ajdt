/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.builder;

import java.util.Map;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.internal.ui.AspectJProjectNature;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
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
import org.eclipse.osgi.util.NLS;
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
									UIMessages.Builder_migration_title,
									NLS.bind(UIMessages.Builder_migration_message, project.getName()),
										UIMessages.Builder_migration_toggle, true, null, null);
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
			AJLog.log(AJLog.BUILDER,"AJDT migration builder: addition of new builder failed!"); //$NON-NLS-1$
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog
							.openError(
									null,
									UIMessages.Builder_migration_failed_title,
									NLS.bind(UIMessages.Builder_migration_failed_message,
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
		String jobName = NLS.bind(UIMessages.Builder_migration_build, project.getName());
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