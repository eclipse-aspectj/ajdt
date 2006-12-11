/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.ajde;

import org.aspectj.bridge.AbortException;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.ajde.AJDTErrorHandler;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaModelMarker;

/**
 * Tests the ErrorHandler class
 */
public class ErrorHandlerTest extends UITestCase {

	public void testHandleWarning() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		AspectJPlugin.getDefault().setCurrentProject(project);
		AspectJUIPlugin.getDefault().getErrorHandler().handleWarning("fake warning"); //$NON-NLS-1$
		waitForJobsToComplete();
		IMarker[] markers = project.findMarkers(
				IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
				true,
				IResource.DEPTH_INFINITE);
		boolean foundFakeWarning = false;
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			if (marker.getAttribute(IMarker.MESSAGE).equals("fake warning")) { //$NON-NLS-1$
				foundFakeWarning = true;
			}
		}
		assertTrue("expected to handle AspectJ warning by adding a marker" + //$NON-NLS-1$
				" to the project, but couldn't find marker",foundFakeWarning); //$NON-NLS-1$
	}
	
	public void testHandleErrorWithMessage() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		AspectJPlugin.getDefault().setCurrentProject(project);
		AJDTErrorHandler.setShowErrorDialogs(false);
		String message = ""; //$NON-NLS-1$
		try {
			AspectJUIPlugin.getDefault().getErrorHandler().handleError("fake error"); //$NON-NLS-1$
		} catch (RuntimeException re) {
			message = re.getMessage();
		}
		assertTrue("expected a runtime error with message 'fake error' when " + //$NON-NLS-1$
				" testing error handling but didn't find one",  //$NON-NLS-1$
				message.equals("fake error")); //$NON-NLS-1$
	}
	
	public void testHandleErrorWithMessageAndThrowable() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		AspectJPlugin.getDefault().setCurrentProject(project);
		AJDTErrorHandler.setShowErrorDialogs(false);
		String message = ""; //$NON-NLS-1$
		try {
			AspectJUIPlugin.getDefault().getErrorHandler().handleError("fake abort", new AbortException("fake abort")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (RuntimeException re) {
			message = re.getMessage();
		}
		assertTrue("expected a runtime error with message 'fake error' when " + //$NON-NLS-1$
				" testing error handling but didn't find one",  //$NON-NLS-1$
				message.equals("org.aspectj.bridge.AbortException: fake abort")); //$NON-NLS-1$
	}
	
}
