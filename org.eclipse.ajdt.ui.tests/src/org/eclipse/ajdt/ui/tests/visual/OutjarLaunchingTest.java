/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.visual;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;

/**
 * Visual test for bug 103232 - run projects with outjars properly
 */
public class OutjarLaunchingTest extends VisualTestCase {

	private String outputStringStart = "p1 ="; //$NON-NLS-1$
	
	public void testLaunchingWithAnOutJar() throws Exception {
		IProject project = createPredefinedProject("Outjar Example"); //$NON-NLS-1$
		assertTrue("The Outjar Example project should have been created", project != null); //$NON-NLS-1$
		IJavaProject jp = JavaCore.create(project);
		String outJar = AspectJCorePreferences.getProjectOutJar(project);
		assertTrue("The Outjar Example project should have an outjar", outJar != null && outJar.equals("bean.jar")); //$NON-NLS-1$ //$NON-NLS-2$
		IPackageFragment p1 = jp.getPackageFragmentRoot(project.findMember("src")).getPackageFragment("bean"); //$NON-NLS-1$ //$NON-NLS-2$
		ICompilationUnit demo = p1.getCompilationUnit("Demo.java"); //$NON-NLS-1$
		selectInPackageExplorer(demo);
		
		// Run as AspectJ/Java Application
		postKeyDown(SWT.ALT);
		postKey('r');	
		postKeyUp(SWT.ALT);
		postKey('s');
		postKey(SWT.CR);
		
		waitForJobsToComplete();

		String output = getConsoleViewContents();
		assertNotNull(output);
		assertTrue("program did not run correctly", output.indexOf(outputStringStart) != -1); //$NON-NLS-1$
	}
	
}
