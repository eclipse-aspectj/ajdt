/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.refactoring.ReaderInputStream;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.ajdt.ui.tests.testutils.TestLogger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author hawkinsh
 */
public class BuilderTest extends UITestCase {

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Test for bug 78579 - creating a new file (non java/aj) in a package
	 * copies the file over to the bin directory - deleting it should
	 * remove it from the bin directory
	 * 
	 * @throws CoreException
	 */
	public void testCopyAndRemoveNewNonSrcFile() throws CoreException {
		// test setup.....
		IProject simpleProject = createPredefinedProject("AnotherSimpleAJProject"); //$NON-NLS-1$
		IJavaProject javaProject = JavaCore.create(simpleProject);		
		waitForJobsToComplete();
		String srcPath = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "src" + File.separator + "p1"  //$NON-NLS-1$ //$NON-NLS-2$
							+ File.separator + "newFile.txt"; //$NON-NLS-1$
		
		String binPath = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "bin" + File.separator + "p1"  //$NON-NLS-1$ //$NON-NLS-2$
							+ File.separator + "newFile.txt"; //$NON-NLS-1$

		assertFalse("newFile.txt should not exist under src tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("newFile.txt should not exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$


		// start of test
		IFolder src = simpleProject.getFolder("src"); //$NON-NLS-1$
		if (!src.exists()){
			src.create(true, true, null);
		}
		IFolder p1 = src.getFolder("p1"); //$NON-NLS-1$
		if (!p1.exists()){
			p1.create(true, true, null);
		}
		assertNotNull("src folder should not be null", src); //$NON-NLS-1$
		assertNotNull("package p1 should not be null", p1); //$NON-NLS-1$
		
		waitForJobsToComplete();
		
		IFile newFile = null;

		IFile f = p1.getFile("newFile.txt"); //$NON-NLS-1$
		if (!f.exists()) {
			f.create(new ByteArrayInputStream(new byte[0]), true, null);
		}
		newFile = p1.getFile("newFile.txt"); //$NON-NLS-1$

		waitForJobsToComplete();
		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();
		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();
		
		// If either of these fail, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		assertTrue("newFile.txt should exist under src tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("newFile.txt should exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$

		// check that the .java file hasn't been copied over...
		String binPathToMain = javaProject.getUnderlyingResource().getLocation().toOSString() 
			+ File.separator + "bin" + File.separator + "p1"  //$NON-NLS-1$ //$NON-NLS-2$
			+ File.separator + "Main.java"; //$NON-NLS-1$
		assertFalse("Main.java should not exist under bin tree! (path=" + binPathToMain + ")",new File(binPathToMain).exists());		 //$NON-NLS-1$ //$NON-NLS-2$
		
		// now delete the file
		newFile.delete(true,null);
		waitForJobsToComplete();

		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();

		// If either of these fail, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		assertFalse("newFile.txt should NOT exist under src tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();
		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();
		assertFalse("newFile.txt should NOT exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Test for bug 78579 - creating a new file (non java/aj) in the default package
	 * copies the file over to the bin directory - deleting the file in the src tree
	 * should then delete it from the bin dir.
	 * 
	 * @throws CoreException
	 */
	public void testCreateAndRemoveNewNonSrcFileFromDefaultPackage() throws CoreException {
		IProject simpleProject = createPredefinedProject("AnotherSimpleAJProject"); //$NON-NLS-1$

		IJavaProject javaProject = JavaCore.create(simpleProject);
		waitForJobsToComplete();
		
		String srcPath = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "src" + File.separator + "newFile2.txt"; //$NON-NLS-1$ //$NON-NLS-2$
		
		String binPath = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "bin" + File.separator + "newFile2.txt"; //$NON-NLS-1$ //$NON-NLS-2$

		assertFalse("newFile2.txt should not exist under src tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("newFile2.txt should not exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$

		
		IFolder src = simpleProject.getFolder("src"); //$NON-NLS-1$
		if (!src.exists()){
			src.create(true, true, null);
		}
		assertNotNull("src folder should not be null", src); //$NON-NLS-1$
		
		waitForJobsToComplete();

		IFile newFile = null;
		IFile f = src.getFile("newFile2.txt"); //$NON-NLS-1$
		if (!f.exists()) {
			f.create(new ByteArrayInputStream(new byte[0]), true, null);
		}
		newFile = src.getFile("newFile2.txt"); //$NON-NLS-1$

		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();
		
		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();

		// If either of these fail, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		assertTrue("newFile2.txt should exist under src tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();
		assertTrue("newFile2.txt should exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$

		// check that the .java file hasn't been copied over...
		String binPathToMain = javaProject.getUnderlyingResource().getLocation().toOSString() 
			+ File.separator + "bin" + File.separator + "p1"  //$NON-NLS-1$ //$NON-NLS-2$
			+ File.separator + "Main.java"; //$NON-NLS-1$
		assertFalse("Main.java should not exist under bin tree! (path=" + binPathToMain + ")",new File(binPathToMain).exists()); //$NON-NLS-1$ //$NON-NLS-2$

		
		// now delete the file for cleanup
		newFile.delete(true,null);
		waitForJobsToComplete();
		
		// If either of these fail, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		assertFalse("newFile2.txt should NOT exist under src tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();
		assertFalse("newFile2.txt should NOT exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Test for bug 78579 - creating a new file (non java/aj) in a package
	 * copies the file over to the bin directory - when there are multiple
	 * src dirs. Deleting this file should then remove it from the bin dir.
	 * 
	 * @throws CoreException
	 */
	public void testCopyAndRemoveNewNonSrcFileWithMultipleSrcDirs() throws CoreException {
		IProject simpleProject = createPredefinedProject("MultipleSourceFolders"); //$NON-NLS-1$
		IJavaProject javaProject = JavaCore.create(simpleProject);
		waitForJobsToComplete();
		
		String srcPath = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "src2" + File.separator + "pack"  //$NON-NLS-1$ //$NON-NLS-2$
							+ File.separator + "newFile.txt"; //$NON-NLS-1$
		
		String binPath = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "bin" + File.separator + "pack"  //$NON-NLS-1$ //$NON-NLS-2$
							+ File.separator + "newFile.txt"; //$NON-NLS-1$
		assertFalse("newFile.txt should not exist under src tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("newFile.txt should not exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$

		IFolder src2 = simpleProject.getFolder("src2"); //$NON-NLS-1$
		if (!src2.exists()){
			src2.create(true, true, null);
		}
		IFolder pack = src2.getFolder("pack"); //$NON-NLS-1$
		if (!pack.exists()){
			pack.create(true, true, null);
		}
		assertNotNull("src2 folder should not be null", src2); //$NON-NLS-1$
		assertNotNull("package pack should not be null", pack); //$NON-NLS-1$
		
		waitForJobsToComplete();

		IFile newFile = null;
		IFile f = pack.getFile("newFile.txt"); //$NON-NLS-1$
		if (!f.exists()) {
			f.create(new ByteArrayInputStream(new byte[0]), true, null);
		}
		newFile = pack.getFile("newFile.txt"); //$NON-NLS-1$

		waitForJobsToComplete();
		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();

		// If either of these fail, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		assertTrue("newFile.txt should exist under src2 tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();
		assertTrue("newFile.txt should exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$

		// check that the .java file hasn't been copied over...
		String binPathToMain = javaProject.getUnderlyingResource().getLocation().toOSString() 
			+ File.separator + "bin" + File.separator + "pack"  //$NON-NLS-1$ //$NON-NLS-2$
			+ File.separator + "Class3.java"; //$NON-NLS-1$
		assertFalse("Class3.java should not exist under bin tree! (path=" + binPathToMain + ")",new File(binPathToMain).exists());  //$NON-NLS-1$//$NON-NLS-2$

		// now delete the file for cleanup
		newFile.delete(true,null);
		waitForJobsToComplete();

		// If either of these fail, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		assertFalse("newFile.txt should NOT exist under src tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();
		assertFalse("newFile.txt should NOT exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Test for bug 78579 - creating a new file (non java/aj) in a package
	 * copies the file over to the bin directory - when the output directory
	 * is a non standard one (ie not called "bin").
	 * 
	 * @throws CoreException
	 */
	public void testCopyAndRemoveNewNonSrcFileWithNonStandardOutputDir() throws CoreException {
		IProject simpleProject = createPredefinedProject("NonStandardOutputLocation"); //$NON-NLS-1$

		IJavaProject javaProject = JavaCore.create(simpleProject);
		waitForJobsToComplete();
		
		String srcPath = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "src" + File.separator + "pack"  //$NON-NLS-1$ //$NON-NLS-2$
							+ File.separator + "newFile.txt"; //$NON-NLS-1$
		
		String binPath = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "nonStandardBin" + File.separator + "pack"  //$NON-NLS-1$ //$NON-NLS-2$
							+ File.separator + "newFile.txt"; //$NON-NLS-1$
		assertFalse("newFile.txt should not exist under src tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("newFile.txt should not exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$

		IFolder src = simpleProject.getFolder("src"); //$NON-NLS-1$
		if (!src.exists()){
			src.create(true, true, null);
		}
		IFolder pack = src.getFolder("pack"); //$NON-NLS-1$
		if (!pack.exists()){
			pack.create(true, true, null);
		}
		assertNotNull("src folder should not be null", src); //$NON-NLS-1$
		assertNotNull("package pack should not be null", pack); //$NON-NLS-1$
		
		waitForJobsToComplete();

		IFile newFile = null;
		IFile f = pack.getFile("newFile.txt"); //$NON-NLS-1$
		if (!f.exists()) {
			f.create(new ByteArrayInputStream(new byte[0]), true, null);
		}
		newFile = pack.getFile("newFile.txt"); //$NON-NLS-1$

		waitForJobsToComplete();
		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();
		
		// If either of these fail, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		assertTrue("newFile.txt should exist under src2 tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();
		assertTrue("newFile.txt should exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$

		// check that the .java file hasn't been copied over...
		String binPathToMain = javaProject.getUnderlyingResource().getLocation().toOSString() 
			+ File.separator + "nonStandardBin" + File.separator + "pack"  //$NON-NLS-1$ //$NON-NLS-2$
			+ File.separator + "Main.java"; //$NON-NLS-1$
		assertFalse("Main.java should not exist under bin tree! (path=" + binPathToMain + ")",new File(binPathToMain).exists()); //$NON-NLS-1$ //$NON-NLS-2$

		// now delete the file for cleanup
		newFile.delete(true,null);
		waitForJobsToComplete();

		// If either of these fail, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		assertFalse("newFile.txt should NOT exist under src tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();
		assertFalse("newFile.txt should NOT exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Test for bug 78579 - updating a nonsrc file (non java/aj) in a package
	 * copies over the updated version to the bin dir
	 * 
	 * @throws CoreException
	 */
	public void testUpdateNonSrcFile() throws CoreException, IOException {
		// create the project and the new file
		IProject simpleProject = createPredefinedProject("AnotherSimpleAJProject"); //$NON-NLS-1$

		IJavaProject javaProject = JavaCore.create(simpleProject);
		waitForJobsToComplete();
		
		String srcPath = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "src" + File.separator + "p1"  //$NON-NLS-1$ //$NON-NLS-2$
							+ File.separator + "newFile4.txt"; //$NON-NLS-1$
		
		String binPath = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "bin" + File.separator + "p1"  //$NON-NLS-1$ //$NON-NLS-2$
							+ File.separator + "newFile4.txt"; //$NON-NLS-1$
		assertFalse("newFile4.txt should not exist under src tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("newFile4.txt should not exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$

		IFolder src = simpleProject.getFolder("src"); //$NON-NLS-1$
		if (!src.exists()){
			src.create(true, true, null);
		}
		IFolder p1 = src.getFolder("p1"); //$NON-NLS-1$
		if (!p1.exists()){
			p1.create(true, true, null);
		}
		assertNotNull("src folder should not be null", src); //$NON-NLS-1$
		assertNotNull("package p1 should not be null", p1); //$NON-NLS-1$
		
		waitForJobsToComplete();

		IFile newFile = null;
		IFile f = p1.getFile("newFile4.txt"); //$NON-NLS-1$
		if (!f.exists()) {
			f.create(new ByteArrayInputStream(new byte[0]), true, null);
		}
		newFile = p1.getFile("newFile4.txt"); //$NON-NLS-1$
		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();

		// If either of these fail, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		assertTrue("newFile4.txt should exist under src tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();

		assertTrue("newFile4.txt should exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$

		// check that the .java file hasn't been copied over...
		String binPathToMain = javaProject.getUnderlyingResource().getLocation().toOSString() 
			+ File.separator + "bin" + File.separator + "p1"  //$NON-NLS-1$ //$NON-NLS-2$
			+ File.separator + "Main.java"; //$NON-NLS-1$
		assertFalse("Main.java should not exist under bin tree! (path=" + binPathToMain + ")",new File(binPathToMain).exists());		 //$NON-NLS-1$ //$NON-NLS-2$
		
		IFolder bin = simpleProject.getFolder("bin"); //$NON-NLS-1$
		if (!bin.exists()){
			bin.create(true, true, null);
		}
		IFolder binp1 = bin.getFolder("p1"); //$NON-NLS-1$
		if (!binp1.exists()){
			binp1.create(true, true, null);
		}
		IFile binNewFile = binp1.getFile("newFile4.txt");		 //$NON-NLS-1$
		
		// get the contents of the file - shouldn't be anything in there
		InputStream fileContents = newFile.getContents();	
		BufferedReader reader = new BufferedReader(new InputStreamReader(fileContents));		
		String line = reader.readLine();
		assertNull("File should not contain anything", line);		 //$NON-NLS-1$
		reader.close();
		fileContents.close();

		waitForJobsToComplete();

		// get the contents of the file under "bin" tree - should contain nothing
		InputStream fileContents4 = binNewFile.getContents();	
		BufferedReader reader4 = new BufferedReader(new InputStreamReader(fileContents4));		
		String line4 = reader4.readLine();
		assertNull("File should not contain anything", line4);		 //$NON-NLS-1$
		reader4.close();
		fileContents4.close();

		waitForJobsToComplete();
		
		// write "blah blah blah" to the file
		StringBuffer sb = new StringBuffer("blah blah blah");		 //$NON-NLS-1$
		StringReader sr = new StringReader(sb.toString());
		newFile.setContents(new ReaderInputStream(sr),IResource.FORCE, null);		
		sr.close();

		waitForJobsToComplete();

		// get the contents of the file under "src" tree - should contain "blah blah blah"
		InputStream fileContents2 = newFile.getContents();	
		BufferedReader reader2 = new BufferedReader(new InputStreamReader(fileContents2));		
		String line2 = reader2.readLine();
		assertEquals("file under src should contain 'blah blah blah'","blah blah blah",line2);		 //$NON-NLS-1$ //$NON-NLS-2$
		reader2.close();
		fileContents2.close();

		waitForJobsToComplete();

		// get the contents of the file under "bin" tree - should contain "blah blah blah"
		InputStream fileContents3 = binNewFile.getContents();	
		BufferedReader reader3 = new BufferedReader(new InputStreamReader(fileContents3));		
		String line3 = reader3.readLine();
		assertEquals("file under bin should contain 'blah blah blah'","blah blah blah",line3);		 //$NON-NLS-1$ //$NON-NLS-2$
		reader3.close();
		fileContents3.close();
		waitForJobsToComplete();
		
		// now delete the file for cleanup
		newFile.delete(true,null);
		waitForJobsToComplete();
		// If either of these fail, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		assertFalse("newFile4.txt should NOT exist under src tree! (path=" + srcPath + ")",new File(srcPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();
		assertFalse("newFile4.txt should NOT exist under bin tree! (path=" + binPath + ")",new File(binPath).exists()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Test for bug 78579 - creating a nonsrc file (non java/aj) in a 
	 * project which has the same output and src directories
	 * 
	 * @throws CoreException
	 */
	public void testCopyAndRemoveResourceWithoutSrcFolder() throws CoreException {
		IProject project = createPredefinedProject("WithoutSourceFolder"); //$NON-NLS-1$

		IJavaProject javaProject = JavaCore.create(project);
		waitForJobsToComplete();
		
		String path = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "newFile.txt"; //$NON-NLS-1$
				
		assertFalse("newFile.txt should not exist under src tree! (path=" + path + ")",new File(path).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		
		waitForJobsToComplete();

		IFile newFile = null;
		IFile f = project.getFile("newFile.txt"); //$NON-NLS-1$
		if (!f.exists()) {
			f.create(new ByteArrayInputStream(new byte[0]), true, null);
		}
		newFile = project.getFile("newFile.txt"); //$NON-NLS-1$

		project.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();

		// If this fails, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		assertTrue("newFile.txt should exist in the top dir (path=" + path + ")",new File(path).exists()); //$NON-NLS-1$ //$NON-NLS-2$
		
		newFile.delete(true,null);
		waitForJobsToComplete();
		// If this fails, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		assertFalse("newFile.txt should NOT exist in the top dir(path=" + path + ")",new File(path).exists()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	
	/**
	 * Test for bug 78579 - when you create a package, this should be copied
	 * over to the bin dir. Similarly, when you delete the package, the bin
	 * dir should be updated.
	 * 
	 * @throws Exception
	 */
	public void testCreateAndDeleteNewPackage() throws Exception {
		IProject simpleProject = createPredefinedProject("AnotherSimpleAJProject"); //$NON-NLS-1$

		IJavaProject javaProject = JavaCore.create(simpleProject);
		waitForJobsToComplete();
		
		String srcPath = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "src" + File.separator + "newPackage";  //$NON-NLS-1$ //$NON-NLS-2$
		
		String binPath = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "bin" + File.separator + "newPackage"; //$NON-NLS-1$ //$NON-NLS-2$

		IFolder src = simpleProject.getFolder("src"); //$NON-NLS-1$
		if (!src.exists()){
			src.create(true, true, null);
		}
		waitForJobsToComplete();
		
		IFolder newPackage = src.getFolder("newPackage"); //$NON-NLS-1$
		assertFalse("newPackage should not exist under src tree! (path=" + srcPath + ")",newPackage.exists()); //$NON-NLS-1$ //$NON-NLS-2$

		IFolder bin = simpleProject.getFolder("bin"); //$NON-NLS-1$
		if (!bin.exists()){
			bin.create(true, true, null);
		}
		waitForJobsToComplete();
		IFolder newBinPackage = bin.getFolder("newPackage"); //$NON-NLS-1$
		assertFalse("newPackage should not exist under bin tree! (path=" + binPath + ")",newBinPackage.exists()); //$NON-NLS-1$ //$NON-NLS-2$
		
		waitForJobsToComplete();
		
		String str= "AnotherSimpleAJProject" + File.separator + "src"; //$NON-NLS-1$ //$NON-NLS-2$
		IPath path= new Path(str); 
        IResource res= ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(res);
		root.createPackageFragment("newPackage", true, null); //$NON-NLS-1$

		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();

		// If either of these fail, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		IFolder newPackage2 = src.getFolder("newPackage"); //$NON-NLS-1$
		assertTrue("newPackage should exist under src tree! (path=" + srcPath + ")",newPackage2.exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();
		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();
		IFolder newBinPackage2 = bin.getFolder("newPackage"); //$NON-NLS-1$
		assertTrue("newPackage should exist under bin tree! (path=" + binPath + ")",newBinPackage2.exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();
		
		newPackage.delete(true,null);
		waitForJobsToComplete();

		// If either of these fail, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		IFolder newPackage3 = src.getFolder("newPackage"); //$NON-NLS-1$
		assertFalse("newPackage should not exist under src tree! (path=" + srcPath + ")",newPackage3.exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();
		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();
		IFolder newBinPackage3 = bin.getFolder("newPackage"); //$NON-NLS-1$
		assertFalse("newPackage should not exist under bin tree! (path=" + binPath + ")",newBinPackage3.exists()); //$NON-NLS-1$ //$NON-NLS-2$

		waitForJobsToComplete();
	}

	
	public void testCreateAndDeleteNewFolder() throws CoreException {
		IProject simpleProject = createPredefinedProject("AnotherSimpleAJProject"); //$NON-NLS-1$

		IJavaProject javaProject = JavaCore.create(simpleProject);
		waitForJobsToComplete();
		
		String srcPath = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "src" + File.separator + "newFolder";  //$NON-NLS-1$ //$NON-NLS-2$
		
		String binPath = javaProject.getUnderlyingResource().getLocation().toOSString() 
							+ File.separator + "bin" + File.separator + "newFolder"; //$NON-NLS-1$ //$NON-NLS-2$
		IFolder src = simpleProject.getFolder("src"); //$NON-NLS-1$
		if (!src.exists()){
			src.create(true, true, null);
		}
		waitForJobsToComplete();
		
		IFolder newFolder = src.getFolder("newFolder"); //$NON-NLS-1$
		assertFalse("newFolder should not exist under src tree! (path=" + srcPath + ")",newFolder.exists()); //$NON-NLS-1$ //$NON-NLS-2$

		IFolder bin = simpleProject.getFolder("bin"); //$NON-NLS-1$
		if (!bin.exists()){
			bin.create(true, true, null);
		}
		waitForJobsToComplete();
		IFolder newBinFolder = bin.getFolder("newFolder"); //$NON-NLS-1$
		assertFalse("newFolder should not exist under bin tree! (path=" + binPath + ")",newBinFolder.exists()); //$NON-NLS-1$ //$NON-NLS-2$
		
		waitForJobsToComplete();

		IFolder f = src.getFolder("newFolder"); //$NON-NLS-1$
		if (!f.exists()) {
			f.create(true, true, null);
		}		
		
		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		waitForJobsToComplete();

		// If either of these fail, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		IFolder newFolder2 = src.getFolder("newFolder"); //$NON-NLS-1$
		assertTrue("newFolder should exist under src tree! (path=" + srcPath + ")",newFolder2.exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();
		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		//monitor.waitForCompletion();
		IFolder newBinFolder2 = bin.getFolder("newFolder"); //$NON-NLS-1$
		assertTrue("newFolder should exist under bin tree! (path=" + binPath + ")",newBinFolder2.exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();
		
		newFolder.delete(true,null);
		waitForJobsToComplete();
		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		// If either of these fail, then it's more likely than not to be
		// down to the timings of driving this programatically (this is
		// why there is a sleep above.
		IFolder newFolder3 = src.getFolder("newFolder"); //$NON-NLS-1$
		assertFalse("newFolder should not exist under src tree! (path=" + srcPath + ")",newFolder3.exists()); //$NON-NLS-1$ //$NON-NLS-2$
		waitForJobsToComplete();
		simpleProject.refreshLocal(IResource.DEPTH_INFINITE,null);
		//monitor.waitForCompletion();
		IFolder newBinFolder3 = bin.getFolder("newFolder"); //$NON-NLS-1$
		assertFalse("newFolder should not exist under bin tree! (path=" + binPath + ")",newBinFolder3.exists()); //$NON-NLS-1$ //$NON-NLS-2$
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

			waitForJobsToComplete();
			
			assertTrue("Successful build should have occurred", testLog //$NON-NLS-1$
					.containsMessage("AspectJ reports build successful")); //$NON-NLS-1$
			
			rep = testLog.getMostRecentMatchingMessage("AspectJ reports build successful"); //$NON-NLS-1$
			assertNotNull("Successful build should have been reported",rep); //$NON-NLS-1$
			
			assertTrue("The build should have been an incremental one",wasIncrementalBuild(rep)); //$NON-NLS-1$
		} finally {
			AspectJPlugin.getDefault().setAJLogger(null);
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

			waitForJobsToComplete();

			assertFalse("Source file changes should have been detected", testLog //$NON-NLS-1$
					.containsMessage("no source file changes for project bug102652")); //$NON-NLS-1$

			assertTrue("Successful build should have occurred", testLog //$NON-NLS-1$
					.containsMessage("AspectJ reports build successful")); //$NON-NLS-1$
			
			String rep = testLog.getMostRecentMatchingMessage("AspectJ reports build successful"); //$NON-NLS-1$
			assertNotNull("Successful build should have been reported",rep); //$NON-NLS-1$
			assertTrue("The build should have been an incremental one",wasIncrementalBuild(rep)); //$NON-NLS-1$
		} finally {
			AspectJPlugin.getDefault().setAJLogger(null);
		}
	}
		
	private boolean wasIncrementalBuild(String msg) {
		return msg.toLowerCase().indexOf("was: incremental") != -1; //$NON-NLS-1$
	}
}
