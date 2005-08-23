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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * Build configuration visual tests
 */
public class BuildConfigurationTest2 extends VisualTestCase {

	/**
	 * Build configuration test
	 * @throws Exception
	 */
	public void test2() throws Exception {
		// Open the 'New' wizard
		postKeyDown(SWT.CTRL);		
		postKey('n');
		postKeyUp(SWT.CTRL);
		
		Runnable r = new Runnable() {
			public void run() {
				sleep();
				
				// Open the 'New AspectJ Project' wizard
				postKey(SWT.CR);

				sleep();
				
				// Enter a name for the project
				postString("Project1");
		
				// Go to the next page
				postKeyDown(SWT.ALT);
				postKey('n');
				postKeyUp(SWT.ALT);	
				sleep();
				
				// Add a source folder called src
				postKey('a');
				postString("src");
				sleep();
				postKey(SWT.CR);
				sleep();
				postKey(SWT.CR);		
				sleep();
				
				// Complete the wizard
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		
		// Wait for the project to be created
		waitForJobsToComplete();
		
		final IWorkspace workspace= JavaPlugin.getWorkspace();		
		
		new DisplayHelper() {

			protected boolean condition() {
				boolean ret = workspace.getRoot().getProject("Project1").exists();
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);

		IProject project = workspace.getRoot().getProject("Project1");

		assertTrue("Should have created a project", project.exists());	
		
		// Test that a build file has been created
		IFile buildFile = checkBuildFileExists(project);
		
		// Test that the new build file has the correct contents
		checkOriginalContents(buildFile);

		// Test that adding a new folder (not source folder) does not alter the build file
		addNewFolderAndCheckBuildFile(buildFile);
		
		// Test that when the new folder is added to the build path the build file updates correctly
		addFolderToBuildPathAndCheckBuildFile("src2", project, buildFile);
		
		// Test that when the new folder is removed from the build path the build file updates correctly
		removeFolderFromBuildPathAndCheckBuildFile("src2", project, buildFile);
	}

	private void addFolderToBuildPathAndCheckBuildFile(String folderName, IProject project,  IFile buildFile) throws CoreException, IOException {
		PackageExplorerPart packageExplorer = PackageExplorerPart.getFromActivePerspective();
		packageExplorer.setFocus();
		if (runningEclipse31) {
			IResource folder = project.findMember(folderName);
			if (!(folder instanceof IFolder)) {
				fail("Folder \"" + folderName + "\" should have been found in the project");
			}
			packageExplorer.tryToReveal(folder);
			postKeyDown(SWT.SHIFT);
			postKey(SWT.F10);
			postKeyUp(SWT.SHIFT);
			postKey('b');
			postKey(SWT.CR);
		} else {
			packageExplorer.tryToReveal(project);
			postKeyDown(SWT.ALT);
			postKey(SWT.CR);
			postKeyUp(SWT.ALT);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			Runnable r = new Runnable() {
				public void run() {
					sleep();
					postKey(SWT.TAB);
					postKey(SWT.ARROW_LEFT);
					postKey(SWT.ARROW_LEFT);
					postKey(SWT.ARROW_LEFT);
					postKey('a');
					sleep();
					postKey('s');
					postKey('s');
					postKey(' ');
					postKey(SWT.CR);
					sleep();
					postKey(SWT.TAB);
					postKey(SWT.TAB);
					postKey(SWT.CR);
					
				}
			};
			new Thread(r).start();
			
		}	
		waitForJobsToComplete();
		InputStream stream = buildFile.getContents();
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		try {
			String line1 = br.readLine();
			assertTrue("Contents of the build configuration file are wrong after adding a folder to the build path", line1.trim().equals("src.includes = src/,\\"));
			String line2 = br.readLine();
			assertTrue("Contents of the build configuration file are wrong after adding a folder to the build path", line2.trim().equals("src2/"));		
		} finally {	
			br.close();
		}		
	}

	private void removeFolderFromBuildPathAndCheckBuildFile(String folderName, IProject project,  IFile buildFile) throws CoreException, IOException {
		PackageExplorerPart packageExplorer = PackageExplorerPart.getFromActivePerspective();
		packageExplorer.setFocus();
		if (runningEclipse31) {
			IResource folder = project.findMember(folderName);
			if (!(folder instanceof IFolder)) {
				fail("Folder \"" + folderName + "\" should have been found in the project");
			}
			packageExplorer.tryToReveal(folder);
			postKeyDown(SWT.SHIFT);
			postKey(SWT.F10);
			postKeyUp(SWT.SHIFT);
			postKey('b');
			postKey(SWT.CR);
		} else {
			packageExplorer.tryToReveal(project);
			postKeyDown(SWT.ALT);
			postKey(SWT.CR);
			postKeyUp(SWT.ALT);
			Runnable r = new Runnable() {
				public void run() {
					sleep();			
					postKey(SWT.TAB);
					postKey(SWT.TAB);
					postKey(SWT.ARROW_DOWN);
					postKey(SWT.DEL);
					postKey(SWT.CR);					
				}
			};
			new Thread(r).start();
		}	
		waitForJobsToComplete();
		InputStream stream = buildFile.getContents();
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String line1 = br.readLine();
		br.close();
		assertTrue("Contents of the build configuration file are wrong after removing a folder from the build path", line1.trim().equals("src.includes = src/"));		
	}

	private void addNewFolderAndCheckBuildFile(IFile buildFile) throws CoreException, IOException {
		PackageExplorerPart.getFromActivePerspective().setFocus();
		
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
		if (runningEclipse31) {
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);			
		}
		postKey(SWT.CR);
		
		Runnable r = new Runnable() {
		
			public void run() {
				sleep();
				postString("src2");
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		
		waitForJobsToComplete();
	
		InputStream stream = buildFile.getContents();
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String line1 = br.readLine();
		br.close();
		assertTrue("Contents of the build configuration file are wrong after adding a folder", line1.trim().equals("src.includes = src/"));		
	}
	
	/**
	 * Check that when the build file is first created it has the correct contents
	 * @param buildFile
	 */
	private void checkOriginalContents(IFile buildFile) throws CoreException, IOException {
		InputStream stream = buildFile.getContents();
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String line1 = br.readLine();
		br.close();
		assertTrue("Original contents of the build configuration file are wrong", line1.trim().equals("src.includes = src/"));
	}

	private IFile checkBuildFileExists(IProject project) {
		IFile buildFile = (IFile)project.findMember(BuildConfiguration.STANDARD_BUILD_CONFIGURATION_FILE);		
		assertTrue("Should have created a build configuration file", buildFile.exists());
		return buildFile;
	}
	
}
