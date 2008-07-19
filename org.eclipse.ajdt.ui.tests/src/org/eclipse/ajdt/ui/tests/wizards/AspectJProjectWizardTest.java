/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ian McGrath - initial version
 ******************************************************************************/

package org.eclipse.ajdt.ui.tests.wizards;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.internal.ui.wizards.AspectJProjectWizard;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class AspectJProjectWizardTest extends UITestCase {

	private File testDestinationFile;
	private File testSrcFile;

	NewJavaProjectWizardPageOne testPage;

	NewJavaProjectWizardPageTwo secondTestPage;

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
			fail("addPages() failed, 2 pages have not be accurately added."); //$NON-NLS-1$
		}

		IWizardPage firstPage = projectWizard
				.getPage("NewJavaProjectWizardPageOne"); //$NON-NLS-1$
		IWizardPage secondPage = projectWizard.getPage("JavaCapabilityConfigurationPage"); //$NON-NLS-1$

		if (firstPage == null)
			fail("addPages() has failed to add a NewJavaProjectWizardPageOne at the correct location."); //$NON-NLS-1$
		if (secondPage == null)
			fail("addPages() has failed to add a JavaCapabilityConfigurationPage at the correct location."); //$NON-NLS-1$

		try {
			AJTitleCorrect = firstPage
					.getTitle()
					.equals(UIMessages.NewAspectJProject_CreateAnAspectJProject);
			AJDescriptionCorrect = firstPage
					.getDescription()
					.equals(UIMessages.NewAspectJProject_CreateAnAspectJProjectDescription);
		} catch (NullPointerException e) {
			fail("The title or description for the AJ page is incorrect."); //$NON-NLS-1$
		}

		if (!AJTitleCorrect) {
			fail("The title for the AJ page is incorrect."); //$NON-NLS-1$
		}
		if (!AJDescriptionCorrect) {
			fail("The description for the AJ page is incorrect."); //$NON-NLS-1$
		}

	}

	/**
	 * Tests the projects which are created by the perform finish method
	 */
	public void testProjectWizardPerformFinish() throws Exception {

		IProject testSrcProject = createPredefinedProject("SourceProject1"); //$NON-NLS-1$
		IJavaProject javaProject = JavaCore.create(testSrcProject);
		int ID = 1;
		String pDestinationName = "NotVisible" + ID; //$NON-NLS-1$
		String pSrcName = testSrcProject.getName();
		ID++;
		
		testDestinationFile = AspectJPlugin.getWorkspace().getRoot()
				.getLocation().append(pDestinationName).toFile();
		testSrcFile = AspectJPlugin.getWorkspace().getRoot().getLocation()
				.append(pSrcName).toFile();

		copyFileStructure(testSrcFile, testDestinationFile);
		IProject wizardCreatedProject = makeNewWizardProject("TestWizardProject"); //$NON-NLS-1$
		runGeneralTests(wizardCreatedProject, "TestWizardProject"); //$NON-NLS-1$
		IJavaProject jp = JavaCore.create(wizardCreatedProject);

		try {
			//Eclipse 3.3: the default is now to create projects with a bin folder
			assertEquals("The wizard created project does not have the correct output folder", //$NON-NLS-1$
					jp.getPath().append("bin"),jp.getOutputLocation()); //$NON-NLS-1$
		} catch (JavaModelException e) {
			fail("Failed attempting to find the output location of the project"); //$NON-NLS-1$
		}
		
		IProject newlyFoundProject = makeNewWizardProject(pDestinationName);		// The wizard should make the project from the one
		runGeneralTests(newlyFoundProject, pDestinationName); 			// existing in the file structure
		IJavaProject discoveredAJProject = JavaCore.create(newlyFoundProject);
		
		try {
			assertTrue("The wizard discovered project does not have the correct output folder", //$NON-NLS-1$
				javaProject.getOutputLocation().lastSegment().equals(
						discoveredAJProject.getOutputLocation().lastSegment()));
			
		} catch (JavaModelException e) {	
			fail("Failed attempting to find the output location of the project"); //$NON-NLS-1$
		}	
		
		String packagePath = "src" + IPath.SEPARATOR + "TestPackage"; //$NON-NLS-1$ //$NON-NLS-2$
		IResource projectPackage = newlyFoundProject.findMember(packagePath);
		
		assertTrue("The TestPackage of the discovered project has not been identified correctly", //$NON-NLS-1$
				projectPackage != null);
			
		String helloPath = packagePath + IPath.SEPARATOR + "Hello.java"; //$NON-NLS-1$
		String aspPath = packagePath + IPath.SEPARATOR + "Asp.aj"; //$NON-NLS-1$
		String wrongFile = packagePath + IPath.SEPARATOR + "wrongFile.imc"; //$NON-NLS-1$
		
		assertTrue("The Hello.java file has not been correctly discovered", //$NON-NLS-1$
				newlyFoundProject.findMember(helloPath) != null);
	
	
		assertTrue("The Asp.aj file has not been correctly discovered", //$NON-NLS-1$
				newlyFoundProject.findMember(aspPath) != null);
		
		assertTrue("An incorrect file has been created during the discovery of the project", //$NON-NLS-1$
				newlyFoundProject.findMember(wrongFile) == null);
	}

	/**
	 * Runs a set of general tests which any correctly formed aj project should pass.
	 * Takes project specific variables as arguments
	 */
	private void runGeneralTests(IProject project, String expectedProjectName) {
		
		if(expectedProjectName != null) {
			assertTrue("The wizard created project has not been named correctly", //$NON-NLS-1$
				project.getName().equals(expectedProjectName));
		}
		assertTrue("The Wizard created project doesnt appear in the workspace", //$NON-NLS-1$
				project.exists());
		assertTrue("The Wizard created project has not opened properly", //$NON-NLS-1$
				project.isOpen());

		assertTrue("A .project file has not been created", project //$NON-NLS-1$
				.findMember(".project") != null); //$NON-NLS-1$
		assertTrue("A .classpath file has not been created", //$NON-NLS-1$
				project.findMember(".classpath") != null); //$NON-NLS-1$

		IProject preOpenCloseWizard = project;

		try {
			project.close(null);
			project.open(null);
		} catch (CoreException e) {
			e.printStackTrace();
			fail("Project does not open and close correctly"); //$NON-NLS-1$
		}

		assertTrue("When a project is closed and then opened its properties change", //$NON-NLS-1$
				project.equals(preOpenCloseWizard));

		try {
			assertTrue("The Wizard created project does not have Java Nature", //$NON-NLS-1$
					project.hasNature("org.eclipse.jdt.core.javanature")); //$NON-NLS-1$

			assertTrue("The Wizard created project does not have AspectJ Nature", //$NON-NLS-1$
					project.hasNature("org.eclipse.ajdt.ui.ajnature")); //$NON-NLS-1$
			
		} catch (CoreException e) {
			e.printStackTrace();
			fail("Failed attempting to check the nature of the project"); //$NON-NLS-1$
		}

		assertTrue("The wizard created project has not been created in the correct location", //$NON-NLS-1$
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
					fail("Did not successfully copy files" + e.getMessage()); //$NON-NLS-1$
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
