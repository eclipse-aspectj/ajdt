/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * Sian January - initial version
 * ...
 **********************************************************************/
package org.eclipse.ajdt.ui.tests.wizards;

import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.ui.wizards.BuildConfigurationFileWizard;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests for the new build configuration wizard 
 */
public class BuildConfigurationFileWizardTest extends TestCase {

//	private JavaTestProject testSrcProject;
	private IProject testSrcProject;
	
	public void testWizardPerformFinish() throws CoreException {
		
		testSrcProject = Utils.createPredefinedProject("AJ Project For BuildConfigurationTest");
		ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator().getProjectBuildConfigurator(testSrcProject);
		assertNotNull("The new project should have a build configurator", pbc);
		Collection configs = pbc.getBuildConfigurations();
		assertTrue("The new project should have one build configuration", configs.size() == 1);
		BuildConfigurationFileWizard wiz = new BuildConfigurationFileWizard();
		Shell shell = JavaPlugin.getActiveWorkbenchShell();
		wiz.init(JavaPlugin.getDefault().getWorkbench(), new StructuredSelection(testSrcProject));
		MyWizardDialog dialog = new MyWizardDialog(shell, wiz);
		dialog.setBlockOnOpen(false);
		dialog.create();
		dialog.open();
		dialog.finishPressed();
		Collection newconfigs = pbc.getBuildConfigurations();
		assertTrue("The new project should have two build configurations", configs.size() == 2);
		Utils.deleteProject(testSrcProject);
	}

	
	/**
	 * Generates an ajdt project in the workspace with some test files
	 */
//	public JavaTestProject createTestProject() throws CoreException {
//
//		JavaTestProject testSrcProject = null;
//
//		// sets up the aj test project
//		testSrcProject = new JavaTestProject("SourceProject1");
//		Utils.waitForJobsToComplete();
//		AJDTUtils.addAspectJNature(testSrcProject.getProject());
//		Utils.waitForJobsToComplete();
//		IPackageFragment testPackage = testSrcProject
//				.createPackage("TestPackage");
//
//		IType helloType = testSrcProject.createType(testPackage, "Hello.java",
//				"public class Hello {\n"
//						+ "  public static void main(String[] args) {\n"
//						+ "    Hello.printMessage();\n" + "  }\n"
//						+ "	 private static void printMessage() {\n"
//						+ "    System.out.println(\"Hello\");\n" + "  }\n"
//						+ "}");
//
//		testSrcProject
//				.createFile(
//						(IFolder) helloType.getPackageFragment()
//								.getUnderlyingResource(),
//						"Asp.aj",
//						"package TestPackage;"
//								+ "public aspect Asp {\n"
//								+ "  pointcut extendMessage() : call(* Hello.printMessage(..));\n"
//								+ "  before() : extendMessage() {\n"
//								+ "    System.out.println(\"Pre Message\");\n"
//								+ "  }\n" + "}");
//
//		return testSrcProject;
//	}
	
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
