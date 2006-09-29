/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Ford  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.visual.tests;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;

public class AspectpathAndInpathTest extends VisualTestCase {

	public void testAspectPath() throws Exception {

		IProject project = createPredefinedProject("jarCreatingProject"); //$NON-NLS-1$
		IResource jar = project.findMember("myJar.jar"); //$NON-NLS-1$

		selectInPackageExplorer(jar);

		//		Add to Aspectpath
		// bring up context menu
		postKeyDown(SWT.SHIFT);
		postKey(SWT.F10);
		postKeyUp(SWT.SHIFT);

		sleep();

		postKey('j'); // AspectJ Tools
		postKey('a'); // Add to Aspect path

		waitForJobsToComplete();

		String[] Aspectpath = AspectJCorePreferences
				.getProjectAspectPath(project);
		String[] seperatedAspectpath = Aspectpath[0].split(";"); //$NON-NLS-1$
		assertEquals("/jarCreatingProject/myJar.jar",seperatedAspectpath[0]); //$NON-NLS-1$

		selectInPackageExplorer(jar);
		//		Remove from Aspect Path
		postKeyDown(SWT.SHIFT);
		postKey(SWT.F10);
		postKeyUp(SWT.SHIFT);

		sleep();

		postKey('j'); // AspectJ Tools
		postKey('a'); // remove from aspect path

		waitForJobsToComplete();

		Aspectpath = AspectJCorePreferences.getProjectAspectPath(project);
		seperatedAspectpath = Aspectpath[0].split(";"); //$NON-NLS-1$
		assertEquals("",seperatedAspectpath[0]); //$NON-NLS-1$
	}

	public void testInpath() throws Exception {

		IProject project = createPredefinedProject("jarCreatingProject"); //$NON-NLS-1$
		IResource jar = project.findMember("myJar.jar"); //$NON-NLS-1$

		selectInPackageExplorer(jar);

		//		Add to Inpath
		// bring up context menu
		postKeyDown(SWT.SHIFT);
		postKey(SWT.F10);
		postKeyUp(SWT.SHIFT);

		sleep();

		postKey('j'); // AspectJ Tools
		postKey('i'); // Add to Inpath

		waitForJobsToComplete();

		String[] Inpath = AspectJCorePreferences.getProjectInPath(project);
		String[] seperatedInpath = Inpath[0].split(";"); //$NON-NLS-1$
		assertEquals("/jarCreatingProject/myJar.jar",seperatedInpath[0]); //$NON-NLS-1$

		selectInPackageExplorer(jar);
		//		Remove from InPath
		postKeyDown(SWT.SHIFT);
		postKey(SWT.F10);
		postKeyUp(SWT.SHIFT);

		sleep();

		postKey('j'); // AspectJ Tools
		postKey('i'); // remove from Inpath

		waitForJobsToComplete();

		Inpath = AspectJCorePreferences.getProjectAspectPath(project);
		seperatedInpath = Inpath[0].split(";"); //$NON-NLS-1$
		assertEquals("",seperatedInpath[0]); //$NON-NLS-1$
	}
}
