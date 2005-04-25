/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.codeconversion;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.internal.codeconversion.CodeChecker;
import org.eclipse.ajdt.internal.core.CoreUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 *  
 */
public class CodeCheckerTest extends AJDTCoreTestCase {

	/**
	 * Check that CodeChecker.containsAspectJConstructs() returns true for every
	 * type in the given list, and false for everything else.
	 * 
	 * @throws Exception
	 */
	public void testContainsAspectJConstructs() throws Exception {
		IProject project = createPredefinedProject("Spacewar Example");

		final List trueList = new ArrayList();
		trueList.add("Ship"); // pointcut in a class
		trueList.add("RegistrySynchronization"); // aspect
		trueList.add("Registry"); // inner aspect in a class
		trueList.add("GameSynchronization"); // aspect
		trueList.add("EnsureShipIsAlive"); // aspect
		trueList.add("Display2"); // inner aspect in a class
		trueList.add("Display1"); // inner aspect in a class
		trueList.add("Display"); // inner aspect in a class
		trueList.add("Debug"); // aspect and class
		trueList.add("Coordinator"); // abstract aspect with inner classes
		
		project.accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (resource.getType() == IResource.FILE) {
					String name = resource.getName();
					if (CoreUtils.ASPECTJ_SOURCE_FILTER.accept(name)) {
						boolean ret = CodeChecker
								.containsAspectJConstructs((IFile) resource);
						int index = name.lastIndexOf('.');
						if (index != -1) {
							name = name.substring(0, index);
						}
						name = name.intern();
						if (trueList.contains(name)) {
							if (ret) {
								trueList.remove(name);
							} else {
								fail("Returned false from CodeChecker.containsAspectJConstructs for "
										+ name + ", expected true");
							}
						} else if (ret) {
							fail("Returned true from CodeChecker.containsAspectJConstructs for "
									+ name + ", expected false");
						}
					}
				}
				return true;
			}
		});
		assertTrue("Didn't correctly identify all resources. Missed: "
				+ trueList, trueList.size() == 0);

		deleteProject(project);
	}
}
