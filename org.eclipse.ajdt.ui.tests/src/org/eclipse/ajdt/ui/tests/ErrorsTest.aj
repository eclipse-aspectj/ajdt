/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests;

import junit.framework.TestCase;

import org.eclipse.ajdt.ui.tests.ras.PluginFFDCTest;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.pde.internal.runtime.logview.LogEntry;
import org.eclipse.pde.internal.runtime.logview.LogView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.Workbench;

/**
 * Aspect that causes tests to fail if they add errors to the error log
 */
public aspect ErrorsTest {
	
	
	pointcut uiTestRun() : execution(public void UITestCase+.test*())
		&& !execution(void org.eclipse.ajdt.ui.tests.ajde.ErrorHandlerTest.testHandleErrorWithMessageAndThrowable())
		&& !execution(void org.eclipse.ajdt.ui.tests.ajde.AJDTErrorHandlerTest.testHandleAJDTErrorWithMessage())
		&& !execution(void org.eclipse.ajdt.ui.tests.ajde.AJDTErrorHandlerTest.testHandleAJDTErrorWithMessageAndTitle())
	 	&& !this(PluginFFDCTest);
	
	void around(): uiTestRun() {
		IViewPart view;
		try {
			view = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getPage().showView("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
			LogView logView = (LogView)view;
			LogEntry[] logs = logView.getLogs();
			int numErrors = logs.length;
			proceed();
			logs = logView.getLogs();
			String failureText = ""; //$NON-NLS-1$
			if(logs.length > numErrors) { // Check for errors or warnings
				int numAdded = logs.length - numErrors;
				for (int i = 0; i < numAdded; i++) { // New entries are always added at the start
					LogEntry entry = logs[i];
					if(entry.getSeverity() == IStatus.ERROR || entry.getSeverity() == IStatus.WARNING) {
						failureText += "The test added errors to the log: " + entry.getMessage() + "\n" + entry.getStack() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
				if (failureText.length() > 0) {
					TestCase.fail(failureText);
				}
			}
		} catch (PartInitException e) {
			e.printStackTrace();
			TestCase.fail("Exception occurred when accessing the log view"); //$NON-NLS-1$
		}
	}
	

}
