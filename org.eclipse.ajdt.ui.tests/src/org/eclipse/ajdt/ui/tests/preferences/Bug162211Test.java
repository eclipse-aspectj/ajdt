/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.preferences;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.ajde.AJDTErrorHandler;
import org.eclipse.ajdt.internal.ui.preferences.AJCompilerPreferencePage;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.ajdt.ui.tests.testutils.TestLogger;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

public class Bug162211Test extends UITestCase {

	IProject project;

	IJavaProject jp;

	AJCompilerPreferencePage page;

	TestLogger testLog;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		AJDTErrorHandler.setShowErrorDialogs(false);
		testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		project = createPredefinedProject("project.java.Y"); //$NON-NLS-1$
		jp = JavaCore.create(project);
		page = new AJCompilerPreferencePage();
		page.createControl(JavaPlugin.getActiveWorkbenchShell());
		page.setIsTesting(true);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		AJDTErrorHandler.setShowErrorDialogs(true);
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		store.setValue(AspectJPreferences.COMPILER_OPTIONS, ""); //$NON-NLS-1$
		page.dispose();
		AspectJPlugin.getDefault().setAJLogger(null);
	}

	/**
	 * Test for bug 162211: Causing a full build should not cause problems for
	 * Java projects
	 */
	public void testPerformApplyAndBuild() throws Exception {
		// make a change on the page which should result in asking
		// whether the user wants to do a build
		page.setButtonChanged();
		try {
			// want to do a build when asked after pressing "apply"
			page.setBuildNow(true);
			// perform Apply - expect there to be a build
			testLog.clearLog();
			page.performApply();
			// if the above triggers an AJDT internal error, it will be written
			// to the log and thereby cause this test to fail

			// also check that the Java project wasn't built by the AJ builder
			assertFalse(
					"Should not have used AspectJ builder on plain Java project", //$NON-NLS-1$
					testLog
							.containsMessage("unable to find org.aspectj.lang.JoinPoint")); //$NON-NLS-1$
		} finally {
			// restore preference settings!
			page.setButtonChanged();
			page.performOk();
		}
	}

}
