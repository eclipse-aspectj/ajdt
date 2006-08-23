/**********************************************************************
Copyright (c) 2002 2006 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * ErrorHandler handles warnings and errors produced from the AJ Tools plugin
 */
public class ErrorHandler implements org.aspectj.ajde.ErrorHandler {

	private static final int MSG_LIMIT = 600;
	
	/**
	 * Handle warnings reported by AspectJ
	 */
	public void handleWarning(final String message) {
		// 154483 instead of displaying a dialog we create a problem marker
		AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					IWorkspaceRunnable r = new IWorkspaceRunnable() {
						public void run(IProgressMonitor monitor) {
							IProject project = AspectJPlugin.getDefault()
									.getCurrentProject();
							if (project != null) {
								try {
									IMarker marker = project
											.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
									marker
											.setAttribute(
													IMarker.SEVERITY,
													new Integer(
															IMarker.SEVERITY_ERROR));
									marker.setAttribute(IMarker.MESSAGE,
											limitMessageLength(message,
													MSG_LIMIT));
								} catch (CoreException e) {
								}
							}
						}
					};
					AspectJPlugin.getWorkspace().run(r, null);
				} catch (CoreException t) {
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
							limitMessageLength(message,MSG_LIMIT));
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
	
	// 154483: limit the length of messages used in dialogs
	private static String limitMessageLength(String msg, int length) {
		if (msg.length() > length) {
			int endLength = 150;
			String gap = " ... ";
			return msg.substring(0, length - endLength - gap.length())
				+ " ... " + msg.substring(msg.length() - endLength);
		} else {
			return msg;
		}
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
		
		AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow iww = AspectJUIPlugin.getDefault()
						.getActiveWorkbenchWindow();
				// This really should not be null ...
				if (iww != null) {
					Shell shell = iww.getShell();
					AJDTErrorDialog.openError(shell, title, shortMessage,
							limitMessageLength(longMessage, MSG_LIMIT));
				}
			}
		});
	}

}