/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Ian McGrath - initial version
 ******************************************************************************/

package org.eclipse.ajdt.ui.tests.wizards;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.wizards.AspectJProjectWizard;
import org.eclipse.ajdt.internal.ui.wizards.AspectJProjectWizardSecondPage;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.wizards.JavaProjectWizardFirstPage;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class AspectJProjectWizardTest extends TestCase {

	private File testDestinationFile;
	private File testSrcFile;

	JavaProjectWizardFirstPage testPage;

	AspectJProjectWizardSecondPage secondTestPage;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		
	}

	public void testProjectWizardPageAddition() {
		AspectJProjectWizard projectWizard = new AspectJProjectWizard();
		boolean AJTitleCorrect = false;
		boolean AJDescriptionCorrect = false;
		projectWizard.addPages();

		if (projectWizard.getPageCount() != 2) {
			fail("addPages() failed, 2 pages have not be accurately added.");
		}

		IWizardPage firstPage = projectWizard
				.getPage("SimpleProjectFirstPage");
		IWizardPage secondPage = projectWizard.getPage("JavaCapabilityConfigurationPage");

		if (firstPage == null)
			fail("addPages() has failed to add a JavaProjectWizardFirstPage at the correct location.");
		if (secondPage == null)
			fail("addPages() has failed to add an AspectJProjectWizardSecondPage at the correct location.");

		try {
			AJTitleCorrect = firstPage
					.getTitle()
					.equals(
							AspectJUIPlugin
									.getResourceString("NewAspectJProject.CreateAnAspectJProject"));
			AJDescriptionCorrect = firstPage
					.getDescription()
					.equals(
							AspectJUIPlugin
									.getResourceString("NewAspectJProject.CreateAnAspectJProjectDescription"));
		} catch (NullPointerException e) {
			fail("The title or description for the AJ page is incorrect.");
		}

		if (!AJTitleCorrect) {
			fail("The title for the AJ page is incorrect.");
		}
		if (!AJDescriptionCorrect) {
			fail("The description for the AJ page is incorrect.");
		}

	}

	/**
	 * Tests the projects which are created by the perform finish method
	 */
	public void testProjectWizardPerformFinish() throws Exception {

		IProject testSrcProject = Utils.createPredefinedProject("SourceProject1");
		IJavaProject javaProject = JavaCore.create(testSrcProject);
		int ID = 1;
		String pDestinationName = "NotVisible" + ID;
		String pSrcName = testSrcProject.getName();
		ID++;
		
		testDestinationFile = AspectJPlugin.getWorkspace().getRoot()
				.getLocation().append(pDestinationName).toFile();
		testSrcFile = AspectJPlugin.getWorkspace().getRoot().getLocation()
				.append(pSrcName).toFile();

		copyFileStructure(testSrcFile, testDestinationFile);
		IProject wizardCreatedProject = makeNewWizardProject("TestWizardProject");
		runGeneralTests(wizardCreatedProject, "TestWizardProject");
		IJavaProject jp = JavaCore.create(wizardCreatedProject);

		try {
			assertTrue("The wizard created project does not have the correct output folder",
				jp.getOutputLocation().equals(jp.getPath()));
		} catch (JavaModelException e) {
			fail("Failed attempting to find the output location of the project");
		}
		
		IProject newlyFoundProject = makeNewWizardProject(pDestinationName);		// The wizard should make the project from the one
		runGeneralTests(newlyFoundProject, pDestinationName); 			// existing in the file structure
		IJavaProject discoveredAJProject = JavaCore.create(newlyFoundProject);
		
		try {
			assertTrue("The wizard discovered project does not have the correct output folder",
				javaProject.getOutputLocation().lastSegment().equals(
						discoveredAJProject.getOutputLocation().lastSegment()));
			
		} catch (JavaModelException e) {	
			fail("Failed attempting to find the output location of the project");
		}	
		
		String packagePath = "src" + IPath.SEPARATOR + "TestPackage";
		IResource projectPackage = newlyFoundProject.findMember(packagePath);
		
		assertTrue("The TestPackage of the discovered project has not been identified correctly",
				projectPackage != null);
			
		String helloPath = packagePath + IPath.SEPARATOR + "Hello.java";
		String aspPath = packagePath + IPath.SEPARATOR + "Asp.aj";
		String wrongFile = packagePath + IPath.SEPARATOR + "wrongFile.imc";
		
		assertTrue("The Hello.java file has not been correctly discovered",
				newlyFoundProject.findMember(helloPath) != null);
	
	
		assertTrue("The Asp.aj file has not been correctly discovered",
				newlyFoundProject.findMember(aspPath) != null);
		
		assertTrue("An incorrect file has been created during the discovery of the project",
				newlyFoundProject.findMember(wrongFile) == null);
		
		Utils.deleteProject(testSrcProject);
		Utils.deleteProject(wizardCreatedProject);
		Utils.deleteProject(newlyFoundProject);
	
		Utils.waitForJobsToComplete();
	}

	/**
	 * Runs a set of general tests which any correctly formed aj project should pass.
	 * Takes project specific variables as arguments
	 */
	private void runGeneralTests(IProject project, String expectedProjectName) {
		
		if(expectedProjectName != null) {
			assertTrue("The wizard created project has not been named correctly",
				project.getName().equals(expectedProjectName));
		}
		assertTrue("The Wizard created project doesnt appear in the workspace",
				project.exists());
		assertTrue("The Wizard created project has not opened properly",
				project.isOpen());

		assertTrue("A .project file has not been created", project
				.findMember(".project") != null);
		assertTrue("A .classpath file has not been created",
				project.findMember(".classpath") != null);

		IProject preOpenCloseWizard = project;

		try {
			project.close(null);
			project.open(null);
		} catch (CoreException e) {
			e.printStackTrace();
			fail("Project does not open and close correctly");
		}

		assertTrue("When a project is closed and then opened its properties change",
				project.equals(preOpenCloseWizard));

		try {
			assertTrue("The Wizard created project does not have Java Nature",
					project.hasNature("org.eclipse.jdt.core.javanature"));

			assertTrue("The Wizard created project does not have AspectJ Nature",
					project.hasNature("org.eclipse.ajdt.ui.ajnature"));
			
		} catch (CoreException e) {
			e.printStackTrace();
			fail("Failed attempting to check the nature of the project");
		}

		assertTrue("The wizard created project has not been created in the correct location",
				project.getParent().getLocation().equals(
								AspectJPlugin.getWorkspace().getRoot().getRawLocation()));
	}

	/**
	 * Invokes the wizard to create a new project, with the user input simulated
	 */
	public IProject makeNewWizardProject(String projectName) {

		AspectJProjectWizardExtension testProjectWizard = new AspectJProjectWizardExtension();
		Shell shell = JavaPlugin.getActiveWorkbenchShell();
		testProjectWizard.init(JavaPlugin.getDefault().getWorkbench(), null);
		testProjectWizard.setProjectDefaultName(projectName);
		MyWizardDialog dialog = new MyWizardDialog(shell, testProjectWizard);

		dialog.setBlockOnOpen(false);
		dialog.create();
		dialog.open();
		JavaProjectWizardFirstPage curPage = (JavaProjectWizardFirstPage) dialog
				.getCurrentPage();
		dialog.finishPressed();
		IProject wizardCreatedProject = AspectJPlugin.getWorkspace().getRoot()
				.getProject(projectName);
		return wizardCreatedProject;
	}


	/**
	 * Copies a project from one location to another. Used instead of the
	 * workspace copy methods because otherwise the new file would automatically
	 * be identified as a resource
	 */
	private void copyFileStructure(File fileSource, File fileDestination) {
		class CopyFileFilter implements FileFilter {
			private void copyFile(File fileSource, File fileDestination) {
				try {
					FileInputStream input = new FileInputStream(fileSource);
					FileOutputStream output = new FileOutputStream(
							fileDestination);
					int n;
					while ((n = input.read()) >= 0) {
						output.write(n);
					}
					input.close();
					output.flush();
					output.close();
				} catch (IOException e) {
					fail("Did not successfully copy files" + e.getMessage());
				}
			}

			public CopyFileFilter(File fileParent) {
				m_fileParent = fileParent;
			}

			public boolean accept(File file) {
				File fileNew = new File(m_fileParent.getAbsolutePath()
						+ File.separator + file.getName());
				if (file.isDirectory()) {
					fileNew.mkdir();
					file.listFiles(new CopyFileFilter(fileNew));
				} else {
					copyFile(file, fileNew);
				}
				return false;
			}

			private final File m_fileParent;
		}
		fileDestination.mkdirs();
		fileSource.listFiles(new CopyFileFilter(fileDestination));
	}


	private class MyWizardDialog extends WizardDialog {

		/**
		 * @param parentShell
		 * @param newWizard
		 */
		public MyWizardDialog(Shell parentShell, IWizard newWizard) {
			super(parentShell, newWizard);
			// TODO Auto-generated constructor stub
		}
		
		public void finishPressed() {
			super.finishPressed();
		}
		
	}
	
}
