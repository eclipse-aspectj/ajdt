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
package org.eclipse.contribution.xref.ui.tests.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
public class TestXRefProvider implements IXReferenceProvider, IXReferenceProviderExtension {

	public static boolean beBad = false; // for setting up test conditions

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getClasses()
	 */
	public Class<?>[] getClasses() {
		return new Class[] { TestXRefClass.class };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getXReferences(java.lang.Object)
	 */
	public Collection<IXReference> getXReferences(IAdaptable o, List<String> checked) {
		XReference e = new XReference("extends"); //$NON-NLS-1$
		XReference i = new XReference("implements"); //$NON-NLS-1$
		List<IXReference> l = new ArrayList<IXReference>();
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
	
	public void setCheckedFilters(List<String> l) {	}

	public void setCheckedInplaceFilters(List<String> l) { }

	public List<String> getFilterCheckedList() { return null; }

	public List<String> getFilterCheckedInplaceList() { return null; }
	
	public List<String> getFilterList() { return null; }

	public List<String> getFilterDefaultList() { return null; }

    public Collection<IXReference> getXReferences(Object o, List<String> l) {
        Assert.isLegal(o instanceof IAdaptable, "Object should be of type IAdaptable: " + o);
        return getXReferences((IAdaptable) o, l);
    }

}
