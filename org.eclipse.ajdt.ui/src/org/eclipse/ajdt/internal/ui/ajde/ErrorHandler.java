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
package org.eclipse.ajdt.internal.ui.ajde;

import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * ErrorHandler displays warnings and errors produced from the AJ Tools plugin
 */
public class ErrorHandler implements org.aspectj.ajde.ErrorHandler {

	/**
	 * Display a warning dialog
	 */
	public void handleWarning(final String message) {
		// Need to open the dialog on the right thread.  In some cases we *might*
		// be on the right thread - since this ErrorHandler class is usable from
		// inside the plugin as well as AJDE.  This might not be the best approach,
		// perhaps we need our own to use from the plugin, so that we are not
		// creating unnecessary threads.  But I suppose this is the 'error case'
		// so performance isn't the critical factor here.
		AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					IWorkbenchWindow iww = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow();
					// This really should not be null ...
					if (iww != null) {
						Shell shell = iww.getShell();
						MessageDialog.openWarning(
							shell,
							UIMessages.ajWarningDialogTitle,
							message);
					}
				} catch (Exception t) {
				}
			}
		});
	}

	/**
	 * Display an error dialog
	 */
	public void handleError(final String message) {
		AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					IWorkbenchWindow iww = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow();
					// This really should not be null ...
					if (iww != null) {
						Shell shell = iww.getShell();
						MessageDialog.openError(
							shell,
							UIMessages.ajErrorDialogTitle,
							message);
					}
				} catch (Exception t) {
				}
			}
		});
	}

	/**
	 * Display an error dialog with exception
	 */
	public void handleError(String message, Throwable t) {
		handleError(UIMessages.ajErrorDialogTitle,
				message, t);
	}
	
	/**
	 * Display an error dialog with exception
	 */
	public void handleError(final String title, final String message, Throwable t) {
		final IStatus status;

		// If the throwable is a CoreException then retrieve its status, otherwise
		// build a new status object
		if (t instanceof CoreException) {
			status = ((CoreException) t).getStatus();
		} else {
			status =
				new Status(Status.ERROR, AspectJUIPlugin.PLUGIN_ID, Status.OK, message, t);
		}

		// make sure we log the exception, as it may have come from AspectJ, and therefore
		// it will not have been handled by our FFDC aspect
		AspectJUIPlugin.getDefault().getLog().log(status);
		
		// See notes in handleWarning - could collapse these two code blocks
		// that create threads into one.

		AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					IWorkbenchWindow iww = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow();
					// This really should not be null ...
					if (iww != null) {
						Shell shell = iww.getShell();
						// FIXME - Including message in this openError call and in the status object
						// causes the messy duplication of the message in the dialog that appears.
						ErrorDialog.openError(
							shell,
							title,
							message,
							status);

					}
				} catch (Exception e) {
				}
			}
		});
	}

}