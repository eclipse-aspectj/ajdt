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

import org.eclipse.contribution.xref.core.IXReferenceProvider;
import org.eclipse.contribution.xref.core.XReference;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author hawkinsh
 *  
 */
public class TestProvider implements IXReferenceProvider {

	private List checkedFilterList;
	private List checkedFilterInplaceList;
	
	public static boolean beBad = false; // for setting up test conditions
	
	public TestProvider() {
		checkedFilterList = new ArrayList();
		checkedFilterInplaceList = new ArrayList();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.tests.IXReferenceProvider#getClasses()
	 */
	public Class[] getClasses() {
		return new Class[] { String.class };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.tests.IXReferenceProvider#getXReferences(java.lang.Object)
	 */
	public Collection getXReferences(Object o, List checked) {
		String s = (String) o;
		Set a = new HashSet();
		a.add(s.toUpperCase());
		XReference xr = new XReference("In Upper Case", a); //$NON-NLS-1$
		List l = new ArrayList();
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

	public void setCheckedFilters(List l) {
		checkedFilterList = l;
	}

	public void setCheckedInplaceFilters(List l) {
		checkedFilterInplaceList = l;
	}

	public List getFilterCheckedList() {
		return checkedFilterList;
	}

	public List getFilterCheckedInplaceList() {
		return checkedFilterInplaceList;
	}
	
	public List getFilterList() {
		return null;
	}

	public List getFilterDefaultList() {
		return null;
	}
}
