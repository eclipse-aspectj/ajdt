package org.eclipse.ajdt.ui.tests.builder;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.ajdt.ui.tests.testutils.TestLogger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

public class ITDinSeparateProjects extends UITestCase {
    
    /**
     * Test to ensure that a dependent project on the aspect path can send an ITD
     * into another project
     */
    public void testBug121810() throws CoreException {
        try {
            TestLogger testLog = new TestLogger();
            AspectJPlugin.getDefault().setAJLogger(testLog);
            
            IProject project = createPredefinedProject("ProjectWithITD_Bug121810"); //$NON-NLS-1$
            createPredefinedProject("DependentProjectWithITD_Bug121810"); //$NON-NLS-1$
            
            project.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
            waitForJobsToComplete();
            
            // now the ITD should have been applied to the java file in ProjectWithITD_Bug121810
            // should get a warning
            // the ITD makes the class serializable should now have a warning on the woven class
            // that says it doesn't have a serialVersionUID field.
            IMarker[] markers = getAllProblemViewMarkers();
            boolean found = false;
            for (int i = 0; i < markers.length; i++) {
                IMarker marker = markers[i];
                String message = (String) marker.getAttribute(IMarker.MESSAGE);
                if (message != null && message.indexOf("serialVersionUID")!=-1) { //$NON-NLS-1$
                    found = true;
                    break;
                }
            }
            assertTrue("ITD has not been woven into class in separate project.", found); //$NON-NLS-1$
        } finally {
            AspectJPlugin.getDefault().setAJLogger(null);
        }
        
        
        
    }
}
