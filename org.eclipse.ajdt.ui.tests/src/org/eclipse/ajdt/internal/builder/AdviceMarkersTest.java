/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/
package org.eclipse.ajdt.internal.builder;

import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;

import junit.framework.TestCase;

/**
 * Test advice markers are added correctly
 */
public class AdviceMarkersTest extends TestCase {

	private IProject myProject;

	protected void setUp() throws Exception {
		super.setUp();
		myProject = Utils.getPredefinedProject("Simple AJ Project", true);
		Utils.waitForJobsToComplete(myProject);
	}
	
	public void testMarkersAreAdded() throws Exception {
		myProject.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
		Utils.waitForJobsToComplete(myProject);
		assertTrue("Simple AJ Project should contain 4 advice markers after building", myProject.findMarkers(IAJModelMarker.ADVICE_MARKER, true,IResource.DEPTH_INFINITE).length == 4);
	}
	
	
	protected void tearDown() throws Exception {
		super.tearDown();
		myProject.delete(true, new NullProgressMonitor());
	}
	
}
