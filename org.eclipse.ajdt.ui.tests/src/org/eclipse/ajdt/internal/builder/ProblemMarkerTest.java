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

import junit.framework.TestCase;

import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 *  Test for bug 75373: problem-markers created by other builders on java-resources disappear
 */
public class ProblemMarkerTest extends TestCase {

	private IProject myProject;

	protected void setUp() throws Exception {
		super.setUp();
		myProject = Utils.getPredefinedProject("Simple AJ Project", true);
		Utils.waitForJobsToComplete(myProject);
	}

	public void testMarkerIsNotRemoved() throws CoreException {
		IFile f = myProject.getFile("src/p1/Main.java");
		IMarker marker = f.createMarker(IMarker.PROBLEM);
		marker.setAttribute(IMarker.MESSAGE, "hello");
		myProject.build(IncrementalProjectBuilder.FULL_BUILD,
				new NullProgressMonitor());
		Utils.waitForJobsToComplete(myProject);
		assertTrue("Marker we created should still exist after a build", marker
				.exists());
	}

}