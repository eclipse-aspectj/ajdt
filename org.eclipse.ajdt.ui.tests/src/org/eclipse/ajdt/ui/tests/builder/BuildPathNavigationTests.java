package org.eclipse.ajdt.ui.tests.builder;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class BuildPathNavigationTests extends UITestCase {
	IClasspathContainer container;
    protected void setUp() throws Exception {
        super.setUp();
        final IProject containerProj = createPredefinedProject("PathTesting-ContainerOnAspectPath"); //$NON-NLS-1$
        createPredefinedProject("PathTesting-JarOnAspectPath"); //$NON-NLS-1$
        createPredefinedProject("PathTesting-ProjectOnAspectPath"); //$NON-NLS-1$
        IProject varProj = createPredefinedProject("PathTesting-VariableOnAspectPath"); //$NON-NLS-1$
        JavaCore.setClasspathVariable("Aspect_Path_Var", varProj.getFullPath(), null); //$NON-NLS-1$
		IClasspathAttribute[] attribute = new IClasspathAttribute[] {AspectJCorePreferences.ASPECTPATH_ATTRIBUTE };
		final IClasspathEntry entry = 
				JavaCore.newProjectEntry(
						containerProj.getFullPath(), 
						null, false, 
						attribute, 
						false);
		
		container = new IClasspathContainer() {
			public IClasspathEntry[] getClasspathEntries() {
				return new IClasspathEntry[] { entry };
			}
			public String getDescription() {
				return "org.eclipse.jdt.USER_LIBRARY/Aspect_Path_Lib"; //$NON-NLS-1$
			}
			public int getKind() {
			      return IClasspathContainer.K_APPLICATION;
			}
			public IPath getPath() {
				return new Path("org.eclipse.jdt.USER_LIBRARY/Aspect_Path_Lib"); //$NON-NLS-1$
			}
		};
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
    public void testInpathMarkers() throws Exception {
        try {
            IProject inpathProj = createPredefinedProject("PathTesting-HasInpath"); //$NON-NLS-1$
    		JavaCore.setClasspathContainer(container.getPath(), new IJavaProject[] { JavaCore.create(inpathProj) }, 
    				new IClasspathContainer[] { container }, null);

            getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
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
    		JavaCore.setClasspathContainer(container.getPath(), new IJavaProject[] { JavaCore.create(aspectPathProj) }, 
    				new IClasspathContainer[] { container }, null);

    		getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
            waitForJobsToComplete();
            IFile aspectFile = aspectPathProj.getFile("src/main/Main.java"); //$NON-NLS-1$
            
            IMarker[] markers = aspectFile.findMarkers(IMarker.MARKER, true, 2);
            
            System.out.println(getAllProblemViewMarkers()[0].getAttribute(IMarker.MESSAGE));
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
