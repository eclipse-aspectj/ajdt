/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SpringSource - initial API and implementation 
 *              Andrew Eisenberg
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.builder;

import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.testutils.Utils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Tests that check to see if resources are properly copied over
 * on build and deleted on clean
 * 
 * Also, that all build artifacts are marked as derived
 * @author andrew
 *
 */
public class DerivedTests extends AJDTCoreTestCase {
    protected void setUp() throws Exception {
        super.setUp();
        Utils.setAutobuilding(false);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        Utils.setAutobuilding(true);
    }

    
    public void testCopyDerived() throws Exception {
        IProject proj = createPredefinedProject("CopyDerived1");
        getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
        
        // all out folders were empty.
        // project was built when created
        // check all build artifacts to make sure that 
        // they exist and are marked derived
        
        IFolder out = proj.getFolder("bin");
        checkFolderIsNotDerived(out, "");
        checkFileIsDerived(out, "file.txt");
        checkFileIsDerived(out, "Nothing.class");
        checkFileIsDerived(out, "Nothing2.class");
        checkFolderIsDerived(out, "package1");
        checkFileIsDerived(out, "package1/file.txt");
        checkFileIsDerived(out, "package1/Nothing.class");
        checkFileIsDerived(out, "package1/Nothing2.class");
        
        // deep out folder
        out = proj.getFolder("folder/bin2");
        checkFolderIsNotDerived(out, "");
        checkFileIsDerived(out, "file.txt");
        checkFileIsDerived(out, "Nothing3.class");
        checkFileIsDerived(out, "Nothing4.class");
        checkFolderIsDerived(out, "package1");
        checkFileIsDerived(out, "package1/file.txt");
        checkFileIsDerived(out, "package1/Nothing3.class");
        checkFileIsDerived(out, "package1/Nothing4.class");
        
        // binary folder == out folder
        out = proj.getFolder("src3");
        checkFolderIsNotDerived(out, "");
        checkFileIsNotDerived(out, "file.txt");
        checkFileIsDerived(out, "Nothing5.class");
        checkFileIsDerived(out, "Nothing6.class");
        checkFolderIsNotDerived(out, "package1");
        checkFileIsNotDerived(out, "package1/file.txt");
        checkFileIsDerived(out, "package1/Nothing5.class");
        checkFileIsDerived(out, "package1/Nothing6.class");
        
        // should delete all binaries and all resources
        // except for resources in source folder
        proj.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        
        out = proj.getFolder("bin");
        checkFolderIsNotDerived(out, "");
        checkFileNoExist(out, "file.txt");
        checkFileNoExist(out, "Nothing.class");
        checkFileNoExist(out, "Nothing2.class");
        checkFolderNoExist(out, "package1");
        checkFileNoExist(out, "package1/file.txt");
        checkFileNoExist(out, "package1/Nothing.class");
        checkFileNoExist(out, "package1/Nothing2.class");
        
        // deep out folder
        out = proj.getFolder("folder/bin2");
        checkFolderIsNotDerived(out, "");
        checkFileNoExist(out, "file.txt");
        checkFileNoExist(out, "Nothing3.class");
        checkFileNoExist(out, "Nothing4.class");
        checkFolderNoExist(out, "package1");
        checkFileNoExist(out, "package1/file.txt");
        checkFileNoExist(out, "package1/Nothing3.class");
        checkFileNoExist(out, "package1/Nothing4.class");
        
        // binary folder == out folder
        out = proj.getFolder("src3");
        checkFolderIsNotDerived(out, "");
        checkFileIsNotDerived(out, "file.txt");
        checkFileNoExist(out, "Nothing5.class");
        checkFileNoExist(out, "Nothing6.class");
        checkFolderIsNotDerived(out, "package1");
        checkFileIsNotDerived(out, "package1/file.txt");
        checkFileNoExist(out, "package1/Nothing5.class");
        checkFileNoExist(out, "package1/Nothing6.class");
    }
    
    // As above, but test that when root folder is out folder
    public void testCopyDerivedInRoot() throws Exception {
        IProject proj = createPredefinedProject("CopyDerived2");
        getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        checkFileIsNotDerived(proj, "file.txt");
        checkFileIsDerived(proj, "Nothing.class");
        checkFileIsDerived(proj, "Nothing2.class");
        checkFolderIsNotDerived(proj, "package1");
        checkFileIsNotDerived(proj, "package1/file.txt");
        checkFileIsDerived(proj, "package1/Nothing.class");
        checkFileIsDerived(proj, "package1/Nothing2.class");

        // should delete all binaries but leave all resources
        proj.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        
        checkProjectIsNotDerived(proj, "");
        checkFileIsNotDerived(proj, "file.txt");
        checkFileNoExist(proj, "Nothing1.class");
        checkFileNoExist(proj, "Nothing2.class");
        checkFolderIsNotDerived(proj, "package1");
        checkFileIsNotDerived(proj, "package1/file.txt");
        checkFileNoExist(proj, "package1/Nothing1.class");
        checkFileNoExist(proj, "package1/Nothing2.class");

    }
    
    // test that after a source folder is deleted, all 
    // class files nd resources are removed from the 
    // output folder
    public void testDeleteSourceFolder() throws Exception {
        IProject proj = createPredefinedProject("CopyDerived1");
        getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        IJavaProject jProj = JavaCore.create(proj);
        IClasspathEntry[] classpath = jProj.getRawClasspath();
        IClasspathEntry[] newClasspath = new IClasspathEntry[classpath.length-1];

        // remove the classpath entry corresponding to src
        for (int i = 0, j = 0; i < classpath.length; i++, j++) {
            if (classpath[i].getPath().lastSegment().toString().equals("src")) {
                j--;
                continue;
            }
            newClasspath[j] = classpath[i];
        }
        
        jProj.setRawClasspath(newClasspath, true, null);
        
        proj.build(IncrementalProjectBuilder.FULL_BUILD, null);
        
        IFolder out = proj.getFolder("bin");
        checkFolderIsNotDerived(out, "");
        checkFileNoExist(out, "file.txt");
        checkFileNoExist(out, "Nothing.class");
        checkFileNoExist(out, "Nothing2.class");
        checkFolderNoExist(out, "package1");
        checkFileNoExist(out, "package1/file.txt");
        checkFileNoExist(out, "package1/Nothing.class");
        checkFileNoExist(out, "package1/Nothing2.class");

    }
    
    void checkFileIsDerived(IContainer container, String fName) {
        IFile file = container.getFile(new Path(fName));
        assertTrue(file + " should exist", file.exists());
        assertTrue(file + " should be derived", file.isDerived());
    }
    void checkFileIsNotDerived(IContainer container, String fName) {
        IFile file = container.getFile(new Path(fName));
        assertTrue(file + " should exist", file.exists());
        assertFalse(file + " should not be derived", file.isDerived());
    }
    void checkFileNoExist(IContainer container, String fName) {
        IFile file = container.getFile(new Path(fName));
        assertFalse(file + " should not exist", file.exists());
    }
    void checkFolderIsDerived(IContainer container, String fName) {
        IFolder folder = container.getFolder(new Path(fName));
        assertTrue(folder + " should exist", folder.exists());
        assertTrue(folder + " should be derived", folder.isDerived());
    }
    void checkFolderIsNotDerived(IContainer container, String fName) {
        IFolder folder = container.getFolder(new Path(fName));
        assertTrue(folder + " should exist", folder.exists());
        assertFalse(folder + " should not be derived", folder.isDerived());
    }
    void checkProjectIsNotDerived(IProject proj, String string) {
        assertTrue(proj + " should exist", proj.exists());
        assertFalse(proj + " should not be derived", proj.isDerived());
    }

    void checkFolderNoExist(IContainer container, String fName) {
        IFolder folder = container.getFolder(new Path(fName));
        assertFalse(folder + " should not exist", folder.exists());
    }
}
