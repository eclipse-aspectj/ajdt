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
package org.eclipse.ajdt.ui.tests.buildconfig;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.ajdt.ui.buildconfig.DefaultBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IBuildConfiguration;
import org.eclipse.ajdt.ui.buildconfig.IBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IProjectBuildConfigurator;
import org.eclipse.ajdt.ui.tests.UITestCase;
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
public class ProjectBuildConfigurationTest extends UITestCase {

	IProject ajProject = null;
	IFile fileA;
	IFile fileB;
	IFile fileDef;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		ajProject = createPredefinedProject("AJ Project For BuildConfigurationTest"); //$NON-NLS-1$
		waitForJobsToComplete();
		setupSandboxSourceFolder();
		waitForJobsToComplete();
	}


	private void setupSandboxSourceFolder() throws Exception {
		IFolder src = ajProject.getFolder("testSrcPath"); //$NON-NLS-1$
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

		fileDef = src.getFile("InDefaultPack.java"); //$NON-NLS-1$
		if (!fileDef.exists()) {
			//fileDef.create(new StringBufferInputStream("public class
			// InDefaultPack{}"), true, null);
			String content = "public class InDefaultPack{}"; //$NON-NLS-1$
			ByteArrayInputStream source = new ByteArrayInputStream(content
					.getBytes());
			fileDef.create(source, true, null);
		}
		IFolder pack = src.getFolder("package1"); //$NON-NLS-1$
		if (!pack.exists()) {
			pack.create(true, true, null);
		}

		fileA = pack.getFile("A.java"); //$NON-NLS-1$
		if (!fileA.exists()) {
			//fileA.create(new StringBufferInputStream("package
			// package1;\npublic class A{}"), true, null);
			String content = "package package1;\npublic class A{}"; //$NON-NLS-1$
			ByteArrayInputStream source = new ByteArrayInputStream(content
					.getBytes());
			fileA.create(source, true, null);
		}

		fileB = pack.getFile("B.java"); //$NON-NLS-1$
		if (!fileB.exists()) {
			//fileB.create(new StringBufferInputStream("package
			// package1;\npublic class B{}"), true, null);
			String content = "package package1;\npublic class B{}"; //$NON-NLS-1$
			ByteArrayInputStream source = new ByteArrayInputStream(content
					.getBytes());
			fileB.create(source, true, null);
		}
	}

	public void testGetBuildConfigurator() {
		//TODO: test could be improved using threads to do accesses
		// simultanously
		IBuildConfigurator conf = DefaultBuildConfigurator.getBuildConfigurator();
		IBuildConfigurator conf2 = DefaultBuildConfigurator.getBuildConfigurator();
		if (conf != conf2) {
			fail("Build Configurator not unique."); //$NON-NLS-1$
		}
	}

	public void testGetBuildConfiguration() {
		IBuildConfigurator conf = DefaultBuildConfigurator.getBuildConfigurator();
		IProjectBuildConfigurator pbc;

		pbc = conf.getProjectBuildConfigurator(ajProject);

		IBuildConfiguration bc = pbc.getActiveBuildConfiguration();
		if (bc == null)
			fail("Could not get active BuildConfiguration."); //$NON-NLS-1$

		Collection bcs = new ArrayList(pbc.getBuildConfigurations());

		Iterator iter = bcs.iterator();
		while (iter.hasNext()) {
			BuildConfiguration tempbc = (BuildConfiguration) iter.next();
			pbc.removeBuildConfiguration(tempbc);
		}

		bc = pbc.getActiveBuildConfiguration();

//		//recover old configuration
//		iter = bcs.iterator();
//		boolean isIncluded = false;
//		while (iter.hasNext()) {
//			BuildConfiguration tempbc = (BuildConfiguration) iter.next();
//			pbc.addBuildConfiguration(tempbc);
//			if (tempbc.getFile().equals(bc.getFile())) {
//				isIncluded = true;
//			}
//		}
//		if (!isIncluded)
//			pbc.removeBuildConfiguration(bc);

		if (bc != null)
			fail("A new build configuration should NOT have been created after removing all old ones."); //$NON-NLS-1$
	}

	public void testNatureConversion() throws CoreException {
		IBuildConfigurator conf = DefaultBuildConfigurator.getBuildConfigurator();
		IProjectBuildConfigurator pbc;
		AJDTUtils.removeAspectJNature(ajProject);
		waitForJobsToComplete();

		pbc = conf.getProjectBuildConfigurator(ajProject);
		if (pbc != null) {
			AJDTUtils.addAspectJNature(ajProject);
			fail("could obtain pbc despite of removed aj nature"); //$NON-NLS-1$
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

		waitForJobsToComplete();

		pbc = conf.getProjectBuildConfigurator(ajProject);
		if (pbc == null) {
			fail("No ProjectBuildConfigurator was created after adding aj nature."); //$NON-NLS-1$
		}

		IBuildConfiguration bc = pbc.getActiveBuildConfiguration();
		if (bc != null) {
			fail("An active build configuration was created when added aj nature to project."); //$NON-NLS-1$
		}

		waitForJobsToComplete();
		//this cannot be tested because the file writing thread may still not
		// have written its file
//		if (bc.isIncluded(fileA)) {
//			fail("jdt exclusion filter not taken into account when converted to aj project."); //$NON-NLS-1$
//		}
//
//		if (!bc.isIncluded(fileDef) || !bc.isIncluded(fileB)) {
//			fail("not all desired files included after conversion to aj project."); //$NON-NLS-1$
//		}
//
//		if (!bc.getFile().exists()) {
//			fail("New build configuration was created when adding aj nature, but file not written."); //$NON-NLS-1$
//		}
//
//		jp.getRawClasspath();
//		cpes = jp.getRawClasspath();
//		for (int i = 0; i < cpes.length; i++) {
//			if (cpes[i].getPath().equals(fileDef.getParent().getFullPath())) {
//				if (cpes[i].getExclusionPatterns().length > 0)
//					fail("Exclusion patterns not reset when converting to aj project."); //$NON-NLS-1$
//			}
//		}

	}

}

