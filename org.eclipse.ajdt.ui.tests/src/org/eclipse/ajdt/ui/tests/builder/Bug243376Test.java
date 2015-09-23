package org.eclipse.ajdt.ui.tests.builder;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.launching.AspectJApplicationLaunchShortcut;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.ajdt.ui.tests.testutils.TestLogger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;


public class Bug243376Test extends UITestCase {
    
    /**
     * When a Project puts another project's out folder onto its aspect path,
     * after a full build happens there is an autobuild of the dependent project.
     * This is because the dependent project sees that the out folder has changed 
     * because of the full build and so the dependent project itself requires a full
     * build.  
     * 
     * Up until AJDT 1.5.3, this has been the standard way to put projects on the 
     * aspect path, but this causes an unnecessary auto build. 
     * 
     * The way around this is to use project dependencies (added in AJDT 1.5.3).  
     * This test case tests to ensure that when a project dependency is on the 
     * aspect path, the unnecessary auto build does *not* occur.
     */
	public void testnothing() {}
//    public void testNoAutoBuildAfterFullBuild() throws Exception {
//        // requires a completely clean workspace
//        super.cleanWorkspace(true);
//
//        IProject hasAspectPath = createPredefinedProject("Project with Aspect Path"); //$NON-NLS-1$
//        /* IProject onAspectPath = */ createPredefinedProject("Project on Aspect Path"); //$NON-NLS-1$
//        
//        TestLogger testLog = new TestLogger();
//        AspectJPlugin.getDefault().setAJLogger(testLog);
//        hasAspectPath.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
//        waitForJobsToComplete();
//     
//        // open in editor and trigger something that would normally cause 
//        // an incremental build if there was a class folder on the aspect path
//        IFile file = hasAspectPath.getFile("src/n/Nothing.java"); //$NON-NLS-1$
//        JavaEditor editor = (JavaEditor) openFileInDefaultEditor(file, true);
//        editor.getViewer().getTextWidget().setText("fff"); //$NON-NLS-1$
//        hasAspectPath.getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
//        editor.close(false);
//
//        waitForJobsToComplete();
//        
//        // check that there has been 2 full builds and no auto or incremental builds
//        assertEquals("Should have had 2 full builds", 2, testLog.numberOfEntriesForMessage("FULLBUILD")); //$NON-NLS-1$ //$NON-NLS-2$
//        assertEquals("Should have had no auto builds", 0, testLog.numberOfEntriesForMessage("AUTOBUILD")); //$NON-NLS-1$ //$NON-NLS-2$
//        assertEquals("Should have had no incremental builds", 0, testLog.numberOfEntriesForMessage("INCREMENTAL_BUILD")); //$NON-NLS-1$ //$NON-NLS-2$
//        
//        // Ensure that the aspect has been properly applied
//        
//        // do launch
//        AspectJApplicationLaunchShortcut launcher = new AspectJApplicationLaunchShortcut();
//        launcher.launch(
//                openFileInAspectJEditor(
//                        file, true),
//                 ILaunchManager.RUN_MODE);
//        waitForJobsToComplete();
//        waitForJobsToComplete();
//        String console = getConsoleViewContents();
//        assertTrue("Aspect has not been woven",console.indexOf("Aspect has been woven")!=-1); //$NON-NLS-1$ //$NON-NLS-2$
//    }
}
