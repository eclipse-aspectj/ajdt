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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.contribution.xref.core.IXReference;
import org.eclipse.contribution.xref.core.XReferenceAdapter;
import org.eclipse.contribution.xref.core.XReferenceProviderDefinition;
import org.eclipse.contribution.xref.core.XReferenceProviderManager;
import org.eclipse.core.runtime.IAdaptable;

/**
 * @author hawkinsh
 *  
 */
public class XReferenceAdapterTest extends TestCase {

	public void testGetReferenceSource() {
		AdaptableString o = new AdaptableString();
		XReferenceAdapter xra = new XReferenceAdapter(o);
		assertEquals(o, xra.getReferenceSource());
	}

	public void testGetXReferences() {
	    AdaptableString o = new AdaptableString();
		XReferenceAdapter xra = new XReferenceAdapter(o);
		Collection<IXReference> references = xra.getXReferences();
		assertEquals(0, references.size());
		XReferenceAdapter xra2 = new XReferenceAdapter(new AdaptableString("more lower case")); //$NON-NLS-1$
		references = xra2.getXReferences();
		assertEquals(1, references.size());

		IXReference xref = references.iterator().next();
		assertEquals("In Upper Case", xref.getName()); //$NON-NLS-1$
		Iterator<IAdaptable> it = xref.getAssociates();
		int numAssociates = 0;
		while (it.hasNext()) {
		    AdaptableString element = (AdaptableString) it.next();
			assertEquals("MORE LOWER CASE", element.getVal()); //$NON-NLS-1$
			numAssociates++;
		}
		assertEquals(1, numAssociates);

		XReferenceProviderManager manager =
			XReferenceProviderManager.getManager();
		List<XReferenceProviderDefinition> providers = manager.getRegisteredProviders();
		for (XReferenceProviderDefinition provider : providers) {
    		provider.setEnabled(false);            
        }

		XReferenceAdapter xra3 = new XReferenceAdapter(new AdaptableString("more lower case")); //$NON-NLS-1$
		references = xra3.getXReferences();
		assertEquals(0, references.size());

		for (XReferenceProviderDefinition provider : providers) {
    		provider.setEnabled(true);
        }		
	}
}
