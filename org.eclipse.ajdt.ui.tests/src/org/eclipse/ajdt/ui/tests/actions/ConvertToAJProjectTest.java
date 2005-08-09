/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.actions;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.ajdt.ui.tests.AllUITests;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;


public class ConvertToAJProjectTest extends TestCase {

	/**
	 * Test for bug 98911 - .aj files should not be copied to the output folder in a java
	 * project.  Note that we didn't do anything to fix it in Eclipse 3.1, 
	 * but should do something if the test ever fails.
	 */
	public void testBug98911() throws Exception {
		AllUITests.setupAJDTPlugin();
		IProject project = Utils.createPredefinedProject("Simple AJ Project");
		assertTrue("The Simple AJ Project should have been created", project != null);
		AJDTUtils.removeAspectJNature(project);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
		IFolder outputDirectory = project.getFolder("bin");
		assertTrue("Should have found the output directory", outputDirectory.exists());
		IResource ajFile = outputDirectory.findMember("p2/Aspect.aj");
		assertTrue(".aj files should not have been copied to the output directory", ajFile == null);		
	}
	
}
