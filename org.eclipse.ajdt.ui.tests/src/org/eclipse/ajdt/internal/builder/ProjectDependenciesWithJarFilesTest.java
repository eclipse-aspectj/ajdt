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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import junit.framework.TestCase;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.refactoring.ReaderInputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author hawkinsh
 *
 */
public class ProjectDependenciesWithJarFilesTest extends TestCase {
		
	/**
	 * This tests part of the fix for 71371 with dependencies between projects with
	 * exported jar files. Have two projects A and B where A contains a jar file and 
	 * java files. The source in project B depends on code in both the jar file and 
	 * A's class files. 
	 * 
	 * Everything should work ok if A is converted to be an AJ project
	 */
	 public void testWithExportedJarFile() throws Exception {
		IProject projectA = Utils.createPredefinedProject("another.project.AAA");
		IProject projectB = Utils.createPredefinedProject("another.project.B");
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
		Utils.waitForJobsToComplete();
		projectB.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
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
		Utils.waitForJobsToComplete();
		projectB.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
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

		Utils.deleteProject(projectA);
		Utils.deleteProject(projectB);
//		// close all the projects so that they don't hang around and hold
//		// up the build for other tests
//		BlockingProgressMonitor monitor = new BlockingProgressMonitor();
//		monitor.reset();
//		projectA.close(monitor);
//		monitor.waitForCompletion();
//		monitor.reset();
//		projectB.close(monitor);
//		monitor.waitForCompletion();
		
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
		
		IProject projA = Utils.createPredefinedProject("non.plugin.project.A1");
		IProject projB = Utils.createPredefinedProject("non.plugin.project.B");
		IProject projC = Utils.createPredefinedProject("non.plugin.project.C");

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
		Utils.waitForJobsToComplete();
		projC.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();

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
		Utils.waitForJobsToComplete();
		projC.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
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
		
		Utils.deleteProject(projA);
		Utils.deleteProject(projB);
		Utils.deleteProject(projC);
//		// close all the projects so that they don't hang around and hold
//		// up the build for other tests
//		BlockingProgressMonitor monitor = new BlockingProgressMonitor();
//		monitor.reset();
//		projA.close(monitor);
//		monitor.waitForCompletion();
//		monitor.reset();
//		projB.close(monitor);
//		monitor.waitForCompletion();
//		monitor.reset();
//		projC.close(monitor);
//		monitor.waitForCompletion();
		
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
		IProject projectB = Utils.createPredefinedProject("pd.non.plugin.project.B");
		IProject projectB1 = Utils.createPredefinedProject("pd.non.plugin.project.B1");

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
		Utils.waitForJobsToComplete();
		projectB1.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
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
		Utils.waitForJobsToComplete();
		projectB1.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
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

		Utils.deleteProject(projectB);
		Utils.deleteProject(projectB1);
//		// close all the projects so that they don't hang around and hold
//		// up the build for other tests
//		BlockingProgressMonitor monitor = new BlockingProgressMonitor();
//		monitor.reset();
//		projectB.close(monitor);
//		monitor.waitForCompletion();
//		monitor.reset();
//		projectB1.close(monitor);
//		monitor.waitForCompletion();
		
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
		IProject projectY = Utils.createPredefinedProject("project.java.Y");
		IProject projectX = Utils.createPredefinedProject("project.java.X");

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
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project", 
				AspectJPlugin.isAJProject(projectX));
		
		// convert project Y to be an AJ project and check setup is correct
		AJDTUtils.addAspectJNature(projectY);
		Utils.waitForJobsToComplete();
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
		AspectJCorePreferences.setProjectOutJar(projectY,outJar);
		Utils.waitForJobsToComplete();

		// build the project so it picks up the outjar and sends 
		// output there
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null);
		Utils.waitForJobsToComplete();
		projectY.refreshLocal(IResource.DEPTH_INFINITE,null);
		Utils.waitForJobsToComplete();

		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null);
		Utils.waitForJobsToComplete();
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();

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

		AspectJCorePreferences.setProjectOutJar(projectY,"");
		Utils.waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null);
		Utils.waitForJobsToComplete();
		
		StringBuffer sb = new StringBuffer(outJar);
		IPath path = new Path(sb.substring(sb.lastIndexOf(projectY.getName())));
		IResource jarFile = projectY.getWorkspace().getRoot().findMember(path.makeAbsolute());
		jarFile.delete(true,null);
		Utils.waitForJobsToComplete();
		assertFalse("Output jar should NOT exist! (path=" + outJar + ")",new File(outJar).exists());
		
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertFalse("project X should not have outjar on classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY,outJar));
		
		// reset projects to their original state:
		AJDTUtils.removeAspectJNature(projectY);
		Utils.waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project", 
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project", 
				AspectJPlugin.isAJProject(projectX));

		Utils.deleteProject(projectX);
		Utils.deleteProject(projectY);
//		// close all the projects so that they don't hang around and hold
//		// up the build for other tests
//		monitor.reset();
//		projectX.close(monitor);
//		monitor.waitForCompletion();
//		monitor.reset();
//		projectY.close(monitor);
//		monitor.waitForCompletion();
		
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
		IProject projectY = Utils.createPredefinedProject("project.java.Y");
		IProject projectX = Utils.createPredefinedProject("project.java.X");

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
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project", 
				AspectJPlugin.isAJProject(projectX));
		
		// convert project Y to be an AJ project and check setup is correct
		AJDTUtils.addAspectJNature(projectY);
		Utils.waitForJobsToComplete();
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
		Utils.waitForJobsToComplete();
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
		AspectJCorePreferences.setProjectOutJar(projectY,outJar);
		Utils.waitForJobsToComplete();
		assertFalse("Output jar should not yet exist! (path=" + outJar + ")",new File(outJar).exists());

		// build the project so it picks up the outjar and sends 
		// output there
		//BlockingProgressMonitor monitor = new BlockingProgressMonitor();
		//monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null);
		//monitor.waitForCompletion();
		Utils.waitForJobsToComplete();

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
		AspectJCorePreferences.setProjectOutJar(projectY,"");
		Utils.waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null);
		Utils.waitForJobsToComplete();
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));

		projectX.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null);
		Utils.waitForJobsToComplete();
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertFalse("project X should not have outjar on classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY,outJar));

				
		// reset projects to their original state:
		AJDTUtils.removeAspectJNature(projectX);
		Utils.waitForJobsToComplete();
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();

		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null);
		Utils.waitForJobsToComplete();

		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));

		projectY.refreshLocal(IResource.DEPTH_INFINITE,null);
		Utils.waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null);
		Utils.waitForJobsToComplete();
		

		
		AJDTUtils.removeAspectJNature(projectY);
		Utils.waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();;
		
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project", 
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project", 
				AspectJPlugin.isAJProject(projectX));

		Utils.deleteProject(projectX);
		Utils.deleteProject(projectY);
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

		
//		// close all the projects so that they don't hang around and hold
//		// up the build for other tests
//		monitor.reset();
//		projectX.close(monitor);
//		monitor.waitForCompletion();
//		monitor.reset();
//		projectY.close(monitor);
//		monitor.waitForCompletion();
		
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
		IProject projectY = Utils.createPredefinedProject("project.java.Y");
		IProject projectX = Utils.createPredefinedProject("project.java.X");

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
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project", 
				AspectJPlugin.isAJProject(projectX));
		
		// convert project Y to be an AJ project and check setup is correct
		AJDTUtils.addAspectJNature(projectY);
		Utils.waitForJobsToComplete();
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
		AspectJCorePreferences.setProjectOutJar(projectY,outJar);
		Utils.waitForJobsToComplete();
		assertFalse("Output jar should NOT exist! (path=" + outJar + ")",new File(outJar).exists());

		// build the project so it picks up the outjar and sends 
		// output there
		//BlockingProgressMonitor monitor = new BlockingProgressMonitor();
		//monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null);
		//monitor.waitForCompletion();
		Utils.waitForJobsToComplete();

		assertTrue("Output jar should exist! (path=" + outJar + ")",new File(outJar).exists());
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));

		// TODO
		assertTrue("project X should have project Y's outjar on it's classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, outJar));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));		
		assertFalse("project X should not have a class folder dependency on project Y",
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));

		// set project Y to send output to jar file newJar.jar
		// setup the outjar
		String outJar2 = ProjectDependenciesUtils.setupOutJar("newJar.jar",projectY);
		AspectJCorePreferences.setProjectOutJar(projectY,outJar2);
		Utils.waitForJobsToComplete();
		assertFalse("Output jar should NOT exist! (path=" + outJar2 + ")",new File(outJar2).exists());

		// build the project so it picks up the outjar and sends 
		// output there
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null);
		Utils.waitForJobsToComplete();

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
		jarFile.delete(true,null);
		Utils.waitForJobsToComplete();
		assertFalse("Output jar should NOT exist! (path=" + outJar + ")",new File(outJar).exists());

		AspectJCorePreferences.setProjectOutJar(projectY,"");
		Utils.waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null);
		Utils.waitForJobsToComplete();

		StringBuffer sb2 = new StringBuffer(outJar2);
		IPath path2 = new Path(sb2.substring(sb2.lastIndexOf(projectY.getName())));
		IResource jarFile2 = projectY.getWorkspace().getRoot().findMember(path2.makeAbsolute());
		jarFile2.delete(true,null);
		Utils.waitForJobsToComplete();
		assertFalse("Second Output jar should NOT exist! (path=" + outJar2 + ")"
				,new File(outJar2).exists());
		
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertFalse("project X should not have outjar on classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY,outJar2));
		
		// reset projects to their original state:
		AJDTUtils.removeAspectJNature(projectY);
		Utils.waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project", 
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project", 
				AspectJPlugin.isAJProject(projectX));
		
		Utils.deleteProject(projectX);
		Utils.deleteProject(projectY);
//		// close all the projects so that they don't hang around and hold
//		// up the build for other tests
//		monitor.reset();
//		projectX.close(monitor);
//		monitor.waitForCompletion();
//		monitor.reset();
//		projectY.close(monitor);
//		monitor.waitForCompletion();
		
	}
	
	/**
	 * Want to check that if call build twice on the same project with an
	 * outjar set, then for any depending projects, their classpath 
	 * only contains one entry corresponding to this outjar 
	 */
	public void testBuildTwiceWithOutJar() throws Exception {
		IProject projectY = Utils.createPredefinedProject("project.java.Y");
		IProject projectX = Utils.createPredefinedProject("project.java.X");

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
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project", 
				AspectJPlugin.isAJProject(projectX));
		
		// convert project Y to be an AJ project and check setup is correct
		AJDTUtils.addAspectJNature(projectY);
		Utils.waitForJobsToComplete();
		
		String outJar = ProjectDependenciesUtils.setupOutJar("anotherJar.jar",projectY);
		AspectJCorePreferences.setProjectOutJar(projectY,outJar);
		Utils.waitForJobsToComplete();
		assertFalse("Output jar should NOT exist! (path=" + outJar + ")",new File(outJar).exists());

		// build the project so it picks up the outjar and sends 
		// output there
//		BlockingProgressMonitor monitor = new BlockingProgressMonitor();
//		monitor.reset();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null);
		Utils.waitForJobsToComplete();
		
		// check that outjar exists etc, but that the jar only appears
		// once on the classpath and aspect path
		assertTrue("Output jar should exist! (path=" + outJar + ")",new File(outJar).exists());
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));

		// TODO
//		assertTrue("project X should have project Y's outjar on it's classpath",
//				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, outJar));
		assertEquals(1,ProjectDependenciesUtils.numberOfTimesOutJarOnClasspath(projectX,
				projectY,outJar));
		
		// build again and check that the same still holds
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null);
		Utils.waitForJobsToComplete();

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
		Utils.waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
		assertFalse("project Y should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y",
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project", 
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project", 
				AspectJPlugin.isAJProject(projectX));
		
		Utils.deleteProject(projectX);
		Utils.deleteProject(projectY);
//		// close all the projects so that they don't hang around and hold
//		// up the build for other tests
//		monitor.reset();
//		projectX.close(monitor);
//		monitor.waitForCompletion();
//		monitor.reset();
//		projectY.close(monitor);
//		monitor.waitForCompletion();
		
	}	

	/**
	 * Test for bug 48518 - if contents of a jar file changes, then force a
	 * build for dependent projects.
	 * 
	 * @throws Exception
	 */
	public void testDependingProjectBuiltWhenOutjarChanges() throws Exception {
		// test setup
		IProject jarCreatingProject = Utils.createPredefinedProject("jarCreatingProject");
		IProject jarDependentProject = Utils.createPredefinedProject("jarDependentProject");

		// sanity check on setup of projects....
		
		String outjar = AspectJCorePreferences.getProjectOutJar(jarCreatingProject);
		String jar = ProjectDependenciesUtils.setupOutJar("myJar.jar",jarCreatingProject);
		if(outjar == null || !outjar.equals("myJar.jar")) {			
			AspectJCorePreferences.setProjectOutJar(jarCreatingProject,jar);
		}
		outjar = AspectJCorePreferences.getProjectOutJar(jarCreatingProject);
		assertEquals("the outjar should be called myjar.jar",jar,outjar);
		assertTrue("jarDependentProject should have a project dependency on jarCreatingProject",
				ProjectDependenciesUtils.projectHasProjectDependency(jarDependentProject, jarCreatingProject));
		assertTrue("jarDependentProject should have jarCreatingProject's outjar on it's classpath",
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(jarDependentProject,jarCreatingProject, outjar));
		
		jarCreatingProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
		jarDependentProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
		
		assertFalse("jarCreatingProject should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(jarCreatingProject,null));
		assertFalse("jarDependentProject should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(jarDependentProject,null));
		assertTrue("jarDependentProject should have a project dependency on jarCreatingProject",
				ProjectDependenciesUtils.projectHasProjectDependency(jarDependentProject, jarCreatingProject));
		assertTrue("jarCreatingProject should be an AJ project", 
				AspectJPlugin.isAJProject(jarCreatingProject));
		assertTrue("jarDependentProject should be an AJ project", 
				AspectJPlugin.isAJProject(jarDependentProject));
		
		// add new abstract pointcut to A.aj in jarCreatingProject
		IFolder src = jarCreatingProject.getFolder("src");
		if (!src.exists()){
			src.create(true, true, null);
		}
		IFolder p1 = src.getFolder("p1");
		if (!p1.exists()){
			p1.create(true, true, null);
		}
				
		IFile A = p1.getFile("A.aj");
		assertNotNull("There should be an aspect called A",A);
		
		InputStream contentsOfA = A.getContents();	
		StringBuffer sb = new StringBuffer();		
		BufferedReader reader = new BufferedReader(new InputStreamReader(contentsOfA));		
		String line = reader.readLine();
		while (line != null) {
			sb.append(line);
			if (line.indexOf("public abstract pointcut myPC();") != -1) {
				sb.append("public abstract pointcut anotherPC();");
			}
			line = reader.readLine();
		}
		StringReader sr = new StringReader(sb.toString());
		A.setContents(new ReaderInputStream(sr),IResource.FORCE, null);
		
		sr.close();
		reader.close();
		contentsOfA.close();

		// build jarCreatingProject which should trigger rebuild of 
		// jarDependingProject which should then have an error marker against
		// it saying "inherited abstract pointcut p1.A.anotherPC() is not made concrete in Concrete"
		jarCreatingProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();

		assertFalse("jarCreatingProject should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(jarCreatingProject,null));
		assertTrue("jarDependentProject should have a build error: inherited abstract pointcut p1.A.anotherPC() is not made concrete in Concrete",
				ProjectDependenciesUtils.projectIsMarkedWithError(jarDependentProject,"inherited abstract pointcut p1.A.anotherPC() is not made concrete in Concrete"));

		InputStream contentsOfA2 = A.getContents();	
		StringBuffer sb2 = new StringBuffer();		
		BufferedReader reader2 = new BufferedReader(new InputStreamReader(contentsOfA2));		
		String line2 = reader2.readLine();
		while (line2 != null) {
			// comment out offending line
			if (line2.indexOf("public abstract pointcut anotherPC();") != -1) {
				sb2.append("// public abstract pointcut anotherPC();");
			} else {
				sb2.append(line2);
			}
			line2 = reader2.readLine();
		}
		StringReader sr2 = new StringReader(sb2.toString());
		A.setContents(new ReaderInputStream(sr2),IResource.FORCE, null);
		
		sr2.close();
		reader2.close();
		contentsOfA2.close();
		
		jarCreatingProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();

		assertFalse("jarCreatingProject should build with no errors",
				ProjectDependenciesUtils.projectIsMarkedWithError(jarCreatingProject,null));
		assertFalse("jarDependentProject should have a build error: inherited abstract pointcut p1.A.anotherPC() is not made concrete in Concrete",
				ProjectDependenciesUtils.projectIsMarkedWithError(jarDependentProject,"inherited abstract pointcut p1.A.anotherPC() is not made concrete in Concrete"));
		
		Utils.deleteProject(jarDependentProject);
		Utils.deleteProject(jarCreatingProject);
	}
	

//	private int numberOfTimesOutJarOnAspectPath(IProject projectWhichHasDependency,
//			IProject projectDependedOn,
//			String outJar) throws CoreException {
//		IClasspathEntry[] aspectPathEntries = 
//			getInitialAspectpathValue(projectWhichHasDependency);		
//		int counter = 0;
//		IPath newPath = getRelativePath(projectDependedOn,outJar);
//		if (newPath == null) {
//			return counter;
//		}
//		IClasspathEntry cpEntry = JavaCore.newLibraryEntry(newPath
//				.makeAbsolute(), null, null);
//		
//		if (aspectPathEntries != null) {
//			for (int i = 0; i < aspectPathEntries.length; i++) {
//				if (aspectPathEntries[i].getPath().equals(cpEntry.getPath())
//						&& aspectPathEntries[i].getContentKind() == cpEntry.getContentKind()
//						&& aspectPathEntries[i].getEntryKind() == cpEntry.getEntryKind()){
//					counter++;
//				}
//			}
//		}
//		return counter;
//	}

//	private IClasspathEntry[] getInitialAspectpathValue(IProject project)
//			throws CoreException {
//		List result = new ArrayList();
//		String paths = project
//				.getPersistentProperty(BuildOptionsAdapter.ASPECTPATH);
//		String cKinds = project
//				.getPersistentProperty(BuildOptionsAdapter.ASPECTPATH_CON_KINDS);
//		String eKinds = project
//				.getPersistentProperty(BuildOptionsAdapter.ASPECTPATH_ENT_KINDS);
//
//		if ((paths != null && paths.length() > 0)
//				&& (cKinds != null && cKinds.length() > 0)
//				&& (eKinds != null && eKinds.length() > 0)) {
//			StringTokenizer sTokPaths = new StringTokenizer(paths,
//					File.pathSeparator);
//			StringTokenizer sTokCKinds = new StringTokenizer(cKinds,
//					File.pathSeparator);
//			StringTokenizer sTokEKinds = new StringTokenizer(eKinds,
//					File.pathSeparator);
//
//			if ((sTokPaths.countTokens() == sTokCKinds.countTokens())
//					&& (sTokPaths.countTokens() == sTokEKinds.countTokens())) {
//				while (sTokPaths.hasMoreTokens()) {
//					IClasspathEntry entry = new ClasspathEntry(Integer
//							.parseInt(sTokCKinds.nextToken()), // content kind
//							Integer.parseInt(sTokEKinds.nextToken()), // entrykind
//							new Path(sTokPaths.nextToken()), // path
//							new IPath[] {}, // inclusion patterns
//							new IPath[] {}, // exclusion patterns
//							null, // src attachment path
//							null, // src attachment root path
//							null, // output location
//							false); // is exported ?
//					result.add(entry);
//				}
//			}
//		}
//		if (result.size() > 0) {
//			return (IClasspathEntry[]) result.toArray(new IClasspathEntry[0]);
//		} else {
//			return null;
//		}
//	}
//
//	
//	private IPath getRelativePath(IProject project, String outJar) {
//		StringBuffer sb = new StringBuffer(outJar);
//		int index = sb.lastIndexOf(project.getName());
//		if (index > 0) {
//			IPath path = new Path(sb.substring(sb.lastIndexOf(project.getName())));
//			return path.makeAbsolute();
//		} else {
//			return null;
//		}
//	}
	
}

