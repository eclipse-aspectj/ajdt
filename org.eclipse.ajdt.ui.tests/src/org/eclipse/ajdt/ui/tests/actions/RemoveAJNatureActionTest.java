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
import org.eclipse.ajdt.internal.ui.actions.RemoveAJNatureAction;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
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
        AspectJUIPlugin.convertToAspectJProject(testProject);
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

        // avoid prompting for depency removal.
        AspectJPreferences.setAskPDEAutoRemoveImport(false);
        // automatically remove import from classpath
        AspectJPreferences.setDoPDEAutoRemoveImport(true);

        RemoveAJNatureAction rna = new RemoveAJNatureAction();
        rna.selectionChanged(action, sel);

        // Remove the nature
        rna.run(action);
        assertFalse(AspectJPlugin.isAJProject(testProject));
        assertFalse(
                "Should not have aspectjruntime.jar on the classpath", hasAjrtOnBuildPath(testProject)); //$NON-NLS-1$
    }

    public void testRemovesAJNatureKeepClasspath() throws CoreException {
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

        // avoid prompting for depency removal.
        AspectJPreferences.setAskPDEAutoRemoveImport(false);
        // keep the dependency on the classpath
        AspectJPreferences.setDoPDEAutoRemoveImport(false);

        RemoveAJNatureAction rna = new RemoveAJNatureAction();
        rna.selectionChanged(action, sel);

        // Remove the nature
        rna.run(action);
        assertFalse(AspectJPlugin.isAJProject(testProject));
        assertTrue(
                "Should have aspectjruntime.jar on the classpath", hasAjrtOnBuildPath(testProject)); //$NON-NLS-1$
    }

    public static boolean hasAjrtOnBuildPath(IProject project)
            throws CoreException {
        IJavaProject javaProject = JavaCore.create(project);
        IClasspathEntry[] originalCP = javaProject.getRawClasspath();

        // Go through each current classpath entry one at a time. If it
        // is not a reference to the aspectjrt.jar then do not add it
        // to the collection of new classpath entries.
        for (int i = 0; i < originalCP.length; i++) {
            IPath path = originalCP[i].getPath();
            if (path.toOSString().endsWith("ASPECTJRT_LIB") //$NON-NLS-1$
                    || path.toOSString().endsWith("aspectjrt.jar")) { //$NON-NLS-1$
                return true;
            }
            if (originalCP[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                if (path.segment(0).equals(AspectJPlugin.ASPECTJRT_CONTAINER)) {
                    return true;
                }
            }
        }// end for
        return false;
    }
}