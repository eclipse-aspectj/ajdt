/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Andrew Eisenberg - Initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;


public class AdviceMarkersTest6 extends UITestCase {

    public void testBug235204() throws Exception {
        IProject project = createPredefinedProject("Bug235204-Recursive Calls"); //$NON-NLS-1$
        assertTrue("The example project should have been created", project != null); //$NON-NLS-1$
        
        // Find the file
        IFile aspectFile = (IFile) project.findMember("src/recursive/RecursiveCatcher.aj");      //$NON-NLS-1$
        
        assertTrue("Should have found RecursiveCatcher.aj", aspectFile.exists()); //$NON-NLS-1$
        
        openFileInDefaultEditor(aspectFile, false);

        // wait for annotation model to be created
        waitForJobsToComplete();

        IMarker[] markers = getMarkers(aspectFile);
        assertEquals("Didn't find any advice markers, but should have found 3", 3, markers.length); //$NON-NLS-1$

        Set lineNumbers = new HashSet();
        lineNumbers.add(new Integer(markers[0].getAttribute(IMarker.LINE_NUMBER, -1)));
        lineNumbers.add(new Integer(markers[1].getAttribute(IMarker.LINE_NUMBER, -1)));
        lineNumbers.add(new Integer(markers[2].getAttribute(IMarker.LINE_NUMBER, -1)));
        
        assertTrue("Advice marker is on wrong line number.  Should be at the call site, not execution site", //$NON-NLS-1$
                lineNumbers.contains(new Integer(22)));
        assertTrue("Advice marker is on wrong line number.  Should be at the call site, not execution site", //$NON-NLS-1$
                lineNumbers.contains(new Integer(23)));
        assertTrue("Advice marker is on wrong line number.  Should be at the call site, not execution site", //$NON-NLS-1$
                lineNumbers.contains(new Integer(24)));
          
    }
    
    protected IMarker[] getMarkers(IResource resource)
            throws Exception {
            return resource.findMarkers(
                    IAJModelMarker.BEFORE_ADVICE_MARKER, true,
                    IResource.DEPTH_INFINITE);
        
    }
    
}
