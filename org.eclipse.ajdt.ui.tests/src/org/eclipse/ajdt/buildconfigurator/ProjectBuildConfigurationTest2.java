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
		IProject project = Utils.createPredefinedProject("bug84310a");

		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc = conf
				.getProjectBuildConfigurator(project);
		assertNotNull(
				"Didn't get a project build configurator for our project", pbc);

		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
		if (!bc.getName().equals("build")) {
			fail("The active build configuration for project bug84310a should be \"build\". Instead it's: "
					+ bc.getName());
		}

		Utils.deleteProject(project);
	}

	public void testBug84310b() throws Exception {
		IProject project = Utils.createPredefinedProject("bug84310b");

		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc = conf
				.getProjectBuildConfigurator(project);
		assertNotNull(
				"Didn't get a project build configurator for our project", pbc);

		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
		if (!bc.getName().equals("aardvark")) {
			fail("The active build configuration for project bug84310a should be \"aardvark\". Instead it's: "
					+ bc.getName());
		}
		
		Utils.deleteProject(project);
	}

}
