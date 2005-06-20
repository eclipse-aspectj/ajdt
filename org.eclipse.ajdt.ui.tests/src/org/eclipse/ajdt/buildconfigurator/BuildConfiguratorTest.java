/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.buildconfigurator;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author hawkinsh
 *  
 */
public class BuildConfiguratorTest extends TestCase {

	IProject ajProject = null;
	IProject javaProject = null;

	IFile fileA;
	IFile fileB;
	IFile fileDef;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		ajProject = Utils.createPredefinedProject("AJ Project For BuildConfigurationTest");
		Utils.waitForJobsToComplete();
		javaProject = Utils.createPredefinedProject("java.project.Y");
		
		setupSandboxSourceFolder();
		Utils.waitForJobsToComplete();

	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if (ajProject != null) {
			Utils.deleteProject(ajProject);			
		}
		if (javaProject != null) {
			Utils.deleteProject(javaProject);			
		}
		Utils.waitForJobsToComplete();
	}

	private void setupSandboxSourceFolder() throws Exception {
		IFolder src = ajProject.getFolder("testSrcPath");
		if (!src.exists()) {
			src.create(true, true, null);
		}
		IJavaProject jp = JavaCore.create(ajProject);
		IClasspathEntry[] cpes = jp.getRawClasspath();
		IClasspathEntry[] newCpes = new IClasspathEntry[cpes.length + 1];

		boolean alreadyThere = false;
		for (int i = 0; i < cpes.length; i++) {
			newCpes[i] = cpes[i];
			if (cpes[i].getPath().equals(src.getFullPath()))
				alreadyThere = true;
		}
		if (!alreadyThere) {
			newCpes[cpes.length] = JavaCore.newSourceEntry(src.getFullPath());
			jp.setRawClasspath(newCpes, null);
		}

		fileDef = src.getFile("InDefaultPack.java");
		if (!fileDef.exists()) {
			//fileDef.create(new StringBufferInputStream("public class
			// InDefaultPack{}"), true, null);
			String content = "public class InDefaultPack{}";
			ByteArrayInputStream source = new ByteArrayInputStream(content
					.getBytes());
			fileDef.create(source, true, null);
		}
		IFolder pack = src.getFolder("package1");
		if (!pack.exists()) {
			pack.create(true, true, null);
		}

		fileA = pack.getFile("A.java");
		if (!fileA.exists()) {
			//fileA.create(new StringBufferInputStream("package
			// package1;\npublic class A{}"), true, null);
			String content = "package package1;\npublic class A{}";
			ByteArrayInputStream source = new ByteArrayInputStream(content
					.getBytes());
			fileA.create(source, true, null);
		}

		fileB = pack.getFile("B.java");
		if (!fileB.exists()) {
			//fileB.create(new StringBufferInputStream("package
			// package1;\npublic class B{}"), true, null);
			String content = "package package1;\npublic class B{}";
			ByteArrayInputStream source = new ByteArrayInputStream(content
					.getBytes());
			fileB.create(source, true, null);
		}
	}

	public void testGetProjectBuildConfigurator() throws CoreException {
		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc;

		pbc = conf.getProjectBuildConfigurator(javaProject);
		if (pbc != null)
			fail("Could obtain a ProjectBuildConfigurator for non-aj project. This should not be possible.");

		Utils.waitForJobsToComplete();

		ajProject.close(null);

		Utils.waitForJobsToComplete();

		pbc = conf.getProjectBuildConfigurator(ajProject);
		if (pbc != null)
			fail("Could obtain a ProjectBuildConfigurator for closed project. This should not be possible.");

		Utils.waitForJobsToComplete();

		ajProject.open(null);
		pbc = conf.getProjectBuildConfigurator(ajProject);
		if (pbc == null)
			fail("Could not get a ProjectBuildConfigurator for an aj project.");

		//test does not work. buildConfigurator gets not notified when
		// selection changes...
		//		PackageExplorerPart.getFromActivePerspective().selectAndReveal(ajProject);
		//		ProjectBuildConfigurator pbc2 =
		// conf.getActiveProjectBuildConfigurator();
		//		if (pbc2 != pbc){
		//				fail("getActiveProjectBuildConfigurator did not return the pbc of the
		// selected project.");
		//		}
	}
}

