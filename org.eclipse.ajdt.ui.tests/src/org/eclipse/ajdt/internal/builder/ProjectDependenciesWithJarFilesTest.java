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
package org.eclipse.ajdt.internal.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.internal.ui.ajde.BuildOptionsAdapter;
import org.eclipse.ajdt.test.utils.BlockingProgressMonitor;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClasspathEntry;

/**
 * @author hawkinsh
 *
 */
public class ProjectDependenciesWithJarFilesTest extends TestCase {
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Utils.blockPreferencesConfigWizard();		
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		Utils.restoreBlockedSettings();		
	}
		
	/**
	 * This tests part of the fix for 71371 with dependencies between projects with
	 * exported jar files. Have two projects A and B where A contains a jar file and 
	 * java files. The source in project B depends on code in both the jar file and 
	 * A's class files. 
	 * 
	 * Everything should work ok if A is converted to be an AJ project
	 */
	 public void testWithExportedJarFile() throws Exception {
		IProject projectA = Utils.getPredefinedProject("another.project.AAA", true);
		projectA.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectA);
		IProject projectB = Utils.getPredefinedProject("another.project.B", true);
		projectB.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectB);
		// sanity check: at this point there should be no error markers, both 
		// projects should build as they're both java projects, project A should
		// export a jar file and project B should have a project dependency on
		// project A
		assertFalse("project A should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectA,null));
		assertFalse("project B should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectB,null));
		assertTrue("project A should export a jar file", 
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projectA));
		assertTrue("project B should have a project dependency on project A",
				ProjectDependenciesUtils.projectHasProjectDependency(projectB, projectA));
		
		// convert project A to be an aspectj project
		AJDTUtils.addAspectJNature(projectA);
		ProjectDependenciesUtils.waitForJobsToComplete(projectA);
		projectB.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectB);
		// there should still not be build errors on project B, project A should still 
		// export the jar file, project B should now have the exported jar file as an
		// entry on it's classpath, and project B should have a classfolder dependency on
		// project A
		assertFalse("project B should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectB,null));
		assertTrue("project A should export a jar file", 
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projectA));
		// PD: switching following 3 asserts because current thinking is not to change project dependencies to classfolder ones
		//assertTrue("project B should have jar file on classpath (and not export it)",
		//		projectHasJarOnClasspath(projectB,projectA));
		//assertTrue("project B should have class folder dependency on project A", 
		//		projectHasClassFolderDependency(projectB,projectA));
		//assertFalse("project B should not have a project dependency on project A",
		//		projectHasProjectDependency(projectB,projectA));
		assertFalse("project B should NOT have jar file on classpath (and not export it)",
				ProjectDependenciesUtils.projectHasJarOnClasspath(projectB,projectA));
		assertFalse("project B should NOT have class folder dependency on project A", 
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectB,projectA));
		assertTrue("project B should still have a project dependency on project A",
				ProjectDependenciesUtils.projectHasProjectDependency(projectB,projectA));
		
		
		// remove AJ nature frrom project A
		AJDTUtils.removeAspectJNature(projectA);
		ProjectDependenciesUtils.waitForJobsToComplete(projectA);
		projectB.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectB);
		// There should be no build errors on project B, project B should have a project dependency 
		// on project A, project A should still export the jar file, project B should not include 
		// the exported jar file on it's classpath
		assertFalse("project B should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectB,null));
		assertTrue("project B should have a project dependency on project A",
				ProjectDependenciesUtils.projectHasProjectDependency(projectB,projectA));
		assertFalse("project B should not have a class folder dependency on project A",
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectB,projectA));
		assertTrue("project A should export a jar file", 
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projectA));
		assertFalse("project B should not have jar file on classpath (and not export it)",
				ProjectDependenciesUtils.projectHasJarOnClasspath(projectB,projectA));

		// close all the projects so that they don't hang around and hold
		// up the build for other tests
		BlockingProgressMonitor monitor = new BlockingProgressMonitor();
		monitor.reset();
		projectA.close(monitor);
		monitor.waitForCompletion();
		monitor.reset();
		projectB.close(monitor);
		monitor.waitForCompletion();
		
	}
	
	/**
	 * This tests part of the fix for 71371 with dependencies between projects with
	 * exported jar files. Have three projects A,B,C where:
	 * - A is a binary project which just contains a jar file
	 * - B uses the jar file from project A and reexports it, as well as containing
	 *   some classes of it's own (which use the jar file)
	 * - C has a project dependency on B and has classes which use the classes from
	 *   B as well as the classes in the jar file.
	 * 
	 * Everything should build ok if B is converted to be an AJ project! 
	 */
	public void testWithExportedJarFileAndBinaryProject() throws Exception {
		
		IProject projA = Utils.getPredefinedProject("non.plugin.project.A1",true);
		ProjectDependenciesUtils.waitForJobsToComplete(projA);
		IProject projB = Utils.getPredefinedProject("non.plugin.project.B",true);
		projB.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projB);
		IProject projC = Utils.getPredefinedProject("non.plugin.project.C",true);
		projC.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projC);
		// sanity check: at this point there should be no error markers and both 
		// projects B and C should build as they're both java projects.
		// Also:
		// - project A should be a binary project with no src
		// - project B should export a jar file
		// - project C should have a project dependency on project B
		assertFalse("project B should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projB,null));
		assertFalse("project C should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projC,null));
		assertTrue("project A should have no src", 
				ProjectDependenciesUtils.projectHasNoSrc(projA));
		assertTrue("project B should export a jar file", 
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projB));
		assertTrue("project C should have a project dependency on project B",
				ProjectDependenciesUtils.projectHasProjectDependency(projC,projB));
		assertFalse("project C should not have a class dependency on project B",
				ProjectDependenciesUtils.projectHasClassFolderDependency(projC,projB));

		// convert project B to be an AspectJ project
		AJDTUtils.addAspectJNature(projB);
		ProjectDependenciesUtils.waitForJobsToComplete(projB);
		projC.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projC);

		assertFalse("project B should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projB,null));		
		assertFalse("project C should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projC,null));
		assertTrue("project B should still export the jar file", 
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projB));
		// PD: switching following 3 asserts because current thinking is not to change project dependencies to classfolder ones
		//assertTrue("project C should have jar file on classpath (and not export it)",
		//		projectHasJarOnClasspath(projC,projB));		
		//assertTrue("project C should have a class folder dependency on project B",
		//		projectHasClassFolderDependency(projC,projB));
		//assertFalse("project C should NOT have a project dependency on project B",
		//		projectHasProjectDependency(projC,projB));
		assertFalse("project C should NOT have jar file on classpath (and not export it)",
				ProjectDependenciesUtils.projectHasJarOnClasspath(projC,projB));		
		assertFalse("project C should NOT have a class folder dependency on project B",
				ProjectDependenciesUtils.projectHasClassFolderDependency(projC,projB));
		assertTrue("project C should still have a project dependency on project B",
				ProjectDependenciesUtils.projectHasProjectDependency(projC,projB));
		
		// remove AJ nature from project B
		AJDTUtils.removeAspectJNature(projB);
		ProjectDependenciesUtils.waitForJobsToComplete(projB);
		projC.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projC);
		// the situation:
		// - there should still not be build errors on project B or project C
		// - project B should still export the jar file, 
		// - project C should not have the exported jar file as an entry on it's classpath
		// - project B should have a project dependency on project A
		assertFalse("project B should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projB,null));		
		assertFalse("project C should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projC,null));
		assertTrue("project B should still export the jar file", 
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projB));
		assertFalse("project C should have jar file on classpath (and not export it)",
				ProjectDependenciesUtils.projectHasJarOnClasspath(projC,projB));		
		assertFalse("project C should have a class folder dependency on project B",
				ProjectDependenciesUtils.projectHasClassFolderDependency(projC,projB));
		assertTrue("project C should NOT have a project dependency on project B",
				ProjectDependenciesUtils.projectHasProjectDependency(projC,projB));
		
		// close all the projects so that they don't hang around and hold
		// up the build for other tests
		BlockingProgressMonitor monitor = new BlockingProgressMonitor();
		monitor.reset();
		projA.close(monitor);
		monitor.waitForCompletion();
		monitor.reset();
		projB.close(monitor);
		monitor.waitForCompletion();
		monitor.reset();
		projC.close(monitor);
		monitor.waitForCompletion();
		
	}

	/**
	 * This tests part of the fix for 71371 with dependencies between projects with
	 * exported jar files. Have two projects A and B where A contains just a jar 
	 * file. The source in project B depends on code in both the jar file. 
	 * 
	 * Everything should work ok if A is converted to be an AJ project, in particular,
	 * there should be no class folder dependency added onto project B.
	 */
	 public void testWithExportedJarFileAndNoSrcEntry() throws Exception {
		IProject projectB = Utils.getPredefinedProject("pd.non.plugin.project.B", true);
		projectB.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectB);
		IProject projectB1 = Utils.getPredefinedProject("pd.non.plugin.project.B1", true);
		projectB1.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectB1);
		// sanity check: at this point there should be no error markers, both 
		// projects should build as they're both java projects, project A should
		// export a jar file and project B should have a project dependency on
		// project A
		assertFalse("project B should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectB,null));
		assertFalse("project B1 should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectB1,null));
		assertTrue("project B should export a jar file", 
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projectB));
		assertTrue("project B1 should have a project dependency on project B",
				ProjectDependenciesUtils.projectHasProjectDependency(projectB1, projectB));
		assertTrue("project B should contain no src directory", 
				ProjectDependenciesUtils.projectHasNoSrc(projectB));
		
		// convert project A to be an aspectj project
		AJDTUtils.addAspectJNature(projectB);
		ProjectDependenciesUtils.waitForJobsToComplete(projectB);
		projectB1.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectB1);
		// there should still not be build errors on project B, project A should still 
		// export the jar file, project B should now have the exported jar file as an
		// entry on it's classpath, and project B should have a classfolder dependency on
		// project A
		assertFalse("project B1 should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectB1,null));
		assertTrue("project B should export a jar file", 
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projectB));
		assertFalse("project B1 should not have class folder dependency on project B", 
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectB1,projectB));
		// PD: switching following 2 asserts because current thinking is not to change project dependencies to classfolder ones
		//assertTrue("project B1 should have jar file on classpath (and not export it)",
		//		projectHasJarOnClasspath(projectB1,projectB));
		//assertFalse("project B1 should not have a project dependency on project B",
		//		projectHasProjectDependency(projectB1,projectB));
		assertFalse("project B1 should NOT have jar file added on classpath (and not export it)",
				ProjectDependenciesUtils.projectHasJarOnClasspath(projectB1,projectB));
		assertTrue("project B1 should still have a project dependency on project B",
				ProjectDependenciesUtils.projectHasProjectDependency(projectB1,projectB));
		
		// remove AJ nature frrom project A
		AJDTUtils.removeAspectJNature(projectB);
		ProjectDependenciesUtils.waitForJobsToComplete(projectB);
		projectB1.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectB1);
		// There should be no build errors on project B, project B should have a project dependency 
		// on project A, project A should still export the jar file, project B should not include 
		// the exported jar file on it's classpath
		assertFalse("project B1 should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectB1,null));
		assertTrue("project B1 should have a project dependency on project B",
				ProjectDependenciesUtils.projectHasProjectDependency(projectB1,projectB));
		assertFalse("project B1 should not have a class folder dependency on project B",
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectB1,projectB));
		assertTrue("project B should export a jar file", 
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projectB));
		assertFalse("project B1 should not have jar file on classpath (and not export it)",
				ProjectDependenciesUtils.projectHasJarOnClasspath(projectB1,projectB));

		// close all the projects so that they don't hang around and hold
		// up the build for other tests
		BlockingProgressMonitor monitor = new BlockingProgressMonitor();
		monitor.reset();
		projectB.close(monitor);
		monitor.waitForCompletion();
		monitor.reset();
		projectB1.close(monitor);
		monitor.waitForCompletion();
		
	}
	
	/**
	 * This tests scenario 1 of bug 43674
	 * 
	 * - project X is a java project
     * - project Y is an AJ project
     * - project X has a project dependency on project Y 
     * - build X and Y and there are no errors
     * - make Y build to an outjar and build project Y
     * - project X should now have Y's outjar on it's classpath
     *   rather than any of the other types of dependencies. This
     *   needs to be reversed if project Y decides not to sent it's output
     *   to an outjar. 
     * 
	 * @throws Exception
	 */ 
	public void testWithOutJarSwitch1() throws Exception {
		IProject projectY = Utils.getPredefinedProject("project.java.Y", true);
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		IProject projectX = Utils.getPredefinedProject("project.java.X", true);
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectX);
		// sanity check: at this point there should be no error markers, both 
		// projects should build as they're both java projects, project X should
		// should have a project dependency on project Y
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project", 
				projectY.hasNature(AspectJUIPlugin.ID_NATURE));
		assertFalse("project X should not be an AJ project", 
				projectX.hasNature(AspectJUIPlugin.ID_NATURE));
		
		// convert project Y to be an AJ project and check setup is correct
		AJDTUtils.addAspectJNature(projectY);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		// PD: switching following 2 asserts because current thinking is not to change project dependencies to classfolder ones
		//assertFalse("project X should not have a project dependency on project Y",
		//		projectHasProjectDependency(projectX, projectY));
		//assertTrue("project X should have a class folder dependency on project Y",
		//		projectHasClassFolderDependency(projectX,projectY));
		assertTrue("project X should still have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project X should NOT have a class folder dependency on project Y",
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));
		
		// set project Y to send output to jar file
		// setup the outjar
		String outJar = ProjectDependenciesUtils.setupOutJar("mainWork.jar",projectY);
		projectY.setPersistentProperty(BuildOptionsAdapter.OUTPUTJAR, outJar);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);

		// build the project so it picks up the outjar and sends 
		// output there
		BlockingProgressMonitor monitor = new BlockingProgressMonitor();
		monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		monitor.reset();
		projectY.refreshLocal(IResource.DEPTH_INFINITE,monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);

		monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectX);

		assertTrue("Output jar should exist! (path=" + outJar + ")",new File(outJar).exists());
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have project Y's outjar on it's classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, outJar));

		// PD: switching following assert because current thinking is not to change project dependencies to classfolder ones
		//assertFalse("project X should not have a project dependency on project Y",
		//		projectHasProjectDependency(projectX, projectY));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		
		
		assertFalse("project X should not have a class folder dependency on project Y",
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));

		projectY.setPersistentProperty(BuildOptionsAdapter.OUTPUTJAR, "");
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		
		StringBuffer sb = new StringBuffer(outJar);
		IPath path = new Path(sb.substring(sb.lastIndexOf(projectY.getName())));
		IResource jarFile = projectY.getWorkspace().getRoot().findMember(path.makeAbsolute());
		monitor.reset();
		jarFile.delete(true,monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("Output jar should NOT exist! (path=" + outJar + ")",new File(outJar).exists());
		
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectX);
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertFalse("project X should not have outjar on classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY,outJar));
		
		// reset projects to their original state:
		AJDTUtils.removeAspectJNature(projectY);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project", 
				projectY.hasNature(AspectJUIPlugin.ID_NATURE));
		assertFalse("project X should not be an AJ project", 
				projectX.hasNature(AspectJUIPlugin.ID_NATURE));

		// close all the projects so that they don't hang around and hold
		// up the build for other tests
		monitor.reset();
		projectX.close(monitor);
		monitor.waitForCompletion();
		monitor.reset();
		projectY.close(monitor);
		monitor.waitForCompletion();
		
	}

	/**
	 * This tests scenario 2 of bug 43674
	 * 
	 * - project X is an AJ project
     * - project Y is an AJ project
     * - project X has a project dependency on project Y 
     * - build X and Y and there are no errors
     * - make Y build to an outjar and build project Y
     * - project X should now have Y's outjar on it's classpath
     *   rather than any of the other types of dependencies. This
     *   needs to be reversed if project Y decides not to sent it's output
     *   to an outjar. 
	 */ 
	public void testWithOutJarSwitch2() throws Exception {
		IProject projectY = Utils.getPredefinedProject("project.java.Y", true);
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		IProject projectX = Utils.getPredefinedProject("project.java.X", true);
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectX);
		// sanity check: at this point there should be no error markers, both 
		// projects should build as they're both java projects, project X should
		// should have a project dependency on project Y
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project", 
				projectY.hasNature(AspectJUIPlugin.ID_NATURE));
		assertFalse("project X should not be an AJ project", 
				projectX.hasNature(AspectJUIPlugin.ID_NATURE));
		
		// convert project Y to be an AJ project and check setup is correct
		AJDTUtils.addAspectJNature(projectY);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		// PD: switching following 2 asserts because current thinking is not to change project dependencies to classfolder ones
		//assertFalse("project X should not have a project dependency on project Y",
		//		projectHasProjectDependency(projectX, projectY));
		//assertTrue("project X should have a class folder dependency on project Y",
		//		projectHasClassFolderDependency(projectX,projectY));
		assertTrue("project X should still have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project X should NOT have a class folder dependency on project Y",
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));

		// convert project X to be an AJ project and check setup is correct
		AJDTUtils.addAspectJNature(projectX);
		ProjectDependenciesUtils.waitForJobsToComplete(projectX);
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		// PD: switching following 2 asserts because current thinking is not to change project dependencies to classfolder ones
		//assertFalse("project X should not have a project dependency on project Y",
		//		projectHasProjectDependency(projectX, projectY));
		//assertTrue("project X should have a class folder dependency on project Y",
		//		projectHasClassFolderDependency(projectX,projectY));
		assertTrue("project X should still have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project X should NOT have a class folder dependency on project Y",
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));

		// set project Y to send output to jar file
		// setup the outjar
		String outJar = ProjectDependenciesUtils.setupOutJar("mainWork.jar",projectY);
		projectY.setPersistentProperty(BuildOptionsAdapter.OUTPUTJAR, outJar);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("Output jar should not yet exist! (path=" + outJar + ")",new File(outJar).exists());

		// build the project so it picks up the outjar and sends 
		// output there
		BlockingProgressMonitor monitor = new BlockingProgressMonitor();
		monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);

		assertTrue("Output jar should now exist! (path=" + outJar + ")",new File(outJar).exists());
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have project Y's outjar on it's classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, outJar));
		// don't want to add outjar to aspect path
		assertNull("project X should have NOT have outjar on aspect path!",
				AspectJUIPlugin.getDefault().getAjdtProjectProperties().getAspectPath());
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));		
		assertFalse("project X should not have a class folder dependency on project Y",
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));

		// now set the outjar option back to nothing and build projectY 
		// to send output to bin directory rather than jar file
		projectY.setPersistentProperty(BuildOptionsAdapter.OUTPUTJAR, "");
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));

		monitor.reset();
		projectX.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectX);
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertFalse("project X should not have outjar on classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY,outJar));

				
		// reset projects to their original state:
		AJDTUtils.removeAspectJNature(projectX);
		ProjectDependenciesUtils.waitForJobsToComplete(projectX);
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectX);

		monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);

		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));

		monitor.reset();
		projectY.refreshLocal(IResource.DEPTH_INFINITE,monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		

		
		AJDTUtils.removeAspectJNature(projectY);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project", 
				projectY.hasNature(AspectJUIPlugin.ID_NATURE));
		assertFalse("project X should not be an AJ project", 
				projectX.hasNature(AspectJUIPlugin.ID_NATURE));

		// DONT know why trying to delete causes problems - maybe we're holding
		// onto a reference somewhere - this doesn't happen when test this manually.
		// this only happens when both projects are AJ projects
//		monitor.reset();
//		projectY.refreshLocal(IResource.DEPTH_INFINITE,monitor);
//		monitor.waitForCompletion();
//		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
//		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
//		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
//		
//		StringBuffer sb = new StringBuffer(outJar);
//		IPath path = new Path(sb.substring(sb.lastIndexOf(projectY.getName())));
//		IResource jarFile = projectY.getWorkspace().getRoot().findMember(path.makeAbsolute());	
//		monitor.reset();
//		jarFile.delete(true,monitor);
//		monitor.waitForCompletion();
//		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
//		assertFalse("Output jar should NOT exist! (path=" + outJar + ")",new File(outJar).exists());

		
		// close all the projects so that they don't hang around and hold
		// up the build for other tests
		monitor.reset();
		projectX.close(monitor);
		monitor.waitForCompletion();
		monitor.reset();
		projectY.close(monitor);
		monitor.waitForCompletion();
		
	}

	/**
	 * This tests another scenario of bug 43674
	 * 
	 * - project X is a java project
     * - project Y is an AJ project
     * - project X has a project dependency on project Y 
     * - build X and Y and there are no errors
     * - make Y build to an outjar and build project Y
     * - project X should now have Y's outjar on it's classpath
     * - project Y now decides to send output to a different
     *   jar file
     * - project X should now have the new jar file on it's
     *   classpath (and not the old one)
	 */ 
	public void testWithOutJarSwitch3() throws Exception {
		IProject projectY = Utils.getPredefinedProject("project.java.Y", true);
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		IProject projectX = Utils.getPredefinedProject("project.java.X", true);
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectX);
		// sanity check: at this point there should be no error markers, both 
		// projects should build as they're both java projects, project X should
		// should have a project dependency on project Y
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project", 
				projectY.hasNature(AspectJUIPlugin.ID_NATURE));
		assertFalse("project X should not be an AJ project", 
				projectX.hasNature(AspectJUIPlugin.ID_NATURE));
		
		// convert project Y to be an AJ project and check setup is correct
		AJDTUtils.addAspectJNature(projectY);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should still have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project X should NOT have a class folder dependency on project Y",
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));
		
		// set project Y to send output to jar file mainWork.jar
		// setup the outjar
		String outJar = ProjectDependenciesUtils.setupOutJar("firstJar.jar",projectY);
		projectY.setPersistentProperty(BuildOptionsAdapter.OUTPUTJAR, outJar);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("Output jar should NOT exist! (path=" + outJar + ")",new File(outJar).exists());

		// build the project so it picks up the outjar and sends 
		// output there
		BlockingProgressMonitor monitor = new BlockingProgressMonitor();
		monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);

		assertTrue("Output jar should exist! (path=" + outJar + ")",new File(outJar).exists());
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have project Y's outjar on it's classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, outJar));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));		
		assertFalse("project X should not have a class folder dependency on project Y",
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));

		// set project Y to send output to jar file newJar.jar
		// setup the outjar
		String outJar2 = ProjectDependenciesUtils.setupOutJar("newJar.jar",projectY);
		projectY.setPersistentProperty(BuildOptionsAdapter.OUTPUTJAR, outJar2);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("Output jar should NOT exist! (path=" + outJar2 + ")",new File(outJar2).exists());

		// build the project so it picks up the outjar and sends 
		// output there
		monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);

		assertTrue("Output jar should exist! (path=" + outJar2 + ")",new File(outJar2).exists());
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have project Y's NEW outjar on it's classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, outJar2));
		assertFalse("project X should NOT have project Y's old outjar on it's classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, outJar));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));		
		assertFalse("project X should not have a class folder dependency on project Y",
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));

		StringBuffer sb = new StringBuffer(outJar);
		IPath path = new Path(sb.substring(sb.lastIndexOf(projectY.getName())));
		IResource jarFile = projectY.getWorkspace().getRoot().findMember(path.makeAbsolute());
		monitor.reset();
		jarFile.delete(true,monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("Output jar should NOT exist! (path=" + outJar + ")",new File(outJar).exists());

		projectY.setPersistentProperty(BuildOptionsAdapter.OUTPUTJAR, "");
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);

		StringBuffer sb2 = new StringBuffer(outJar2);
		IPath path2 = new Path(sb2.substring(sb2.lastIndexOf(projectY.getName())));
		IResource jarFile2 = projectY.getWorkspace().getRoot().findMember(path2.makeAbsolute());
		monitor.reset();
		jarFile2.delete(true,monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("Second Output jar should NOT exist! (path=" + outJar2 + ")"
				,new File(outJar2).exists());
		
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectX);
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertFalse("project X should not have outjar on classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY,outJar2));
		
		// reset projects to their original state:
		AJDTUtils.removeAspectJNature(projectY);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project", 
				projectY.hasNature(AspectJUIPlugin.ID_NATURE));
		assertFalse("project X should not be an AJ project", 
				projectX.hasNature(AspectJUIPlugin.ID_NATURE));
		
		// close all the projects so that they don't hang around and hold
		// up the build for other tests
		monitor.reset();
		projectX.close(monitor);
		monitor.waitForCompletion();
		monitor.reset();
		projectY.close(monitor);
		monitor.waitForCompletion();
		
	}
	
	/**
	 * Want to check that if call build twice on the same project with an
	 * outjar set, then for any depending projects, their classpath 
	 * only contains one entry corresponding to this outjar 
	 */
	public void testBuildTwiceWithOutJar() throws Exception {
		
		IProject projectY = Utils.getPredefinedProject("project.java.Y", true);
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		IProject projectX = Utils.getPredefinedProject("project.java.X", true);
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectX);
		// sanity check: at this point there should be no error markers, both 
		// projects should build as they're both java projects, project X should
		// should have a project dependency on project Y
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project", 
				projectY.hasNature(AspectJUIPlugin.ID_NATURE));
		assertFalse("project X should not be an AJ project", 
				projectX.hasNature(AspectJUIPlugin.ID_NATURE));
		
		// convert project Y to be an AJ project and check setup is correct
		AJDTUtils.addAspectJNature(projectY);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		
		String outJar = ProjectDependenciesUtils.setupOutJar("anotherJar.jar",projectY);
		projectY.setPersistentProperty(BuildOptionsAdapter.OUTPUTJAR, outJar);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("Output jar should NOT exist! (path=" + outJar + ")",new File(outJar).exists());

		// build the project so it picks up the outjar and sends 
		// output there
		BlockingProgressMonitor monitor = new BlockingProgressMonitor();
		monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		
		// check that outjar exists etc, but that the jar only appears
		// once on the classpath and aspect path
		assertTrue("Output jar should exist! (path=" + outJar + ")",new File(outJar).exists());
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have project Y's outjar on it's classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, outJar));
		assertEquals(1,ProjectDependenciesUtils.numberOfTimesOutJarOnClasspath(projectX,
				projectY,outJar));
		
		// build again and check that the same still holds
		monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, monitor);
		monitor.waitForCompletion();
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);

		assertTrue("Output jar should exist! (path=" + outJar + ")",new File(outJar).exists());
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have project Y's outjar on it's classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, outJar));
		assertEquals(1,ProjectDependenciesUtils.numberOfTimesOutJarOnClasspath(projectX,
				projectY,outJar));
		
		// reset projects to their original state:
		AJDTUtils.removeAspectJNature(projectY);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project", 
				projectY.hasNature(AspectJUIPlugin.ID_NATURE));
		assertFalse("project X should not be an AJ project", 
				projectX.hasNature(AspectJUIPlugin.ID_NATURE));
		
		// close all the projects so that they don't hang around and hold
		// up the build for other tests
		monitor.reset();
		projectX.close(monitor);
		monitor.waitForCompletion();
		monitor.reset();
		projectY.close(monitor);
		monitor.waitForCompletion();
		
	}	

	private int numberOfTimesOutJarOnAspectPath(IProject projectWhichHasDependency,
			IProject projectDependedOn,
			String outJar) throws CoreException {
		IClasspathEntry[] aspectPathEntries = 
			getInitialAspectpathValue(projectWhichHasDependency);		
		int counter = 0;
		IPath newPath = getRelativePath(projectDependedOn,outJar);
		if (newPath == null) {
			return counter;
		}
		IClasspathEntry cpEntry = JavaCore.newLibraryEntry(newPath
				.makeAbsolute(), null, null);
		
		if (aspectPathEntries != null) {
			for (int i = 0; i < aspectPathEntries.length; i++) {
				if (aspectPathEntries[i].getPath().equals(cpEntry.getPath())
						&& aspectPathEntries[i].getContentKind() == cpEntry.getContentKind()
						&& aspectPathEntries[i].getEntryKind() == cpEntry.getEntryKind()){
					counter++;
				}
			}
		}
		return counter;
	}

	private IClasspathEntry[] getInitialAspectpathValue(IProject project)
			throws CoreException {
		List result = new ArrayList();
		String paths = project
				.getPersistentProperty(BuildOptionsAdapter.ASPECTPATH);
		String cKinds = project
				.getPersistentProperty(BuildOptionsAdapter.ASPECTPATH_CON_KINDS);
		String eKinds = project
				.getPersistentProperty(BuildOptionsAdapter.ASPECTPATH_ENT_KINDS);

		if ((paths != null && paths.length() > 0)
				&& (cKinds != null && cKinds.length() > 0)
				&& (eKinds != null && eKinds.length() > 0)) {
			StringTokenizer sTokPaths = new StringTokenizer(paths,
					File.pathSeparator);
			StringTokenizer sTokCKinds = new StringTokenizer(cKinds,
					File.pathSeparator);
			StringTokenizer sTokEKinds = new StringTokenizer(eKinds,
					File.pathSeparator);

			if ((sTokPaths.countTokens() == sTokCKinds.countTokens())
					&& (sTokPaths.countTokens() == sTokEKinds.countTokens())) {
				while (sTokPaths.hasMoreTokens()) {
					IClasspathEntry entry = new ClasspathEntry(Integer
							.parseInt(sTokCKinds.nextToken()), // content kind
							Integer.parseInt(sTokEKinds.nextToken()), // entrykind
							new Path(sTokPaths.nextToken()), // path
							new IPath[] {}, // inclusion patterns
							new IPath[] {}, // exclusion patterns
							null, // src attachment path
							null, // src attachment root path
							null, // output location
							false); // is exported ?
					result.add(entry);
				}
			}
		}
		if (result.size() > 0) {
			return (IClasspathEntry[]) result.toArray(new IClasspathEntry[0]);
		} else {
			return null;
		}
	}

	
	private IPath getRelativePath(IProject project, String outJar) {
		StringBuffer sb = new StringBuffer(outJar);
		int index = sb.lastIndexOf(project.getName());
		if (index > 0) {
			IPath path = new Path(sb.substring(sb.lastIndexOf(project.getName())));
			return path.makeAbsolute();
		} else {
			return null;
		}
	}
	
}

