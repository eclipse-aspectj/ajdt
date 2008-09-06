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
 * this set of tests ensures that the CoreCompilerConfiguration.getClasspathElementsWithModifiedContents() method 
 * works properly
 * 
 * Actually, this test class tests the method AJBuilder.getChangedRequiredProjects
 */
public class CoreCompilerConfigurationTests2 extends AJDTCoreTestCase {

    private TestLogger testLog;

    private IProject jp1;
//    private IProject jp2;  // not used
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
        /* jp2 = */ createPredefinedProject("JavaProj2-On Inpath");
        jp3 = createPredefinedProject("JavaProj3-ClassFolder");
        ap1 = createPredefinedProject("AspectProj1");
        ap2 = createPredefinedProject("AspectProj2-On AspectPath");
        ap3 = createPredefinedProject("AspectProj3-Has Outjar");
        myProj = createPredefinedProject("AspectProjWeCareAbout");
        
        AspectJCorePreferences.setIncrementalCompilationOptimizationsEnabled(true);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        Utils.setAutobuilding(true);
        AspectJCorePreferences.setIncrementalCompilationOptimizationsEnabled(false);
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
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();
        
        // check log
        // should see that the project on the inpath was considered changed.
        assertEquals(1, testLog.numberOfEntriesForMessage("[/Users/andrew/Eclipse/Workspaces/junit-workspace/JavaProj2-On Inpath/bin]"));
        
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
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();
        
        // check log
        // This first entry is from when project ap1 is built
        assertEquals(1, testLog.numberOfEntriesForMessage("   []"));
        // this message comes from the project we care about
        assertEquals(1, testLog.numberOfEntriesForMessage("[/Users/andrew/Eclipse/Workspaces/junit-workspace/AspectProj1/bin, /Users/andrew/Eclipse/Workspaces/junit-workspace/JavaProj2-On Inpath/bin]"));

        // ensure that these are the only 2 times the list has been set
        assertEquals(2, testLog.numberOfEntriesForMessage("Setting list of classpath elements with modified contents:"));
    }
    
    public void testChangeAspectProjectOnAspectPath() throws Exception {
        testLog.clearLog();

        // change the project
        ap2.getFile("src/AFile.java").create(new ReaderInputStream(new StringReader("class AFile {}")), false, null);
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();
        
        // check log
        // This first entry is from when project ap2 is built
        assertEquals(1, testLog.numberOfEntriesForMessage("   []"));
        // this message comes from the project we care about
        assertEquals(1, testLog.numberOfEntriesForMessage("[/Users/andrew/Eclipse/Workspaces/junit-workspace/AspectProj2-On AspectPath/bin, /Users/andrew/Eclipse/Workspaces/junit-workspace/JavaProj2-On Inpath/bin]"));

        // ensure that these are the only 2 times the list has been set
        assertEquals(2, testLog.numberOfEntriesForMessage("Setting list of classpath elements with modified contents:"));
    }
    
    public void testChangeJavaProject() throws Exception {
        testLog.clearLog();

        // change the project
        jp1.getFile("src/AFile.java").create(new ReaderInputStream(new StringReader("class AFile {}")), false, null);
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();
        
        // check log
        // this message comes from the project we care about
        assertEquals(1, testLog.numberOfEntriesForMessage("[/Users/andrew/Eclipse/Workspaces/junit-workspace/JavaProj1/bin, /Users/andrew/Eclipse/Workspaces/junit-workspace/JavaProj2-On Inpath/bin]"));

        // ensure that this is the only time the list has been set
        assertEquals(1, testLog.numberOfEntriesForMessage("Setting list of classpath elements with modified contents:"));
    }
    
    public void testChangeJavaProjectNonStructuralChange() throws Exception {
        testLog.clearLog();

        // change the project---non-structural change
        jp1.getFile("src/c/B.java").touch(null);
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();
        
        // check log
        // this message comes from the project we care about
        // nothing other than the inpath should be on the list
        assertEquals(1, testLog.numberOfEntriesForMessage("[/Users/andrew/Eclipse/Workspaces/junit-workspace/JavaProj2-On Inpath/bin]"));

        // ensure that this is the only time the list has been set
        assertEquals(1, testLog.numberOfEntriesForMessage("Setting list of classpath elements with modified contents:"));
    }
    
    public void testChangeClassFolder() throws Exception {
        testLog.clearLog();

        // change the project
        jp3.getFile("src/AFile.java").create(new ReaderInputStream(new StringReader("class AFile {}")), false, null);
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();
        
        // incremental build...again
        // we have a situation where there is a double build because there 
        // is a binary folder dependency.  
        // In general, we don't recommend binary folder dependencies for
        // this reason (should use project dependencies instead)
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();

        
        // check log
        // this message comes from the project we care about on the first build
        assertEquals(1, testLog.numberOfEntriesForMessage("[/Users/andrew/Eclipse/Workspaces/junit-workspace/JavaProj2-On Inpath/bin]"));

        // this message comes from the project we care about on the second build
        assertEquals(1, testLog.numberOfEntriesForMessage("[/Users/andrew/Eclipse/Workspaces/junit-workspace/JavaProj3-ClassFolder/bin, /Users/andrew/Eclipse/Workspaces/junit-workspace/JavaProj2-On Inpath/bin]"));

        // ensure that this is the only time the list has been set
        assertEquals(2, testLog.numberOfEntriesForMessage("Setting list of classpath elements with modified contents:"));
    }
    
    public void testChangeJar() throws Exception {
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();

        testLog.clearLog();

        // change the project
        ap3.getFile("src/AFile.java").create(new ReaderInputStream(new StringReader("class AFile {}")), false, null);
        
        // incremental build
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
        waitForAutoBuild();
        
        // check log
        // This first entry is from when project ap3 is built
        assertEquals(1, testLog.numberOfEntriesForMessage("   []"));
        // this message comes from the project we care about
        assertEquals(1, testLog.numberOfEntriesForMessage("[/Users/andrew/Eclipse/Workspaces/junit-workspace/AspectProj3-Has Outjar/output.jar, /Users/andrew/Eclipse/Workspaces/junit-workspace/JavaProj2-On Inpath/bin]"));

        // ensure that this is the only time the list has been set
        assertEquals(2, testLog.numberOfEntriesForMessage("Setting list of classpath elements with modified contents:"));
    }
}
