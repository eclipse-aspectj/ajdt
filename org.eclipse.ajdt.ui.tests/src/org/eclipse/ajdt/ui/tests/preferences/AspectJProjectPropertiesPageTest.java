/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.preferences;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.AspectJProjectPropertiesPage;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.ajdt.ui.tests.testutils.TestLogger;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;

public class AspectJProjectPropertiesPageTest extends UITestCase {

	IProject project;
	IJavaProject jp;
	AspectJProjectPropertiesPage page;
	TestLogger testLog;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		project = createPredefinedProject("Simple AJ Project"); //$NON-NLS-1$
		jp = JavaCore.create(project);
		page = new AspectJProjectPropertiesPage();
		page.setThisProject(project);
		page.setIsTesting(true);
		page.createControl(JavaPlugin.getActiveWorkbenchShell());
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		page.dispose();
	}
	
	public void testPerformApply() throws Exception {
		// check nothing in sfe
		assertEquals("expected there not to be an outjar set by default but" + //$NON-NLS-1$
				" there was","",page.getOutjarValue()); //$NON-NLS-1$ //$NON-NLS-2$
		// set the outjar value
		page.setOutjarValue("output.jar"); //$NON-NLS-1$
		assertEquals("expected the outjar to be output.jar but found " //$NON-NLS-1$
				+ page.getOutjarValue(),"output.jar",page.getOutjarValue()); //$NON-NLS-1$
		// perform Apply - expect there to be a build
		testLog.clearLog();			
		page.performApply();
		waitForJobsToComplete();
		boolean didBuild = testLog.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
		assertTrue("expected build to occur after performApply but did not",didBuild);		 //$NON-NLS-1$
	}

	public void testPerformOk() throws Exception {
		// check nothing in sfe
		assertEquals("expected there not to be an outjar set by default but" + //$NON-NLS-1$
				" there was","",page.getOutjarValue()); //$NON-NLS-1$ //$NON-NLS-2$
		// set the outjar value
		page.setOutjarValue("output.jar"); //$NON-NLS-1$
		assertEquals("expected the outjar to be output.jar but found " //$NON-NLS-1$
				+ page.getOutjarValue(),"output.jar",page.getOutjarValue()); //$NON-NLS-1$
		// perform Ok - expect there to be a build
		testLog.clearLog();			
		page.performOk();
        waitForJobsToComplete();
		boolean didBuild = testLog.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
		assertTrue("expected build to occur after performOky but did not",didBuild);		 //$NON-NLS-1$
	}
	
	public void testPerformCancel() throws Exception {
		// check nothing in sfe
		assertEquals("expected there not to be an outjar set by default but" + //$NON-NLS-1$
				" there was","",page.getOutjarValue()); //$NON-NLS-1$ //$NON-NLS-2$
		// set the outjar value
		page.setOutjarValue("output.jar"); //$NON-NLS-1$
		assertEquals("expected the outjar to be output.jar but found " //$NON-NLS-1$
				+ page.getOutjarValue(),"output.jar",page.getOutjarValue()); //$NON-NLS-1$
		// perform cancel - dont expect there to be a build
		testLog.clearLog();			
		page.performCancel();
        waitForJobsToComplete();
		boolean didBuild = testLog.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
		assertFalse("didn't expect build to occur after performCancel " + //$NON-NLS-1$
				"but a build happened",didBuild);		 //$NON-NLS-1$
	}

	public void testPerformDefaults() throws Exception {
		// check nothing in sfe
		assertEquals("expected there not to be an outjar set by default but" + //$NON-NLS-1$
				" there was","",page.getOutjarValue()); //$NON-NLS-1$ //$NON-NLS-2$
		// set the outjar value
		page.setOutjarValue("output.jar"); //$NON-NLS-1$
		assertEquals("expected the outjar to be output.jar but found " //$NON-NLS-1$
				+ page.getOutjarValue(),"output.jar",page.getOutjarValue()); //$NON-NLS-1$
		// perform defaults - dont expect there to be a build because 
		// nothing has changed
		testLog.clearLog();			
		page.performDefaults();
        waitForJobsToComplete();
		boolean didBuild = testLog.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
		assertFalse("didn't expect build to occur after performDefaults " + //$NON-NLS-1$
				"but a build happened",didBuild);		 //$NON-NLS-1$
	}
	
	public void testPerformApplyThenOk() throws Exception {
		// check nothing in sfe
		assertEquals("expected there not to be an outjar set by default but" + //$NON-NLS-1$
				" there was","",page.getOutjarValue()); //$NON-NLS-1$ //$NON-NLS-2$
		// set the outjar value
		page.setOutjarValue("output.jar"); //$NON-NLS-1$
		assertEquals("expected the outjar to be output.jar but found " //$NON-NLS-1$
				+ page.getOutjarValue(),"output.jar",page.getOutjarValue()); //$NON-NLS-1$
		// perform Apply - expect there to be a build
		testLog.clearLog();	
		page.performApply();
        waitForJobsToComplete();
		boolean didBuild = testLog.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
		assertTrue("expected build to occur after performApply but did not",didBuild);		 //$NON-NLS-1$
		// clear the log and performOk which shouldn't result in a build
		testLog.clearLog();
		page.performOk();
        waitForJobsToComplete();
		didBuild = testLog.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
		assertFalse("didn't expect build to occur after performOk because " + //$NON-NLS-1$
				"already done one but one happened",didBuild);		 //$NON-NLS-1$
		
	}
}
