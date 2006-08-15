/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Matt Chapman   - initial version
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.builder;

import java.io.File;

import org.eclipse.ajdt.core.builder.CoreOutputLocationManager;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class CoreOutputLocationManagerTest extends AJDTCoreTestCase {

	public void testOutputLocationManager() throws Exception {
		IProject project = createPredefinedProject("MultipleOutputFolders"); //$NON-NLS-1$
		IJavaProject jp = JavaCore.create(project);
		try {
			CoreOutputLocationManager om = new CoreOutputLocationManager(jp);
			IFile class1 = (IFile) project.findMember("src/p1/Class1.java"); //$NON-NLS-1$
			File file1 = class1.getLocation().toFile();
			File out1 = om.getOutputLocationForClass(file1);
			assertTrue("Output location for " + class1 //$NON-NLS-1$
					+ " should end in bin. Got: " + out1, out1.toString() //$NON-NLS-1$
					.endsWith("bin")); //$NON-NLS-1$

			IFile class2 = (IFile) project.findMember("src2/p2/Class2.java"); //$NON-NLS-1$
			File file2 = class2.getLocation().toFile();
			File out2 = om.getOutputLocationForClass(file2);
			assertTrue("Output location for " + class2 //$NON-NLS-1$
					+ " should end in bin2. Got: " + out2, out2.toString() //$NON-NLS-1$
					.endsWith("bin2")); //$NON-NLS-1$

			IFile class3 = (IFile) project.findMember("src2/p2/GetInfo.aj"); //$NON-NLS-1$
			File file3 = class3.getLocation().toFile();
			File out3 = om.getOutputLocationForClass(file3);
			assertTrue("Output location for " + class3 //$NON-NLS-1$
					+ " should end in bin2. Got: " + out3, out3.toString() //$NON-NLS-1$
					.endsWith("bin2")); //$NON-NLS-1$

		} finally {
			deleteProject(project);
		}
	}

	public void testOutputLocationManagerBug153682() throws Exception {
		IProject project = createPredefinedProject("bug153682"); //$NON-NLS-1$
		IJavaProject jp = JavaCore.create(project);
		try {
			CoreOutputLocationManager om = new CoreOutputLocationManager(jp);
			IFile class1 = (IFile) project.findMember("foo/Test.java"); //$NON-NLS-1$
			File file1 = class1.getLocation().toFile();
			File out1 = om.getOutputLocationForClass(file1);
			assertTrue("Output location for " + class1 //$NON-NLS-1$
					+ " should end in bin. Got: " + out1, out1.toString() //$NON-NLS-1$
					.endsWith("bin")); //$NON-NLS-1$

			IFile class2 = (IFile) project.findMember("foo/test.properties"); //$NON-NLS-1$
			File file2 = class2.getLocation().toFile();
			File out2 = om.getOutputLocationForResource(file2);
			assertTrue("Output location for " + class2 //$NON-NLS-1$
					+ " should end in bin. Got: " + out2, out2.toString() //$NON-NLS-1$
					.endsWith("bin")); //$NON-NLS-1$
		} finally {
			deleteProject(project);
		}
	}
}
