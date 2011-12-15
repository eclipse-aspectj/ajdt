/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.javamodel;

import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.ajdt.ui.tests.testutils.SynchronizationUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class Bug117327Test extends UITestCase {

	public void testMarkersForAspectInJava() throws CoreException {
		IProject project = createPredefinedProject("bug117327"); //$NON-NLS-1$
		String filename = "C.java"; //$NON-NLS-1$
		IResource file = project.findMember(filename);
		if (file == null)
			fail("Required file not found: " + filename); //$NON-NLS-1$
		SynchronizationUtils.joinBackgroudActivities();
		IMarker[] markers = file.findMarkers(IAJModelMarker.DECLARATION_MARKER, true,
					IResource.DEPTH_INFINITE);
		assertEquals("Expected two declaration markers", 2, markers.length); //$NON-NLS-1$
		Integer lineNumber = (Integer)markers[0].getAttribute(IMarker.LINE_NUMBER);
		assertTrue("Expected a declaration marker at line 15 or 18", 15 == lineNumber.intValue() || 18 == lineNumber.intValue()); //$NON-NLS-1$
		if(lineNumber.intValue() == 15) {
			lineNumber = (Integer)markers[1].getAttribute(IMarker.LINE_NUMBER);
			assertEquals("Expected a declaration marker at line 18", 18, lineNumber.intValue()); //$NON-NLS-1$				
		} else {
			lineNumber = (Integer)markers[1].getAttribute(IMarker.LINE_NUMBER);
			assertEquals("Expected a declaration marker at line 15", 15, lineNumber.intValue()); //$NON-NLS-1$	
		}
	}
	
	
}
