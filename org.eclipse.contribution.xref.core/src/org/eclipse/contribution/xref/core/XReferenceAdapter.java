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
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Convenience implementation of IXReferenceAdapter
 * @see org.eclipse.contribution.xref.core.IXReferenceAdapter
 *  
 */
public class XReferenceAdapter extends PlatformObject implements IXReferenceAdapter {

	private IAdaptable referenceSource;
	
	/**
	 * @param source
	 *            the object for which we're providing cross references
	 */
	public XReferenceAdapter(IAdaptable source) {
		referenceSource = source;
	}

	public IJavaElement[] getExtraChildren(IJavaElement je) {
		XReferenceProviderManager manager =
			XReferenceProviderManager.getManager();
		List<IXReferenceProvider> providers = manager.getProvidersFor(referenceSource);
		for (IXReferenceProvider provider : providers) {
			return provider.getExtraChildren(je);
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contributions.xref.core.IXReferenceAdapter#getReferenceSource()
	 */
	public IAdaptable getReferenceSource() {
		return referenceSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contributions.xref.core.IXReferenceAdapter#getXReferences()
	 */
	@SuppressWarnings("deprecation")
    public Collection<IXReference> getXReferences() {		
		XReferenceProviderManager manager =
			XReferenceProviderManager.getManager();
		List<IXReferenceProvider> providers = manager.getProvidersFor(referenceSource);
		List<IXReference> xrefs = new ArrayList<IXReference>();
		boolean isInplace = manager.getIsInplace();
		for (IXReferenceProvider provider : providers) {
		    List<String> filter;
		    Collection<IXReference> c;
            if (isInplace) {
			    filter = provider.getFilterCheckedInplaceList();
			} else {
			    filter = provider.getFilterCheckedList();
			}
			if (provider instanceof IXReferenceProviderExtension) {
			    c = ((IXReferenceProviderExtension) provider).getXReferences(referenceSource, filter);
			} else {
			    c = provider.getXReferences(referenceSource, filter);
			}
			if (c != null) {
			    xrefs.addAll(c);                
			}
		}
		return xrefs;
	}
}
