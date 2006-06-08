/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.javamodel.elements;

import org.eclipse.ajdt.ui.tests.javamodel.AbstractTestCase;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceType;

/**
 * 
 * @author Luzius Meisser
 */
public class AJCompilationUnitTest extends AbstractTestCase {

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetChildren() throws JavaModelException{
		IJavaElement[] children = unit.getChildren();
		
		if (children == null)
			fail("Could not get children of CompilationUnit"); //$NON-NLS-1$

		if (children.length != 1)
			fail("Wrong number of children in CompilationUnit. (" + children.length + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		
	}

	public void testGetBuffer() throws JavaModelException{
		IBuffer fake = unit.getBuffer();
		unit.requestOriginalContentMode();
		IBuffer orig = unit.getBuffer();
		unit.discardOriginalContentMode();
		if (fake == orig)
			fail("CompilationUnit did not provide different buffers in different modes."); //$NON-NLS-1$
		
		
	}

	public void testOriginalContentMode() {
		if (unit.isInOriginalContentMode())
			fail("AJCompilationUnit should not be in original mode at startup."); //$NON-NLS-1$
		
		unit.requestOriginalContentMode();
		
		if (!unit.isInOriginalContentMode())
			fail("Request originalContentMode did not succeed."); //$NON-NLS-1$
		
		unit.discardOriginalContentMode();
		
		if (unit.isInOriginalContentMode())
			fail("Discard originalContentMode did not succeed."); //$NON-NLS-1$
		
	}


	public void testFindPrimaryType() {
		IType type = unit.findPrimaryType();
		
		if (!(type instanceof SourceType))
			fail("Incorrect class of main type."); //$NON-NLS-1$
		
		if (!"Aspect".equals(type.getElementName())) //$NON-NLS-1$
			fail("Incorrect main type."); //$NON-NLS-1$
		
	}

	public void testGetMainTypeName() {
		if (!"Aspect".equals(new String(unit.getMainTypeName()))) //$NON-NLS-1$
			fail("Could not get correct main type name."); //$NON-NLS-1$
	}

}
