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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.views.log.AbstractEntry;
import org.eclipse.ui.internal.views.log.LogEntry;
import org.eclipse.ui.internal.views.log.LogView;

/**
 * Test that we don't get any spurious errors or warnings in the log on startup
 * (E.g. bug 106707)
 */
public class ErrorLogTest extends UITestCase {

	private static final String KNOWN_MSG1 = "org.eclipse.contribution.xref.core.tests.unknownprovider"; //$NON-NLS-1$
    private static final String KNOWN_MSG2 = "The following is a complete list"; //$NON-NLS-1$
    private static final String KNOWN_MSG3 = "One or more bundles"; //$NON-NLS-1$
    private static final String KNOWN_MSG4 = "Could not locate the running profile instance."; //$NON-NLS-1$
    private static final String KNOWN_MSG5 = "ITDInserter"; //$NON-NLS-1$
	
	
    public boolean matchesMsg1(String msg) {
        return msg.toLowerCase().indexOf(KNOWN_MSG1) != -1;
    }

    public boolean matchesMsg2(String msg) {
        return msg.startsWith(KNOWN_MSG2);
    }
    public boolean matchesMsg3(String msg) {
        return msg.startsWith(KNOWN_MSG3);
    }
    public boolean matchesMsg4(String msg) {
        return msg.startsWith(KNOWN_MSG4);
    }
    public boolean matchesMsg5(String msg) {
        return msg.indexOf(KNOWN_MSG5) >= 0;
    }


    public void testDisabled() throws Exception {
        System.out.println("Tests in this class have been disabled");
    }
    
	public void _testNoWarningsOnStartup() throws Exception {
		IViewPart view = Workbench.getInstance().getActiveWorkbenchWindow()
				.getActivePage().getActivePart().getSite().getPage().showView(
						"org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
		if (view instanceof LogView) {
			LogView logView = (LogView) view;
			AbstractEntry[] logs = logView.getElements();
			// Ignore information entries in the log
			List errorsAndWarnings = new ArrayList();
			for (int i = 0; i < logs.length; i++) {
				LogEntry entry = (LogEntry) logs[i];
				if (entry.getSeverity() == IStatus.ERROR
						|| entry.getSeverity() == IStatus.WARNING) {
				    String msg = entry.getMessage();
					if (!matchesMsg1(msg) &&
					        !matchesMsg2(msg) && 
                            !matchesMsg3(msg) && 
                            !matchesMsg4(msg) && 
                            !matchesMsg5(msg)) {
					    // ignore messages about missing bundles that are not from AJDT
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
