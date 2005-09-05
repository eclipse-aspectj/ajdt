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

import junit.framework.TestCase;

import org.eclipse.ajdt.ui.tests.ras.PluginFFDCTest;
import org.eclipse.pde.internal.runtime.logview.LogEntry;
import org.eclipse.pde.internal.runtime.logview.LogView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.Workbench;

public aspect ErrorsTest {
	
	
	pointcut uiTestRun() : execution(void UITestCase+.test*())
	 && !this(PluginFFDCTest);
	
	void around(): uiTestRun() {
		IViewPart view;
		try {
			view = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getPage().showView("org.eclipse.pde.runtime.LogView");
			LogView logView = (LogView)view;
			LogEntry[] logs = logView.getLogs();
			int numErrors = logs.length;
			proceed();
			logs = logView.getLogs();
			if(logs.length > numErrors) {
				TestCase.fail("The test added errors to the log");
			}
		} catch (PartInitException e) {
			TestCase.fail("Exception occurred when accessing the log view");
			e.printStackTrace();
		}
	}
	
//	after() returning: uiTestRun() {
//		IViewPart view;
//		try {
//			view = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getPage().showView("org.eclipse.pde.runtime.LogView");
//			LogView logView = (LogView)view;
//			LogEntry[] logs = logView.getLogs();
//			if(logs.length > numErrors) {
//				TestCase.fail("The test added errors to the log");
//			}
//		} catch (PartInitException e) {
//			e.printStackTrace();
//		}
		
//	}

}
