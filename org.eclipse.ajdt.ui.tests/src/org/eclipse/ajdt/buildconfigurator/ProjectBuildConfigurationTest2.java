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
package org.eclipse.ajdt.buildconfigurator;

import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IProject;

import junit.framework.TestCase;

/**
 * 
 */
public class ProjectBuildConfigurationTest2 extends TestCase {

	public void testBug84310a() throws Exception {
		final String expected = "build";
		IProject project = Utils.createPredefinedProject("bug84310a");

		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc = conf
				.getProjectBuildConfigurator(project);
		assertNotNull(
				"Didn't get a project build configurator for our project", pbc);

		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
		if (!bc.getName().equals(expected)) {
			fail("The active build configuration for project bug84310a should be \""+expected+"\". Instead it's: "
					+ bc.getName());
		}

		Utils.deleteProject(project);
	}

	public void testBug84310b() throws Exception {
		final String expected = "aardvark";
		IProject project = Utils.createPredefinedProject("bug84310b");

		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc = conf
				.getProjectBuildConfigurator(project);
		assertNotNull(
				"Didn't get a project build configurator for our project", pbc);

		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
		if (!bc.getName().equals(expected)) {
			fail("The active build configuration for project bug84310a should be \""+expected+"\". Instead it's: "
					+ bc.getName());
		}
		
		Utils.deleteProject(project);
	}

	public void testGetActiveConfigFromSettings() throws Exception {
		final String expected = "monkey";
		// this project should have a preference value under the .settings
		// directory indicating the active build configuration
		IProject project = Utils.createPredefinedProject("DotSettings");

		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc = conf
				.getProjectBuildConfigurator(project);
		assertNotNull(
				"Didn't get a project build configurator for our project", pbc);

		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
		if (!bc.getName().equals(expected)) {
			fail("The active build configuration for project DotSettings should be \""+expected+"\". Instead it's: "
					+ bc.getName());
		}

		Utils.deleteProject(project);
	}
}
