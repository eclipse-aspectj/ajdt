/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.buildconfig;

import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;

/**
 * 
 */
public class ProjectBuildConfigurationTest2 extends UITestCase {

	public void testBug84310a() throws Exception {
		final String expected = "build"; //$NON-NLS-1$
		IProject project = createPredefinedProject("bug84310a"); //$NON-NLS-1$

		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc = conf
				.getProjectBuildConfigurator(project);
		assertNotNull(
				"Didn't get a project build configurator for our project", pbc); //$NON-NLS-1$

		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
		if (!bc.getName().equals(expected)) {
			fail("The active build configuration for project bug84310a should be \""+expected+"\". Instead it's: " //$NON-NLS-1$ //$NON-NLS-2$
					+ bc.getName());
		}
	}

	public void testBug84310b() throws Exception {
		final String expected = "aardvark"; //$NON-NLS-1$
		IProject project = createPredefinedProject("bug84310b"); //$NON-NLS-1$

		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc = conf
				.getProjectBuildConfigurator(project);
		assertNotNull(
				"Didn't get a project build configurator for our project", pbc); //$NON-NLS-1$

		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
		if (!bc.getName().equals(expected)) {
			fail("The active build configuration for project bug84310a should be \""+expected+"\". Instead it's: " //$NON-NLS-1$ //$NON-NLS-2$
					+ bc.getName());
		}
	}

	public void testGetActiveConfigFromSettings() throws Exception {
		final String expected = "monkey"; //$NON-NLS-1$
		// this project should have a preference value under the .settings
		// directory indicating the active build configuration
		IProject project = createPredefinedProject("DotSettings"); //$NON-NLS-1$

		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc = conf
				.getProjectBuildConfigurator(project);
		assertNotNull(
				"Didn't get a project build configurator for our project", pbc); //$NON-NLS-1$

		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
		if (!bc.getName().equals(expected)) {
			fail("The active build configuration for project DotSettings should be \""+expected+"\". Instead it's: " //$NON-NLS-1$ //$NON-NLS-2$
					+ bc.getName());
		}
	}
}
