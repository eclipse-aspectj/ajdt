/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sian January - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class CustomMarkersTest extends UITestCase {

	public void testSavedMarkerPreferences() throws CoreException {
		IProject project = createPredefinedProject("Custom Markers"); //$NON-NLS-1$
		waitForJobsToComplete();
		
		assertTrue("The example project should have been created", project != null); //$NON-NLS-1$
		IFile boundPointFile = (IFile)project.findMember("src/bean/BoundPoint.aj");		 //$NON-NLS-1$
		assertTrue("Should have found Boundpoint.aj", boundPointFile.exists()); //$NON-NLS-1$
		IFile demoFile = (IFile)project.findMember("src/bean/Demo.java");		 //$NON-NLS-1$
		assertTrue("Should have found Demo.java", demoFile.exists()); //$NON-NLS-1$
		
//        IMarker[] allMarkers = boundPointFile.findMarkers(IMarker.MARKER, true, 0);
//        for (int i = 0; i < allMarkers.length; i++) {
//            System.out.println(allMarkers[i].getType() + " : "
//                    + allMarkers[i].getAttribute(IMarker.MESSAGE));
//        }
//        allMarkers = demoFile.findMarkers(IMarker.MARKER, true, 0);
//        for (int i = 0; i < allMarkers.length; i++) {
//            System.out.println(allMarkers[i].getType() + " : "
//                    + allMarkers[i].getAttribute(IMarker.MESSAGE));
//        }
		
		IMarker[] markers = boundPointFile.findMarkers(IAJModelMarker.CUSTOM_MARKER, true, 0);
		assertEquals("BoundPoint.aj should contain a custom marker for each advice and declare declarations", 8, markers.length); //$NON-NLS-1$
		markers = boundPointFile.findMarkers(IAJModelMarker.ADVICE_MARKER, true, 0);
		assertEquals("BoundPoint.aj should contain no advice markers", 0, markers.length); //$NON-NLS-1$
		markers = boundPointFile.findMarkers(IAJModelMarker.ITD_MARKER, true, 0);
		assertEquals("BoundPoint.aj should contain no ITD markers", 0, markers.length); //$NON-NLS-1$
			
		markers = demoFile.findMarkers(IAJModelMarker.CUSTOM_MARKER, true, 0);
		assertEquals("Demo.java should contain 2 custom markers", 2, markers.length); //$NON-NLS-1$
		markers = demoFile.findMarkers(IAJModelMarker.ADVICE_MARKER, true, 0);
		assertEquals("Demo.java should contain no advice markers", 0, markers.length); //$NON-NLS-1$
		markers = demoFile.findMarkers(IAJModelMarker.ITD_MARKER, true, 0);
		assertEquals("Demo.java should contain no ITD markers", 0, markers.length); //$NON-NLS-1$						
	}
	
}
