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

import java.util.List;
import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.pde.internal.runtime.logview.LogEntry;
import org.eclipse.pde.internal.runtime.logview.LogView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.Workbench;

/**
 * Test that we don't get any spurious errors or warnings in the log on startup
 * (E.g. bug 106707)
 */
public class ErrorLogTest extends UITestCase {

	
	public void testNoWarningsOnStartup() throws Exception {
		IViewPart view = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getPage().showView("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
		if(view instanceof LogView) {
			LogView logView = (LogView)view;
			LogEntry[] logs = logView.getLogs();
			// Ignore information entries in the log
			List errorsAndWarnings = new ArrayList();
			for (int i = 0; i < logs.length; i++) {
				if(logs[i].getSeverity() == IStatus.ERROR || 
						logs[i].getSeverity() == IStatus.WARNING) {
					errorsAndWarnings.add(logs[i]);
				}
			}
			LogEntry[] logsMinusInfos = new LogEntry[errorsAndWarnings.size()];
			for (int i = 0; i < logsMinusInfos.length; i++) {
				logsMinusInfos[i] = (LogEntry) errorsAndWarnings.get(i);
			}
			assertTrue("There should be exactly two entries in the log, found " + logsMinusInfos.length + ": " + logsMinusInfos[logsMinusInfos.length - 1].getMessage() + ", ...", logsMinusInfos.length == 2); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			fail("Could not find the Error log."); //$NON-NLS-1$
		}
	}
	
}
