/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.ajdt.ui.tests.testutils.BlockingProgressMonitor;
import org.eclipse.ajdt.ui.tests.utils.AJDTUtilsTest.MyJobChangeListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author hawkinsh
 *  
 */
public class ProjectDependenciesTest extends UITestCase {

	BlockingProgressMonitor monitor;

	protected void setUp() throws Exception {
		super.setUp();
        // avoid prompting for depency removal.
        AspectJPreferences.setAskPDEAutoRemoveImport(false);
        // automatically remove import from classpath
        AspectJPreferences.setDoPDEAutoRemoveImport(true);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Because of the changes in how we deal with dependencies between projects
	 * (see the manual testing/project dependencies/The history of project
	 * dependencies.txt file for more information), we need a test which spells
	 * out which one we're using. This is purely to detect whether we're:
	 * 
	 * a) changing project dependencies to class folder dependencies or, b)
	 * leaving project dependencies alone.
	 * 
	 * If this test fails, then other asserts need to be switched (some are
	 * marked with a PD in the comment).
	 */
	public void testHowDealingWithProjectDependencies() throws Exception {
		IProject projectY = createPredefinedProject("project.java.Y"); //$NON-NLS-1$
		IProject projectX = createPredefinedProject("project.java.X"); //$NON-NLS-1$

		// sanity check: at this point there should be no error markers, both
		// projects should build as they're both java projects, project X should
		// should have a project dependency on project Y
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,
						null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,
						null));
		assertTrue("project X should have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX,
						projectY));

		// convert project Y to be an AJ project and check setup is correct
		AspectJUIPlugin.convertToAspectJProject(projectY);
		waitForJobsToComplete();
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,
						null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,
						null));

		// PD: The following is how we know how we're dealing with Project
		// dependencies...

		// If we're changing project dependencies to class folder dependencies
		// (both when
		// AJ nature is added and when an AJ build is done) then the following
		// two asserts
		// should be uncommented
		//assertFalse("project X should not have a project dependency on
		// project Y",
		//		projectHasProjectDependency(projectX, projectY));
		//assertTrue("project X should have a class folder dependency on
		// project Y",
		//		projectHasClassFolderDependency(projectX,projectY));

		// If we're not touching project dependencies when AJ nature is
		// added/removed
		// then the following two asserts should be uncommented.
		assertTrue(
				"project X should still have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX,
						projectY));
		assertFalse(
				"project X should NOT have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(
						projectX, projectY));

		// remove AJ nature from Y and the following asserts should always be
		// true
		AspectJUIPlugin.convertFromAspectJProject(projectY);
		waitForJobsToComplete();
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,
						null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,
						null));
		assertTrue(
				"project X should still have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX,
						projectY));
		assertFalse(
				"project X should NOT have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(
						projectX, projectY));

	}

	/**
	 * Test project dependencies work with plugin projects i.e. have two plugin
	 * projects, X and Y where X depends on Y. Then convert project Y to be an
	 * AspectJ project - both projects should still build. In particular,
	 * project X should not have any error markers from the java builder saying
	 * something along the lines of "please rebuild prerequisite project Y" (Bug
	 * 70288)
	 */
	public void testProjectDependenciesWithPluginProjects() throws Exception {
		setUpPluginEnvironment();
		MyJobChangeListener listener = new MyJobChangeListener();
		Job.getJobManager().addJobChangeListener(listener);

		// create two plugin projects (java)
		IProject projectY = createPredefinedProject("java.plugin.project.Y"); //$NON-NLS-1$
		waitForJobsToComplete();
		IProject projectX = createPredefinedProject("java.plugin.project.X"); //$NON-NLS-1$
		waitForJobsToComplete();
		
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,null);
		waitForJobsToComplete();
		projectX.build(IncrementalProjectBuilder.FULL_BUILD,null);
		waitForJobsToComplete();
		
		// NB it has been setup such that java.plugin.project.X has a plugin
		// dependency on java.plugin.project.Y
//		assertTrue("projectX should have plugin dependency on projectY",
//				ProjectDependenciesUtils.projectHasPluginDependency(projectX,"java.plugin.project.Y"));
		assertFalse("project X should not have any IJavaModelMarkers", //$NON-NLS-1$
				ProjectDependenciesUtils.projectMarkedWithPrereqMessage(
						projectX, projectY));

		// convert project Y to be an AJ project ==> still no build errors
		AspectJUIPlugin.convertToAspectJProject(projectY);
		waitForJobsToComplete();
		assertFalse("project X should still not have any IJavaModelMarkers", //$NON-NLS-1$
				ProjectDependenciesUtils.projectMarkedWithPrereqMessage(
						projectX, projectY));

		resetPluginEnvironment();
	}

	/**
	 * This creates two projects and then adds and remove AJ nature to them in
	 * various sequences - ensuring that the dependencies remain as they should
	 */
	public void testProjectDependencies1() throws Exception {
		IProject projectY = createPredefinedProject("project.java.Y"); //$NON-NLS-1$
		IProject projectX = createPredefinedProject("project.java.X"); //$NON-NLS-1$

		// sanity check: at this point there should be no error markers, both
		// projects should build as they're both java projects, project X should
		// should have a project dependency on project Y
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,
						null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,
						null));
		assertTrue("project X should have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX,
						projectY));

		// convert project Y to be an AJ project and check setup is correct
		AspectJUIPlugin.convertToAspectJProject(projectY);
		waitForJobsToComplete();
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue(
				"project X should still have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX,projectY));
		assertFalse(
				"project X should NOT have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX, projectY));

		// convert project X to be an AJ project and check setup is correct
		AspectJUIPlugin.convertToAspectJProject(projectX);
		waitForJobsToComplete();
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue(
				"project X should still have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX,projectY));
		assertFalse(
				"project X should NOT have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX, projectY));
		
		// remove AJ nature from project Y		
		AspectJUIPlugin.convertFromAspectJProject(projectY);
		waitForJobsToComplete();
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue(
				"project X should still have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX,projectY));
		assertFalse(
				"project X should NOT have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX, projectY));

		// convert project Y to be an AJ project and check setup is correct
		AspectJUIPlugin.convertToAspectJProject(projectY);
		waitForJobsToComplete();
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue(
				"project X should still have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX,projectY));
		assertFalse(
				"project X should NOT have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX, projectY));

		// remove AJ nature from project X		
		AspectJUIPlugin.convertFromAspectJProject(projectX);
		waitForJobsToComplete();
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue(
				"project X should still have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX,projectY));
		assertFalse(
				"project X should NOT have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX, projectY));
		
		// remove AJ nature from project Y		
		AspectJUIPlugin.convertFromAspectJProject(projectY);
		waitForJobsToComplete();
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue(
				"project X should still have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX,projectY));
		assertFalse(
				"project X should NOT have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX, projectY));
	}



}

