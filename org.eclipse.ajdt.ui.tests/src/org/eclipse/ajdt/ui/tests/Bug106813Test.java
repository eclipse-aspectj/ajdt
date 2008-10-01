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

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.views.log.AbstractEntry;
import org.eclipse.ui.internal.views.log.LogView;

public class Bug106813Test extends UITestCase {

	
	public void testBug106813() throws Exception {
		IViewPart view = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getPage().showView("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
		if(view instanceof LogView) {
			LogView logView = (LogView)view;
			AbstractEntry[] logs = logView.getElements();
			int originalNumberOfLogEntries = logs.length;
			IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
			assertTrue("The Bean Example project should have been created", project != null); //$NON-NLS-1$
			project.close(null);
			waitForJobsToComplete();
			assertFalse("The Bean Example project should be closed", project.isOpen()); //$NON-NLS-1$
			// Check that no more errors have appeared in the error log
			logs = logView.getElements();
			assertEquals("The error log should not have had any errors added to it.", originalNumberOfLogEntries, logs.length); //$NON-NLS-1$
		}	
	}
	
}
