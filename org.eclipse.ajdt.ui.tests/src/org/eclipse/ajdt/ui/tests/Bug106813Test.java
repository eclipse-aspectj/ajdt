/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests;

import org.eclipse.core.resources.IProject;
import org.eclipse.pde.internal.runtime.logview.LogEntry;
import org.eclipse.pde.internal.runtime.logview.LogView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.Workbench;

public class Bug106813Test extends UITestCase {

	
	public void testBug106813() throws Exception {
		IViewPart view = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getPage().showView("org.eclipse.pde.runtime.LogView");
		if(view instanceof LogView) {
			LogView logView = (LogView)view;
			LogEntry[] logs = logView.getLogs();
			int originalNumberOfLogEntries = logs.length;
			IProject project = createPredefinedProject("Bean Example");
			assertTrue("The Bean Example project should have been created", project != null);
			try {
				project.close(null);
				waitForJobsToComplete();
				assertFalse("The Bean Example project should be closed", project.isOpen());
				// Check that no more errors have appeared in the error log
				logs = logView.getLogs();
				assertEquals("The error log should not have had any errors added to it.", originalNumberOfLogEntries, logs.length);
			} finally {
				deleteProject(project);
			}
		}	
	}
	
}
