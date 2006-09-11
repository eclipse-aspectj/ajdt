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

import java.util.List;

import org.eclipse.ajdt.core.AJProperties;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * Tests for AJProperties
 */
public class AJPropertiesTest extends AJDTCoreTestCase {

	/**
	 * Test that getIncludedSourceFiles returns the complete set of source files
	 * in a project
	 * 
	 * @throws Exception
	 */
	public void testGetAJPropertiesFiles() throws Exception {
		IProject project = createPredefinedProject("Spacewar Example"); //$NON-NLS-1$
		List props = AJProperties.getAJPropertiesFiles(project);
		assertEquals("Project should contain two .ajproperties files", 2, //$NON-NLS-1$
				props.size());
		String name1 = ((IFile) props.get(0)).getName();
		String name2 = ((IFile) props.get(1)).getName();
		if (!(name1.equals("demo.ajproperties") || name2.equals("demo.ajproperties"))) { //$NON-NLS-1$//$NON-NLS-2$
			fail("getAJPropertiesFiles didn't return demo.ajproperties"); //$NON-NLS-1$
		}
		if (!(name1.equals("debug.ajproperties") || name2.equals("debug.ajproperties"))) { //$NON-NLS-1$ //$NON-NLS-2$
			fail("getAJPropertiesFiles didn't return debug.ajproperties"); //$NON-NLS-1$
		}
	}

}