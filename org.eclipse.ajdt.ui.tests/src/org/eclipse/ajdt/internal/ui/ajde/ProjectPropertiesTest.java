/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * 
 * 
 * @author mchapman
 */
public class ProjectPropertiesTest extends TestCase {

	/* bug 82258 */
	public void testCaseInsensitiveDriveLetters() throws Exception {
		IProject project = Utils.createPredefinedProject("Hello World Project");

		// create two paths, one where the drive letter (if there is one) has a
		// different case to the other
		String fullpath1 = project.getLocation().toOSString() + File.separator
				+ "src" + File.separator + "HelloWorld.java";
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
		assertNotNull("Regression of bug 82258: handling of windows-style drive letters",res1);
		
		IResource res2 = AspectJUIPlugin.getDefault()
				.getAjdtProjectProperties().findResource(fullpath2, project);
		assertNotNull("Regression of bug 82258: handling of windows-style drive letters",res2);

		Utils.deleteProject(project);
	}
}