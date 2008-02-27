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

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.internal.views.log.AbstractEntry;
import org.eclipse.ui.internal.views.log.LogEntry;
import org.eclipse.ui.internal.views.log.LogView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.Workbench;

/**
 * Test that we don't get any spurious errors or warnings in the log on startup
 * (E.g. bug 106707)
 */
public class ErrorLogTest extends UITestCase {

	private static final String KNOWN_MSG = "org.eclipse.contribution.xref.core.tests.unknownprovider"; //$NON-NLS-1$

	public void testNoWarningsOnStartup() throws Exception {
		IViewPart view = openLogView();
		if (view instanceof LogView) {
			LogView logView = (LogView) view;
			AbstractEntry[] logs = logView.getElements();
			// Ignore information entries in the log
			List errorsAndWarnings = new ArrayList();
			for (int i = 0; i < logs.length; i++) {
				LogEntry le = (LogEntry)logs[i];
				if (le.getSeverity() == IStatus.ERROR
						|| le.getSeverity() == IStatus.WARNING) {
					if (le.getMessage().toLowerCase().indexOf(KNOWN_MSG) == -1) {
						errorsAndWarnings.add(logs[i]);
					}
				}
			}
			if (errorsAndWarnings.size() > 0) {
				StringBuffer errors = new StringBuffer();
				boolean ignore = false;
				for (Iterator iter = errorsAndWarnings.iterator(); iter
						.hasNext();) {
					LogEntry element = (LogEntry) iter.next();
					errors.append(element.getMessage());
					errors.append(" (" + element.getPluginId() + ")\n"); //$NON-NLS-1$ //$NON-NLS-2$
					if (element.hasChildren()) {
						Object[] sub = element.getChildren(null);
						for (int i = 0; i < sub.length; i++) {
							if (sub[i] instanceof LogEntry) {
								LogEntry s = (LogEntry) sub[i];
								String msg = s.getMessage();
								errors.append("    " + msg); //$NON-NLS-1$
								errors.append(" (" + s.getPluginId() + ")\n"); //$NON-NLS-1$ //$NON-NLS-2$
								// ignore if all child warnings are related to
								// unresolved jdt plugins (probably caused by
								// missing java6 constraints)
								if ((element.getSeverity() == IStatus.WARNING)
										&& (msg.indexOf("org.eclipse.jdt") != -1) && (msg.indexOf("was not resolved") != -1)) { //$NON-NLS-1$//$NON-NLS-2$
									ignore = true;
								} else {
									ignore = false;
								}
							}
						}
					}
				}
				if (!ignore) {
					fail("There should be no unexpected entries in the error log. Found:\n" + errors.toString()); //$NON-NLS-1$
				}
			}
		} else {
			fail("Could not find the Error log."); //$NON-NLS-1$
		}
	}

}
