/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.core.tests;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.contribution.xref.core.XReference;
import org.eclipse.core.runtime.IAdaptable;

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
		Iterator<IAdaptable> it = xref.getAssociates();
		assertFalse(it.hasNext());
	}

	/*
	 * Class to test for void XReference(String, Set)
	 */
	public void testXReferenceStringSet() {
		Set<IAdaptable> s = new HashSet<IAdaptable>();
		s.add(new AdaptableString("one"));
		s.add(new AdaptableString("two"));
		XReference xref = new XReference("extends",s);
		assertEquals("extends",xref.getName());
		int numElements = 0;
		for (Iterator<IAdaptable> iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = ((AdaptableString) iter.next()).getVal();
			assertTrue(element.equals("one") || element.equals("two"));
		}
		assertEquals(2,numElements);
	}

	public void testAddAssociate() {
		XReference xref = new XReference("extends");
		xref.addAssociate(new AdaptableString("one"));
		int numElements = 0;
		for (Iterator<IAdaptable> iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = ((AdaptableString) iter.next()).getVal();
			assertTrue(element.equals("one"));			
		}
		assertEquals(1,numElements);
		xref.addAssociate(new AdaptableString("two"));
		numElements = 0;
		for (Iterator<IAdaptable> iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = ((AdaptableString) iter.next()).getVal();
			assertTrue(element.equals("one") || element.equals("two"));
		}
		assertEquals(2,numElements);
		xref.addAssociate(new AdaptableString("one"));
		numElements = 0;
		for (Iterator<IAdaptable> iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = ((AdaptableString) iter.next()).getVal();
			assertTrue(element.equals("one") || element.equals("two"));
		}
		assertEquals(2,numElements);		
	}

	public void testRemoveAssociate() {
		XReference xref = new XReference("extends");
		xref.addAssociate(new AdaptableString("one"));
		xref.addAssociate(new AdaptableString("two"));
		xref.removeAssociate(new AdaptableString("one"));
		int numElements = 0;
		for (Iterator<IAdaptable> iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = ((AdaptableString) iter.next()).getVal();
			assertTrue(element.equals("two"));			
		}
		assertEquals(1,numElements);
		xref.removeAssociate(new AdaptableString("three"));
		numElements = 0;
		for (Iterator<IAdaptable> iter = xref.getAssociates(); iter.hasNext();) {
			numElements++;
			String element = ((AdaptableString) iter.next()).getVal();
			assertTrue(element.equals("two"));			
		}
		assertEquals(1,numElements);
		xref.removeAssociate(new AdaptableString("two"));
		assertFalse(xref.getAssociates().hasNext());		
	}

}
