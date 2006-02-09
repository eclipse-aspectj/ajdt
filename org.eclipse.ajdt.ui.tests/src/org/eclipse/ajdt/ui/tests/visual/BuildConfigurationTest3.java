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

import org.eclipse.ajdt.internal.bc.BuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * Tests for build configurations in the AspectJ example projects
 */
public class BuildConfigurationTest3 extends VisualTestCase {

	public void testExamples() throws Exception {
		// tests are all run from one method because the ordering is important 
		// and JUnit does not guarantee an order
		beanExampleTest();
		introductionExampleTest();
		observerExampleTest();
		spacewarExampleTest();
		telecomExampleTest();
		tjpExampleTest();
		tracingExampleTest();
	}	

	/**
	 * Test the Bean Example Project
	 * @throws Exception
	 */
	public void beanExampleTest() throws Exception {		
		startNewWizard();
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();	
				// Navigate to the bean example
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				if (runningEclipse31) {
					postKey(SWT.ARROW_DOWN);					
				}
				postKey(SWT.ARROW_RIGHT);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_DOWN);
				postKey(SWT.ARROW_RIGHT);
				postKey(SWT.ARROW_DOWN);
				
				// Create the Bean Example project
				postKey(SWT.CR);
				sleep();	
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
					
		// Wait for the project to be created
		waitForJobsToComplete();	
		
		IProject project = getProject("Bean Example"); //$NON-NLS-1$
		testForDefaultBuildFile(project);
	}

	/**
	 * Test the Introduction Example Project
	 * @throws Exception
	 */	
	public void introductionExampleTest() throws Exception {		
		startNewWizard();	
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();	
				// Navigate to the Introduction example
				postKey(SWT.ARROW_DOWN);
				// Create the Introduction Example project
				postKey(SWT.CR);
				sleep();	
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
					
		// Wait for the project to be created
		waitForJobsToComplete();	
		
		final IWorkspace workspace= JavaPlugin.getWorkspace();		
		
		new DisplayHelper() {

			protected boolean condition() {
				boolean ret = workspace.getRoot().getProject("Introduction Example").exists(); //$NON-NLS-1$
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);

		IProject project = getProject("Introduction Example"); //$NON-NLS-1$
		testForDefaultBuildFile(project);
	}
	/**
	 * Test the Observer Example project
	 * @throws Exception
	 */
	private void observerExampleTest() throws Exception {
		startNewWizard();		
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();	
				// Navigate to the Observer example
				postKey(SWT.ARROW_DOWN);
				// Create the Observer Example project
				postKey(SWT.CR);
				sleep();	
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
					
		// Wait for the project to be created
		waitForJobsToComplete();	
		
		IProject project = getProject("Observer Example"); //$NON-NLS-1$
		testForDefaultBuildFile(project);
	}
	
	/**
	 * Test the Spacewar Example project
	 */
	private void spacewarExampleTest() {
		startNewWizard();		
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();	
				// Navigate to the Spacewar example
				postKey(SWT.ARROW_DOWN);
				// Create the Spacewar Example project
				postKey(SWT.CR);
				sleep();	
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
					
		// Wait for the project to be created
		waitForJobsToComplete();	
		
		IProject project = getProject("Spacewar Example"); //$NON-NLS-1$
		assertTrue("Should have created a project", project.exists());	 //$NON-NLS-1$
		
		// Test that the correct build files have been created and the default build file has not been created
		IFile buildFile = (IFile)project.findMember("debug." + BuildConfiguration.EXTENSION);		 //$NON-NLS-1$
		assertTrue("Should have created a 'debug' build configuration file", buildFile.exists()); //$NON-NLS-1$
		
		buildFile = (IFile)project.findMember("demo." + BuildConfiguration.EXTENSION);		 //$NON-NLS-1$
		assertTrue("Should have created a 'demo' build configuration file", buildFile.exists()); //$NON-NLS-1$
		
		IFile defaultBuildFile = (IFile)project.findMember(BuildConfiguration.STANDARD_BUILD_CONFIGURATION_FILE);
		assertTrue("Should not have created a default build configuration file", defaultBuildFile == null);			 //$NON-NLS-1$
	}

	/**
	 * Test the Telecom Example project
	 *
	 */
	private void telecomExampleTest() {
		startNewWizard();		
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();	
				// Navigate to the Telecom example
				postKey(SWT.ARROW_DOWN);
				// Create the Spacewar Telecom project
				postKey(SWT.CR);
				sleep();	
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
					
		// Wait for the project to be created
		waitForJobsToComplete();	
		
		final IWorkspace workspace= JavaPlugin.getWorkspace();		
		new DisplayHelper() {
			
			protected boolean condition() {
				boolean ret = workspace.getRoot().getProject("Telecom Example").exists(); //$NON-NLS-1$
				return ret;
			}	
			
		}.waitForCondition(Display.getCurrent(), 5000);

		IProject project = getProject("Telecom Example"); //$NON-NLS-1$
		// Test that the correct build files have been created and the default build file has not been created
		IFile buildFile = (IFile)project.findMember("billing." + BuildConfiguration.EXTENSION);		 //$NON-NLS-1$
		assertTrue("Should have created a 'billing' build configuration file", buildFile.exists()); //$NON-NLS-1$
		
		buildFile = (IFile)project.findMember("timing." + BuildConfiguration.EXTENSION);		 //$NON-NLS-1$
		assertTrue("Should have created a 'timing' build configuration file", buildFile.exists()); //$NON-NLS-1$
		
		IFile defaultBuildFile = (IFile)project.findMember(BuildConfiguration.STANDARD_BUILD_CONFIGURATION_FILE);
		assertTrue("Should not have created a default build configuration file", defaultBuildFile == null);			 //$NON-NLS-1$
	}

	/**
	 * Test the TJP Example project
	 * @throws Exception
	 */
	private void tjpExampleTest() throws Exception {
		startNewWizard();		
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();	
				// Navigate to the TJP example
				postKey(SWT.ARROW_DOWN);
				// Create the TJP Example project
				postKey(SWT.CR);
				sleep();	
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
					
		// Wait for the project to be created
		waitForJobsToComplete();	
		
		IProject project = getProject("TJP Example"); //$NON-NLS-1$
		testForDefaultBuildFile(project);
	}

	/**
	 * Test for the Tracing Example project
	 */
	private void tracingExampleTest() {
		startNewWizard();		
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();	
				// Navigate to the Telecom example
				postKey(SWT.ARROW_DOWN);
				// Create the Telecom project
				postKey(SWT.CR);
				sleep();
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
					
		// Wait for the project to be created
		waitForJobsToComplete();	
		
		IProject project = getProject("Tracing Example"); //$NON-NLS-1$
		// Test that the correct build files have been created and the default build file has not been created
		IFile buildFile = (IFile)project.findMember("notrace." + BuildConfiguration.EXTENSION);		 //$NON-NLS-1$
		assertTrue("Should have created a 'notrace' build configuration file", buildFile.exists()); //$NON-NLS-1$
		
		buildFile = (IFile)project.findMember("tracev1." + BuildConfiguration.EXTENSION);		 //$NON-NLS-1$
		assertTrue("Should have created a 'tracev1' build configuration file", buildFile.exists()); //$NON-NLS-1$
		
		IFile defaultBuildFile = (IFile)project.findMember(BuildConfiguration.STANDARD_BUILD_CONFIGURATION_FILE);
		assertTrue("Should not have created a default build configuration file", defaultBuildFile == null);			 //$NON-NLS-1$
	}

	/**
	 * Test that the defualt build file has been created and has the correct contents
	 * @param project
	 * @throws CoreException
	 * @throws IOException
	 */
	private void testForDefaultBuildFile(IProject project) throws CoreException, IOException {
		// Test that a build file has been created
		IFile buildFile = (IFile)project.findMember(BuildConfiguration.STANDARD_BUILD_CONFIGURATION_FILE);		
		assertTrue("Should have created a build configuration file", buildFile.exists()); //$NON-NLS-1$
		
		// Test that the new build file has the correct contents
		InputStream stream = buildFile.getContents();
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String line1 = br.readLine();
		br.close();
		assertTrue("Original contents of the build configuration file are wrong", line1.trim().equals("src.includes = src/")); //$NON-NLS-1$ //$NON-NLS-2$

	}

	/**
	 * Start the 'New' wizard
	 */
	private void startNewWizard() {
		postKeyDown(SWT.CTRL);		
		postKey('n');
		postKeyUp(SWT.CTRL);
	}
	
	/**
	 * Find the project with the given name in the workspace.  
	 * Returns null if the project cannot be found.
	 * @param projectName
	 * @return
	 */
	private IProject getProject(final String projectName) {
		final IWorkspace workspace= JavaPlugin.getWorkspace();		
		
		new DisplayHelper() {

			protected boolean condition() {
				boolean ret = workspace.getRoot().getProject(projectName).exists();
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);

		IProject project = workspace.getRoot().getProject(projectName);
		assertTrue("Should have created a project", project.exists());		 //$NON-NLS-1$
		return project;
	}
		

}