/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     George Harley - initial version
 * 	   Helen Hawkins - converting for use with AJDT 1.1.11 codebase  
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.actions;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.actions.AddAJNatureAction;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;


/**
 * @author gharley
 */
public class AddAJNatureActionTest extends UITestCase {

    // Lets create a new Java project, check its nature and then
    // convert it to an AspectJ project. Another check of its nature should
    // reveal it to be AspectJ.

	private IProject testProject = null;

    protected void setUp() throws Exception {
        super.setUp();
		testProject = createPredefinedProject("project.java.Y"); //$NON-NLS-1$
		waitForJobsToComplete();

    }

    public void testAddsAJNature() throws CoreException {
        // Ensure that we are starting with a Java project.	
        assertTrue(testProject.hasNature("org.eclipse.jdt.core.javanature")); //$NON-NLS-1$
        
        // Next, create the necessary arguments for the nature addition.
        ISelection sel = new StructuredSelection(testProject);
        IAction action = new Action() {
            public void run() {
                    // NO OP
            }
        };
        AddAJNatureAction aja = new AddAJNatureAction();
        aja.selectionChanged(action, sel);
        
        // Attempt to add the nature
        aja.run(action);
        assertTrue(AspectJPlugin.isAJProject(testProject));
        assertTrue("Should have aspectjruntime.jar on the classpath", RemoveAJNatureActionTest.hasAjrtOnBuildPath(testProject)); //$NON-NLS-1$
    }
}
