/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui.visual.tests;

import org.eclipse.ajdt.core.AJProperties;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.BuildConfig;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * Tests that the AspectJ example projects can be created using
 * the examples wizard, and that they have the correct build
 * configuration
 */
public class ExampleProjectsTest extends VisualTestCase {

	/**
	 * Test the Bean Example Project
	 * @throws Exception
	 */
	public void testBeanExample() throws Exception {		
		startNewWizard();
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();	
				
				// use text filter to select the right wizard
				postString("bean"); //$NON-NLS-1$
				sleep();
				postKey(SWT.CR);
				sleep();
			
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
		IFile defaultBuildFile = (IFile)project.findMember("build."+AJProperties.EXTENSION); //$NON-NLS-1$
		assertNull("Should not have created a default build configuration file", defaultBuildFile); //$NON-NLS-1$
	}

	/**
	 * Test the Introduction Example Project
	 * @throws Exception
	 */	
	public void testIntroductionExample() throws Exception {		
		startNewWizard();	
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();	

				// use text filter to select the right wizard
				postString("introduction"); //$NON-NLS-1$
				sleep();
				postKey(SWT.CR);
				sleep();

				// Create the Introduction Example project
				postKey(SWT.CR);
				sleep();	
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
					
		// Wait for the project to be created
		waitForJobsToComplete();	
		
		final IWorkspace workspace= AspectJPlugin.getWorkspace();		
		
		new DisplayHelper() {

			protected boolean condition() {
				boolean ret = workspace.getRoot().getProject("Introduction Example").exists(); //$NON-NLS-1$
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);

		IProject project = getProject("Introduction Example"); //$NON-NLS-1$
		IFile defaultBuildFile = (IFile)project.findMember("build."+AJProperties.EXTENSION); //$NON-NLS-1$
		assertNull("Should not have created a default build configuration file", defaultBuildFile); //$NON-NLS-1$
	}
	/**
	 * Test the Observer Example project
	 * @throws Exception
	 */
	public void testObserverExample() throws Exception {
		startNewWizard();		
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();	
				
				// use text filter to select the right wizard
				postString("observer"); //$NON-NLS-1$
				sleep();
				postKey(SWT.CR);
				sleep();

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
		IFile defaultBuildFile = (IFile)project.findMember("build."+AJProperties.EXTENSION); //$NON-NLS-1$
		assertNull("Should not have created a default build configuration file", defaultBuildFile); //$NON-NLS-1$
	}
	
	/**
	 * Test the Spacewar Example project
	 */
	public void testSpacewarExample() {
		startNewWizard();		
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();
				
				// use text filter to select the right wizard
				postString("spacewar"); //$NON-NLS-1$
				sleep();
				postKey(SWT.CR);
				sleep();

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
		IFile buildFile = (IFile)project.findMember("debug." + AJProperties.EXTENSION);		 //$NON-NLS-1$
		assertNotNull("Should have created a 'debug' build configuration file", buildFile); //$NON-NLS-1$
		
		buildFile = (IFile)project.findMember("demo." + AJProperties.EXTENSION);		 //$NON-NLS-1$
		assertNotNull("Should have created a 'demo' build configuration file", buildFile); //$NON-NLS-1$
		
		IFile defaultBuildFile = (IFile)project.findMember("build."+AJProperties.EXTENSION); //$NON-NLS-1$
		assertNull("Should not have created a default build configuration file", defaultBuildFile);			 //$NON-NLS-1$

		// check that the demo configuration has been applied, such that Debug.aj
		// is not included
		IFile debug = (IFile)project.findMember("src/spacewar/Debug.aj"); //$NON-NLS-1$
		assertNotNull("Couldn't find Debug.aj",debug); //$NON-NLS-1$
		assertFalse("Debug.aj should NOT be included",BuildConfig.isIncluded(debug)); //$NON-NLS-1$
		
		// sanity check that another file in the project IS included
		IFile ship = (IFile)project.findMember("src/spacewar/Ship.aj"); //$NON-NLS-1$
		assertNotNull("Couldn't find Ship.aj",ship); //$NON-NLS-1$
		assertTrue("Ship.aj should be included",BuildConfig.isIncluded(ship)); //$NON-NLS-1$
	}

	/**
	 * Test the Telecom Example project
	 *
	 */
	public void testTelecomExample() {
		startNewWizard();		
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();
				
				// use text filter to select the right wizard
				postString("telecom"); //$NON-NLS-1$
				sleep();
				postKey(SWT.CR);
				sleep();

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
		
		final IWorkspace workspace= AspectJPlugin.getWorkspace();		
		new DisplayHelper() {
			
			protected boolean condition() {
				boolean ret = workspace.getRoot().getProject("Telecom Example").exists(); //$NON-NLS-1$
				return ret;
			}	
			
		}.waitForCondition(Display.getCurrent(), 5000);

		IProject project = getProject("Telecom Example"); //$NON-NLS-1$
		// Test that the correct build files have been created and the default build file has not been created
		IFile buildFile = (IFile)project.findMember("billing." + AJProperties.EXTENSION);		 //$NON-NLS-1$
		assertNotNull("Should have created a 'billing' build configuration file", buildFile); //$NON-NLS-1$
		
		buildFile = (IFile)project.findMember("timing." + AJProperties.EXTENSION);		 //$NON-NLS-1$
		assertNotNull("Should have created a 'timing' build configuration file", buildFile); //$NON-NLS-1$
		
		IFile defaultBuildFile = (IFile)project.findMember("build."+AJProperties.EXTENSION); //$NON-NLS-1$
		assertNull("Should not have created a default build configuration file", defaultBuildFile);			 //$NON-NLS-1$

		// check that the basic configuration has been applied, such that
		// Billing.aj is not included
		IFile billing = (IFile)project.findMember("src/telecom/Billing.aj"); //$NON-NLS-1$
		assertNotNull("Couldn't find Billing.aj",billing); //$NON-NLS-1$
		assertFalse("Billing.aj should NOT be included",BuildConfig.isIncluded(billing)); //$NON-NLS-1$
		
		// sanity check that another file in the project IS included
		IFile basic = (IFile)project.findMember("src/telecom/BasicSimulation.java"); //$NON-NLS-1$
		assertNotNull("Couldn't find BasicSimulation.java",basic); //$NON-NLS-1$
		assertTrue("BasicSimulation.java should be included",BuildConfig.isIncluded(basic)); //$NON-NLS-1$

	}

	/**
	 * Test the TJP Example project
	 * @throws Exception
	 */
	public void testTjpExample() throws Exception {
		startNewWizard();		
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();	
							
				// use text filter to select the right wizard
				postString("tjp"); //$NON-NLS-1$
				sleep();
				postKey(SWT.CR);
				sleep();

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
		IFile defaultBuildFile = (IFile)project.findMember("build."+AJProperties.EXTENSION); //$NON-NLS-1$
		assertNull("Should not have created a default build configuration file", defaultBuildFile); //$NON-NLS-1$
	}

	/**
	 * Test for the Tracing Example project
	 */
	public void testTracingExampleTest() {
		startNewWizard();		
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();
				
				// use text filter to select the right wizard
				postString("tracing"); //$NON-NLS-1$
				sleep();
				postKey(SWT.CR);
				sleep();

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
		IFile buildFile = (IFile)project.findMember("notrace." + AJProperties.EXTENSION);		 //$NON-NLS-1$
		assertNotNull("Should have created a 'notrace' build configuration file", buildFile); //$NON-NLS-1$
		
		buildFile = (IFile)project.findMember("tracev1." + AJProperties.EXTENSION);		 //$NON-NLS-1$
		assertNotNull("Should have created a 'tracev1' build configuration file", buildFile); //$NON-NLS-1$
		
		IFile defaultBuildFile = (IFile)project.findMember("build."+AJProperties.EXTENSION); //$NON-NLS-1$
		assertNull("Should not have created a default build configuration file", defaultBuildFile);			 //$NON-NLS-1$
		
		// check that the notrace configuration has been applied, such that
		// version1/Trace.java is not included
		IFile trace = (IFile)project.findMember("src/tracing/version1/Trace.java"); //$NON-NLS-1$
		assertNotNull("Couldn't find Trace.java",trace); //$NON-NLS-1$
		assertFalse("Travc.java should NOT be included",BuildConfig.isIncluded(trace)); //$NON-NLS-1$
		
		// sanity check that another file in the project IS included
		IFile circle = (IFile)project.findMember("src/tracing/Circle.java"); //$NON-NLS-1$
		assertNotNull("Couldn't find Circle.java",circle); //$NON-NLS-1$
		assertTrue("Circle.java should be included",BuildConfig.isIncluded(circle)); //$NON-NLS-1$
	}

	/**
	 * Test the Progress Monitor plugin project
	 * @throws Exception
	 */
	/* temporarily disable: build machine has mozilla probs
	public void testProgressMonitorExample() throws Exception {
		startNewWizard();		
		
		// Give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();	
							
				// use text filter to select the right wizard
				postString("progress monitor"); //$NON-NLS-1$
				sleep();
				postKey(SWT.CR);
				sleep();

				// Create the project
				postKey(SWT.CR);
				sleep();	
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
					
		// Wait for the project to be created
		waitForJobsToComplete();	
		
		IProject project = getProject("org.eclipse.ajdt.examples.progressmon"); //$NON-NLS-1$
		IFile defaultBuildFile = (IFile)project.findMember("build."+AJProperties.EXTENSION); //$NON-NLS-1$
		assertNull("Should not have created a default build configuration file", defaultBuildFile); //$NON-NLS-1$

		// check that a file exists
		IFile manifest = (IFile)project.findMember("META-INF/MANIFEST.MF"); //$NON-NLS-1$
		assertNotNull("Couldn't find MANIFEST.MF",manifest); //$NON-NLS-1$

	}
*/
	
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
	protected IProject getProject(final String projectName) {
		final IWorkspace workspace= AspectJPlugin.getWorkspace();		
		
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