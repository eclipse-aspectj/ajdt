/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.builder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class AJBuilderTest2 extends AJDTCoreTestCase {
    @Override
    protected void setUp() throws Exception {
        AspectJPlugin.getDefault().setHeadless(true);
        super.setUp();
    }

	protected void tearDown() throws Exception {
		super.tearDown();
		AspectJPlugin.getDefault().setAJLogger(null);
	}

	/**
	 * Part of Bug 91420 - if the output folder has changed then need to do a
	 * build
	 */
	public void testChangeInOutputDirCausesReBuild() throws Exception {
		IProject project = createPredefinedProject("bug91420"); //$NON-NLS-1$
		IJavaProject javaProject = JavaCore.create(project);
		IPath origOutput = javaProject.getOutputLocation();

		IPath newOutput = new Path("/bug91420/newBin"); //$NON-NLS-1$
		assertFalse("should be setting output dir to new place", //$NON-NLS-1$
				origOutput.toString().equals(newOutput.toString()));

		TestLogger testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		javaProject.setOutputLocation(newOutput, null);
		
		joinBackgroudActivities();
		
		assertNotSame(
				"should have set output directory to new place", origOutput, newOutput); //$NON-NLS-1$

		List log = testLog.getMostRecentEntries(3);
		// print log to the screen for test case development purposes
		testLog.printLog();

		assertTrue(
				"output dir has changed so should have spent time in AJDE", testLog.containsMessage("Total time spent in AJDE")); //$NON-NLS-1$ //$NON-NLS-2$  
		assertTrue(
				"output dir has changed so should have spent time in AJBuilder.build()", testLog.containsMessage("Total time spent in AJBuilder.build()")); //$NON-NLS-1$ //$NON-NLS-2$

		// reset the output dir back to its original setting
		javaProject.setOutputLocation(origOutput, null);
		waitForAutoBuild();
		assertEquals(
				"should have reset the output directory", origOutput.toString(), javaProject.getOutputLocation().toString()); //$NON-NLS-1$
	}

	/**
	 * Part of Bug 91420 - if a library has been added to the classpath then we
	 * need to do a rebuild.
	 */
	public void testChangeInRequiredLibsCausesReBuild() throws Exception {
		IProject project = createPredefinedProject("bug91420"); //$NON-NLS-1$
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] origClasspath = javaProject.getRawClasspath();

		
		TestLogger testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		// add a library to the classpath
		addLibraryToClasspath(project, "testJar.jar"); //$NON-NLS-1$
		joinBackgroudActivities();

		// check it's been added to the classpath
		assertTrue(
				"library should have been added to classpath", projectHasLibraryOnClasspath(javaProject, "testJar.jar")); //$NON-NLS-1$ //$NON-NLS-2$

		// check that a build has in fact occured
		List log = testLog.getMostRecentEntries(3);
		// print log to the screen for test case development purposes
		testLog.printLog();

		assertTrue(
				"classpath has changed (new required library) so should have spent time in AJDE", testLog.containsMessage("Total time spent in AJDE")); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(
				"classpath has changed (new required library) so should have spent time in AJBuilder.build()", testLog.containsMessage("Total time spent in AJBuilder.build()")); //$NON-NLS-1$ //$NON-NLS-2$

		// reset the changes
		javaProject.setRawClasspath(origClasspath, null);
		joinBackgroudActivities();

		assertFalse(
				"library should no longer be on the classpath", projectHasLibraryOnClasspath(javaProject, "testJar.jar")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Part of Bug 91420 - if there has been a change in required projects then
	 * need to to a rebuild
	 */
	public void testChangeInRequiredProjectsCausesReBuild() throws Exception {
		IProject project = createPredefinedProject("bug91420"); //$NON-NLS-1$
		IProject project2 = createPredefinedProject("bug101481"); //$NON-NLS-1$
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] origClasspath = javaProject.getRawClasspath();

		TestLogger testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		addProjectDependency(project, project2);
		joinBackgroudActivities();

		// check the dependency is there
		assertTrue(
				"project bug91420 should have a project dependency on project bug101481", //$NON-NLS-1$
				projectHasProjectDependency(javaProject, project2));

		List log = testLog.getMostRecentEntries(3);
		// print log to the screen for test case development purposes
		testLog.printLog();

		assertTrue(
				"classpath has changed (new project dependency) so should have spent time in AJDE", testLog.containsMessage("Total time spent in AJDE")); //$NON-NLS-1$ //$NON-NLS-2$  
		assertTrue(
				"classpath has changed (new project dependency) so should have spent time in AJBuilder.build()", testLog.containsMessage("Total time spent in AJBuilder.build()")); //$NON-NLS-1$ //$NON-NLS-2$ 

		// reset the changes
		javaProject.setRawClasspath(origClasspath, null);
        joinBackgroudActivities();
		assertFalse("project dependencies should have been removed", //$NON-NLS-1$
				projectHasProjectDependency(javaProject, project2));
	}
	
	   /**
     * Test for bug 101489 - if there are compilation errors in the compiled
     * class files then the resources (.txt files etc.) are not copied over to
     * the output directory. This is a problem when a full build happens.
     * 
     * NOTE - this isn't actually fixed yet......
     */
    public void testBug101489() throws Exception {
        TestLogger testLog = new TestLogger();
        AspectJPlugin.getDefault().setAJLogger(testLog);
        IProject project = createPredefinedProject("bug101481"); //$NON-NLS-1$
        try {
            // test setup
            assertTrue("autobuilding should be true",Utils.isAutobuilding()); //$NON-NLS-1$
            IFolder src = project.getFolder("src"); //$NON-NLS-1$
            if (!src.exists()) {
                src.create(true, true, null);
            }
            IFolder pack = src.getFolder("pack"); //$NON-NLS-1$
            if (!pack.exists()) {
                pack.create(true, true, null);
            }
            IFile textFile = null;

            IFile f = pack.getFile("newFile.txt"); //$NON-NLS-1$
            if (!f.exists()) {
                f.create(new ByteArrayInputStream(new byte[0]), true, null);
            }
            textFile = pack.getFile("newFile.txt"); //$NON-NLS-1$
            project.refreshLocal(IResource.DEPTH_INFINITE,null);
            project.refreshLocal(IResource.DEPTH_INFINITE,null);
            joinBackgroudActivities();
            
            assertNotNull("src folder should not be null", src); //$NON-NLS-1$
            assertNotNull("package pack should not be null", pack); //$NON-NLS-1$
            assertNotNull("newFile.txt should not be null", textFile); //$NON-NLS-1$
            assertTrue("newFile.txt should exist", textFile.exists()); //$NON-NLS-1$

            IFile c = pack.getFile("C.java"); //$NON-NLS-1$
            assertNotNull("file C.java should not be null", c); //$NON-NLS-1$
            assertTrue("C.java should exist", c.exists()); //$NON-NLS-1$

            IFolder bin = project.getFolder("bin"); //$NON-NLS-1$
            if (!bin.exists()) {
                bin.create(true, true, null);
            }
            IFolder binPack = bin.getFolder("pack"); //$NON-NLS-1$
            if (!binPack.exists()) {
                binPack.create(true, true, null);
            }
            IFile binTextFile = binPack.getFile("newFile.txt"); //$NON-NLS-1$
            
            assertTrue("bin directory should contain txt file", //$NON-NLS-1$
                    outputDirContainsFile(project, "pack", "newFile.txt")); //$NON-NLS-1$ //$NON-NLS-2$
            assertTrue("newFile.txt should exist in the output directory", //$NON-NLS-1$
                    binTextFile.exists());
            
            // Test starts here.....
            
            // turn autobuilding off
            Utils.setAutobuilding(false);
            assertFalse("autobuilding should be set to false", Utils //$NON-NLS-1$
                    .isAutobuilding());
            assertFalse("project should have no errors", testLog //$NON-NLS-1$
                    .containsMessage("error"));  //$NON-NLS-1$
            
            StringBuffer origContents = new StringBuffer("package pack; "); //$NON-NLS-1$
            origContents.append(System.getProperty("line.separator")); //$NON-NLS-1$
            origContents.append("public class C {}"); //$NON-NLS-1$

            // add compilation error to the class
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
            waitForAutoRefresh();
            
            // force a clean build (which should clear the output
            // directory)
            project.build(IncrementalProjectBuilder.CLEAN_BUILD, null);

            // TODO - since 101489 isn't fixed, this test will fail here...
            // there should be no newFile.txt in the output directory
            binTextFile = binPack.getFile("newFile.txt"); //$NON-NLS-1$
            assertFalse("newFile.txt should not exist in output directory", //$NON-NLS-1$
                    binTextFile.exists());

            // force a full build
            project.build(IncrementalProjectBuilder.FULL_BUILD, null);

            // there should not be a class file in the output directory
            assertTrue("class file should be in output folder even though it has errors", //$NON-NLS-1$
                    outputDirContainsFile(project, "pack", "C.class")); //$NON-NLS-1$ //$NON-NLS-2$

            // testing the refresh output dir after a clean (without doing
            // a build) part of bug 101481
            binTextFile = binPack.getFile("newFile.txt"); //$NON-NLS-1$
            assertTrue("newFile.txt should exist in output directory", binTextFile //$NON-NLS-1$
                    .exists());

        } finally {
            AspectJPlugin.getDefault().setAJLogger(null);
            Utils.setAutobuilding(true);
        }
    }

    private boolean outputDirContainsFile(IProject project, String packageName,
            String fileName) throws JavaModelException {
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

        File outputDir = new File(realOutputLocation + File.separator
                + packageName);
        File[] outputFiles = outputDir.listFiles();
        for (int i = 0; i < outputFiles.length; i++) {
            if (outputFiles[i].getName().equals(fileName)) {
                return true;
            }
        }
        return false;
    }


	private void addLibraryToClasspath(IProject project, String libName)
			throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);

		IClasspathEntry[] originalCP = javaProject.getRawClasspath();
		IClasspathEntry ajrtLIB = JavaCore.newLibraryEntry(project
				.getLocation().append(libName).makeAbsolute(), // library
																// location
				null, // no source
				null // no source
				);
		int originalCPLength = originalCP.length;
		IClasspathEntry[] newCP = new IClasspathEntry[originalCPLength + 1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCPLength);
		newCP[originalCPLength] = ajrtLIB;
		javaProject.setRawClasspath(newCP, null);
	}

	private boolean projectHasLibraryOnClasspath(IJavaProject proj,
			String libName) throws JavaModelException {
		IClasspathEntry[] entries = proj.getRawClasspath();
		IPath libPath = proj.getProject().getLocation().append(libName);
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				if (entry.getPath().equals(libPath)
						|| entry.getPath().equals(libPath.makeAbsolute())) {
					return true;
				}
			}
		}
		return false;
	}

	private void addProjectDependency(IProject project,
			IProject projectDependedOn) throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] originalCP = javaProject.getRawClasspath();
		IClasspathEntry newEntry = JavaCore.newProjectEntry(projectDependedOn
				.getFullPath());
		int originalCPLength = originalCP.length;
		IClasspathEntry[] newCP = new IClasspathEntry[originalCPLength + 1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCPLength);
		newCP[originalCPLength] = newEntry;
		javaProject.setRawClasspath(newCP, null);
	}

	private boolean projectHasProjectDependency(IJavaProject proj,
			IProject projectDependedOn) throws JavaModelException {
		IClasspathEntry[] entries = proj.getRawClasspath();
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				if (entry.getPath().equals(projectDependedOn.getFullPath())
						|| entry.getPath().equals(
								projectDependedOn.getFullPath().makeAbsolute())) {
					return true;
				}
			}
		}
		return false;
	}

}
