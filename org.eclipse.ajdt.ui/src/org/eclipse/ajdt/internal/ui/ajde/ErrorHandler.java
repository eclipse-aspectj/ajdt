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

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

//import org.aspectj.ajde.IErrorHandler;

/**
 * ErrorHandler displays warnings and errors produced from the AJ Tools plugin
 */
public class ErrorHandler implements org.aspectj.ajde.ErrorHandler {

	/**
	 * Display a warning dialog
	 */
	public void handleWarning(String message) {

		// Finalise the variables to allow them to be included in the run()
		// block below
		final Status status =
			new Status(Status.WARNING, AspectJUIPlugin.PLUGIN_ID, Status.OK, message, null);
		final String message_final = message;

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
							AspectJUIPlugin.getResourceString("ajWarningDialogTitle"),
							message_final );
					} else {
						AspectJUIPlugin.getDefault().getLog().log(
							new Status(
								Status.ERROR,
								AspectJUIPlugin.PLUGIN_ID,
								Status.OK,
								"Unable to determine workbenchwindow instance for opening an error dialog",
								null));
					}
				} catch (Throwable t) {
					//ErrorHandler.handleError("Document Outline update failed", t);	
				}
			}
		});

	}
	

	/**
	 * Display an error dialog (no exception)
	 */
	public void handleError(String errorMessage) {
		handleError(errorMessage, null);
	}

	/**
	 * Display an error dialog with exception trace, and record the error in
	 * the log for the plugin
	 */
	public void handleError(String message, Throwable t) {
		final IStatus status;
		final String message_final = message;

//Err, wtf is happening here? t could be null if handleError was called with just a message
// like the above method allows??!

		System.err.println( t.getMessage() );
		t.printStackTrace( System.err );


		// If the throwable is a CoreException then retrieve its status, otherwise
		// build a new status object
		if (t instanceof CoreException) {
			status = ((CoreException) t).getStatus();
		} else {
			status =
				new Status(Status.ERROR, AspectJUIPlugin.PLUGIN_ID, Status.OK, message, t);
		}

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
							AspectJUIPlugin.getResourceString("ajErrorDialogTitle"),
							message_final,
							status);

					} else {
						AspectJUIPlugin.getDefault().getLog().log(
							new Status(
								Status.ERROR,
								AspectJUIPlugin.PLUGIN_ID,
								Status.OK,
								"Unable to determine workbenchwindow instance for opening an error dialog",
								null));
					}
				} catch (Throwable t) {
					//ErrorHandler.handleError("Document Outline update failed", t);	
				}
			}
		});

		// Record the problem in the log
		AspectJUIPlugin.getDefault().getLog().log(status);
	}

}