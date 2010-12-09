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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.contribution.xref.core.IXReference;
import org.eclipse.contribution.xref.core.IXReferenceProvider;
import org.eclipse.contribution.xref.core.IXReferenceProviderExtension;
import org.eclipse.contribution.xref.core.XReference;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author hawkinsh
 *  
 */
public class TestProvider implements IXReferenceProvider, IXReferenceProviderExtension {

	private List<String> checkedFilterList;
	private List<String> checkedFilterInplaceList;
	
	public static boolean beBad = false; // for setting up test conditions
	
	public TestProvider() {
		checkedFilterList = new ArrayList<String>();
		checkedFilterInplaceList = new ArrayList<String>();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.tests.IXReferenceProvider#getClasses()
	 */
	public Class<?>[] getClasses() {
		return new Class[] { String.class };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.tests.IXReferenceProvider#getXReferences(java.lang.Object)
	 */
	public Collection<IXReference> getXReferences(IAdaptable o, List<String> checked) {
		String s = ((AdaptableString) o).getVal();
		Set<IAdaptable> a = new HashSet<IAdaptable>();
		a.add(new AdaptableString(s.toUpperCase()));
		XReference xr = new XReference("In Upper Case", a); //$NON-NLS-1$
		List<IXReference> l = new ArrayList<IXReference>();
		l.add(xr);
		return l;
	}

	public IJavaElement[] getExtraChildren(IJavaElement je) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.tests.IXReferenceProvider#getProviderDescription()
	 */
	public String getProviderDescription() {
		// let's be deliberately untrustworthy here, to test the SafeExecution
		// aspect
		if (beBad) {
			throw new RuntimeException("You should never trust a provider you know..."); //$NON-NLS-1$
		}
		return "My Description"; //$NON-NLS-1$
	}

	public void setCheckedFilters(List<String> l) {
		checkedFilterList = l;
	}

	public void setCheckedInplaceFilters(List<String> l) {
		checkedFilterInplaceList = l;
	}

	public List<String> getFilterCheckedList() {
		return checkedFilterList;
	}

	public List<String> getFilterCheckedInplaceList() {
		return checkedFilterInplaceList;
	}
	
	public List<String> getFilterList() {
		return null;
	}

	public List<String> getFilterDefaultList() {
		return null;
	}
	

    public Collection<IXReference> getXReferences(Object o, List<String> l) {
        Assert.isLegal(o instanceof IAdaptable, "Object should be of type IAdaptable: " + o);
        return getXReferences((IAdaptable) o, l);
    }

}
