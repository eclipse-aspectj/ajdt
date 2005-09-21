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
package org.eclipse.ajdt.ui.tests.actions;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.actions.RemoveAJNatureAction;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 *  
 */
public class RemoveAJNatureActionTest extends UITestCase {

	private IProject testProject = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		testProject = createPredefinedProject("project.java.Y"); //$NON-NLS-1$
		waitForJobsToComplete();
		AJDTUtils.addAspectJNature(testProject);
		waitForJobsToComplete();
	}

	public void testRemovesAJNature() throws CoreException {
		// Ensure that we are starting with a project that has an AspectJ
		// nature.
		assertTrue(AspectJPlugin.isAJProject(testProject));

		// Next, create the necessary arguments for the nature addition.
		ISelection sel = new StructuredSelection(testProject);
		IAction action = new Action() {
			public void run() {
				// NO OP
			}
		};
		RemoveAJNatureAction rna = new RemoveAJNatureAction();
		rna.selectionChanged(action, sel);

		// Remove the nature
		rna.run(action);
		assertFalse(AspectJPlugin.isAJProject(testProject));
	}

}