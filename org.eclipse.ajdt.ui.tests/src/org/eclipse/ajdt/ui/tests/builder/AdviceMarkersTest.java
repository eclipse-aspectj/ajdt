/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Test advice markers are added correctly
 */
public class AdviceMarkersTest extends UITestCase {

	private IProject myProject;

	protected void setUp() throws Exception {
		super.setUp();
		myProject = createPredefinedProject("Simple AJ Project");
	}
	
	public void testMarkersAreAdded() throws Exception {
		//myProject.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
		//waitForJobsToComplete();
		assertTrue("Simple AJ Project should contain 4 advice markers after building", myProject.findMarkers(IAJModelMarker.ADVICE_MARKER, true,IResource.DEPTH_INFINITE).length == 2);
	}

	
}
