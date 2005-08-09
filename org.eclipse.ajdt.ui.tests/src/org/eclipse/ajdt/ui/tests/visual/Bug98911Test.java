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
package org.eclipse.ajdt.ui.tests.visual;

import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.swt.SWT;


public class Bug98911Test extends VisualTestCase {

	/**
	 * Test for bug 98911 
	 */
	public void testBug98911() throws Exception {
		final IProject project = Utils.createPredefinedProject("Simple AJ Project");
		assertTrue("The Simple AJ Project should have been created", project != null);
		AJDTUtils.removeAspectJNature(project);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
		IFolder outputDirectory = project.getFolder("bin");
		assertTrue("Should have found the output directory", outputDirectory.exists());
		IResource ajFile = outputDirectory.findMember("p2/Aspect.aj");
		assertTrue(".aj files should have been copied to the output directory", ajFile != null);
		
		Runnable r = new Runnable() {
			public void run() {
				sleep();
				postCharacterKey(SWT.CR);
			}
		};
		new Thread(r).start();
		AJDTUtils.addAspectJNature(project);
		Utils.waitForJobsToComplete();
		
		IResource ajFile2 = outputDirectory.findMember("p2/Aspect.aj");
		assertTrue(".aj files should have been deleted from the output directory", ajFile2 == null);
		
	}
	
	/**
	 * Second test for bug 98911 
	 */
	public void testBug98911again() throws Exception {
		final IProject project = Utils.createPredefinedProject("Figures Demo");
		assertTrue("The Figures Demo project should have been created", project != null);
		AJDTUtils.removeAspectJNature(project);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete();
		IResource ajFile = project.findMember("figures/support/SaveAndRestore.aj");
		assertTrue("Should have found file 'SaveAndRestore.aj'", ajFile != null);
		
		Runnable r = new Runnable() {
			public void run() {
				sleep();
				postCharacterKey(SWT.CR);
			}
		};
		new Thread(r).start();
		AJDTUtils.addAspectJNature(project);
		Utils.waitForJobsToComplete();
		
		IResource ajFile2 = project.findMember("figures/support/SaveAndRestore.aj");
		assertTrue("Should not have deleted any .aj files", ajFile2 != null);
		
	}
}

