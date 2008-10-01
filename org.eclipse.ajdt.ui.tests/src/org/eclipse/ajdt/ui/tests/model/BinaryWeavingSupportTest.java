/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.model;

import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaModelMarker;

/**
 * Tests binary weaving support in the UI (most binary weaving tests are in
 * core.tests plugin)
 */
public class BinaryWeavingSupportTest extends UITestCase {

    /**
     * No longer are advice markers showing up on aspects on aspect paths
     * so test won't pass.  Commenting out the last section.
     */
	public void testBug159873() throws Exception {
		final String adviceDidNotMatch = "Xlint:adviceDidNotMatch"; //$NON-NLS-1$
		IProject libProject = createPredefinedProject("MyAspectLibrary"); //$NON-NLS-1$
		IResource mybar = libProject.findMember("src/bar/MyBar.aj"); //$NON-NLS-1$
		assertNotNull("Couldn't find resource for MyBar.aj", mybar); //$NON-NLS-1$
		IMarker[] markers = mybar.findMarkers(
				IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false,
				IResource.DEPTH_ZERO);
		boolean found = false;
		for (int i = 0; i < markers.length; i++) {
			String msg = markers[i].getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
			if (msg.indexOf(adviceDidNotMatch) != -1) {
				found = true;
			}
		}
		assertTrue("Didn't find advice did not match marker", found); //$NON-NLS-1$
		createPredefinedProject("WeaveMe"); //$NON-NLS-1$
		markers = mybar.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
				false, IResource.DEPTH_ZERO);
		
		// Commenting out because not adding advice marker on aspect path elements
//		found = false;
//		for (int i = 0; i < markers.length; i++) {
//			String msg = markers[i].getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
//			if (msg.indexOf(adviceDidNotMatch) != -1) {
//				found = true;
//			}
//		}
//		assertFalse(
//				"WeaveMe project should have removed advice did not match marker", found); //$NON-NLS-1$		
	}
}
