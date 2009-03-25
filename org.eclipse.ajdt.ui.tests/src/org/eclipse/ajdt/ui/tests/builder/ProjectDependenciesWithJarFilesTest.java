/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - initial version
 *     Helen Hawkins   - updated for new ajde interface (bug 148190)
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.internal.ui.refactoring.ReaderInputStream;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author hawkinsh
 *
 */
public class ProjectDependenciesWithJarFilesTest extends UITestCase {
		
    protected void setUp() throws Exception {
        super.setUp();
        // avoid prompting for depency removal.
        AspectJPreferences.setAskPDEAutoRemoveImport(false);
        // automatically remove import from classpath
        AspectJPreferences.setDoPDEAutoRemoveImport(true);
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
		IProject projectA = createPredefinedProject("another.project.AAA"); //$NON-NLS-1$
		IProject projectB = createPredefinedProject("another.project.B"); //$NON-NLS-1$
		// sanity check: at this point there should be no error markers, both 
		// projects should build as they're both java projects, project A should
		// export a jar file and project B should have a project dependency on
		// project A
		assertFalse("project A should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectA,null));
		assertFalse("project B should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectB,null));
		assertTrue("project A should export a jar file",  //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projectA));
		assertTrue("project B should have a project dependency on project A", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectB, projectA));

		checkForJDTBug84214(projectB,projectA);
		// convert project A to be an aspectj project
		AspectJUIPlugin.convertToAspectJProject(projectA);
		waitForJobsToComplete();
		projectB.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();
		// there should still not be build errors on project B, project A should still 
		// export the jar file, project B should now have the exported jar file as an
		// entry on it's classpath, and project B should have a classfolder dependency on
		// project A
		assertFalse("project B should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectB,null));
		assertTrue("project A should export a jar file",  //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projectA));
		// PD: switching following 3 asserts because current thinking is not to change project dependencies to classfolder ones
		//assertTrue("project B should have jar file on classpath (and not export it)",
		//		projectHasJarOnClasspath(projectB,projectA));
		//assertTrue("project B should have class folder dependency on project A", 
		//		projectHasClassFolderDependency(projectB,projectA));
		//assertFalse("project B should not have a project dependency on project A",
		//		projectHasProjectDependency(projectB,projectA));
		assertFalse("project B should NOT have jar file on classpath (and not export it)", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasJarOnClasspath(projectB,projectA));
		assertFalse("project B should NOT have class folder dependency on project A",  //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectB,projectA));
		assertTrue("project B should still have a project dependency on project A", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectB,projectA));
		
		
		// remove AJ nature frrom project A
		AspectJUIPlugin.convertFromAspectJProject(projectA);
		waitForJobsToComplete();
		projectB.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();
		// There should be no build errors on project B, project B should have a project dependency 
		// on project A, project A should still export the jar file, project B should not include 
		// the exported jar file on it's classpath
		assertFalse("project B should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectB,null));
		assertTrue("project B should have a project dependency on project A", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectB,projectA));
		assertFalse("project B should not have a class folder dependency on project A", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectB,projectA));
		assertTrue("project A should export a jar file",  //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projectA));
		assertFalse("project B should not have jar file on classpath (and not export it)", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasJarOnClasspath(projectB,projectA));

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
		
		IProject projA = createPredefinedProject("non.plugin.project.A1"); //$NON-NLS-1$
		IProject projB = createPredefinedProject("non.plugin.project.B"); //$NON-NLS-1$
		IProject projC = createPredefinedProject("non.plugin.project.C"); //$NON-NLS-1$

		// sanity check: at this point there should be no error markers and both 
		// projects B and C should build as they're both java projects.
		// Also:
		// - project A should be a binary project with no src
		// - project B should export a jar file
		// - project C should have a project dependency on project B
		assertFalse("project B should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projB,null));
		assertFalse("project C should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projC,null));
		assertTrue("project A should have no src",  //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasNoSrc(projA));
		assertTrue("project B should export a jar file",  //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projB));
		assertTrue("project C should have a project dependency on project B", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projC,projB));
		assertFalse("project C should not have a class dependency on project B", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projC,projB));

		checkForJDTBug84214(projC,projB);
	
		// convert project B to be an AspectJ project
		AspectJUIPlugin.convertToAspectJProject(projB);
		waitForJobsToComplete();
		projC.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();

		assertFalse("project B should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projB,null));		
		assertFalse("project C should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projC,null));
		assertTrue("project B should still export the jar file",  //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projB));
		// PD: switching following 3 asserts because current thinking is not to change project dependencies to classfolder ones
		//assertTrue("project C should have jar file on classpath (and not export it)",
		//		projectHasJarOnClasspath(projC,projB));		
		//assertTrue("project C should have a class folder dependency on project B",
		//		projectHasClassFolderDependency(projC,projB));
		//assertFalse("project C should NOT have a project dependency on project B",
		//		projectHasProjectDependency(projC,projB));
		assertFalse("project C should NOT have jar file on classpath (and not export it)", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasJarOnClasspath(projC,projB));		
		assertFalse("project C should NOT have a class folder dependency on project B", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projC,projB));
		assertTrue("project C should still have a project dependency on project B", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projC,projB));
		
		// remove AJ nature from project B
		AspectJUIPlugin.convertFromAspectJProject(projB);
		waitForJobsToComplete();
		projC.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();
		// the situation:
		// - there should still not be build errors on project B or project C
		// - project B should still export the jar file, 
		// - project C should not have the exported jar file as an entry on it's classpath
		// - project B should have a project dependency on project A
		assertFalse("project B should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projB,null));		
		assertFalse("project C should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projC,null));
		assertTrue("project B should still export the jar file",  //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projB));
		assertFalse("project C should have jar file on classpath (and not export it)", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasJarOnClasspath(projC,projB));		
		assertFalse("project C should have a class folder dependency on project B", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projC,projB));
		assertTrue("project C should NOT have a project dependency on project B", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projC,projB));

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
		IProject projectB = createPredefinedProject("pd.non.plugin.project.B"); //$NON-NLS-1$
		IProject projectB1 = createPredefinedProject("pd.non.plugin.project.B1"); //$NON-NLS-1$

		// sanity check: at this point there should be no error markers, both 
		// projects should build as they're both java projects, project A should
		// export a jar file and project B should have a project dependency on
		// project A
		assertFalse("project B should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectB,null));
		assertFalse("project B1 should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectB1,null));
		assertTrue("project B should export a jar file",  //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projectB));
		assertTrue("project B1 should have a project dependency on project B", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectB1, projectB));
		assertTrue("project B should contain no src directory",  //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasNoSrc(projectB));
		
		checkForJDTBug84214(projectB1,projectB);
		// convert project A to be an aspectj project
		AspectJUIPlugin.convertToAspectJProject(projectB);
		waitForJobsToComplete();
		projectB1.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();
		// there should still not be build errors on project B, project A should still 
		// export the jar file, project B should now have the exported jar file as an
		// entry on it's classpath, and project B should have a classfolder dependency on
		// project A
		assertFalse("project B1 should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectB1,null));
		assertTrue("project B should export a jar file",  //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projectB));
		assertFalse("project B1 should not have class folder dependency on project B",  //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectB1,projectB));
		// PD: switching following 2 asserts because current thinking is not to change project dependencies to classfolder ones
		//assertTrue("project B1 should have jar file on classpath (and not export it)",
		//		projectHasJarOnClasspath(projectB1,projectB));
		//assertFalse("project B1 should not have a project dependency on project B",
		//		projectHasProjectDependency(projectB1,projectB));
		assertFalse("project B1 should NOT have jar file added on classpath (and not export it)", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasJarOnClasspath(projectB1,projectB));
		assertTrue("project B1 should still have a project dependency on project B", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectB1,projectB));
		
		// remove AJ nature frrom project A
		AspectJUIPlugin.convertFromAspectJProject(projectB);
		waitForJobsToComplete();
		projectB1.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();
		// There should be no build errors on project B, project B should have a project dependency 
		// on project A, project A should still export the jar file, project B should not include 
		// the exported jar file on it's classpath
		assertFalse("project B1 should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectB1,null));
		assertTrue("project B1 should have a project dependency on project B", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectB1,projectB));
		assertFalse("project B1 should not have a class folder dependency on project B", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectB1,projectB));
		assertTrue("project B should export a jar file",  //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasAnExportedClasspathEntry(projectB));
		assertFalse("project B1 should not have jar file on classpath (and not export it)", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasJarOnClasspath(projectB1,projectB));
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
		IProject projectY = createPredefinedProject("project.java.Y"); //$NON-NLS-1$
		IProject projectX = createPredefinedProject("project.java.X"); //$NON-NLS-1$

		// sanity check: at this point there should be no error markers, both 
		// projects should build as they're both java projects, project X should
		// should have a project dependency on project Y
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectX));

		checkForJDTBug84214(projectX,projectY);
		// convert project Y to be an AJ project and check setup is correct
		AspectJUIPlugin.convertToAspectJProject(projectY);
		waitForJobsToComplete();
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		// PD: switching following 2 asserts because current thinking is not to change project dependencies to classfolder ones
		//assertFalse("project X should not have a project dependency on project Y",
		//		projectHasProjectDependency(projectX, projectY));
		//assertTrue("project X should have a class folder dependency on project Y",
		//		projectHasClassFolderDependency(projectX,projectY));
		assertTrue("project X should still have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project X should NOT have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));
			
		// set project Y to send output to jar file
		// setup the outjar
		String outJar = "mainWork.jar";
		String fullPathOutJar = ProjectDependenciesUtils.makeFullPath(outJar,projectY); //$NON-NLS-1$
		AspectJCorePreferences.setProjectOutJar(projectY,outJar);
		waitForJobsToComplete();

		// build the project so it picks up the outjar and sends 
		// output there
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();
		waitForJobsToComplete();
		projectY.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();

		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();
		waitForJobsToComplete();
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();

		assertTrue("Output jar should exist! (path=" + fullPathOutJar + ")",new File(fullPathOutJar).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have project Y's outjar on it's classpath", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, fullPathOutJar));

		// PD: switching following assert because current thinking is not to change project dependencies to classfolder ones
		//assertFalse("project X should not have a project dependency on project Y",
		//		projectHasProjectDependency(projectX, projectY));
		assertTrue("project X should have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		
		
		assertFalse("project X should not have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));

		AspectJCorePreferences.setProjectOutJar(projectY,""); //$NON-NLS-1$
		waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();
		waitForJobsToComplete();
		
		StringBuffer sb = new StringBuffer(fullPathOutJar);
		IPath path = new Path(sb.substring(sb.lastIndexOf(projectY.getName())));
		IResource jarFile = projectY.getWorkspace().getRoot().findMember(path.makeAbsolute());
		jarFile.delete(true,null);
		waitForJobsToComplete();
		assertFalse("Output jar should NOT exist! (path=" + fullPathOutJar + ")",new File(fullPathOutJar).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();
		waitForJobsToComplete();
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertFalse("project X should not have outjar on classpath", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY,fullPathOutJar));
		
		// reset projects to their original state:
		AspectJUIPlugin.convertFromAspectJProject(projectY);
		waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();
		waitForJobsToComplete();
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project",  //$NON-NLS-1$
					AspectJPlugin.isAJProject(projectX));
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
		IProject projectY = createPredefinedProject("project.java.Y"); //$NON-NLS-1$
		IProject projectX = createPredefinedProject("project.java.X"); //$NON-NLS-1$

		
		// sanity check: at this point there should be no error markers, both 
		// projects should build as they're both java projects, project X should
		// should have a project dependency on project Y
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectX));
	
		checkForJDTBug84214(projectX,projectY);
		
		// convert project Y to be an AJ project and check setup is correct
		AspectJUIPlugin.convertToAspectJProject(projectY);
		waitForJobsToComplete();
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		// PD: switching following 2 asserts because current thinking is not to change project dependencies to classfolder ones
		//assertFalse("project X should not have a project dependency on project Y",
		//		projectHasProjectDependency(projectX, projectY));
		//assertTrue("project X should have a class folder dependency on project Y",
		//		projectHasClassFolderDependency(projectX,projectY));
		assertTrue("project X should still have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project X should NOT have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));

		// convert project X to be an AJ project and check setup is correct
		AspectJUIPlugin.convertToAspectJProject(projectX);
		waitForJobsToComplete();
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		// PD: switching following 2 asserts because current thinking is not to change project dependencies to classfolder ones
		//assertFalse("project X should not have a project dependency on project Y",
		//		projectHasProjectDependency(projectX, projectY));
		//assertTrue("project X should have a class folder dependency on project Y",
		//		projectHasClassFolderDependency(projectX,projectY));
		assertTrue("project X should still have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project X should NOT have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));

		// set project Y to send output to jar file
		// setup the outjar
		String outJar = "mainWork.jar"; 
		String fullPathOutJar = ProjectDependenciesUtils.makeFullPath(outJar,projectY); //$NON-NLS-1$
		AspectJCorePreferences.setProjectOutJar(projectY,outJar);
		waitForJobsToComplete();

		assertTrue("Output jar should now exist! (path=" + fullPathOutJar + ")",new File(fullPathOutJar).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have project Y's outjar on it's classpath", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, fullPathOutJar));
		// don't want to add outjar to aspect path
		assertNull("project X should have NOT have outjar on aspect path!", //$NON-NLS-1$
				AspectJUIPlugin.getDefault().getCompilerFactory().getCompilerForProject(projectX).getCompilerConfiguration().getAspectPath());
		assertTrue("project X should have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));		
		assertFalse("project X should not have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));

		// now set the outjar option back to nothing and build projectY 
		// to send output to bin directory rather than jar file
		AspectJCorePreferences.setProjectOutJar(projectY,""); //$NON-NLS-1$
		waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));

		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertFalse("project X should not have outjar on classpath", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY,outJar));

				
		// reset projects to their original state:
		AspectJUIPlugin.convertFromAspectJProject(projectX);
		waitForJobsToComplete();
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null); //$NON-NLS-1$

		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));

		projectY.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
				
		AspectJUIPlugin.convertFromAspectJProject(projectY);
		waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectX));
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
		IProject projectY = createPredefinedProject("project.java.Y"); //$NON-NLS-1$
		IProject projectX = createPredefinedProject("project.java.X"); //$NON-NLS-1$

		// sanity check: at this point there should be no error markers, both 
		// projects should build as they're both java projects, project X should
		// should have a project dependency on project Y
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectX));

			
		checkForJDTBug84214(projectX,projectY);
		
		// convert project Y to be an AJ project and check setup is correct
		AspectJUIPlugin.convertToAspectJProject(projectY);
		waitForJobsToComplete();
		waitForJobsToComplete();
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should still have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project X should NOT have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));
		
		// set project Y to send output to jar file mainWork.jar
		// setup the outjar
		String outJar = "firstJar.jar";
		String fullPathOutJar = ProjectDependenciesUtils.makeFullPath(outJar,projectY); //$NON-NLS-1$
		AspectJCorePreferences.setProjectOutJar(projectY,outJar);
		waitForJobsToComplete();

		assertTrue("Output jar should exist! (path=" + fullPathOutJar + ")",new File(fullPathOutJar).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));

		assertTrue("project X should have project Y's outjar on it's classpath", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, fullPathOutJar));
		assertTrue("project X should have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));		
		assertFalse("project X should not have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));

		// set project Y to send output to jar file newJar.jar
		// setup the outjar
		String outJar2 = "newJar.jar";
		String fullPathOutJar2 = ProjectDependenciesUtils.makeFullPath(outJar2,projectY); //$NON-NLS-1$
		AspectJCorePreferences.setProjectOutJar(projectY,outJar2);
		waitForJobsToComplete();

		assertTrue("Output jar should exist! (path=" + fullPathOutJar2 + ")",new File(fullPathOutJar2).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have project Y's NEW outjar on it's classpath", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, fullPathOutJar2));
		assertFalse("project X should NOT have project Y's old outjar on it's classpath", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, fullPathOutJar));
		assertTrue("project X should have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));		
		assertFalse("project X should not have a class folder dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasClassFolderDependency(projectX,projectY));

		StringBuffer sb = new StringBuffer(fullPathOutJar);
		IPath path = new Path(sb.substring(sb.lastIndexOf(projectY.getName())));
		IResource jarFile = projectY.getWorkspace().getRoot().findMember(path.makeAbsolute());
		jarFile.delete(true,null);
		waitForJobsToComplete();
		assertFalse("Output jar should NOT exist! (path=" + fullPathOutJar + ")",new File(fullPathOutJar).exists()); //$NON-NLS-1$ //$NON-NLS-2$

		AspectJCorePreferences.setProjectOutJar(projectY,""); //$NON-NLS-1$
		waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);

		StringBuffer sb2 = new StringBuffer(fullPathOutJar2);
		IPath path2 = new Path(sb2.substring(sb2.lastIndexOf(projectY.getName())));
		IResource jarFile2 = projectY.getWorkspace().getRoot().findMember(path2.makeAbsolute());
		jarFile2.delete(true,null);
		waitForJobsToComplete();
		assertFalse("Second Output jar should NOT exist! (path=" + fullPathOutJar2 + ")" //$NON-NLS-1$ //$NON-NLS-2$
				,new File(fullPathOutJar2).exists());
		
		projectX.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();
		waitForJobsToComplete();
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertFalse("project X should not have outjar on classpath", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY,fullPathOutJar2));
		
		// reset projects to their original state:
		AspectJUIPlugin.convertFromAspectJProject(projectY);
		waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectX));

	}
	
	/**
	 * Want to check that if call build twice on the same project with an
	 * outjar set, then for any depending projects, their classpath 
	 * only contains one entry corresponding to this outjar 
	 */
	public void testBuildTwiceWithOutJar() throws Exception {
		IProject projectY = createPredefinedProject("project.java.Y"); //$NON-NLS-1$
		IProject projectX = createPredefinedProject("project.java.X"); //$NON-NLS-1$

		// sanity check: at this point there should be no error markers, both 
		// projects should build as they're both java projects, project X should
		// should have a project dependency on project Y
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectX));

		// convert project Y to be an AJ project and check setup is correct
		AspectJUIPlugin.convertToAspectJProject(projectY);
		waitForJobsToComplete();
		
		checkForJDTBug84214(projectX,projectY);
		
		String outJar = "anotherJar.jar"; 
		String fullPathOutJar = ProjectDependenciesUtils.makeFullPath(outJar,projectY); //$NON-NLS-1$
		AspectJCorePreferences.setProjectOutJar(projectY,outJar);
		waitForJobsToComplete();
		
		// check that outjar exists etc, but that the jar only appears
		// once on the classpath and aspect path
		assertTrue("Output jar should exist! (path=" + fullPathOutJar + ")",new File(fullPathOutJar).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));

		assertTrue("project X should have project Y's outjar on it's classpath", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, fullPathOutJar));
		assertEquals(1,ProjectDependenciesUtils.numberOfTimesOutJarOnClasspath(projectX,
				projectY,fullPathOutJar));
		
		// build again and check that the same still holds
		projectY.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null); //$NON-NLS-1$
		waitForJobsToComplete();

		assertTrue("Output jar should exist! (path=" + fullPathOutJar + ")",new File(fullPathOutJar).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have project Y's outjar on it's classpath", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(projectX,projectY, fullPathOutJar));
		assertEquals(1,ProjectDependenciesUtils.numberOfTimesOutJarOnClasspath(projectX,
				projectY,fullPathOutJar));
		
		// reset projects to their original state:
		AspectJUIPlugin.convertFromAspectJProject(projectY);
		waitForJobsToComplete();
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();
		assertFalse("project Y should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectY,null));
		assertFalse("project X should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(projectX,null));
		assertTrue("project X should have a project dependency on project Y", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(projectX, projectY));
		assertFalse("project Y should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectY));
		assertFalse("project X should not be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(projectX));		
	}	

	/**
	 * Test for bug 48518 - if contents of a jar file changes, then force a
	 * build for dependent projects.
	 * 
	 * @throws Exception
	 */
	public void testDependingProjectBuiltWhenOutjarChanges() throws Exception {
		// test setup
		IProject jarCreatingProject = createPredefinedProject("jarCreatingProject"); //$NON-NLS-1$
		IProject jarDependentProject = createPredefinedProject("jarDependentProject"); //$NON-NLS-1$

		// sanity check on setup of projects....
		checkForJDTBug84214(jarDependentProject,jarCreatingProject);
		
		String outJar = "myJar.jar";
        AspectJCorePreferences.setProjectOutJar(jarCreatingProject, outJar);
		String fullPathOutJar = ProjectDependenciesUtils.makeFullPath("myJar.jar",jarCreatingProject); //$NON-NLS-1$

		assertTrue("jarDependentProject should have a project dependency on jarCreatingProject", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(jarDependentProject, jarCreatingProject));
		assertTrue("jarDependentProject should have jarCreatingProject's outjar on it's classpath", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasOutJarOnClasspath(jarDependentProject,jarCreatingProject, fullPathOutJar));
		
		jarCreatingProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
		jarDependentProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
		
		assertFalse("jarCreatingProject should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(jarCreatingProject,null));
		assertFalse("jarDependentProject should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(jarDependentProject,null));
		assertTrue("jarDependentProject should have a project dependency on jarCreatingProject", //$NON-NLS-1$
				ProjectDependenciesUtils.projectHasProjectDependency(jarDependentProject, jarCreatingProject));
		assertTrue("jarCreatingProject should be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(jarCreatingProject));
		assertTrue("jarDependentProject should be an AJ project",  //$NON-NLS-1$
				AspectJPlugin.isAJProject(jarDependentProject));
		
		// add new abstract pointcut to A.aj in jarCreatingProject
		IFolder src = jarCreatingProject.getFolder("src"); //$NON-NLS-1$
		if (!src.exists()){
			src.create(true, true, null);
		}
		IFolder p1 = src.getFolder("p1"); //$NON-NLS-1$
		if (!p1.exists()){
			p1.create(true, true, null);
		}
				
		IFile A = p1.getFile("A.aj"); //$NON-NLS-1$
		assertNotNull("There should be an aspect called A",A); //$NON-NLS-1$
		
		InputStream contentsOfA = A.getContents();	
		StringBuffer sb = new StringBuffer();		
		BufferedReader reader = new BufferedReader(new InputStreamReader(contentsOfA));		
		String line = reader.readLine();
		while (line != null) {
			sb.append(line);
			if (line.indexOf("public abstract pointcut myPC();") != -1) { //$NON-NLS-1$
				sb.append("public abstract pointcut anotherPC();"); //$NON-NLS-1$
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

		assertTrue("jarCreatingProject should have a build error: inherited abstract pointcut p1.A.anotherPC() is not made concrete in Concrete", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(jarCreatingProject,"inherited abstract pointcut p1.A.anotherPC() is not made concrete in Concrete")); //$NON-NLS-1$
		assertTrue("jarDependentProject should have a build error: inherited abstract pointcut p1.A.anotherPC() is not made concrete in Concrete", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(jarDependentProject,"inherited abstract pointcut p1.A.anotherPC() is not made concrete in Concrete")); //$NON-NLS-1$

		InputStream contentsOfA2 = A.getContents();	
		StringBuffer sb2 = new StringBuffer();		
		BufferedReader reader2 = new BufferedReader(new InputStreamReader(contentsOfA2));		
		String line2 = reader2.readLine();
		while (line2 != null) {
			// comment out offending line
			if (line2.indexOf("public abstract pointcut anotherPC();") != -1) { //$NON-NLS-1$
				sb2.append("// public abstract pointcut anotherPC();"); //$NON-NLS-1$
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

		assertFalse("jarCreatingProject should build with no errors", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(jarCreatingProject,null));
		assertFalse("jarDependentProject should have a build error: inherited abstract pointcut p1.A.anotherPC() is not made concrete in Concrete", //$NON-NLS-1$
				ProjectDependenciesUtils.projectIsMarkedWithError(jarDependentProject,"inherited abstract pointcut p1.A.anotherPC() is not made concrete in Concrete")); //$NON-NLS-1$

	}
	
	/**
	 * There is JDT bug 84214 - sometimes project dependencies don't
	 * get picked up properly. Therefore, to work around this, if
	 * remove, then re-add the project dependency.
	 */
	private void checkForJDTBug84214(IProject projectWhichShouldHaveDependency, IProject projectDependedOn) {
		if (projectDependedOn.getReferencingProjects().length == 0) {
			IJavaProject jp = JavaCore.create(projectWhichShouldHaveDependency);
			ProjectDependenciesUtils.removeProjectDependency(jp,projectDependedOn);
			ProjectDependenciesUtils.addProjectDependency(jp,projectDependedOn);
			waitForJobsToComplete();
			waitForJobsToComplete();
		}
		assertEquals(" " + projectDependedOn  + " should have "  //$NON-NLS-1$ //$NON-NLS-2$
				+ projectWhichShouldHaveDependency 
				+ " as it's list of referencing projects - if not, see JDT bug 84214", //$NON-NLS-1$
				1, projectDependedOn.getReferencingProjects().length);
	}
	
}

