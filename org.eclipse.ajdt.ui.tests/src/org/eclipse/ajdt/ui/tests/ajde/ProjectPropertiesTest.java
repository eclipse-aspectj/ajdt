/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.ajde;

import java.io.File;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * 
 * 
 * @author mchapman
 */
public class ProjectPropertiesTest extends UITestCase {

	/* bug 82258 */
	public void testCaseInsensitiveDriveLetters() throws Exception {
		IProject project = createPredefinedProject("Hello World Project"); //$NON-NLS-1$

		// create two paths, one where the drive letter (if there is one) has a
		// different case to the other
		String fullpath1 = project.getLocation().toOSString() + File.separator
				+ "src" + File.separator + "HelloWorld.java"; //$NON-NLS-1$ //$NON-NLS-2$
		String fullpath2;

		// check for windows style drive letter
		if ((fullpath1.charAt(1) == ':')
				&& (((fullpath1.charAt(0) >= 'a') && (fullpath1.charAt(0) <= 'z')) || ((fullpath1
						.charAt(0) >= 'A') && (fullpath1.charAt(0) <= 'Z')))) {
			if (Character.isUpperCase(fullpath1.charAt(0))) {
				fullpath2 = Character.toLowerCase(fullpath1.charAt(0))
						+ fullpath1.substring(1);
			} else {
				fullpath2 = Character.toUpperCase(fullpath1.charAt(0))
						+ fullpath1.substring(1);
			}
		} else {
			fullpath2 = new String(fullpath1);
		}

		// now make sure both versions of the path cause the resource to be
		// found
		IResource res1 = AspectJUIPlugin.getDefault()
				.getAjdtProjectProperties().findResource(fullpath1, project);
		assertNotNull("Regression of bug 82258: handling of windows-style drive letters",res1); //$NON-NLS-1$
		
		IResource res2 = AspectJUIPlugin.getDefault()
				.getAjdtProjectProperties().findResource(fullpath2, project);
		assertNotNull("Regression of bug 82258: handling of windows-style drive letters",res2); //$NON-NLS-1$

	}
	
	/**
	 * Bug 82341  
	 */
	public void testCaseInsensitive() throws Exception {
		IProject project = createPredefinedProject("Hello World Project"); //$NON-NLS-1$

		// create two paths, one where the drive letter (if there is one) has a
		// different case to the other
		String fullpath1 = project.getLocation().toOSString() + File.separator
				+ "src" + File.separator + "HelloWorld.java"; //$NON-NLS-1$ //$NON-NLS-2$
		String fullpath2;
		
		// if on windows then change the case
		if ((fullpath1.charAt(1) == ':')) {
			fullpath2 = project.getLocation().toOSString().toUpperCase() + File.separator
							+ "src" + File.separator + "HelloWorld.java"; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			fullpath2 = fullpath1;
		}	
		// now make sure both the resources can be found
		IResource res1 = AspectJUIPlugin.getDefault()
				.getAjdtProjectProperties().findResource(fullpath1, project);
		assertNotNull("Regression of bug 82341",res1); //$NON-NLS-1$
		
		IResource res2 = AspectJUIPlugin.getDefault()
				.getAjdtProjectProperties().findResource(fullpath2, project);
		assertNotNull("Regression of bug 82341",res2); //$NON-NLS-1$
	}
	
	public void testCaseInsensitiveNoSrcFolder() throws Exception {
		IProject project = createPredefinedProject("WithoutSourceFolder"); //$NON-NLS-1$

		// create two paths, one where the drive letter (if there is one) has a
		// different case to the other
		String fullpath1 = project.getLocation().toOSString() + File.separator
				+ "C.java"; //$NON-NLS-1$
		String fullpath2;
		
		// if on windows then change the case
		if ((fullpath1.charAt(1) == ':')) {
			fullpath2 = project.getLocation().toOSString().toUpperCase() + File.separator
							+ "C.java"; //$NON-NLS-1$
		} else {
			fullpath2 = fullpath1;
		}
		// now make sure both the resources can be found
		IResource res1 = AspectJUIPlugin.getDefault()
				.getAjdtProjectProperties().findResource(fullpath1, project);
		assertNotNull("Regression of bug 82341",res1); //$NON-NLS-1$
		
		IResource res2 = AspectJUIPlugin.getDefault()
				.getAjdtProjectProperties().findResource(fullpath2, project);
		assertNotNull("Regression of bug 82341",res2); //$NON-NLS-1$
		
	}
}