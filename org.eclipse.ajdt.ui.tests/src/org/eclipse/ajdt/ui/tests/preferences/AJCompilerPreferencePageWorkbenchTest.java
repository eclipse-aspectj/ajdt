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

public class AJCompilerPreferencePageWorkbenchTest extends UITestCase {

	IProject project;

	IJavaProject jp;

	AJCompilerPreferencePage page;

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
		page = new AJCompilerPreferencePage();
		page.createControl(JavaPlugin.getActiveWorkbenchShell());
		page.setIsTesting(true);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		store.setValue(AspectJPreferences.COMPILER_OPTIONS, ""); //$NON-NLS-1$
		page.dispose();
		AspectJPlugin.getDefault().setAJLogger(null);
	}

	/**
	 * Change the value of one of the buttons
	 * 
	 * 1. Press "Apply" and "OK" to rebuild now ==> expect build 2. Press "OK"
	 * and "OK" to rebuild now ==> don't expect build
	 */
	public void testPerformApplyAndBuild() throws Exception {
		// make a change on the page which should result in asking
		// whether the user wants to do a build
		page.setButtonChanged();
		try {
			// want to do a build when asked
			page.setBuildNow(true);
			// perform Apply - expect there to be a build
			testLog.clearLog();
			page.performApply();
			waitForJobsToComplete();
			boolean didBuild = testLog
					.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
			assertTrue(
					"expected build to occur after performApply because asked " //$NON-NLS-1$
							+ "for one but build didn't happen", didBuild); //$NON-NLS-1$
			// pressing "Ok" now should not result in a build
			testLog.clearLog();
			page.performOk();
	        waitForJobsToComplete();
			didBuild = testLog
					.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
			assertFalse(
					"did not expect a build to occur after performOk because " //$NON-NLS-1$
							+ " already applied changes but build happened anyway", //$NON-NLS-1$
					didBuild);
		} finally {
			// restore preference settings!
			page.setButtonChanged();
			page.performOk();
		}
	}

	/**
	 * Change the value of one of the buttons
	 * 
	 * 1. Press "Apply" and "Cancel" to not rebuild now ==> don't expect build
	 * 2. Press "OK" and "Cancel" to not rebuild now ==> don't expect build
	 */
	public void testPerformApplyAndDontBuild() throws Exception {
		// make a change on the page which should result in asking
		// whether the user wants to do a build
		page.setButtonChanged();
		try {
			// don't want to do a build when asked after pressing "apply"
			page.setBuildNow(false);
			// perform Apply - don't expect there to be a build
			testLog.clearLog();
			page.performApply();
	        waitForJobsToComplete();
			boolean didBuild = testLog
					.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
			assertFalse(
					"did not expect a build to occur after pressing Apply" //$NON-NLS-1$
							+ " because said didn't want one but build happened anyway", //$NON-NLS-1$
					didBuild);
			// pressing "Ok" now should still not result in a build
			// (because have set dont' want to build)
			testLog.clearLog();
			page.performOk();
	        waitForJobsToComplete();
			didBuild = testLog
					.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
			assertFalse(
					"did not expect a build to occur after performOk because " //$NON-NLS-1$
							+ " said didn't want one but build happened anyway", //$NON-NLS-1$
					didBuild);
		} finally {
			// restore preference settings!
			page.setButtonChanged();
			page.performOk();
		}
	}

	/**
	 * Change the non standard options 1. press "Apply" and "no" to rebuild now
	 * ==> don't expect a build 2. press "Ok" and "no" to rebuild now ==> don't
	 * expect a build
	 */
	public void testChangeNonStandardOptionsAndApply() {
		// make a change on the page which should result in asking
		// whether the user wants to do a build
		page.setNonStandardOption("-showWeaveInfo"); //$NON-NLS-1$
		// don't want to do a build when asked after pressing "apply"
		page.setBuildNow(false);
		// perform Apply - don't expect there to be a build
		testLog.clearLog();
		page.performApply();
		waitForJobsToComplete();
		boolean didBuild = testLog
				.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
		assertFalse("did not expect a build to occur after pressing Apply" //$NON-NLS-1$
				+ " because said didn't want one but build happened anyway", //$NON-NLS-1$
				didBuild);
		// pressing "Ok" now should still not result in a build
		// (because have set dont' want to build)
		testLog.clearLog();
		page.performOk();
        waitForJobsToComplete();
		didBuild = testLog.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
		assertFalse("did not expect a build to occur after performOk because " //$NON-NLS-1$
				+ " said didn't want one but build happened anyway", didBuild); //$NON-NLS-1$
	}

	/**
	 * Change the value of one of the buttons
	 * 
	 * 1. Press "Apply" and "Cancel" to not rebuild now ==> don't expect build
	 * 2. Press "OK" and "OK" to rebuild now ==> expect build
	 */
	public void testPerformApplyAndDontBuild_2() throws Exception {
		// make a change on the page which should result in asking
		// whether the user wants to do a build
		page.setButtonChanged();
		try {
			// don't want to do a build when asked after pressing "apply"
			page.setBuildNow(false);
			// perform Apply - don't expect there to be a build
			testLog.clearLog();
			page.performApply();
	        waitForJobsToComplete();
			boolean didBuild = testLog
					.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
			assertFalse(
					"did not expect a build to occur after pressing Apply" //$NON-NLS-1$
							+ " because said didn't want one but build happened anyway", //$NON-NLS-1$
					didBuild);
			// want to build when press ok
			page.setBuildNow(true);
			// pressing "Ok" now should still not result in a build
			// (because have set dont' want to build)
			testLog.clearLog();
			page.performOk();
	        waitForJobsToComplete();
			didBuild = testLog
					.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
			assertFalse("expected a build to occur after performOk but one" //$NON-NLS-1$
					+ " didn't happen", didBuild); //$NON-NLS-1$
		} finally {
			// restore preference settings!
			page.setButtonChanged();
			page.performOk();
		}
	}

	/**
	 * Change the non standard options 1. press "Apply" and "no" to rebuild now
	 * ==> don't expect a build 2. press "Ok" and "yes" to rebuild now ==> do
	 * expect a build
	 */
	public void testChangeNonStandardOptionsAndApply_3() {
		// make a change on the page which should result in asking
		// whether the user wants to do a build
		page.setNonStandardOption("-showWeaveInfo"); //$NON-NLS-1$
		// don't want to do a build when asked after pressing "apply"
		page.setBuildNow(false);
		// perform Apply - don't expect there to be a build
		testLog.clearLog();
		page.performApply();
        waitForJobsToComplete();
		boolean didBuild = testLog
				.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
		assertFalse("did not expect a build to occur after pressing Apply" //$NON-NLS-1$
				+ " because said didn't want one but build happened anyway", //$NON-NLS-1$
				didBuild);
		// pressing "Ok" now should still not result in a build - this is
		// mirroring the jdt compiler pages
		page.setBuildNow(true);
		testLog.clearLog();
		page.performOk();
        waitForJobsToComplete();
		didBuild = testLog.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
		assertFalse("did not expect a build to occur after pressing ok" //$NON-NLS-1$
				+ " but build happened anyway", didBuild); //$NON-NLS-1$

	}

	/**
	 * Change the non standard options 1. press "Apply" and "yes" to rebuild now
	 * ==> expect a build 2. press "Ok" and "yes" to rebuild now ==> don't
	 * expect a build
	 */
	public void testChangeNonStandardOptionsAndApply_2() {
		// make a change on the page which should result in asking
		// whether the user wants to do a build
		page.setNonStandardOption("-showWeaveInfo"); //$NON-NLS-1$
		// want to do a build when asked
		page.setBuildNow(true);
		// perform Apply - expect there to be a build
		testLog.clearLog();
		page.performApply();
        waitForJobsToComplete();
		boolean didBuild = testLog
				.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
		assertTrue("expected build to occur after performApply because asked " //$NON-NLS-1$
				+ "for one but build didn't happen", didBuild); //$NON-NLS-1$
		// pressing "Ok" now should not result in a build
		page.setBuildNow(false);
		testLog.clearLog();
		page.performOk();
        waitForJobsToComplete();
		didBuild = testLog.containsMessage("AspectJ reports build successful"); //$NON-NLS-1$
		assertFalse("did not expect a build to occur after performOk because " //$NON-NLS-1$
				+ " already applied changes but build happened anyway", //$NON-NLS-1$
				didBuild);
	}

}
