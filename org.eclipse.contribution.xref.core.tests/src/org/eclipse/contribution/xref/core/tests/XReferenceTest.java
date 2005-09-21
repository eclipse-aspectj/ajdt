/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.core.tests;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.contribution.xref.core.XReference;

import junit.framework.TestCase;

/**
 * @author hawkinsh
 *
 */
public class XReferenceTest extends TestCase {

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

	/*
	 * Class to test for void XReference(String)
	 */
	public void testXReferenceString() {
		XReference xref = new XReference("extends"); //$NON-NLS-1$
		assertEquals("extends",xref.getName()); //$NON-NLS-1$
		Iterator it = xref.getAssociates();
		assertFalse(it.hasNext());
	}

	/*
	 * Class to test for void XReference(String, Set)
	 */
	public void testXReferenceStringSet() {
		Set s = new HashSet();
		s.add("one"); //$NON-NLS-1$
		s.add("two"); //$NON-NLS-1$
		XReference xref = new XReference("extends",s); //$NON-NLS-1$
		assertEquals("extends",xref.getName()); //$NON-NLS-1$
		int numElements = 0;
		for (Iterator iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = (String) iter.next();
			assertTrue(element.equals("one") || element.equals("two"));			 //$NON-NLS-1$ //$NON-NLS-2$
		}
		assertEquals(2,numElements);
	}

	public void testAddAssociate() {
		XReference xref = new XReference("extends"); //$NON-NLS-1$
		xref.addAssociate("one"); //$NON-NLS-1$
		int numElements = 0;
		for (Iterator iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = (String) iter.next();
			assertTrue(element.equals("one"));			 //$NON-NLS-1$
		}
		assertEquals(1,numElements);
		xref.addAssociate("two"); //$NON-NLS-1$
		numElements = 0;
		for (Iterator iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = (String) iter.next();
			assertTrue(element.equals("one") || element.equals("two"));			 //$NON-NLS-1$ //$NON-NLS-2$
		}
		assertEquals(2,numElements);
		xref.addAssociate("one"); //$NON-NLS-1$
		numElements = 0;
		for (Iterator iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = (String) iter.next();
			assertTrue(element.equals("one") || element.equals("two"));			 //$NON-NLS-1$ //$NON-NLS-2$
		}
		assertEquals(2,numElements);		
	}

	public void testRemoveAssociate() {
		XReference xref = new XReference("extends"); //$NON-NLS-1$
		xref.addAssociate("one"); //$NON-NLS-1$
		xref.addAssociate("two"); //$NON-NLS-1$
		xref.removeAssociate("one"); //$NON-NLS-1$
		int numElements = 0;
		for (Iterator iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = (String) iter.next();
			assertTrue(element.equals("two"));			 //$NON-NLS-1$
		}
		assertEquals(1,numElements);
		xref.removeAssociate("three"); //$NON-NLS-1$
		numElements = 0;
		for (Iterator iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = (String) iter.next();
			assertTrue(element.equals("two"));			 //$NON-NLS-1$
		}
		assertEquals(1,numElements);
		xref.removeAssociate("two"); //$NON-NLS-1$
		assertFalse(xref.getAssociates().hasNext());		
	}

}
