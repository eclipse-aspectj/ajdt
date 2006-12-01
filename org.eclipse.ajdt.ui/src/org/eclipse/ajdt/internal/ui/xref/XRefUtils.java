/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.xref;

import org.eclipse.ajdt.internal.ui.ajde.AJDTErrorHandler;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

public class XRefUtils {
	private static boolean triedToOpenXRefView;

	public static void openXRefViewIfFirstTime() {
		if (triedToOpenXRefView) {
			return;
		}
		// only try once
		triedToOpenXRefView = true;
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		String workspaceLocation = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toString();
		if (!store.getBoolean(AspectJPreferences.DONE_AUTO_OPEN_XREF_VIEW
				+ workspaceLocation)) {
			store.setValue(AspectJPreferences.DONE_AUTO_OPEN_XREF_VIEW
					+ workspaceLocation, true);
			// open xref view in current perspective if we haven't opened the
			// xref view before.
			Job job = new UIJob(
					UIMessages.BuildConfigurator_workbench_openXRefView) {
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						AspectJUIPlugin.getDefault().getActiveWorkbenchWindow()
								.getActivePage().showView(XReferenceView.ID);
						return Status.OK_STATUS;
					} catch (PartInitException e) {
						AJDTErrorHandler
								.handleAJDTError(
										UIMessages.BuildConfigurator_ErrorOpeningXRefView,
										e);
						return Status.OK_STATUS;
					}
				}
			};
			job.schedule();
		}

	}
}
