/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - initial version
 *     Helen Hawkins   - updated for new ajde interface (bug 148190)
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Error handler used exclusively by AJDT. Handles errors
 * by creating an error dialog.
 */
public class AJDTErrorHandler {

	static final int MSG_LIMIT = 600;
	static boolean showDialogs = true;
	
	/**
	 * Rethrow runtime exceptions for errors instead of showing the error dialog
	 * (useful for testing)
	 * 
	 * @param show
	 */
	public static void setShowErrorDialogs(boolean show) {
		showDialogs = show;
	}
	
	/**
	 * Display an error dialog with exception - only called by AJDT (not AspectJ)
	 */
	public static void handleAJDTError(String message, Throwable t) {
		handleInternalError(UIMessages.ajdtErrorDialogTitle, message, t);
	}
	
	/**
	 * Display an error dialog with exception - only called by AJDT (not AspectJ)
	 */
	public static void handleAJDTError(String title, String message, Throwable t) {
		AJDTErrorHandler.handleInternalError(title, message, t);
	}

	/**
	 * Display an error dialog with exception
	 */
	public static void handleInternalError(final String title, final String message, Throwable t) {
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
	
		if (!AJDTErrorHandler.showDialogs) {
			// rethrow exception instead of showing dialog
			throw new RuntimeException(t);
		}
		
		AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow iww = AspectJUIPlugin.getDefault()
						.getActiveWorkbenchWindow();
				// This really should not be null ...
				if (iww != null) {
					Shell shell = iww.getShell();
					AJDTErrorDialog.openError(shell, title, shortMessage,
							AJDTErrorHandler.limitMessageLength(longMessage, AJDTErrorHandler.MSG_LIMIT));
				}
			}
		});
	}

	// 154483: limit the length of messages used in dialogs
	static String limitMessageLength(String msg, int length) {
		if (msg.length() > length) {
			int endLength = 150;
			String gap = " ... "; //$NON-NLS-1$
			return msg.substring(0, length - endLength - gap.length())
				+ " ... " + msg.substring(msg.length() - endLength); //$NON-NLS-1$
		} else {
			return msg;
		}
	}


}
