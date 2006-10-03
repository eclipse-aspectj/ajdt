/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Matt Chapman   - initial version
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.builder;

import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.IJavaModelMarker;

public class Bug159197Test extends AJDTCoreTestCase {

	public void testMissingAspectPath() throws Exception {
		IProject project = createPredefinedProject("WeaveMe"); //$NON-NLS-1$
		IMarker[] markers = project.findMarkers(
				IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false,
				IResource.DEPTH_ZERO);
		assertTrue(
				"Project should have an error marker indicating missing aspect path entry", //$NON-NLS-1$
				markers.length > 0);
		boolean found = false;
		for (int i = 0; !found && i < markers.length; i++) {
			int sev = markers[i].getAttribute(IMarker.SEVERITY, -1);
			if (sev == IMarker.SEVERITY_ERROR) {
				found = true;
			}
		}
		assertTrue(
				"Project should have an error marker indicating missing aspect path entry", //$NON-NLS-1$
				found);
	}

	public void testMissingInpath() throws Exception {
		IProject project = createPredefinedProject("CorePreferencesTestProject"); //$NON-NLS-1$
		IMarker[] markers = project.findMarkers(
				IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false,
				IResource.DEPTH_ZERO);
		boolean found = false;
		for (int i = 0; !found && i < markers.length; i++) {
			int sev = markers[i].getAttribute(IMarker.SEVERITY, -1);
			if (sev == IMarker.SEVERITY_ERROR) {
				found = true;
			}
		}
		assertFalse("Project should have no error markers", found); //$NON-NLS-1$

		// test.jar is on the inpath - delete it so that it can't be found
		IResource test = project.findMember("test.jar"); //$NON-NLS-1$
		assertNotNull("Couldn't find test.jar", test); //$NON-NLS-1$
		test.delete(true, null);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);

		markers = project.findMarkers(
				IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false,
				IResource.DEPTH_ZERO);
		found = false;
		for (int i = 0; !found && i < markers.length; i++) {
			int sev = markers[i].getAttribute(IMarker.SEVERITY, -1);
			if (sev == IMarker.SEVERITY_ERROR) {
				found = true;
			}
		}
		assertTrue(
				"Project should now have an error marker indicating missing inpath entry", //$NON-NLS-1$
				found);
	}
}
