/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Ian McGrath - initial version
 ******************************************************************************/

package org.eclipse.ajdt.internal.ui.wizards;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.test.utils.JavaTestProject;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class AspectJProjectWizardTest extends TestCase {

	private File testDestinationFile;
	private File testSrcFile;
	private JavaTestProject testSrcProject;
	private  IProject wizardCreatedProject;
	private IProject newlyFoundProject;
	private static int ID = 0;

	AspectJWizardNewProjectCreationPage testPage;

	AspectJWizardNewProjectCreationPage secondTestPage;

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
		
		if((testDestinationFile != null) && testDestinationFile.exists())
			deleteDirectory(testDestinationFile);
		if((testSrcFile != null) && testSrcFile.exists())
			deleteDirectory(testSrcFile);
		if((testSrcFile != null) && 
			AspectJUIPlugin.getWorkspace().getRoot().getLocation().append("TestWizardProject").toFile().exists()) {
			
			deleteDirectory(AspectJUIPlugin.getWorkspace().getRoot().getLocation().append("TestWizardProject").toFile());
		}
		
		try {
			if (testSrcProject != null) 
				testSrcProject.dispose();
			if ((wizardCreatedProject != null) && wizardCreatedProject.exists()) 
				wizardCreatedProject.delete(true, null);
			if ((newlyFoundProject != null) && newlyFoundProject.exists())
				newlyFoundProject.delete(true, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void testProjectWizardPageAddition() {
		AspectJProjectWizard projectWizard = new AspectJProjectWizard();
		boolean AJTitleCorrect = false;
		boolean AJDescriptionCorrect = false;
		projectWizard.addPages();

		if (projectWizard.getPageCount() != 2) {
			fail("addPages() failed, 2 pages have not be accurately added.");
		}

		IWizardPage AJPage = projectWizard
				.getPage("NewAspectjProjectCreationWizard");
		IWizardPage JPage = projectWizard.getPage("NewJavaProjectWizardPage");

		if (AJPage == null)
			fail("addPages() has failed to add an AspectJWizardNewProjectCreationPage at the correct location.");
		if (JPage == null)
			fail("addPages() has failed to add a NewJavaProjectWizardPage at the correct location.");

		try {
			AJTitleCorrect = AJPage
					.getTitle()
					.equals(
							AspectJUIPlugin
									.getResourceString("NewAspectjProjectCreationWizard.MainPage.title"));
			AJDescriptionCorrect = AJPage
					.getDescription()
					.equals(
							AspectJUIPlugin
									.getResourceString("NewAspectjProjectCreationWizard.MainPage.description"));
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
	public void testProjectWizardPerformFinish() {

		testSrcProject = createTestProject(ID);
		String pDestinationName = "NotVisible" + ID;
		String pSrcName = testSrcProject.getName();
		ID++;
		
		testDestinationFile = AspectJUIPlugin.getWorkspace().getRoot()
				.getLocation().append(pDestinationName).toFile();
		testSrcFile = AspectJUIPlugin.getWorkspace().getRoot().getLocation()
				.append(pSrcName).toFile();

		copyFileStructure(testSrcFile, testDestinationFile);
		wizardCreatedProject = makeNewWizardProject("TestWizardProject");
		runGeneralTests(wizardCreatedProject, "TestWizardProject");
		IJavaProject jp = JavaCore.create(wizardCreatedProject);

		try {
			assertTrue("The wizard created project does not have the correct output folder",
				jp.getOutputLocation().equals(jp.getPath()));
		} catch (JavaModelException e) {
			fail("Failed attempting to find the output location of the project");
		}
		
		newlyFoundProject = makeNewWizardProject(pDestinationName);		// The wizard should make the project from the one
		runGeneralTests(newlyFoundProject, pDestinationName); 			// existing in the file structure
		IJavaProject discoveredAJProject = JavaCore.create(newlyFoundProject);
		
		try {
			assertTrue("The wizard discovered project does not have the correct output folder",
				testSrcProject.getJavaProject().getOutputLocation().lastSegment().equals(
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
								AspectJUIPlugin.getWorkspace().getRoot().getRawLocation()));
	}

	/**
	 * Invokes the wizard to create a new project, with the user input simulated
	 */
	public IProject makeNewWizardProject(String projectName) {

		AspectJProjectWizardExtension testProjectWizard = new AspectJProjectWizardExtension();
		Shell shell = JavaPlugin.getActiveWorkbenchShell();
		testProjectWizard.init(JavaPlugin.getDefault().getWorkbench(), null);
		testProjectWizard.setProjectDefaultName(projectName);
		WizardDialog dialog = new WizardDialog(shell, testProjectWizard);

		dialog.setBlockOnOpen(false);
		dialog.create();
		dialog.open();
		AspectJWizardNewProjectCreationPage curPage = (AspectJWizardNewProjectCreationPage) dialog
				.getCurrentPage();
		curPage.getWizard().performFinish();
		dialog.close();
		testProjectWizard.getNewProject();
		IProject wizardCreatedProject = AspectJUIPlugin.getWorkspace().getRoot()
				.getProject(projectName);
		return wizardCreatedProject;
	}

	/**
	 * Generates an ajdt project in the workspace with some test files
	 */
	public JavaTestProject createTestProject(int ID) {

		ID++;
		JavaTestProject testSrcProject = null;
		try { // sets up the aj test project
			testSrcProject = new JavaTestProject("SourceProject" + ID);
			waitForJobsToComplete(testSrcProject.getProject());
			AJDTUtils.addAspectJNature(testSrcProject.getProject());
			waitForJobsToComplete(testSrcProject.getProject());
			IPackageFragment testPackage = testSrcProject
					.createPackage("TestPackage");

			IType helloType = testSrcProject.createType(testPackage,
					"Hello.java", "public class Hello {\n"
							+ "  public static void main(String[] args) {\n"
							+ "    Hello.printMessage();\n" + "  }\n"
							+ "	 private static void printMessage() {\n"
							+ "    System.out.println(\"Hello\");\n" + "  }\n"
							+ "}");

			testSrcProject
					.createFile(
							(IFolder) helloType.getPackageFragment()
									.getUnderlyingResource(),
							"Asp.aj",
									"package TestPackage;"
									+ "public aspect Asp {\n"
									+ "  pointcut extendMessage() : call(* Hello.printMessage(..));\n"
									+ "  before() : extendMessage() {\n"
									+ "    System.out.println(\"Pre Message\");\n"
									+ "  }\n" + "}");

		} catch (CoreException e) {
			e.printStackTrace();
			fail("Project creation failed");
		}
		return testSrcProject;
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

	/**
	 * Recursively deletes all the files in a directory. Used for tidying up.
	 */
	private void deleteDirectory(File dir) {
		boolean x = dir.isDirectory();
		boolean y = (dir == null);

		if ((dir != null) && dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
			dir.delete();
		}
	}

	private void waitForJobsToComplete(IProject pro) {
		Job job = new Job("Dummy Job") {
			public IStatus run(IProgressMonitor m) {
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.DECORATE);
		job.setRule(pro);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			// Do nothing
		}
	}
}
