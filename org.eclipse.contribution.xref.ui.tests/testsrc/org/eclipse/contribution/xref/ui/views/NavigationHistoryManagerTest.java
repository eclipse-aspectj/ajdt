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
package org.eclipse.contribution.xref.ui.views;

import org.eclipse.contribution.xref.internal.ui.actions.NavigationHistoryManager;

import junit.framework.TestCase;

/**
 * @author hawkinsh
 *  
 */
public class NavigationHistoryManagerTest extends TestCase {

	private NavigationHistoryManager nhm = null;

	/**
	 * Constructor for NavigationHistoryManagerTest.
	 * 
	 * @param name
	 */
	public NavigationHistoryManagerTest(String name) {
		super(name);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		nhm = new NavigationHistoryManager();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testHasBack() {
		assertFalse("Empty history cannot go back", nhm.hasBack());
		Object o = new Object();
		nhm.nowLeaving(o);
		assertTrue("Back function available after navigation", nhm.hasBack());
		nhm.goBack(o);
		assertFalse("Back navigation should empty stack", nhm.hasBack());
	}

	public void testHasForward() {
		assertFalse("Empty history cannot go forward", nhm.hasForward());
		Object o = new Object();
		nhm.nowLeaving(o);
		nhm.goBack(o);
		assertTrue(
			"Forward navigation available after back navigation",
			nhm.hasForward());
	}

	public void testGoBack() {
		Object o = new Object();
		nhm.nowLeaving(o);
		assertTrue(
			"if we go back, we get back to where we came from",
			nhm.peekBack().equals(o));
		Object bo = new Object();
		assertTrue(
			"we go back to where we came from",
			nhm.goBack(bo).equals(o));
	}

	public void testGoForward() {
		Object o = new Object();
		nhm.nowLeaving(o);
		Object bo = new Object();
		nhm.goBack(bo);
		assertTrue(
			"if we go forward, we get to where we came from",
			nhm.peekForward().equals(bo));
		assertTrue(
			"we go forward to where we came from",
			nhm.goForward(o).equals(bo));
	}

}
