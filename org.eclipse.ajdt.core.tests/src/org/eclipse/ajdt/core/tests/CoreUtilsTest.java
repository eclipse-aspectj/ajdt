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
package org.eclipse.ajdt.core.tests;

import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class CoreUtilsTest extends AJDTCoreTestCase {

	public void testGetOutputFoldersMultiple() throws Exception {
		IProject project = createPredefinedProject("MultipleOutputFolders"); //$NON-NLS-1$
		IJavaProject jp = JavaCore.create(project);
		IPath[] out = CoreUtils.getOutputFolders(jp);
		assertNotNull("CoreUtils.getOutputFolders should not return null", //$NON-NLS-1$
				out);
		assertEquals(
				"CoreUtils.getOutputFolders should have returned 2 folders", 2, //$NON-NLS-1$
				out.length);
		boolean bin2 = out[0].toString().endsWith("bin2"); //$NON-NLS-1$
		if (bin2) {
			assertTrue("CoreUtils.getOutputFolders didn't return bin folder", //$NON-NLS-1$
					out[1].toString().endsWith("bin")); //$NON-NLS-1$
		} else {
			assertTrue("CoreUtils.getOutputFolders didn't return bin folder", //$NON-NLS-1$
					out[0].toString().endsWith("bin")); //$NON-NLS-1$
			assertTrue("CoreUtils.getOutputFolders didn't return bin2 folder", //$NON-NLS-1$
					out[1].toString().endsWith("bin2")); //$NON-NLS-1$
		}
	}

	public void testGetOutputFoldersRoot() throws Exception {
		IProject project = createPredefinedProject("bug102652"); //$NON-NLS-1$
		IJavaProject jp = JavaCore.create(project);
		IPath[] out = CoreUtils.getOutputFolders(jp);
		assertNotNull("CoreUtils.getOutputFolders should not return null", out); //$NON-NLS-1$
		assertEquals(
				"CoreUtils.getOutputFolders should have returned 1 folder", 1, //$NON-NLS-1$
				out.length);
		assertTrue("CoreUtils.getOutputFolders didn't return project root", //$NON-NLS-1$
				out[0].toString().endsWith("bug102652")); //$NON-NLS-1$
	}

	public void testGetOutputFoldersDefault() throws Exception {
		IProject project = createPredefinedProject("TJP Example"); //$NON-NLS-1$
		IJavaProject jp = JavaCore.create(project);
		IPath[] out = CoreUtils.getOutputFolders(jp);
		assertNotNull("CoreUtils.getOutputFolders should not return null", out); //$NON-NLS-1$
		assertEquals(
				"CoreUtils.getOutputFolders should have returned 1 folder", 1, //$NON-NLS-1$
				out.length);
		assertTrue("CoreUtils.getOutputFolders didn't return bin folder", //$NON-NLS-1$
				out[0].toString().endsWith("bin")); //$NON-NLS-1$
	}
}
