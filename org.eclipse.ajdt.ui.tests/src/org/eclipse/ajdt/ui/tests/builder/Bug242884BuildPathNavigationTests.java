package org.eclipse.ajdt.ui.tests.builder;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * These tests are not working yet.  Bug has not been addressed
 */
public class Bug242884BuildPathNavigationTests extends UITestCase {

	

    protected void setUp() throws Exception {
        super.setUp();
        createPredefinedProject("PathTesting-ContainerOnAspectPath"); //$NON-NLS-1$
        createPredefinedProject("PathTesting-JarOnAspectPath"); //$NON-NLS-1$
        createPredefinedProject("PathTesting-ProjectOnAspectPath"); //$NON-NLS-1$
        createPredefinedProject("PathTesting-VariableOnAspectPath"); //$NON-NLS-1$
    }

    
    /**
     * Test to see that the extra location attributes are associated with advice markers for inpaths
     * 
     * This attribute has the format FFFF:::NNNN:::NNNN:::NNNN
     * - The FFFF is the file which contains the source of the advice or ITD in affect
     * - The other three NNNN fields are integers indicating (in order) the
     *   start line number of the advice in that file, the end line number of the
     *   advice in that file and the column number for the advice.
     */
    public void testInpathMarkers() {
        try {
            IProject inpathProj = createPredefinedProject("PathTesting-HasInpath"); //$NON-NLS-1$
            waitForJobsToComplete();
            IFile aspectFile = inpathProj.getFile("src/aspects/AdviseClassOnInpath.aj"); //$NON-NLS-1$
            
            IMarker[] markers = aspectFile.findMarkers(IMarker.MARKER, true, 2);
            
            // should have one marker for each advice (ie- 5)
            assertEquals("Invalid number of markers on file " + aspectFile, 5, markers.length); //$NON-NLS-1$
            
            // each marker should have extra information attached to it
            for (int i = 0; i < markers.length; i++) {
                IMarker marker = markers[i];
                String loc = (String) marker.getAttribute(AspectJUIPlugin.RELATED_LOCATIONS_ATTRIBUTE_PREFIX + 0);
                assertNotNull("No location attached to marker number " + i, loc); //$NON-NLS-1$
                
                String[] s = loc.split(":::"); //$NON-NLS-1$
                assertEquals("Invalid extra location attribute format\n    " + loc, 4, s.length); //$NON-NLS-1$
            }
        } catch (CoreException e) {
            fail();
        }
    }

    /**
     * Test to see that the extra location attributes are associated with advice markers for aspect paths
     * 
     * This attribute has the format FFFF:::NNNN:::NNNN:::NNNN
     * - The FFFF is the file which contains the source of the advice or ITD in affect
     * - The other three NNNN fields are integers indicating (in order) the
     *   start line number of the advice in that file, the end line number of the
     *   advice in that file and the column number for the advice.
     */
    public void testAspectPathMarkers() {
        try {
            IProject aspectPathProj = createPredefinedProject("PathTesting-HasAspectPath"); //$NON-NLS-1$
            waitForJobsToComplete();
            IFile aspectFile = aspectPathProj.getFile("src/main/Main.java"); //$NON-NLS-1$
            
            IMarker[] markers = aspectFile.findMarkers(IMarker.MARKER, true, 2);
            
            // should have one marker for each advised method (ie- 4)
            assertEquals("Invalid number of markers on file " + aspectFile, 4, markers.length); //$NON-NLS-1$
            
            // each marker should have extra information attached to it
            for (int i = 0; i < markers.length; i++) {
                IMarker marker = markers[i];
                String loc = (String) marker.getAttribute(AspectJUIPlugin.RELATED_LOCATIONS_ATTRIBUTE_PREFIX + 0);
                assertNotNull("No location attached to marker number " + i, loc); //$NON-NLS-1$
                
                String[] s = loc.split(":::"); //$NON-NLS-1$
                assertEquals("Invalid extra location attribute format\n    " + loc, 4, s.length); //$NON-NLS-1$
            }
        } catch (CoreException e) {
            fail();
        }
    }
}
