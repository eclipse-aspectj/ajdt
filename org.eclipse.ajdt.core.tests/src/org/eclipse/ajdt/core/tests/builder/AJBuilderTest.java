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

import java.io.File;
import java.io.StringReader;

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

    IProject project;
    TestLogger testLog;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        testLog = new TestLogger();
        AspectJPlugin.getDefault().setAJLogger(testLog);
        project = createPredefinedProject("bug101481");
        Utils.setAutobuilding(false);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        AspectJPlugin.getDefault().setAJLogger(null);
        Utils.setAutobuilding(true);
        deleteProject(project);		
    }
    
    /**
     * Test for bug 101481 - "clean" build doesn't work
     * and refreshing of output directory
     * 
     * @throws Exception
     */
    public void testCleanBuild() throws Exception {
        assertFalse("autobuilding should be set to false",Utils.isAutobuilding());
        assertFalse("project should have no errors",testLog.containsMessage("error"));
        
        IFolder src = project.getFolder("src");
		if (!src.exists()){
			src.create(true, true, null);
		}
		IFolder pack = src.getFolder("pack");
		if (!pack.exists()){
			pack.create(true, true, null);
		}
		IFile c = pack.getFile("C.java");
		assertNotNull("src folder should not be null", src);
		assertNotNull("package pack should not be null", pack);
		assertNotNull("class c should not be null", c);
		assertTrue("java file should exist", c.exists());
		
        IFolder bin = project.getFolder("bin");
		if (!bin.exists()){
		    bin.create(true, true, null);
		}
		IFolder binPack = bin.getFolder("pack");
		if (!binPack.exists()){
		    binPack.create(true, true, null);
		}
		IFile binC = binPack.getFile("C.class");
		
		assertTrue("bin directory should contain class file",outputDirContainsFile("C.class"));
		// testing the refresh output directory part of bug 101481
		assertTrue("class file should exist",binC.exists());
		
		StringBuffer origContents = new StringBuffer("package pack; ");
		origContents.append(System.getProperty("line.separator"));
		origContents.append("public class C {}");
		
		// write "blah blah blah" to the class
		StringBuffer sb = new StringBuffer("blah blah blah");	
		sb.append(origContents);
		StringReader sr = new StringReader(sb.toString());
		c.setContents(new ReaderInputStream(sr),IResource.FORCE, null);		
		sr.close();
		
		project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD,null);
        assertTrue("project should have errors",testLog.containsMessage("error at blah blah blah"));
		assertTrue("bin directory should contain class file",outputDirContainsFile("C.class"));
        assertFalse("should not have cleaned the output folder",testLog.containsMessage("Cleared AJDT relationship map for project bug101481"));
		int n = testLog.numberOfEntriesForMessage("Builder: Tidied output folder, deleted 1 .class files from");
		
		binC = binPack.getFile("C.class");
		assertTrue("class file should exist",binC.exists());
		
        project.build(IncrementalProjectBuilder.CLEAN_BUILD,null);
        // testing the same steps are taken during a clean as they
        // are in the javaBuilder part of bug 101481
        assertTrue("should have deleted 1 class file from the output dir",testLog.containsMessage("Builder: Tidied output folder, deleted 1 .class files from"));        
        assertTrue("should have removed problems and tasks for the project",testLog.containsMessage("Removed problems and tasks for project"));        
        assertEquals("should have cleaned output folder " + (n+1) + "times",n+1,testLog.numberOfEntriesForMessage("Builder: Tidied output folder, deleted 1 .class files from"));
        assertFalse("bin directory should not contain class file",outputDirContainsFile("C.class"));
        
        // testing the refresh output dir after a clean (without doing
        // a build) part of bug 101481
        binC = binPack.getFile("C.class");
		assertFalse("class file should not exist",binC.exists());
    }
    
    private boolean outputDirContainsFile(String fileName) throws JavaModelException  {
        IJavaProject javaProject = JavaCore.create(project);
        IPath workspaceRelativeOutputPath = javaProject.getOutputLocation();

        String realOutputLocation = null;
		if (workspaceRelativeOutputPath.segmentCount() == 1) { // project
			// root
			realOutputLocation = javaProject.getResource().getLocation()
					.toOSString();
		} else {
			IFolder out = ResourcesPlugin.getWorkspace().getRoot()
					.getFolder(workspaceRelativeOutputPath);
			realOutputLocation = out.getLocation().toOSString();
		}

		File outputDir = new File(realOutputLocation + File.separator + "pack");
		File[] outputFiles = outputDir.listFiles();
		for (int i = 0; i < outputFiles.length; i++) {
            if (outputFiles[i].getName().equals(fileName)) {
                return true; 
            }
        }
		return false;
    }
    
    
}
