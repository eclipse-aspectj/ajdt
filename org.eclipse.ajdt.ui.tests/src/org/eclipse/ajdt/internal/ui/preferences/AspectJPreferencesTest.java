/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.eclipse.ajdt.buildconfigurator.BuildConfiguration;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurator;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Test AspectJPreferences
 */
public class AspectJPreferencesTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testBuildConfigSetting() throws Exception {
		IProject project = Utils.createPredefinedProject("Simple AJ Project");
		IJavaProject jp = JavaCore.create(project);
		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc = conf
				.getProjectBuildConfigurator(project);
		assertNotNull("Didn't get a project build configurator", pbc);

		// create a new configuration and activate it
		final String newconfig = "newconfig";
		BuildConfiguration bc = new BuildConfiguration(newconfig, jp, pbc);
		pbc.addBuildConfiguration(bc);
		pbc.setActiveBuildConfiguration(bc);

		Utils.waitForJobsToComplete();

		// active config should now have been written to .settings file
		IResource res = project.findMember(".settings");

		if (res.getType() != IResource.FOLDER) {
			fail(".settings must be a folder");
		}
		IFolder settings = (IFolder) res;

		final boolean[] success = new boolean[1];
		success[0] = false;
		settings.accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (resource.getType() == IResource.FILE) {
					if (resource.getName().indexOf("ajdt") >= 0) {
						// found an AJDT prefs file, let's see if it mentions
						// our build config
						IFile file = (IFile) resource;
						BufferedReader input = new BufferedReader(
								new InputStreamReader(file.getContents()));
						try {
							String line = input.readLine();
							while (line != null) {
								if (line.indexOf(newconfig) >= 0) {
									success[0] = true;
								}
								line = input.readLine();
							}
							input.close();
						} catch (IOException e) {
						}
					}
				}
				return true;
			}
		});
		assertTrue(
				"Didn't find a .settings preferences file mentioning the new build configuration",
				success[0]);

		Utils.deleteProject(project);
	}
}
