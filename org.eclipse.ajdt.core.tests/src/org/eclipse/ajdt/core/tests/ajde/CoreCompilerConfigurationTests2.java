/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SpringSource 
 *               Andrew Eisenberg
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.ajde;

import java.io.StringReader;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.testutils.ReaderInputStream;
import org.eclipse.ajdt.core.tests.testutils.TestLogger;
import org.eclipse.ajdt.core.tests.testutils.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;

/**
 * this set of tests ensures that the CoreCompilerConfiguration.getClasspathElementsWithModifiedContents() method 
 * works properly
 * 
 * Actually, this test class tests the method AJBuilder.getChangedRequiredProjects
 */
public class CoreCompilerConfigurationTests2 extends AJDTCoreTestCase {

    private TestLogger testLog;

    private IProject jp1;
//    private IProject jp2; not used
    private IProject jp3;
    private IProject ap1;
    private IProject ap2;
    private IProject ap3;
    private IProject myProj;
    
    protected void setUp() throws Exception {
        super.setUp();
        Utils.setAutobuilding(false);
        testLog = new TestLogger();
        AspectJPlugin.getDefault().setAJLogger(testLog);
        
        jp1 = createPredefinedProject("JavaProj1");
        /*jp2 = */ createPredefinedProject("JavaProj2-On Inpath");
        jp3 = createPredefinedProject("JavaProj3-ClassFolder");
        ap1 = createPredefinedProject("AspectProj1");
        ap2 = createPredefinedProject("AspectProj2-On AspectPath");
        ap3 = createPredefinedProject("AspectProj3-Has Outjar");
        AspectJCorePreferences.setProjectOutJar(ap3, "output.jar");
        joinBackgroudActivities();
        getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        // create and build this project last to ensure all prereqs exist
        myProj = createPredefinedProject("AspectProjWeCareAbout");
        myProj.build(IncrementalProjectBuilder.FULL_BUILD, null);
       
    }

    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            Utils.setAutobuilding(true);
        }
    }
    
    /*
     * When there are no changes, only the
     * projects on the inpath should be listed as modified
     */
    public void testNoChange() throws Exception {
        testLog.clearLog();

        // change myProj
        myProj.getFile("src/a/B.java").touch(null);
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        waitForAutoBuild();
        
        // check log
        // should see that the project on the inpath was considered changed.
        // because projects on in path are always considered changed
        // the closing "]" is necessary because it ensures that we are looking at a message for changed elements on classpath
        assertEquals(1, testLog.numberOfEntriesForMessage("/JavaProj2-On Inpath/bin]"));
        
        // ensure that this is the only time the list has been set
        assertEquals(1, testLog.numberOfEntriesForMessage("Setting list of classpath elements with modified contents:"));
    }

    /*
     * the project as well as the in path project should appear
     */
    public void testChangeAspectProject() throws Exception {
        testLog.clearLog();

        // change the project
        ap1.getFile("src/AFile.java").create(new ReaderInputStream(new StringReader("class AFile {}")), false, null);
        ap1.refreshLocal(IResource.DEPTH_INFINITE, null);
        
        // incremental build
        waitForIndexes();
        getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        joinBackgroudActivities();
        
        // check log
        // This first entry is from when project ap1 is built
        assertEquals(1, testLog.numberOfEntriesForMessage("   []"));
        // this message comes from the project we care about
        // the closing "]" is necessary because it ensures that we are looking at a message for changed elements on classpath
        testLog.printLog();
        assertEquals(1, testLog.numberOfEntriesForMessage(new String[] {"/AspectProj1/bin", "/JavaProj2-On Inpath/bin]"}));

        // ensure that these are the only 2 times the list has been set
        assertEquals(2, testLog.numberOfEntriesForMessage("Setting list of classpath elements with modified contents:"));
    }
    
    public void testChangeAspectProjectOnAspectPath() throws Exception {
        testLog.clearLog();

        // change the project
        ap2.getFile("src/AFile.java").create(new ReaderInputStream(new StringReader("class AFile {}")), false, null);
        
        // incremental build
        waitForIndexes();
        getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        joinBackgroudActivities();
        
        // check log
        // This first entry is from when project ap2 is built
        assertEquals(1, testLog.numberOfEntriesForMessage("   []"));
        // this message comes from the project we care about
        // the closing "]" is necessary because it ensures that we are looking at a message for changed elements on classpath
        testLog.printLog();
        assertEquals(1, testLog.numberOfEntriesForMessage(new String[] {"/AspectProj2-On AspectPath/bin", "/JavaProj2-On Inpath/bin]"}));

        // ensure that these are the only 2 times the list has been set
        assertEquals(2, testLog.numberOfEntriesForMessage("Setting list of classpath elements with modified contents:"));
    }
    
    public void testChangeJavaProject() throws Exception {
        testLog.clearLog();

        // change the project
        jp1.getFile("src/AFile.java").create(new ReaderInputStream(new StringReader("class AFile {}")), false, null);
        
        // incremental build
        waitForIndexes();
        getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        joinBackgroudActivities();
        
        // check log
        // this message comes from the project we care about
        // the closing "]" is necessary because it ensures that we are looking at a message for changed elements on classpath
        assertEquals(1, testLog.numberOfEntriesForMessage(new String[] {"/JavaProj1/bin", "/JavaProj2-On Inpath/bin]"}));

        // ensure that this is the only time the list has been set
        assertEquals(1, testLog.numberOfEntriesForMessage("Setting list of classpath elements with modified contents:"));
    }
    
    public void testChangeJavaProjectNonStructuralChange() throws Exception {
        testLog.clearLog();

        // change the project---non-structural change
        jp1.getFile("src/c/B.java").touch(null);
        
        // incremental build
        waitForIndexes();
        getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        joinBackgroudActivities();
        
        // check log
        // this message comes from the project we care about
        // nothing other than the inpath should be on the list
        // the closing "]" is necessary because it ensures that we are looking at a message for changed elements on classpath
        assertEquals(1, testLog.numberOfEntriesForMessage(new String[] {"/JavaProj2-On Inpath/bin]"}));

        // ensure that this is the only time the list has been set
        assertEquals(1, testLog.numberOfEntriesForMessage("Setting list of classpath elements with modified contents:"));
    }
    
    public void testChangeClassFolder() throws Exception {
        testLog.clearLog();

        // change the project
        jp3.getFile("src/AFile.java").create(new ReaderInputStream(new StringReader("class AFile {}")), false, null);
        
        // incremental build
        waitForIndexes();
        getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        joinBackgroudActivities();
        
        // incremental build...again
        // we have a situation where there is a double build because there 
        // is a binary folder dependency.  
        // In general, we don't recommend binary folder dependencies for
        // this reason (should use project dependencies instead)
        // this second build should bypass the compiler since there are no changes.
        getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        joinBackgroudActivities();

        
        // check log
        // This message should occur only once during the first build of jp3.
        // This project should be recompiled and its classpath will be checked.
        // The second build of AspectProjWeCareAbout should bypass the compiler and therefore
        // the classpath is not checked.
        assertEquals(1, testLog.numberOfEntriesForMessage(new String[] {"/JavaProj2-On Inpath/bin]"}));

        // this message should not appear because the build of AspectProjWeCareAbout bypasses the compiler
        // because no source files have changed
        assertEquals(0, testLog.numberOfEntriesForMessage(new String[] {"/JavaProj3-ClassFolder/bin", "/JavaProj2-On Inpath/bin]"}));

        // ensure that this is the only time the list has been set
        assertEquals(1, testLog.numberOfEntriesForMessage("Setting list of classpath elements with modified contents:"));
    }
    
    public void testChangeJar() throws Exception {
        // incremental build
        waitForIndexes();
        getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        joinBackgroudActivities();

        testLog.clearLog();

        // change the project
        ap3.getFile("src/AFile.java").create(new ReaderInputStream(new StringReader("class AFile {}")), false, null);
        
        // incremental build
        waitForIndexes();
        getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        joinBackgroudActivities();
        
        // check log
        // This first entry is from when project ap3 is built
        assertEquals(1, testLog.numberOfEntriesForMessage("   []"));
        // this message comes from the project we care about
        assertEquals(1, testLog.numberOfEntriesForMessage(new String[] {"/AspectProj3-Has Outjar/output.jar", "/JavaProj2-On Inpath/bin]"}));

        // ensure that this is the only time the list has been set
        assertEquals(2, testLog.numberOfEntriesForMessage("Setting list of classpath elements with modified contents:"));
    }
}
