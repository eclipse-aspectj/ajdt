package org.eclipse.ajdt.ui.tests.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.eclipse.ajdt.internal.ui.refactoring.ReaderInputStream;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Tests bug 255332 that if there is a change to an aspect, then all classes it weaves into 
 * also have their model properly updated.
 * @author andrew
 *
 */
public class Bug255332Test extends UITestCase {
    
    IProject proj;
    IFile anAspectFile;
    IFile aClassFile;
    
    protected void setUp() throws Exception {
        super.setUp();
        proj = createPredefinedProject("AutoBuildAspects-Bug255332");
        
        anAspectFile = proj.getFile("src/a/AnAspect.aj");
        aClassFile = proj.getFile("src/b/AClass.java");
        
//        waitForJobsToComplete();
//        proj.build(IncrementalProjectBuilder.AUTO_BUILD, null);
    }

    
    public void testChangeAspectMeansChangeInClassMarkers() throws Exception {
        IMarker[] classMarkers;
        IMarker[] aspectMarkers;
        
        classMarkers = aClassFile.findMarkers(IAJModelMarker.ADVICE_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Should start with 1 marker in the class file, but found " + classMarkers.length, 1, classMarkers.length); //$NON-NLS-1$

        aspectMarkers = anAspectFile.findMarkers(IAJModelMarker.SOURCE_ADVICE_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Should start with 1 marker in the class file, but found " + aspectMarkers.length, 1, aspectMarkers.length); //$NON-NLS-1$

        // change class so that none of them apply
        String origContents = getContents(anAspectFile);
        setContents(origContents.replace("nothing", "noothing"));

        classMarkers = aClassFile.findMarkers(IAJModelMarker.ADVICE_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Should have no advice markers in the class file after the change, but found " + classMarkers.length, 0, classMarkers.length); //$NON-NLS-1$

        aspectMarkers = aClassFile.findMarkers(IAJModelMarker.SOURCE_ADVICE_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Should have no advice markers in the aspect file after the change, but found " + aspectMarkers.length, 0, aspectMarkers.length); //$NON-NLS-1$

        // change back
        setContents(origContents);
        
        classMarkers = aClassFile.findMarkers(IAJModelMarker.ADVICE_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Should have 1 marker in the class file after change, but found " + classMarkers.length, 1, classMarkers.length); //$NON-NLS-1$

        aspectMarkers = anAspectFile.findMarkers(IAJModelMarker.SOURCE_ADVICE_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Should have 1 marker in the class file after change, but found " + aspectMarkers.length, 1, aspectMarkers.length); //$NON-NLS-1$
    }


    private void setContents(String newContents) throws CoreException {
        anAspectFile.setContents(new ReaderInputStream(new StringReader(
                newContents)),  //$NON-NLS-1$
                true, false, null);
        anAspectFile.refreshLocal(IResource.DEPTH_INFINITE, null);
        waitForJobsToComplete();
    }
    
    private String getContents(IFile javaFile) throws CoreException, IOException {
        InputStream is = javaFile.getContents();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer= new StringBuffer();
        char[] readBuffer= new char[2048];
        int n= br.read(readBuffer);
        while (n > 0) {
            buffer.append(readBuffer, 0, n);
            n= br.read(readBuffer);
        }
        return buffer.toString();
    }

}
