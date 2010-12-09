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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.contribution.xref.core.XReferenceProviderDefinition;
import org.eclipse.contribution.xref.core.XReferenceProviderManager;

/**
 * @author hawkinsh
 *
 */
public class XReferenceProviderDefinitionTest extends TestCase {
    
	XReferenceProviderDefinition def = null;
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		XReferenceProviderManager manager = XReferenceProviderManager.getManager();
		List<XReferenceProviderDefinition> providers = manager.getRegisteredProviders();
		for (XReferenceProviderDefinition providerDef : providers) {
            if (providerDef.getProvider().getProviderDescription().equals("My Description")) { //$NON-NLS-1$
                def = providerDef;
            }
        }
		if (def == null) {
			def = providers.get(0);
            
        } 
		
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetLabel() {

		assertEquals("My Label",def.getLabel()); //$NON-NLS-1$
	}

	public void testGetID() {
		assertEquals("org.eclipse.contribution.xref.core.tests.testProvider",def.getID()); //$NON-NLS-1$
	}

	public void testIsAndSetEnabled() {
		assertEquals(true,def.isEnabled());
		def.setEnabled(false);
		assertEquals(false,def.isEnabled());
		def.setEnabled(true);
	}

	public void testGetDescription() {
		assertEquals("My Description",def.getDescription()); //$NON-NLS-1$
	}
	
	public void testSafeExecution() {
		TestProvider.beBad = true;
		try {
			def.getDescription();
		} catch( RuntimeException rEx) {
			fail("IXReferenceProvider.SafeExecution aspect should have protected us from the exception"); //$NON-NLS-1$
		}
		TestProvider.beBad = false;
	}

	public void testSetCheckedFilters() {
		List<String> li = new ArrayList<String>();
		li.add("Item 1"); //$NON-NLS-1$
		def.setCheckedFilters(li);
		List<String> returned = def.getCheckedFilters();
		assertTrue("One item should be checked", returned.size()==1); //$NON-NLS-1$
		// Reset
		def.setCheckedFilters(new ArrayList<String>());		
	}
	
	public void testSetCheckedInplaceFilters() {
		List<String> li = new ArrayList<String>();
		li.add("Item 1"); //$NON-NLS-1$
		def.setCheckedInplaceFilters(li);
		List<String> returned = def.getCheckedInplaceFilters();
		assertTrue("One item should be checked", returned.size()==1); //$NON-NLS-1$
		// Reset
		def.setCheckedInplaceFilters(new ArrayList<String>());		
	}
}
