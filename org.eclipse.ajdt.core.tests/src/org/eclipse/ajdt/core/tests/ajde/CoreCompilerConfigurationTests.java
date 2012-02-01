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
import org.eclipse.core.resources.IncrementalProjectBuilder;


/**
 * This class tests Bug 245566. The use of ICompilerConfiguration's
 * api should reduce overhead in preparing for incremenental builds
 */
public class CoreCompilerConfigurationTests extends AJDTCoreTestCase {
    private TestLogger testLog;

    protected void setUp() throws Exception {
        super.setUp();
        // requires a completely clean workspace
        super.cleanWorkspace(true);
        Utils.setAutobuilding(false);
        testLog = new TestLogger();
        AspectJPlugin.getDefault().setAJLogger(testLog);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        Utils.setAutobuilding(true);
    }
    
    // test to ensure that when the switch is turned off, optimization does not occur
    public void testIncrementalCompilationOptimizationsEnabled() throws Exception {
        try {
	        assertTrue(AspectJCorePreferences.isIncrementalCompilationOptimizationsEnabled());
	        AspectJCorePreferences.setIncrementalCompilationOptimizationsEnabled(false);
	        assertFalse(AspectJCorePreferences.isIncrementalCompilationOptimizationsEnabled());
	        
	        // load project and full build
	        IProject proj = createPredefinedProject("Bean Example");
	        getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
	        
	        testLog.clearLog();
	        
	        proj.getFile("src/bean/Point.java").touch(null);
	        
	        // incremental build
	        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
	        waitForAutoBuild();
	
	        // ensure that the classpath entries list has not been set
	        assertEquals(0, testLog.numberOfEntriesForMessage("Setting list of classpath elements with modified contents:"));
	        assertEquals(1, testLog.numberOfEntriesForMessage("Optimizations turned off, so assuming all parts of configuration have changed"));
	        assertEquals(1, testLog.numberOfEntriesForMessage("Configuration was []"));
	        assertEquals(1, testLog.numberOfEntriesForMessage(new String[] {
	                "Resetting list of modified source files.  Was [",
	                "Bean Example/src/bean/Point.java]"}));
        } finally {
            AspectJCorePreferences.setIncrementalCompilationOptimizationsEnabled(true);
        }
    }
    
    
    public void testChangeSourceFiles() throws Exception {

        // load project and full build
        IProject proj = createPredefinedProject("Bean Example");
        getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        // touch source files (one Java and one AJ)
        proj.getFile("src/bean/Point.java").touch(null);
        proj.getFile("src/bean/BoundPoint.aj").touch(null);

        testLog.clearLog();
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();
        
        // look at AJDT Event trace
        // when build is determining which source files have changed
        assertEquals(1, testLog.numberOfEntriesForMessage("Bean Example/src/bean/Point.java has changed."));
        assertEquals(1, testLog.numberOfEntriesForMessage("Bean Example/src/bean/BoundPoint.aj has changed."));
        
        // should not happen.  this line should only be logged when a file is added or deleted
        assertEquals(0, testLog.numberOfEntriesForMessage("CoreCompilerConfiguration for project Bean Example registered a configuration change: [PROJECTSOURCEFILES_CHANGED]"));

        // during a callback from ajc
        assertEquals(1, testLog.numberOfEntriesForMessage("build: Examined delta - 2 changed, 0 added, and 0 deleted source files in required project Bean Example"));
        
        // after compilation can reset configuration
        assertEquals(1, testLog.numberOfEntriesForMessage(new String[] {"Resetting list of modified source files.  Was [", 
            "Bean Example/src/bean/BoundPoint.aj, ", "Bean Example/src/bean/Point.java]"}));

        // when the configuration is being reset
        assertEquals(1, testLog.numberOfEntriesForMessage("Configuration was []"));
    }
    
    // also test creating and deleting source files and resources
    public void testAddDeleteSourceFiles() throws Exception {
        // load project and full build
        IProject proj = createPredefinedProject("Bean Example");
        getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        testLog.clearLog();

        // create a Java file and an Aspect file
        proj.getFile("src/MyJavaFile.java").create(new ReaderInputStream(new StringReader("public class MyJavaFile { }")), 
                true, null);
        proj.getFile("src/MyAspectFile.aj").create(new ReaderInputStream(new StringReader("public aspect MyAspectFile { }")), 
                true, null);
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();

        // now delete the files
        proj.getFile("src/MyJavaFile.java").delete(true, null);
        proj.getFile("src/MyAspectFile.aj").delete(true, null);
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();

        // check log
        
        // should not have found any changes, only additions and deletions
        assertEquals(1, testLog.numberOfEntriesForMessage("build: Examined delta - 0 changed, 2 added, and 0 deleted source files in required project Bean Example"));
        assertEquals(1, testLog.numberOfEntriesForMessage("build: Examined delta - 0 changed, 0 added, and 2 deleted source files in required project Bean Example"));
        
        // all of these items should appear twice: once on creation and once on deletion
        assertEquals(2, testLog.numberOfEntriesForMessage("Compiler configuration for project Bean Example has been read by compiler.  Resetting."));
        assertEquals(2, testLog.numberOfEntriesForMessage("Configuration was [PROJECTSOURCEFILES_CHANGED]"));
        assertEquals(2, testLog.numberOfEntriesForMessage("Resetting list of modified source files.  Was []"));

        // occurs 4 times: twice on creation and twice on deletion
        assertEquals(4, testLog.numberOfEntriesForMessage("CoreCompilerConfiguration for project Bean Example registered a configuration change: [PROJECTSOURCEFILES_CHANGED]"));
        
        // this last entry occurs only once after the aspect is deleted
        assertEquals(1, testLog.numberOfEntriesForMessage("Preparing for build: not going to be incremental because an aspect was deleted"));
    }
    
    public void testClasspathChange() throws Exception {
        IProject proj = createPredefinedProject("Bean Example");
        getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
        
        proj.getFile(".classpath").touch(null);

        testLog.clearLog();
        
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();
        
        // look at AJDT Event trace
        // the configuration should have registered a change:
        assertEquals(1, testLog.numberOfEntriesForMessage("CoreCompilerConfiguration for project Bean Example registered a configuration change: [ASPECTPATH_CHANGED, CLASSPATH_CHANGED, INPATH_CHANGED, OUTPUTDESTINATIONS_CHANGED]"));
        
        // classpath has been changed, so should not see this message
        assertEquals(0, testLog.numberOfEntriesForMessage("build: Examined delta - no source file or classpath changes for project Bean Example"));
    }

    public void testManifestChange() throws Exception {
        IProject proj = createPredefinedProject("Bean Example");
        getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
        
        proj.getFile("META-INF/MANIFEST.MF").touch(null);

        testLog.clearLog();
        
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();
        
        // look at AJDT Event trace
        // the configuration should have registered a change:
        assertEquals(1, testLog.numberOfEntriesForMessage("CoreCompilerConfiguration for project Bean Example registered a configuration change: [CLASSPATH_CHANGED]"));
        
        // manifest has been changed, so should not see this message
        assertEquals(0, testLog.numberOfEntriesForMessage("build: Examined delta - no source file or classpath changes for project Bean Example"));
    }

    /**
     * before the first build after a clean, any changes to the
     * configuration should not be recorded because the compiler has 
     * nothing to compare the changes to.
     * @throws Exception
     */
    public void testChangeBeforeBuild() throws Exception {
        // load project and full build
        IProject proj = createPredefinedProject("Bean Example");
        getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        testLog.clearLog();
        
        // now do a clean so that the previous build is forgotten
        getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        
        proj.getFile("src/bean/Point.java").touch(null);
        proj.getFile("src/bean/BoundPoint.aj").touch(null);
        proj.getFile(".classpath").touch(null);
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();
        
        
        // since there was no state before and hence no ResourceDelta, 
        // we know that there must be certain entries missing from the log
        assertEquals(0, testLog.numberOfEntriesForMessage("Bean Example/src/bean/Point.java has changed."));
        assertEquals(0, testLog.numberOfEntriesForMessage("Bean Example/src/bean/BoundPoint.aj has changed."));
        assertEquals(0, testLog.numberOfEntriesForMessage("CoreCompilerConfiguration for project Bean Example registered a configuration change: [PROJECTSOURCEFILES_CHANGED]"));

        assertEquals(1, testLog.numberOfEntriesForMessage("Compiler configuration for project Bean Example doesn't know previous state, so assuming EVERYTHING has changed."));
        
        // and what should we be seeing about the classpath?
        
        // the previous set of config changes was *everything*, so
        // this was sent to the compiler
        assertEquals(1, testLog.numberOfEntriesForMessage("Configuration was [PROJECTSOURCEFILES_CHANGED, JAVAOPTIONS_CHANGED, ASPECTPATH_CHANGED, CLASSPATH_CHANGED, INPATH_CHANGED, NONSTANDARDOPTIONS_CHANGED, OUTJAR_CHANGED, PROJECTSOURCERESOURCES_CHANGED, OUTPUTDESTINATIONS_CHANGED, INJARS_CHANGED]"));
    }
    
    // also test creating and deleting source files and resources
    public void testAddDeleteChangeReources() throws Exception {
        // load project and full build
        IProject proj = createPredefinedProject("Bean Example");
        getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
        
        testLog.clearLog();

        // create a resource
        proj.getFile("src/nothing.txt").create(new ReaderInputStream(new StringReader("nothing interesting")), 
                true, null);

        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();

        // touch the resource
        proj.getFile("src/nothing.txt").touch(null);

        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();
        
        // delete the resource
        proj.getFile("src/nothing.txt").delete(true, null);
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();
        
        // check log
        // 3 builds, but only 2 should have resource files changed in it
        // only occurs when resources are added or deleted.
        assertEquals(2, testLog.numberOfEntriesForMessage("CoreCompilerConfiguration for project Bean Example registered a configuration change: [PROJECTSOURCERESOURCES_CHANGED]"));
        
        // no source files have changed, though
        assertEquals(3, testLog.numberOfEntriesForMessage("build: Examined delta - no source file or classpath changes for project Bean Example"));
        
        // also make sure the file was copied and deleted properly
        assertEquals(1, testLog.numberOfEntriesForMessage("Copying added file nothing.txt"));
        assertEquals(1, testLog.numberOfEntriesForMessage("Deleting existing file nothing.txt"));
        assertEquals(1, testLog.numberOfEntriesForMessage("Copying changed file nothing.txt"));
        assertEquals(1, testLog.numberOfEntriesForMessage("Deleting removed file nothing.txt"));
    }
    
    // now test what happens when the build is broken
    // not really sure what the behavior should be.
    public void testBadBuild() throws Exception {
        // load project and full build
        IProject proj = createPredefinedProject("Bean Example");
        getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
        
        testLog.clearLog();

        // create a source file
        proj.getFile("src/MyAspectFile.aj").create(new ReaderInputStream(new StringReader("public aspect MyAspectFile { }")), 
                true, null);
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();

        // add an error to the source file
        proj.getFile("src/MyAspectFile.aj").appendContents(new ReaderInputStream(new StringReader("XXX")), 
                true, false, null);
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();

        
        // remove the error
        proj.getFile("src/MyAspectFile.aj").setContents(new ReaderInputStream(new StringReader("public aspect MyAspectFile { }")), 0, null);
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();
        
        // check log
        testLog.printLog();
    }
    
    

}
