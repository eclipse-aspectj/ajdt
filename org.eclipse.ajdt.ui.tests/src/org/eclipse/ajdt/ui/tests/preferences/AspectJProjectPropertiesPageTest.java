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
import org.eclipse.jface.preference.StringFieldEditor;

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
		assertEquals("expected there not to be an outjar set by default but" +
				" there was","",page.getOutjarValue());
		// set the outjar value
		page.setOutjarValue("output.jar");
		assertEquals("expected the outjar to be output.jar but found "
				+ page.getOutjarValue(),"output.jar",page.getOutjarValue());
		// perform Apply - expect there to be a build
		testLog.clearLog();			
		page.performApply();
		boolean didBuild = testLog.containsMessage("AspectJ reports build successful");
		assertTrue("expected build to occur after performApply but did not",didBuild);		
	}

	public void testPerformOk() throws Exception {
		// check nothing in sfe
		assertEquals("expected there not to be an outjar set by default but" +
				" there was","",page.getOutjarValue());
		// set the outjar value
		page.setOutjarValue("output.jar");
		assertEquals("expected the outjar to be output.jar but found "
				+ page.getOutjarValue(),"output.jar",page.getOutjarValue());
		// perform Ok - expect there to be a build
		testLog.clearLog();			
		page.performOk();
		boolean didBuild = testLog.containsMessage("AspectJ reports build successful");
		assertTrue("expected build to occur after performOky but did not",didBuild);		
	}
	
	public void testPerformCancel() throws Exception {
		// check nothing in sfe
		assertEquals("expected there not to be an outjar set by default but" +
				" there was","",page.getOutjarValue());
		// set the outjar value
		page.setOutjarValue("output.jar");
		assertEquals("expected the outjar to be output.jar but found "
				+ page.getOutjarValue(),"output.jar",page.getOutjarValue());
		// perform cancel - dont expect there to be a build
		testLog.clearLog();			
		page.performCancel();
		boolean didBuild = testLog.containsMessage("AspectJ reports build successful");
		assertFalse("didn't expect build to occur after performCancel " +
				"but a build happened",didBuild);		
	}

	public void testPerformDefaults() throws Exception {
		// check nothing in sfe
		assertEquals("expected there not to be an outjar set by default but" +
				" there was","",page.getOutjarValue());
		// set the outjar value
		page.setOutjarValue("output.jar");
		assertEquals("expected the outjar to be output.jar but found "
				+ page.getOutjarValue(),"output.jar",page.getOutjarValue());
		// perform defaults - dont expect there to be a build because 
		// nothing has changed
		testLog.clearLog();			
		page.performDefaults();
		boolean didBuild = testLog.containsMessage("AspectJ reports build successful");
		assertFalse("didn't expect build to occur after performDefaults " +
				"but a build happened",didBuild);		
	}
	
	public void testPerformApplyThenOk() throws Exception {
		// check nothing in sfe
		assertEquals("expected there not to be an outjar set by default but" +
				" there was","",page.getOutjarValue());
		// set the outjar value
		page.setOutjarValue("output.jar");
		assertEquals("expected the outjar to be output.jar but found "
				+ page.getOutjarValue(),"output.jar",page.getOutjarValue());
		// perform Apply - expect there to be a build
		testLog.clearLog();			
		page.performApply();
		boolean didBuild = testLog.containsMessage("AspectJ reports build successful");
		assertTrue("expected build to occur after performApply but did not",didBuild);		
		// clear the log and performOk which shouldn't result in a build
		testLog.clearLog();
		page.performOk();
		didBuild = testLog.containsMessage("AspectJ reports build successful");
		assertFalse("didn't expect build to occur after performOk because " +
				"already done one but one happened",didBuild);		
		
	}
}
