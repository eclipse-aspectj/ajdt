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

import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class Bug102493Test extends VisualTestCase {

	// Test for bug 102493 - adding a source folder should not include all other
	// source folders in the build
	public void testBug102493() throws Exception {
		// Create the example project
		final IProject project = createPredefinedProject("MultipleSourceFolders");
		assertTrue(
				"The MultipleSourceFolders project should have been created",
				project != null);

		// Exclude the second source folder from the build configuration
		PackageExplorerPart packageExplorer = PackageExplorerPart
				.getFromActivePerspective();
		packageExplorer.setFocus();
		IFolder src2 = project.getFolder("src2");
		assertTrue("Should have found a folder called src2", src2.exists());
		final IFile class3 = src2.getFile("pack/Class3.java");
		assertTrue("Should have found a file called Class3.java", class3
				.exists());
		assertTrue("Class3 should be included in the build", BuildConfigurator
				.getBuildConfigurator().getProjectBuildConfigurator(project)
				.getActiveBuildConfiguration().isIncluded(class3));

		packageExplorer.setFocus();
		packageExplorer.tryToReveal(src2);

		IFile buildFile = (IFile) project
				.findMember(BuildConfiguration.STANDARD_BUILD_CONFIGURATION_FILE);
		openFileInDefaultEditor(buildFile, true);
		waitForJobsToComplete();
		Runnable r = new Runnable() {

			public void run() {
				sleep();
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(' ');
				sleep();
				postKeyDown(SWT.CTRL);
				postKey('s');
				postKeyUp(SWT.CTRL);
			}
		};
		new Thread(r).start();
		waitForJobsToComplete();

		new DisplayHelper() {
			protected boolean condition() {
				return !BuildConfigurator.getBuildConfigurator()
						.getProjectBuildConfigurator(project)
						.getActiveBuildConfiguration().isIncluded(class3);
			}

		}.waitForCondition(Display.getDefault(), 5000);

		assertFalse("Class3 should not be included in the build",
				BuildConfigurator.getBuildConfigurator()
						.getProjectBuildConfigurator(project)
						.getActiveBuildConfiguration().isIncluded(class3));

		// Add a third source folder
		postKeyDown(SWT.ALT);
		postKeyDown(SWT.SHIFT);
		postKey('n');
		postKeyUp(SWT.SHIFT);
		postKeyUp(SWT.ALT);

		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.CR);

		r = new Runnable() {

			public void run() {
				sleep();
				postString("src3");
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		waitForJobsToComplete();

		// Check that src2 is still excluded from the build
		IFolder src3 = project.getFolder("src3");
		assertTrue("Should have found a folder called src3", src3.exists());
		assertFalse("Class3 should not be included in the build",
				BuildConfigurator.getBuildConfigurator()
						.getProjectBuildConfigurator(project)
						.getActiveBuildConfiguration().isIncluded(class3));

	}

}
