/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Luzius Meisser  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.test.utils;

import junit.framework.TestCase;

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
public class TestForPredefinedProjectsTool extends TestCase {
	
	public void testPredefinedProjectsTool() throws CoreException{
		IProject p = Utils.getPredefinedProject("Hello World Project");
		if (p == null)
			fail("Project 'Hello World Project' could not be imported.");
		IJavaProject jp = JavaCore.create(p);
		try {
			IType type = jp.findType("", "HelloWorld");
			if (type == null)
				fail("Project 'Hello World Project' was not imported correctly.");
		} catch (JavaModelException e) {
			fail("Project 'Hello World Project' was not imported correctly.");
		}
		Utils.deleteProject(p);
		
		p = Utils.getPredefinedProject("project name that (hopefully) does not exist");
		if (p != null)
			fail("Could import project that does not exist.");
		
		
	}
	
	/**
	 * Tests whether you can access a project which was originally closed
	 * i.e. is it reopened?
	 * @throws Exception
	 */
	public void testProjectsToolWithClosedProjects() throws Exception {
		IProject p = Utils.getPredefinedProject("Hello World Project");
		p.close(null);
		Utils.waitForJobsToComplete();
		
		IProject p2 = Utils.getPredefinedProject("Hello World Project");
		assertTrue("project should now be open",p2.isOpen());

		Utils.deleteProject(p2);
	}
	
}
