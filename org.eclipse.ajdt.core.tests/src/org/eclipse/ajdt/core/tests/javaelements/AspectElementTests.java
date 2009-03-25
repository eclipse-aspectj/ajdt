/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.javaelements;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.DeclareElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.javaelements.PointcutElement;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IType;

public class AspectElementTests extends AJDTCoreTestCase {

	/**
	 * AspectElement.getPointcuts() should return an array of pointcut elements
	 * 
	 * @throws Exception
	 */
	public void testGetPointcuts() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		IResource bp = project.findMember("src/bean/BoundPoint.aj"); //$NON-NLS-1$
		assertNotNull("Couldn't find BoundPoint.aj file", bp); //$NON-NLS-1$
		AJCompilationUnit cu = AJCompilationUnitManager.INSTANCE
				.getAJCompilationUnit((IFile) bp);
		IType[] types = cu.getAllTypes();
		assertTrue(
				"Compilation unit should contain exactly one type", types.length == 1); //$NON-NLS-1$
		assertTrue(
				"Contained type should be an AspectElement", types[0] instanceof AspectElement); //$NON-NLS-1$
		AspectElement aspect = (AspectElement) types[0];
		PointcutElement[] pointcuts = aspect.getPointcuts();
		assertNotNull(
				"AspectElement.getPointcuts() should not return null", pointcuts); //$NON-NLS-1$
		assertTrue(
				"AspectElement.getPointcuts() should return exactly one pointcut", pointcuts.length == 1); //$NON-NLS-1$
		assertEquals(
				"AspectElement.getPointcuts() should return a pointcut called setter", pointcuts[0].getElementName(), "setter"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * AspectElement.getAdvice() should return an array of advice elements
	 * 
	 * @throws Exception
	 */
	public void testGetAdvice() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		IResource bp = project.findMember("src/bean/BoundPoint.aj"); //$NON-NLS-1$
		assertNotNull("Couldn't find BoundPoint.aj file", bp); //$NON-NLS-1$
		AJCompilationUnit cu = AJCompilationUnitManager.INSTANCE
				.getAJCompilationUnit((IFile) bp);
		IType[] types = cu.getAllTypes();
		assertTrue(
				"Compilation unit should contain exactly one type", types.length == 1); //$NON-NLS-1$
		assertTrue(
				"Contained type should be an AspectElement", types[0] instanceof AspectElement); //$NON-NLS-1$
		AspectElement aspect = (AspectElement) types[0];
		AdviceElement[] advice = aspect.getAdvice();
		assertNotNull(
				"AspectElement.getAdvice() should not return null", advice); //$NON-NLS-1$
		assertTrue(
				"AspectElement.getAdvice() should return exactly one advice element", advice.length == 1); //$NON-NLS-1$
		assertEquals(
				"AspectElement.getAdvice() should return an advice element called around", advice[0].getElementName(), "around"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * AspectElement.getDeclares() should return an array of declare elements
	 * 
	 * @throws Exception
	 */
	public void testGetDeclares() throws Exception {
		IProject project = createPredefinedProject("Comparisons"); //$NON-NLS-1$
		IResource bp = project.findMember("src/foo/MyAspect.aj"); //$NON-NLS-1$
		assertNotNull("Couldn't find MyAspect.aj file", bp); //$NON-NLS-1$
		AJCompilationUnit cu = AJCompilationUnitManager.INSTANCE
				.getAJCompilationUnit((IFile) bp);
		IType[] types = cu.getAllTypes();
		assertTrue(
				"Compilation unit should contain exactly one type", types.length == 1); //$NON-NLS-1$
		assertTrue(
				"Contained type should be an AspectElement", types[0] instanceof AspectElement); //$NON-NLS-1$
		AspectElement aspect = (AspectElement) types[0];
		DeclareElement[] declares = aspect.getDeclares();
		assertNotNull(
				"AspectElement.getDeclares() should not return null", declares); //$NON-NLS-1$
		assertTrue(
				"AspectElement.getDeclares() should return exactly one declare element", declares.length == 1); //$NON-NLS-1$
		assertEquals(
				"AspectElement.getDeclares() should return a declare element with the correct name", declares[0].getElementName(), "declare warning"); //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * AspectElement.getDeclares() should return an array of declare elements
	 * 
	 * @throws Exception
	 */
	public void testGetITDs() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		IResource bp = project.findMember("src/bean/BoundPoint.aj"); //$NON-NLS-1$
		assertNotNull("Couldn't find BoundPoint.aj file", bp); //$NON-NLS-1$
		AJCompilationUnit cu = AJCompilationUnitManager.INSTANCE
				.getAJCompilationUnit((IFile) bp);
		IType[] types = cu.getAllTypes();
		assertTrue(
				"Compilation unit should contain exactly one type", types.length == 1); //$NON-NLS-1$
		assertTrue(
				"Contained type should be an AspectElement", types[0] instanceof AspectElement); //$NON-NLS-1$
		AspectElement aspect = (AspectElement) types[0];
		IntertypeElement[] itds = aspect.getITDs();
		assertNotNull("AspectElement.getITDs() should not return null", itds); //$NON-NLS-1$
		assertEquals(
				"AspectElement.getITDs() didn't return the expected number of ITDs", 6, itds.length); //$NON-NLS-1$
		assertEquals(
				"AspectElement.getITDs() should return a intertype element with the correct name", itds[0].getElementName(), "Point.support"); //$NON-NLS-1$//$NON-NLS-2$
	}
}
