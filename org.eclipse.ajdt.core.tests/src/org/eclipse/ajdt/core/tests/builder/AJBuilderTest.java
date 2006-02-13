/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.builder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.testutils.ReaderInputStream;
import org.eclipse.ajdt.core.tests.testutils.TestLogger;
import org.eclipse.ajdt.core.tests.testutils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class AJBuilderTest extends AJDTCoreTestCase {

	/**
	 * Test for bug 101481 - "clean" build doesn't work and refreshing of output
	 * directory
	 * 
	 * @throws Exception
	 */
	public void testCleanBuild() throws Exception {
		TestLogger testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		IProject project = createPredefinedProject("bug101481"); //$NON-NLS-1$
		try {
			Utils.setAutobuilding(false);

			assertFalse("autobuilding should be set to false", Utils //$NON-NLS-1$
					.isAutobuilding());
			assertFalse("project should have no errors", testLog //$NON-NLS-1$
					.containsMessage("error")); //$NON-NLS-1$

			IFolder src = project.getFolder("src"); //$NON-NLS-1$
			if (!src.exists()) {
				src.create(true, true, null);
			}
			IFolder pack = src.getFolder("pack"); //$NON-NLS-1$
			if (!pack.exists()) {
				pack.create(true, true, null);
			}
			IFile c = pack.getFile("C.java"); //$NON-NLS-1$
			assertNotNull("src folder should not be null", src); //$NON-NLS-1$
			assertNotNull("package pack should not be null", pack); //$NON-NLS-1$
			assertNotNull("class c should not be null", c); //$NON-NLS-1$
			assertTrue("java file should exist", c.exists()); //$NON-NLS-1$

			IFolder bin = project.getFolder("bin"); //$NON-NLS-1$
			if (!bin.exists()) {
				bin.create(true, true, null);
			}
			IFolder binPack = bin.getFolder("pack"); //$NON-NLS-1$
			if (!binPack.exists()) {
				binPack.create(true, true, null);
			}
			IFile binC = binPack.getFile("C.class"); //$NON-NLS-1$

			assertTrue("bin directory should contain class file", //$NON-NLS-1$
					outputDirContainsFile(project, "pack", "C.class")); //$NON-NLS-1$ //$NON-NLS-2$
			// testing the refresh output directory part of bug 101481
			assertTrue("class file should exist", binC.exists()); //$NON-NLS-1$

			StringBuffer origContents = new StringBuffer("package pack; "); //$NON-NLS-1$
			origContents.append(System.getProperty("line.separator")); //$NON-NLS-1$
			origContents.append("public class C {}"); //$NON-NLS-1$

			// write "blah blah blah" to the class
			// NOTE: we add a comment so that thet class file doesn't get
			// deleted, as we test for it later, but this is a somewhat
			// arbitrary test because the behaviour of AJC is different to the
			// JDT compiler when the source has errors (see bug 102733)
			StringBuffer sb = new StringBuffer("blah blah blah/*comment*/"); //$NON-NLS-1$
			sb.append(origContents);
			StringReader sr = new StringReader(sb.toString());
			c.setContents(new ReaderInputStream(sr), IResource.FORCE, null);
			sr.close();
						
			project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			assertTrue("project should have errors", testLog //$NON-NLS-1$
					.containsMessage("error at blah blah blah")); //$NON-NLS-1$
			assertTrue("bin directory should contain class file", //$NON-NLS-1$
					outputDirContainsFile(project, "pack", "C.class")); //$NON-NLS-1$ //$NON-NLS-2$
			assertFalse(
					"should not have cleaned the output folder", //$NON-NLS-1$
					testLog
							.containsMessage("Cleared AJDT relationship map for project bug101481")); //$NON-NLS-1$
			int n = testLog
					.numberOfEntriesForMessage("Builder: Tidied output folder, deleted 1 .class files from"); //$NON-NLS-1$

			binC = binPack.getFile("C.class"); //$NON-NLS-1$
			assertTrue("class file should exist", binC.exists()); //$NON-NLS-1$

			project.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
			// testing the same steps are taken during a clean as they
			// are in the javaBuilder part of bug 101481
			assertTrue(
					"should have deleted 1 class file from the output dir", //$NON-NLS-1$
					testLog
							.containsMessage("Builder: Tidied output folder, deleted 1 .class files from")); //$NON-NLS-1$
			assertTrue(
					"should have removed problems and tasks for the project", //$NON-NLS-1$
					testLog
							.containsMessage("Removed problems and tasks for project")); //$NON-NLS-1$
			assertEquals(
					"should have cleaned output folder " + (n + 1) + "times", //$NON-NLS-1$ //$NON-NLS-2$
					n + 1,
					testLog
							.numberOfEntriesForMessage("Builder: Tidied output folder, deleted 1 .class files from")); //$NON-NLS-1$
			assertFalse("bin directory should not contain class file", //$NON-NLS-1$
					outputDirContainsFile(project, "pack", "C.class")); //$NON-NLS-1$ //$NON-NLS-2$

			// testing the refresh output dir after a clean (without doing
			// a build) part of bug 101481
			binC = binPack.getFile("C.class"); //$NON-NLS-1$
			assertFalse("class file should not exist", binC.exists()); //$NON-NLS-1$

		} finally {
			AspectJPlugin.getDefault().setAJLogger(null);
			Utils.setAutobuilding(true);
			deleteProject(project);
		}
	}

	private boolean outputDirContainsFile(IProject project, String packageName, String fileName)
			throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);
		IPath workspaceRelativeOutputPath = javaProject.getOutputLocation();

		String realOutputLocation = null;
		if (workspaceRelativeOutputPath.segmentCount() == 1) { // project
			// root
			realOutputLocation = javaProject.getResource().getLocation()
					.toOSString();
		} else {
			IFolder out = ResourcesPlugin.getWorkspace().getRoot().getFolder(
					workspaceRelativeOutputPath);
			realOutputLocation = out.getLocation().toOSString();
		}

		File outputDir = new File(realOutputLocation + File.separator + packageName);
		File[] outputFiles = outputDir.listFiles();
		for (int i = 0; i < outputFiles.length; i++) {
			if (outputFiles[i].getName().equals(fileName)) {
				return true;
			}
		}
		return false;
	}

	public void testIncrementalBuildWithSrcFolder() throws Exception {
		TestLogger testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		IProject project = createPredefinedProject("TJP Example"); //$NON-NLS-1$
		try {
			assertFalse("project should have no errors", testLog //$NON-NLS-1$
					.containsMessage("error")); //$NON-NLS-1$
			
			IFolder src = project.getFolder("src"); //$NON-NLS-1$
			if (!src.exists()) {
				src.create(true, true, null);
			}
			IFolder pack = src.getFolder("tjp"); //$NON-NLS-1$
			if (!pack.exists()) {
				pack.create(true, true, null);
			}
			IFile c = pack.getFile("Demo.java"); //$NON-NLS-1$
			assertNotNull("src folder should not be null", src); //$NON-NLS-1$
			assertNotNull("package tjp should not be null", pack); //$NON-NLS-1$
			assertNotNull("class Demo should not be null", c); //$NON-NLS-1$
			assertTrue("java file should exist", c.exists()); //$NON-NLS-1$

			IFolder bin = project.getFolder("bin"); //$NON-NLS-1$
			if (!bin.exists()) {
				bin.create(true, true, null);
			}
			IFolder binPack = bin.getFolder("tjp"); //$NON-NLS-1$
			if (!binPack.exists()) {
				binPack.create(true, true, null);
			}
			IFile binC = binPack.getFile("Demo.class"); //$NON-NLS-1$
			assertTrue("class file should exist", binC.exists()); //$NON-NLS-1$
						
			String rep = testLog.getMostRecentMatchingMessage("AspectJ reports build successful"); //$NON-NLS-1$
			System.out.println("rep: "+rep); //$NON-NLS-1$
			
			// add a comment to the class
			StringReader sr = new StringReader("/* blah blah blah */"); //$NON-NLS-1$
			c.appendContents(new ReaderInputStream(sr), IResource.FORCE, null);

			waitForAutoBuild();
			
			assertTrue("Successful build should have occurred", testLog //$NON-NLS-1$
					.containsMessage("AspectJ reports build successful")); //$NON-NLS-1$
			
			rep = testLog.getMostRecentMatchingMessage("AspectJ reports build successful"); //$NON-NLS-1$
			assertNotNull("Successful build should have been reported",rep); //$NON-NLS-1$
			
			assertTrue("The build should have been an incremental one",wasIncrementalBuild(rep)); //$NON-NLS-1$
		} finally {
			AspectJPlugin.getDefault().setAJLogger(null);
			deleteProject(project);
		}
	}
	
	public void testIncrementalBuildWithoutSrcFolder() throws Exception {
		TestLogger testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		IProject project = createPredefinedProject("bug102652"); //$NON-NLS-1$
		try {
			assertFalse("project should have no errors", testLog //$NON-NLS-1$
					.containsMessage("error")); //$NON-NLS-1$
			
			IFolder pack = project.getFolder("tjp"); //$NON-NLS-1$
			if (!pack.exists()) {
				pack.create(true, true, null);
			}
			IFile c = pack.getFile("Demo.java"); //$NON-NLS-1$
			assertNotNull("package tjp should not be null", pack); //$NON-NLS-1$
			assertNotNull("class Demo should not be null", c); //$NON-NLS-1$
			assertTrue("java file should exist", c.exists()); //$NON-NLS-1$

			IFile binC = pack.getFile("Demo.class"); //$NON-NLS-1$
			assertTrue("class file should exist", binC.exists()); //$NON-NLS-1$
						
			// add a comment to the class
			StringReader sr = new StringReader("/* blah blah blah */"); //$NON-NLS-1$
			c.appendContents(new ReaderInputStream(sr), IResource.FORCE, null);

			waitForAutoBuild();
			
			assertTrue("Successful build should have occurred", testLog //$NON-NLS-1$
					.containsMessage("AspectJ reports build successful")); //$NON-NLS-1$
			
			String rep = testLog.getMostRecentMatchingMessage("AspectJ reports build successful"); //$NON-NLS-1$
			assertNotNull("Successful build should have been reported",rep); //$NON-NLS-1$
			assertTrue("The build should have been an incremental one",wasIncrementalBuild(rep)); //$NON-NLS-1$
		} finally {
			AspectJPlugin.getDefault().setAJLogger(null);
			deleteProject(project);
		}
	}
	
	/**
	 * Bug 74174 
	 * changing a txt file inside a source folder triggers a build 
	 * (either full or incremental) when it doesn't need to
	 */
	public void testBug74174() throws Exception {
		TestLogger testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		IProject project = createPredefinedProject("bug99133b"); //$NON-NLS-1$
		try {
			assertFalse("project should have no errors", testLog //$NON-NLS-1$
					.containsMessage("error")); //$NON-NLS-1$
			IFile f = project.getFile("src/p/anotherTest.txt"); //$NON-NLS-1$
			assertNotNull("file test.txt should not be null", f); //$NON-NLS-1$
			
			if (!f.exists()) {
				f.create(new ByteArrayInputStream(new byte[0]), true, null);
			}
			waitForAutoBuild();
			waitForAutoBuild();
			assertTrue("text file should exist", f.exists()); //$NON-NLS-1$
			
			IFile binF = project.getFile("bin/p/anotherTest.txt"); //$NON-NLS-1$
			assertNotNull("file test.txt should not be null", binF); //$NON-NLS-1$
			assertTrue("text file should exist", binF.exists()); //$NON-NLS-1$
						
			int numberOfBuildsRun = testLog.getNumberOfBuildsRun();
			
			// add text to the file
			StringReader sr = new StringReader("more blah blah blah"); //$NON-NLS-1$
			f.appendContents(new ReaderInputStream(sr), IResource.FORCE, null);
			
			waitForAutoBuild();
			waitForAutoBuild();
			waitForAutoBuild();
			
			// check that we have gone through the AJBuilder.build(..) method
			// and that there are no errors reported
			assertEquals("The number of builds should be " + (numberOfBuildsRun + 1),numberOfBuildsRun + 1,testLog.getNumberOfBuildsRun()); //$NON-NLS-1$
			List buildLog = testLog.getPreviousBuildEntry(1);
			
			// This is the message AJDT put's out when it decides not
			// to do a build. It thinks there are no src changes in the 
			// current project. 
			// NOTE: this will fail if we decide that AJDT can't make these
			// sorts of decisions and pass everything down to the compiler
			// (who can).
			assertTrue("AJDT should have found no source file changes and decided not to build", //$NON-NLS-1$
					listContainsString(buildLog,"build: Examined delta - no " + //$NON-NLS-1$
							"source file changes for project bug99133b")); //$NON-NLS-1$
			assertFalse("There should be no errors in the build log", //$NON-NLS-1$
					listContainsString(buildLog,"error")); //$NON-NLS-1$
			
			// by checking that we don't have the following messages in the
			// log (and the previous checking for no errors) we know that
			// AJDT has exited the build method before calling the compiler.
			//NOTE: these will fail if we decide that AJDT can't make these
			// sorts of decisions and pass everything down to the compiler
			// (who can).
			boolean inc = listContainsString(buildLog,
					"AspectJ reports build successful, build was: INCREMENTAL"); //$NON-NLS-1$
			assertFalse("AJDT should have returned from the build without " + //$NON-NLS-1$
					"going through the compiler, therefore AspectJ shouldn't " + //$NON-NLS-1$
					"report that an incremental build happened",inc); //$NON-NLS-1$
			boolean full = listContainsString(buildLog,
					"AspectJ reports build successful, build was: FULL"); //$NON-NLS-1$
			assertFalse("AJDT should have returned from the build without " + //$NON-NLS-1$
					"going through the compiler, therefore AspectJ shouldn't " + //$NON-NLS-1$
					"report that a full build happened",full); //$NON-NLS-1$
			
			BufferedReader br1 = new BufferedReader(new InputStreamReader(binF
					.getContents()));
			
			String line1 = br1.readLine();
			assertEquals("file in bin directory should contain \"more blah blah blah\"", //$NON-NLS-1$
					"more blah blah blah", line1); //$NON-NLS-1$
			br1.close();
			
		} finally {
			AspectJPlugin.getDefault().setAJLogger(null);
			deleteProject(project);
		}
	}
	
	/**
	 * Bug 98215 - regression of bug 74174
	 * changing a txt file outside of a source file triggers a build 
	 * (either full or incremental) when it doesn't need to
	 */
	public void testBug98125() throws Exception {
		TestLogger testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		IProject project = createPredefinedProject("bug99133b"); //$NON-NLS-1$
		try {
			assertFalse("project should have no errors", testLog //$NON-NLS-1$
					.containsMessage("error")); //$NON-NLS-1$
			IFile f = project.getFile("test.txt"); //$NON-NLS-1$
			assertNotNull("file test.txt should not be null", f); //$NON-NLS-1$
			assertTrue("text file should exist", f.exists()); //$NON-NLS-1$
			
			int numberOfBuildsRun = testLog.getNumberOfBuildsRun();
			
			// add more text to the file
			StringReader sr = new StringReader("more blah blah blah"); //$NON-NLS-1$
			f.appendContents(new ReaderInputStream(sr), IResource.FORCE, null);
			
			waitForAutoBuild();
			waitForAutoBuild();
			waitForAutoBuild();
			
			// check that we have gone through the AJBuilder.build(..) method
			// and that there are no errors reported
			assertEquals("The number of builds should be " + (numberOfBuildsRun + 1),numberOfBuildsRun + 1,testLog.getNumberOfBuildsRun()); //$NON-NLS-1$
			List buildLog = testLog.getPreviousBuildEntry(1);
			
			// This is the message AJDT put's out when it decides not
			// to do a build. It thinks there are no src changes in the 
			// current project. 
			// NOTE: this will fail if we decide that AJDT can't make these
			// sorts of decisions and pass everything down to the compiler
			// (who can).
			assertTrue("AJDT should have found no source file changes and decided not to build", //$NON-NLS-1$
					listContainsString(buildLog,"build: Examined delta - no " + //$NON-NLS-1$
							"source file changes for project bug99133b")); //$NON-NLS-1$
			assertFalse("There should be no errors in the build log", //$NON-NLS-1$
					listContainsString(buildLog,"error")); //$NON-NLS-1$
			
			// by checking that we don't have the following messages in the
			// log (and the previous checking for no errors) we know that
			// AJDT has exited the build method before calling the compiler.
			//NOTE: these will fail if we decide that AJDT can't make these
			// sorts of decisions and pass everything down to the compiler
			// (who can).
			boolean inc = listContainsString(buildLog,
					"AspectJ reports build successful, build was: INCREMENTAL"); //$NON-NLS-1$
			assertFalse("AJDT should have returned from the build without " + //$NON-NLS-1$
					"going through the compiler, therefore AspectJ shouldn't " + //$NON-NLS-1$
					"report that an incremental build happened",inc); //$NON-NLS-1$
			boolean full = listContainsString(buildLog,
					"AspectJ reports build successful, build was: FULL"); //$NON-NLS-1$
			assertFalse("AJDT should have returned from the build without " + //$NON-NLS-1$
					"going through the compiler, therefore AspectJ shouldn't " + //$NON-NLS-1$
					"report that a full build happened",full); //$NON-NLS-1$
			
		} finally {
			AspectJPlugin.getDefault().setAJLogger(null);
			deleteProject(project);
		}
	}
	
	/**
	 * Differs from testBug98215 in that it has two depedent
	 * projects A depending on B and the text file which changes
	 * is in B. In this case, AJDT should return for project B
	 * that there are no src folder changes and it doesn't do
	 * a build. However, it should do an incremental build of
	 * project A. (both A and B are aspectj projects)
	 */
	public void testBug98125WithDependingProjects() throws Exception {
		TestLogger testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		IProject pB = createPredefinedProject("bug99133b"); //$NON-NLS-1$
		waitForAutoBuild();
		waitForAutoBuild();
		IProject pA = createPredefinedProject("bug99133a"); //$NON-NLS-1$
		waitForAutoBuild();
		waitForAutoBuild();
		try {
			assertFalse("project should have no errors", testLog //$NON-NLS-1$
					.containsMessage("error")); //$NON-NLS-1$
			IFile f = pB.getFile("test.txt"); //$NON-NLS-1$
			assertNotNull("file test.txt should not be null", f); //$NON-NLS-1$
			assertTrue("text file should exist", f.exists()); //$NON-NLS-1$
			
			int numberOfBuildsRun = testLog.getNumberOfBuildsRun();

			// add more text to the file
			StringReader sr = new StringReader("more blah blah blah"); //$NON-NLS-1$
			f.appendContents(new ReaderInputStream(sr), IResource.FORCE, null);
			
			waitForAutoBuild();
			waitForAutoBuild();
			waitForAutoBuild();
			waitForAutoBuild();
			waitForAutoBuild();
			waitForAutoBuild();
			waitForAutoBuild();
			waitForAutoBuild();
					
			// check that we have gone through the AJBuilder.build(..) method
			// and that there are no errors reported
			assertEquals("The number of builds should be " + (numberOfBuildsRun + 2),numberOfBuildsRun + 2,testLog.getNumberOfBuildsRun()); //$NON-NLS-1$
			assertFalse("There should be no errors in the build log", //$NON-NLS-1$
					testLog.containsMessage("error")); //$NON-NLS-1$
			
			
			List buildLogB = testLog.getPreviousBuildEntry(2);
			assertTrue("Should have tried to build project bug99133b", //$NON-NLS-1$
					listContainsString(buildLogB,"bug99133b")); //$NON-NLS-1$

			
			// This is the message AJDT put's out when it decides not
			// to do a build. It thinks there are no src changes in the 
			// current project. 
			// NOTE: this will fail if we decide that AJDT can't make these
			// sorts of decisions and pass everything down to the compiler
			// (who can).
			assertTrue("AJDT should have found no source file changes and decided not to build", //$NON-NLS-1$
					listContainsString(buildLogB,"build: Examined delta - no " + //$NON-NLS-1$
							"source file changes for project bug99133b")); //$NON-NLS-1$
			
			
			// by checking that we don't have the following messages in the
			// log (and the previous checking for no errors) we know that
			// AJDT has exited the build method before calling the compiler.
			//NOTE: these will fail if we decide that AJDT can't make these
			// sorts of decisions and pass everything down to the compiler
			// (who can).
			boolean inc = listContainsString(buildLogB,
					"AspectJ reports build successful, build was: INCREMENTAL"); //$NON-NLS-1$
			assertFalse("AJDT should have returned from the build without " + //$NON-NLS-1$
					"going through the compiler, therefore AspectJ shouldn't " + //$NON-NLS-1$
					"report that an incremental build happened",inc); //$NON-NLS-1$
			boolean full = listContainsString(buildLogB,
					"AspectJ reports build successful, build was: FULL"); //$NON-NLS-1$
			assertFalse("AJDT should have returned from the build without " + //$NON-NLS-1$
					"going through the compiler, therefore AspectJ shouldn't " + //$NON-NLS-1$
					"report that a full build happened",full); //$NON-NLS-1$
			
			
			
			List buildLogA = testLog.getPreviousBuildEntry(1);
			assertTrue("Should have caused a build of project bug99133a", //$NON-NLS-1$
					listContainsString(buildLogA,"bug99133a")); //$NON-NLS-1$

			boolean incA = listContainsString(buildLogA,
			"AspectJ reports build successful, build was: INCREMENTAL"); //$NON-NLS-1$
			assertTrue(
					"AJDT should have called the compiler, and therefore " //$NON-NLS-1$
							+ "AspectJ should have reported that an " //$NON-NLS-1$
							+ "incremental build happened", incA); //$NON-NLS-1$
			boolean fullA = listContainsString(buildLogA,
					"AspectJ reports build successful, build was: FULL"); //$NON-NLS-1$
			assertFalse(
					"AspectJ should have reported an incremental build " //$NON-NLS-1$
							+ "occurred, not a FULL build", fullA); //$NON-NLS-1$			
		} finally {
			AspectJPlugin.getDefault().setAJLogger(null);
			deleteProject(pA);
			deleteProject(pB);
		}
	}
	
	private boolean wasIncrementalBuild(String msg) {
		return msg.toLowerCase().indexOf("was: incremental") != -1; //$NON-NLS-1$
	}
	
	private boolean listContainsString(List l, String msg) {
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            String logEntry = (String) iter.next();
            if (logEntry.indexOf(msg) != -1) {
                return true;
            }
        }
        return false;
	}
}
