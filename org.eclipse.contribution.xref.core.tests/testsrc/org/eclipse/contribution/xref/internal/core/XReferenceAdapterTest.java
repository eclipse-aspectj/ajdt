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
package org.eclipse.contribution.xref.internal.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.contribution.xref.core.IXReference;

/**
 * @author hawkinsh
 *  
 */
public class XReferenceAdapterTest extends TestCase {

	public void testGetReferenceSource() {
		Object o = new Object();
		XReferenceAdapter xra = new XReferenceAdapter(o);
		assertEquals(o, xra.getReferenceSource());
	}

	public void testGetXReferences() {
		Object o = new Object();
		XReferenceAdapter xra = new XReferenceAdapter(o);
		Collection references = xra.getXReferences();
		assertEquals(0, references.size());
		XReferenceAdapter xra2 = new XReferenceAdapter("more lower case");
		references = xra2.getXReferences();
		assertEquals(1, references.size());

		IXReference xref = (IXReference) references.iterator().next();
		assertEquals("In Upper Case", xref.getName());
		Iterator it = xref.getAssociates();
		int numAssociates = 0;
		while (it.hasNext()) {
			String element = (String) it.next();
			assertEquals("MORE LOWER CASE", element);
			numAssociates++;
		}
		assertEquals(1, numAssociates);

		XReferenceProviderManager manager =
			XReferenceProviderManager.getManager();
		List providers = manager.getRegisteredProviders();
		for (Iterator iter = providers.iterator(); iter.hasNext();) {
            XReferenceProviderDefinition def = (XReferenceProviderDefinition) iter.next();
    		def.setEnabled(false);            
        }

		XReferenceAdapter xra3 = new XReferenceAdapter("more lower case");
		references = xra3.getXReferences();
		assertEquals(0, references.size());

		for (Iterator iter = providers.iterator(); iter.hasNext();) {
            XReferenceProviderDefinition def = (XReferenceProviderDefinition) iter.next();
    		def.setEnabled(true);
        }

	}

}
