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

import org.eclipse.contribution.xref.core.IXReferenceAdapter;
import org.eclipse.contribution.xref.core.XReferenceAdapterFactory;

import junit.framework.TestCase;

/**
 * @author hawkinsh
 *
 */
public class XReferenceAdapterFactoryTest extends TestCase {

	private XReferenceAdapterFactory xraf;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		xraf = new XReferenceAdapterFactory();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetWrongAdapter() {
		Object adapter = xraf.getAdapter(new Object(),String.class);
		assertNull("We only adapt to IXReferenceAdapter",adapter); //$NON-NLS-1$
	}

	public void testGetAdapter() {
		Object o = new Object();
		Object adapter = xraf.getAdapter(o,IXReferenceAdapter.class);
		assertNotNull(adapter);
        assertTrue(adapter instanceof IXReferenceAdapter);
        IXReferenceAdapter xra = (IXReferenceAdapter)adapter;
		assertEquals(o,xra.getReferenceSource());       
	}
    
    	
	public void testGetAdapterList() {
		@SuppressWarnings("rawtypes")
        Class[] adaptedClasses = xraf.getAdapterList();
		assertEquals(1,adaptedClasses.length);
		assertEquals(IXReferenceAdapter.class,adaptedClasses[0]);
	}

}
