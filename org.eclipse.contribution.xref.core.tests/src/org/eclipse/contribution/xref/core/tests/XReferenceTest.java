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
		XReference xref = new XReference("extends");
		assertEquals("extends",xref.getName());
		Iterator it = xref.getAssociates();
		assertFalse(it.hasNext());
	}

	/*
	 * Class to test for void XReference(String, Set)
	 */
	public void testXReferenceStringSet() {
		Set s = new HashSet();
		s.add("one");
		s.add("two");
		XReference xref = new XReference("extends",s);
		assertEquals("extends",xref.getName());
		int numElements = 0;
		for (Iterator iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = (String) iter.next();
			assertTrue(element.equals("one") || element.equals("two"));			
		}
		assertEquals(2,numElements);
	}

	public void testAddAssociate() {
		XReference xref = new XReference("extends");
		xref.addAssociate("one");
		int numElements = 0;
		for (Iterator iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = (String) iter.next();
			assertTrue(element.equals("one"));			
		}
		assertEquals(1,numElements);
		xref.addAssociate("two");
		numElements = 0;
		for (Iterator iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = (String) iter.next();
			assertTrue(element.equals("one") || element.equals("two"));			
		}
		assertEquals(2,numElements);
		xref.addAssociate("one");
		numElements = 0;
		for (Iterator iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = (String) iter.next();
			assertTrue(element.equals("one") || element.equals("two"));			
		}
		assertEquals(2,numElements);		
	}

	public void testRemoveAssociate() {
		XReference xref = new XReference("extends");
		xref.addAssociate("one");
		xref.addAssociate("two");
		xref.removeAssociate("one");
		int numElements = 0;
		for (Iterator iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = (String) iter.next();
			assertTrue(element.equals("two"));			
		}
		assertEquals(1,numElements);
		xref.removeAssociate("three");
		numElements = 0;
		for (Iterator iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = (String) iter.next();
			assertTrue(element.equals("two"));			
		}
		assertEquals(1,numElements);
		xref.removeAssociate("two");
		assertFalse(xref.getAssociates().hasNext());		
	}

}
