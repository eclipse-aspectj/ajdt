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

import java.io.StringReader;

import org.eclipse.ajdt.internal.ui.refactoring.ReaderInputStream;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
 
public class Bug128803Test extends UITestCase {

	public void testBug128803() throws Exception {
		IProject project = createPredefinedProject("bug128803"); //$NON-NLS-1$
		waitForJobsToComplete();

		String filename = "src/pkg/C.java"; //$NON-NLS-1$
		IResource fileC = project.findMember(filename);
		if (fileC == null)
			fail("Required file not found: " + filename); //$NON-NLS-1$
		filename =  "src/pkg/A.aj"; //$NON-NLS-1$
		IResource fileA = project.findMember(filename);
		if (fileA == null)
			fail("Required file not found: " + filename); //$NON-NLS-1$
		IMarker[] markers = getAllProblemViewMarkers();
		assertEquals("There should be one problem in the project", 1, markers.length); //$NON-NLS-1$
		String newContents = "package pkg; \npublic class C {\n\n\n}"; //$NON-NLS-1$
		((IFile)fileC).setContents(new ReaderInputStream(new StringReader(newContents)), true, true, null);
		fileA.refreshLocal(1, null);
		waitForJobsToComplete();
		assertEquals("There should still be one problem in the project", 1, markers.length); //$NON-NLS-1$			
	}
	
}
