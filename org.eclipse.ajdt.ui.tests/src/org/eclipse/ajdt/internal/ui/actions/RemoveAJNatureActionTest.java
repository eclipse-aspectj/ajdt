/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     George Harley - initial version
 * 	   Helen Hawkins - converting for use with AJDT 1.1.11 codebase  
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.actions;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.test.utils.JavaTestProject;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 *  
 */
public class RemoveAJNatureActionTest extends TestCase {
	private JavaTestProject testProject = null;

	//private AJTestProject testProject = null;
	//private IAspectJProject ajp = null;
	private IType helloType;

	private IFile goodbyeAJFile;

	protected void setUp() throws Exception {
		super.setUp();

	
		// create a Java project that contains all the various Java elements...
		// create a project
		testProject = new JavaTestProject("Test AJ Project");

		waitForJobsToComplete(testProject.getProject());
		// create a package
		IPackageFragment testPackage = testProject.createPackage("mypackage");

		waitForJobsToComplete(testProject.getProject());
		// add a couple of Java files in the package
		helloType = testProject.createType(testPackage, "Hello.java",
				"public class Hello {\n"
						+ "  public static void main(String[] args) {\n"
						+ "    System.out.println(\"Hello\");\n" + "  }\n"
						+ "}");

		goodbyeAJFile = testProject.createFile((IFolder) helloType
				.getPackageFragment().getUnderlyingResource(), "Goodbye.aj",
				"public class Goodbye {\n" + "  public Goodbye() {};\n"
						+ "  public static void main(String[] args) {\n"
						+ "    System.out.println(message);\n" + "  }\n"
						+ "  public String message = \"Goodbye\";\n" + "}");

		// Now create an AspectJ project.
		//        ajp = AspectJCore.create(testProject.getProject());
		//        ajp.addAspectJNature();
		AJDTUtils.addAspectJNature(testProject.getProject());
		waitForJobsToComplete(testProject.getProject());
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		try {
			testProject.dispose();
		} catch (CoreException e) {
			// don't care that problem occured here....
		}
	}

	public void testRemovesAJNature() throws CoreException {
		// Ensure that we are starting with a project that has an AspectJ
		// nature.
		assertTrue(testProject.getProject().hasNature(AspectJUIPlugin.ID_NATURE));

		// Next, create the necessary arguments for the nature addition.
		ISelection sel = new StructuredSelection(testProject.getProject());
		IAction action = new Action() {
			public void run() {
				// NO OP
			}
		};
		RemoveAJNatureAction rna = new RemoveAJNatureAction();
		rna.selectionChanged(action, sel);

		// Remove the nature
		rna.run(action);
		assertFalse(testProject.getProject().hasNature(AspectJUIPlugin.ID_NATURE));
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