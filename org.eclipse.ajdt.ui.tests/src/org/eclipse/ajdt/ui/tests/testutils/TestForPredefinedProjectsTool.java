/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Luzius Meisser  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests.testutils;

import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Tests the PredefinedProject class
 * 
 * @author Luzius Meisser
 */
public class TestForPredefinedProjectsTool extends UITestCase {
	
	public void testPredefinedProjectsTool() throws CoreException{
		IProject p = createPredefinedProject("Hello World Project"); //$NON-NLS-1$
		if (p == null)
			fail("Project 'Hello World Project' could not be imported."); //$NON-NLS-1$
		IJavaProject jp = JavaCore.create(p);
		try {
			IType type = jp.findType("", "HelloWorld"); //$NON-NLS-1$ //$NON-NLS-2$
			if (type == null)
				fail("Project 'Hello World Project' was not imported correctly."); //$NON-NLS-1$
		} catch (JavaModelException e) {
			fail("Project 'Hello World Project' was not imported correctly."); //$NON-NLS-1$
		}
		
		p = createPredefinedProject("project name that (hopefully) does not exist"); //$NON-NLS-1$
		if (p != null)
			fail("Could import project that does not exist."); //$NON-NLS-1$		
	}
	
	/**
	 * Tests whether you can access a project which was originally closed
	 * i.e. is it reopened?
	 * @throws Exception
	 */
	public void testProjectsToolWithClosedProjects() throws Exception {
		IProject p = createPredefinedProject("Hello World Project"); //$NON-NLS-1$
		p.close(null);
		waitForJobsToComplete();
		
		IProject p2 = createPredefinedProject("Hello World Project"); //$NON-NLS-1$
		assertTrue("project should now be open",p2.isOpen()); //$NON-NLS-1$
	}
	
}
