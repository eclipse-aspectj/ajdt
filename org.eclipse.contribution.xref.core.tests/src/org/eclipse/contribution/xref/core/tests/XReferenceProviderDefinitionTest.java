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

import java.util.Iterator;
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
		List providers = manager.getRegisteredProviders();
		for (Iterator iter = providers.iterator(); iter.hasNext();) {
            XReferenceProviderDefinition element = (XReferenceProviderDefinition) iter.next();
            if (element.getProvider().getProviderDescription().equals("My Description")) {
                def = element;
            }
        }
		if (def == null) {
			def = (XReferenceProviderDefinition)providers.get(0);
            
        } 
		
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetLabel() {

		assertEquals("My Label",def.getLabel());
	}

	public void testGetID() {
		assertEquals("org.eclipse.contribution.xref.core.tests.testProvider",def.getID());
	}

	public void testIsAndSetEnabled() {
		assertEquals(true,def.isEnabled());
		def.setEnabled(false);
		assertEquals(false,def.isEnabled());
		def.setEnabled(true);
	}

	public void testGetDescription() {
		assertEquals("My Description",def.getDescription());
	}
	
	public void testSafeExecution() {
		TestProvider.beBad = true;
		try {
			def.getDescription();
		} catch( RuntimeException rEx) {
			fail("IXReferenceProvider.SafeExecution aspect should have protected us from the exception");
		}
		TestProvider.beBad = false;
	}

}
