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
package org.eclipse.ajdt.core.tests.javaelements;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.PointcutElement;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IType;

public class AspectElementTests extends AJDTCoreTestCase {

	/**
	 * AspectElement.getPointcuts() should return an array of pointcut elements
	 * @throws Exception
	 */
	public void testGetPointcuts() throws Exception {
		IProject project = createPredefinedProject("Bean Example");
		try {
			IResource bp = project.findMember("src/bean/BoundPoint.aj");
			assertNotNull("Couldn't find BoundPoint.aj file", bp);
			AJCompilationUnit cu = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit((IFile)bp);
			IType[] types = cu.getAllTypes();
			assertTrue("Compilation unit should contain exactly one type", types.length==1);
			assertTrue("Contained type should be an AspectElement", types[0] instanceof AspectElement);
			AspectElement aspect = (AspectElement)types[0];
			PointcutElement[] pointcuts = aspect.getPointcuts();
			assertNotNull("AspectElement.getPointcuts() should not return null",pointcuts);
			assertTrue("AspectElement.getPointcuts() should return exactly one pointcut",pointcuts.length==1);
			assertEquals("AspectElement.getPointcuts() should return a pointcut called setter",pointcuts[0].getElementName(),"setter");
		} finally {
			deleteProject(project);
		}
	}
}
