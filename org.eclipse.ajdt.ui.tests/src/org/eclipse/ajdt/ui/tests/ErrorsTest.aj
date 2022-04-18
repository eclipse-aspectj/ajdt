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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.views.log.AbstractEntry;
import org.eclipse.ui.internal.views.log.LogEntry;
import org.eclipse.ui.internal.views.log.LogView;

/**
 * Aspect that causes tests to fail if they add errors to the error log
 */
public aspect ErrorsTest {


	pointcut uiTestRun() : execution(public void UITestCase+.test*())
		&& !execution(void org.eclipse.ajdt.ui.tests.ajde.UIMessageHandlerTest.testHandleAbortWithMessageAndThrowable())
		&& !execution(void org.eclipse.ajdt.ui.tests.ajde.UIMessageHandlerTest.testHandleErrorWithMessageAndThrowable())
		&& !execution(void org.eclipse.ajdt.ui.tests.ajde.AJDTErrorHandlerTest.testHandleAJDTErrorWithMessage())
		&& !execution(void org.eclipse.ajdt.ui.tests.ajde.AJDTErrorHandlerTest.testHandleAJDTErrorWithMessageAndTitle())
	 	&& !this(PluginFFDCTest);

	void around(): uiTestRun() {
		IViewPart view = null;
		LogView logView = null;
		AbstractEntry[] logs = null;
		int numErrors = -1;
		try {
		    try {
		        view = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getPage().showView("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
		        logView = (LogView) view;
                logs = logView.getElements();
                numErrors = logs.length;
	        } catch (NullPointerException e) {
	            // ignore the npe happens when something is not initialized.
	        }
			proceed();
			if (logView != null) {
    			logs = logView.getElements();
    			StringBuilder failureText = new StringBuilder(); //$NON-NLS-1$
    			if(logs.length > numErrors) { // Check for errors or warnings
    				int numAdded = logs.length - numErrors;
    				for (int i = 0; i < numAdded; i++) { // New entries are always added at the start
    					LogEntry entry = (LogEntry) logs[i];
    					if(entry.getSeverity() == IStatus.ERROR || entry.getSeverity() == IStatus.WARNING) {
    					    // ignore expected exceptions
    						if (!entry.getMessage().contains("org.eclipse.contribution.xref.core.tests.unknownProvider") && //$NON-NLS-1$
                    !entry.getMessage().contains("org.eclipse.contribution.xref.core.tests.UnknownProvider") && //$NON-NLS-1$
                    !entry.getMessage().contains("One or more bundles are not resolved because the following root constraints are not resolved") && //$NON-NLS-1$
                    !entry.getMessage().contains("Could not load repository template extension") && //$NON-NLS-1$
                    !entry.getMessage().contains("The following is a complete list of bundles which are not resolved")) { //$NON-NLS-1$
    							failureText.append("The test added errors to the log:\n").append(entry.getMessage()).append("\n").append(entry.getStack()).append("\n\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    						}
    					}
    				}
    				if (failureText.length() > 0) {
    					TestCase.fail(failureText.toString());
    				}
    			}
			}
		} catch (PartInitException e) {
			e.printStackTrace();
			TestCase.fail("Exception occurred when accessing the log view"); //$NON-NLS-1$
		}
	}


}
