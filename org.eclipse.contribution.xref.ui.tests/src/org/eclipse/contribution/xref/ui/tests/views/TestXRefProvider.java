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
package org.eclipse.contribution.xref.ui.tests.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.contribution.xref.core.IXReferenceProvider;
import org.eclipse.contribution.xref.core.XReference;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author hawkinsh
 *  
 */
public class TestXRefProvider implements IXReferenceProvider {

	public static boolean beBad = false; // for setting up test conditions

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getClasses()
	 */
	public Class[] getClasses() {
		return new Class[] { TestXRefClass.class };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getXReferences(java.lang.Object)
	 */
	public Collection getXReferences(Object o, List checked) {
		XReference e = new XReference("extends"); //$NON-NLS-1$
		XReference i = new XReference("implements"); //$NON-NLS-1$
		List l = new ArrayList();
		l.add(e);
		l.add(i);
		return l;
	}

	public IJavaElement[] getExtraChildren(IJavaElement je) {
		return null;
	}

 	public String getProviderDescription() {
 		return "Definition of TestXRefProvider"; //$NON-NLS-1$
 		
 	}
	
	public void setCheckedFilters(List l) {	}

	public void setCheckedInplaceFilters(List l) { }

	public List getFilterCheckedList() { return null; }

	public List getFilterCheckedInplaceList() { return null; }
	
	public List getFilterList() { return null; }

	public List getFilterDefaultList() { return null; }
	
}
