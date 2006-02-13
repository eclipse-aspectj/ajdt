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
	public void testCreateElementFromHandle() throws Exception {
		IProject project = createPredefinedProject("TJP Example"); //$NON-NLS-1$
		String expected1 = "Demo.java";
		String expected2 = "GetInfo.aj";
		try {
			List files = BuildConfig.getIncludedSourceFiles(project);
			assertNotNull("getIncludedSourceFiles return null", files);
			assertEquals(
					"getIncludedSourceFiles returned incorrect number of files",
					2, files.size());
			String name1 = ((IFile)files.get(0)).getName();
			String name2 = ((IFile)files.get(1)).getName();
			if (!(name1.equals(expected1) || name2.equals(expected1))) {
				fail("Didn't find "+expected1+" in list of source files");
			}
			if (!(name1.equals(expected2) || name2.equals(expected2))) {
				fail("Didn't find "+expected2+" in list of source files");
			}
		} finally {
			deleteProject(project);
		}
	}
}