/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.tests;

import java.util.List;

import org.eclipse.ajdt.core.BuildConfig;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Tests for BuildConfig
 */
public class BuildConfigTest extends AJDTCoreTestCase {

	/**
	 * Test that getIncludedSourceFiles returns the complete set of source files
	 * in a project
	 * 
	 * @throws Exception
	 */
	public void testGetIncludedSourceFiles() throws Exception {
		IProject project = createPredefinedProject("TJP Example"); //$NON-NLS-1$
		String expected1 = "Demo.java"; //$NON-NLS-1$
		String expected2 = "GetInfo.aj"; //$NON-NLS-1$
		try {
			List files = BuildConfig.getIncludedSourceFiles(project);
			assertNotNull("getIncludedSourceFiles return null", files); //$NON-NLS-1$
			assertEquals(
					"getIncludedSourceFiles returned incorrect number of files", //$NON-NLS-1$
					2, files.size());
			String name1 = ((IFile) files.get(0)).getName();
			String name2 = ((IFile) files.get(1)).getName();
			if (!(name1.equals(expected1) || name2.equals(expected1))) {
				fail("Didn't find " + expected1 + " in list of source files");  //$NON-NLS-1$//$NON-NLS-2$
			}
			if (!(name1.equals(expected2) || name2.equals(expected2))) {
				fail("Didn't find " + expected2 + " in list of source files"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} finally {
			deleteProject(project);
		}
	}

	/**
	 * Test that isIncluded returns true for source files on the build path,
	 * false otherwise
	 * 
	 * @throws Exception
	 */
	public void testIsIncluded() throws Exception {
		IProject project = createPredefinedProject("TJP Example"); //$NON-NLS-1$
		try {
			IResource demo = project.findMember("src/tjp/Demo.java"); //$NON-NLS-1$
			assertNotNull("Couldn't find Demo.java file", demo); //$NON-NLS-1$
			assertTrue("Demo.java should be included", BuildConfig //$NON-NLS-1$
					.isIncluded(demo));
			IResource getinfo = project.findMember("src/tjp/GetInfo.aj"); //$NON-NLS-1$
			assertNotNull("Couldn't find GetInfo.aj file", getinfo); //$NON-NLS-1$
			assertTrue("GetInfo.aj should be included", BuildConfig //$NON-NLS-1$
					.isIncluded(getinfo));
			IResource ajprop = project.findMember("build.ajproperties"); //$NON-NLS-1$
			assertNotNull("Couldn't find build.ajproperties file", ajprop); //$NON-NLS-1$
			assertFalse("build.ajproperties should NOT be included", //$NON-NLS-1$
					BuildConfig.isIncluded(ajprop));
		} finally {
			deleteProject(project);
		}
	}
}