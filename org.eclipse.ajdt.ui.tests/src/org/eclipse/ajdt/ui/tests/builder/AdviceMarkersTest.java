/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.ajdt.ui.tests.testutils.SynchronizationUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Test advice markers are added correctly
 */
public class AdviceMarkersTest extends UITestCase {

	private IProject myProject;

	protected void setUp() throws Exception {
		super.setUp();
		myProject = createPredefinedProject("Simple AJ Project"); //$NON-NLS-1$
	}
	public void testMarkersAreAdded() throws Exception {
		myProject.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
		SynchronizationUtils.joinBackgroudActivities();
		assertEquals("Simple AJ Project should contain 2 advice markers after building", 2, myProject.findMarkers(IAJModelMarker.ADVICE_MARKER, true,IResource.DEPTH_INFINITE).length); //$NON-NLS-1$
		assertEquals("Simple AJ Project should contain 2 source advice markers after building", 2, myProject.findMarkers(IAJModelMarker.SOURCE_ADVICE_MARKER, true,IResource.DEPTH_INFINITE).length); //$NON-NLS-1$
	}

	
}
