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
package org.eclipse.contribution.xref.internal.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.contribution.xref.core.IXReferenceProvider;
import org.eclipse.contribution.xref.core.XReference;

/**
 * @author hawkinsh
 *  
 */
public class TestXRefProviderWithEntities implements IXReferenceProvider {

	private String testAssociate;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getClasses()
	 */
	public Class[] getClasses() {
		return new Class[] { TestXRefClassWithEntities.class };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getXReferences(java.lang.Object)
	 */
	public Collection getXReferences(Object o) {
		XReference e = new XReference("extends");
		XReference i = new XReference("implements");
		testAssociate = "test associate";
		e.addAssociate(testAssociate);
		List l = new ArrayList();
		l.add(e);
		l.add(i);
		return l;
	}

 	public String getProviderDescription() {
 		return "Definition of TestXRefProviderWithEntities";
 		
 	}

}
