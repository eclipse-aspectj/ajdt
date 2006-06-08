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
package org.eclipse.contribution.xref.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.contribution.xref.core.IXReferenceProvider;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Convenience implementation of IXReferenceAdapter
 * @see org.eclipse.contribution.xref.core.IXReferenceAdapter
 *  
 */
public class XReferenceAdapter implements IXReferenceAdapter {

	private Object referenceSource;
	
	/**
	 * @param source
	 *            the object for which we're providing cross references
	 */
	public XReferenceAdapter(Object source) {
		referenceSource = source;
	}

	public IJavaElement[] getExtraChildren(IJavaElement je) {
		XReferenceProviderManager manager =
			XReferenceProviderManager.getManager();
		List providers = manager.getProvidersFor(referenceSource);
		for (Iterator iter = providers.iterator(); iter.hasNext();) {
			IXReferenceProvider element = (IXReferenceProvider) iter.next();
			return element.getExtraChildren(je);
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contributions.xref.core.IXReferenceAdapter#getReferenceSource()
	 */
	public Object getReferenceSource() {
		return referenceSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contributions.xref.core.IXReferenceAdapter#getXReferences()
	 */
	public Collection getXReferences() {		
		XReferenceProviderManager manager =
			XReferenceProviderManager.getManager();
		List providers = manager.getProvidersFor(referenceSource);
		List xrefs = new ArrayList();
		for (Iterator iter = providers.iterator(); iter.hasNext();) {
			IXReferenceProvider element = (IXReferenceProvider) iter.next();
			
			// Establishes which view requires the cross references, (Inplace or View) and passes the corresponding
			// checkedFilterList to the provider
			if (manager.getIsInplace()) {
				Collection c = element.getXReferences(referenceSource, element.getFilterCheckedInplaceList());
				if (c != null) {
						xrefs.addAll(c);                
		          }
			} else {
				Collection c = element.getXReferences(referenceSource, element.getFilterCheckedList());
				if (c != null) {
						xrefs.addAll(c);                
		          }
			}
		}
		return xrefs;
	}
	
}
