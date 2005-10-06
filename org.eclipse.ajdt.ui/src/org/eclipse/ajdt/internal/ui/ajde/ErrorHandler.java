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
	 * Display an error dialog - only called by AspectJ
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
	 * Display an error dialog with exception - only called by AspectJ
	 */
	public void handleError(String message, Throwable t) {
		handleInternalError(UIMessages.ajErrorDialogTitle,
				message, t);
	}
	
	/**
	 * Display an error dialog with exception - only called by AJDT (not AspectJ)
	 */
	public static void handleAJDTError(String title, String message, CoreException t) {
		handleInternalError(title, message, t);
	}

	/**
	 * Display an error dialog with exception - only called by AJDT (not AspectJ)
	 */
	public static void handleAJDTError(String message, CoreException t) {
		handleInternalError(UIMessages.ajdtErrorDialogTitle, message, t);
	}
	/**
	 * Display an error dialog with exception
	 */
	private static void handleInternalError(final String title, final String message, Throwable t) {
		final IStatus status;
		final String shortMessage;
		final String longMessage;
		// If the throwable is a CoreException then retrieve its status, otherwise
		// build a new status object
		if (t instanceof CoreException) {
			status = ((CoreException) t).getStatus();
			longMessage = message;
			shortMessage = message;
		} else {
			String newline = System.getProperty("line.separator"); //$NON-NLS-1$
			shortMessage = UIMessages.ajErrorText;
			StringBuffer sb = new StringBuffer();
			if (t != null) {
				StackTraceElement[] ste = t.getStackTrace();
				sb.append(t.getClass().getName());
				sb.append(newline);
				for (int i = 0; i < ste.length; i++) {
					sb.append("at "); //$NON-NLS-1$
					sb.append(ste[i].toString());
					sb.append(newline);
				}
			}
			longMessage = sb.toString() + newline + message;
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
						AJDTErrorDialog.openError(
							shell,
							title,
							shortMessage,
							longMessage);

					}
				} catch (Exception e) {
				}
			}
		});
	}

}