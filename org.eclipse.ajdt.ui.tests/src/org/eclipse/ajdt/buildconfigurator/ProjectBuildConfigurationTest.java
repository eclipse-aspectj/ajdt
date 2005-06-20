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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author hawkinsh
 *  
 */
public class ProjectBuildConfigurationTest extends TestCase {

	IProject ajProject = null;
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
		setupSandboxSourceFolder();
		Utils.waitForJobsToComplete();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		Utils.deleteProject(ajProject);
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

	public void testGetBuildConfigurator() {
		//TODO: test could be improved using threads to do accesses
		// simultanously
		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		BuildConfigurator conf2 = BuildConfigurator.getBuildConfigurator();
		if (conf != conf2) {
			fail("Build Configurator not unique.");
		}
	}

	public void testGetBuildConfiguration() {
		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc;

		pbc = conf.getProjectBuildConfigurator(ajProject);

		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
		if (bc == null)
			fail("Could not get active BuildConfiguration.");

		Collection bcs = new ArrayList(pbc.getBuildConfigurations());

		Iterator iter = bcs.iterator();
		while (iter.hasNext()) {
			BuildConfiguration tempbc = (BuildConfiguration) iter.next();
			pbc.removeBuildConfiguration(tempbc);
		}

		bc = pbc.getActiveBuildConfiguration();

		//recover old configuration
		iter = bcs.iterator();
		boolean isIncluded = false;
		while (iter.hasNext()) {
			BuildConfiguration tempbc = (BuildConfiguration) iter.next();
			pbc.addBuildConfiguration(tempbc);
			if (tempbc.getFile().equals(bc.getFile())) {
				isIncluded = true;
			}
		}
		if (!isIncluded)
			pbc.removeBuildConfiguration(bc);

		if (bc == null)
			fail("No new build configuration was created when removing all old ones.");
	}

	public void testNatureConversion() throws CoreException {
		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc;
		AJDTUtils.removeAspectJNature(ajProject);
		Utils.waitForJobsToComplete();

		pbc = conf.getProjectBuildConfigurator(ajProject);
		if (pbc != null) {
			AJDTUtils.addAspectJNature(ajProject);
			fail("could obtain pbc despite of removed aj nature");
		}

		IResource[] mems = ajProject.members();
		for (int i = 0; i < mems.length; i++) {
			if (mems[i].getName().endsWith(BuildConfiguration.EXTENSION))
				mems[i].delete(true, null);
		}

		IJavaProject jp = JavaCore.create(ajProject);
		IClasspathEntry[] cpes = jp.getRawClasspath();
		for (int i = 0; i < cpes.length; i++) {
			if (cpes[i].getPath().equals(fileDef.getParent().getFullPath())) {
				IPath[] expats = new IPath[1];
				expats[0] = fileA.getFullPath().removeFirstSegments(
						cpes[i].getPath().matchingFirstSegments(
								fileA.getFullPath()));
				cpes[i] = JavaCore.newSourceEntry(cpes[i].getPath(), expats);
			}
		}
		jp.setRawClasspath(cpes, null);

		AJDTUtils.addAspectJNature(ajProject);

		Utils.waitForJobsToComplete();

		pbc = conf.getProjectBuildConfigurator(ajProject);
		if (pbc == null) {
			fail("No ProjectBuildConfigurator was created after adding aj nature.");
		}

		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
		if (bc == null) {
			fail("No active build configuration was created when added aj nature to project.");
		}

		Utils.waitForJobsToComplete();
		//this cannot be tested because the file writing thread may still not
		// have written its file
		if (bc.isIncluded(fileA)) {
			fail("jdt exclusion filter not taken into account when converted to aj project.");
		}

		if (!bc.isIncluded(fileDef) || !bc.isIncluded(fileB)) {
			fail("not all desired files included after conversion to aj project.");
		}

		if (!bc.getFile().exists()) {
			fail("New build configuration was created when adding aj nature, but file not written.");
		}

		jp.getRawClasspath();
		cpes = jp.getRawClasspath();
		for (int i = 0; i < cpes.length; i++) {
			if (cpes[i].getPath().equals(fileDef.getParent().getFullPath())) {
				if (cpes[i].getExclusionPatterns().length > 0)
					fail("Exclusion patterns not reset when converting to aj project.");
			}
		}

	}

}

