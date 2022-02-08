/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Ian McGrath - initial version
 ******************************************************************************/

package org.eclipse.ajdt.ui.tests.wizards;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.internal.ui.wizards.AspectJProjectWizard;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AspectJProjectWizardTest extends UITestCase {

  private File destinationProjectPath;
  private File sourceProjectPath;

  NewJavaProjectWizardPageOne testPage;
  NewJavaProjectWizardPageTwo secondTestPage;

  public void testProjectWizardPageAddition() {
    AspectJProjectWizard projectWizard = new AspectJProjectWizard();

    projectWizard.addPages();
    if (projectWizard.getPageCount() != 2) {
      fail("addPages() failed, 2 pages have not be accurately added."); //$NON-NLS-1$
    }

    IWizardPage firstPage = projectWizard.getPage("NewJavaProjectWizardPageOne"); //$NON-NLS-1$
    IWizardPage secondPage = projectWizard.getPage("JavaCapabilityConfigurationPage"); //$NON-NLS-1$
    if (firstPage == null)
      fail("addPages() has failed to add a NewJavaProjectWizardPageOne at the correct location."); //$NON-NLS-1$
    if (secondPage == null)
      fail("addPages() has failed to add a JavaCapabilityConfigurationPage at the correct location."); //$NON-NLS-1$

    boolean ajTitleCorrect = false;
    boolean ajDescriptionCorrect = false;
    try {
      ajTitleCorrect = firstPage.getTitle()
        .equals(UIMessages.NewAspectJProject_CreateAnAspectJProject);
      ajDescriptionCorrect = firstPage.getDescription()
        .equals(UIMessages.NewAspectJProject_CreateAnAspectJProjectDescription);
    }
    catch (NullPointerException e) {
      fail("The title or description for the AJ page is incorrect."); //$NON-NLS-1$
    }
    if (!ajTitleCorrect) {
      fail("The title for the AJ page is incorrect."); //$NON-NLS-1$
    }
    if (!ajDescriptionCorrect) {
      fail("The description for the AJ page is incorrect."); //$NON-NLS-1$
    }

  }

  /**
   * Tests projects created by clicking the "Finish" button in the wizard
   */
  public void testProjectWizardPerformFinish() throws Exception {

    // Create empty AspectJ project using wizard
    IProject wizardCreatedProject = makeNewWizardProject("TestWizardProject"); //$NON-NLS-1$
    runGeneralTests(wizardCreatedProject, "TestWizardProject"); //$NON-NLS-1$
    IJavaProject jp = JavaCore.create(wizardCreatedProject);
    try {
      assertEquals("The wizard created project does not have the correct output folder", //$NON-NLS-1$
        jp.getPath().append("bin"), jp.getOutputLocation()); //$NON-NLS-1$
    }
    catch (JavaModelException e) {
      fail("Failed attempting to find the output location of the project"); //$NON-NLS-1$
    }

    // Create predefined Java project without wizard
    String sourceProjectName = "SourceProject1"; //$NON-NLS-1$
    IProject sourceProject = createPredefinedProject(sourceProjectName);
    IJavaProject sourceJavaProject = JavaCore.create(sourceProject);

    // Copy Java project to a destination folder in order to import from there later
    IPath workspaceRoot = AspectJPlugin.getWorkspace().getRoot().getLocation();
    String destinationProjectName = "NotVisible1"; //$NON-NLS-1$
    File sourceProjectPath = workspaceRoot.append(sourceProjectName).toFile();
    File destinationProjectPath = workspaceRoot.append(destinationProjectName).toFile();
    copyFileStructure(sourceProjectPath, destinationProjectPath);

    // Create AspectJ project using wizard, importing from the previously copied folder
    IProject newlyFoundProject = makeNewWizardProject(destinationProjectName);
    runGeneralTests(newlyFoundProject, destinationProjectName);
    IJavaProject discoveredAJProject = JavaCore.create(newlyFoundProject);
    try {
      assertEquals("The wizard discovered project does not have the correct output folder", //$NON-NLS-1$
        sourceJavaProject.getOutputLocation().lastSegment(),
        discoveredAJProject.getOutputLocation().lastSegment());
    }
    catch (JavaModelException e) {
      fail("Failed attempting to find the output location of the project"); //$NON-NLS-1$
    }

    // Check if all expected resources have been discovered correctly
    String packagePath = "src" + IPath.SEPARATOR + "TestPackage"; //$NON-NLS-1$ //$NON-NLS-2$
    String helloPath = packagePath + IPath.SEPARATOR + "Hello.java"; //$NON-NLS-1$
    String aspPath = packagePath + IPath.SEPARATOR + "Asp.aj"; //$NON-NLS-1$
    String wrongFile = packagePath + IPath.SEPARATOR + "wrongFile.imc"; //$NON-NLS-1$
    assertNotNull("The TestPackage of the discovered project has not been identified correctly", //$NON-NLS-1$
      newlyFoundProject.findMember(packagePath));
    assertNotNull("The Hello.java file has not been correctly discovered", //$NON-NLS-1$
      newlyFoundProject.findMember(helloPath));
    assertNotNull("The Asp.aj file has not been correctly discovered", //$NON-NLS-1$
      newlyFoundProject.findMember(aspPath));
    assertNull("An incorrect file has been created during the discovery of the project", //$NON-NLS-1$
      newlyFoundProject.findMember(wrongFile));
  }

  /**
   * Runs a set of general tests which any correctly formed aj project should pass.
   * Takes project specific variables as arguments
   */
  private void runGeneralTests(IProject project, String expectedProjectName) {
    if (expectedProjectName != null) {
      assertEquals("The wizard created project has not been named correctly", //$NON-NLS-1$
        project.getName(), expectedProjectName);
    }
    assertTrue("The Wizard created project doesnt appear in the workspace", //$NON-NLS-1$
      project.exists());
    assertTrue("The Wizard created project has not opened properly", //$NON-NLS-1$
      project.isOpen());

    assertNotNull("A .project file has not been created", //$NON-NLS-1$
      project.findMember(".project"));
    assertNotNull("A .classpath file has not been created", //$NON-NLS-1$
      project.findMember(".classpath"));

    IProject preOpenCloseWizard = project;
    try {
      project.close(null);
      project.open(null);
    }
    catch (CoreException e) {
      e.printStackTrace();
      fail("Project does not open and close correctly"); //$NON-NLS-1$
    }
    assertEquals("When a project is closed and then opened its properties change", //$NON-NLS-1$
      project, preOpenCloseWizard);

    try {
      assertTrue("The Wizard created project does not have Java Nature", //$NON-NLS-1$
        project.hasNature("org.eclipse.jdt.core.javanature")); //$NON-NLS-1$
      assertTrue("The Wizard created project does not have AspectJ Nature", //$NON-NLS-1$
        project.hasNature("org.eclipse.ajdt.ui.ajnature")); //$NON-NLS-1$
    }
    catch (CoreException e) {
      e.printStackTrace();
      fail("Failed attempting to check the nature of the project"); //$NON-NLS-1$
    }

    assertEquals("The wizard created project has not been created in the correct location", //$NON-NLS-1$
      project.getParent().getLocation(),
      AspectJPlugin.getWorkspace().getRoot().getRawLocation());
  }

  /**
   * Invokes the wizard to create a new project, with the user input simulated
   */
  public IProject makeNewWizardProject(String projectName) {
    AspectJProjectWizardExtension aspectJProjectWizard = new AspectJProjectWizardExtension(projectName);
    aspectJProjectWizard.init(JavaPlugin.getDefault().getWorkbench(), null);
    MyWizardDialog wizardDialog = new MyWizardDialog(JavaPlugin.getActiveWorkbenchShell(), aspectJProjectWizard);

    wizardDialog.setBlockOnOpen(false);
    wizardDialog.create();
    wizardDialog.open();
    wizardDialog.finishPressed();

    IWorkspaceRoot workspaceRoot = AspectJPlugin.getWorkspace().getRoot();
    return workspaceRoot.getProject(projectName);
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
          FileOutputStream output = new FileOutputStream(fileDestination);
          int n;
          while ((n = input.read()) >= 0) {
            output.write(n);
          }
          input.close();
          output.flush();
          output.close();
        }
        catch (IOException e) {
          fail("Did not successfully copy files" + e.getMessage()); //$NON-NLS-1$
        }
      }

      public CopyFileFilter(File fileParent) {
        m_fileParent = fileParent;
      }

      public boolean accept(File file) {
        File fileNew = new File(m_fileParent.getAbsolutePath() + File.separator + file.getName());
        if (file.isDirectory()) {
          fileNew.mkdir();
          file.listFiles(new CopyFileFilter(fileNew));
        }
        else {
          copyFile(file, fileNew);
        }
        return false;
      }

      private final File m_fileParent;
    }

    fileDestination.mkdirs();
    fileSource.listFiles(new CopyFileFilter(fileDestination));
  }

  private static class MyWizardDialog extends WizardDialog {
    public MyWizardDialog(Shell parentShell, IWizard newWizard) {
      super(parentShell, newWizard);
    }

    /**
     * Overridden in order to expose the protected parent method
     */
    @Override
    public void finishPressed() {
      super.finishPressed();
    }
  }

}
