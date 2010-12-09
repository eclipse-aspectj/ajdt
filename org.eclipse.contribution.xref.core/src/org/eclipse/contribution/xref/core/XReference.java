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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Convenience implementation of IXReference
 * @see org.eclipse.contribution.xref.core.IXReference
 *
 */
public class XReference implements IXReference {

	private String name;
	private Set<IAdaptable> associates;
	
	
	/**
	 * Creates a new cross reference with the given (user-visible) name and
	 * an empty set of associates.
	 * @param name the reference type name
	 */
	public XReference(String name) {
		this.name = name;
		associates = new HashSet<IAdaptable>();
	}
	
	/**
	 * Creates a new cross reference with the given (user-visible) name and
	 * the given set of associates. For example, "extends", {java.lang.O
	 * @param name the reference type name
	 * @param to the set of objects connected via this reference
	 */
	public XReference(String name, Set<IAdaptable> to) {
		this.name = name;
		this.associates = new HashSet<IAdaptable>(to);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.contributions.xref.core.IXReference#getName()
	 */
	public String getName() { return name; }
	
	/* (non-Javadoc)
	 * @see org.eclipse.contributions.xref.core.IXReference#getAssociates()
	 */
	public Iterator<IAdaptable> getAssociates() { return associates.iterator(); }
	
	/**
	 * Add an associate (object connected via this reference)
	 * @param o the object to be associated
	 */
	public void addAssociate( IAdaptable o ) {
		associates.add(o);
	}
	
	/**
	 * Remove an associate from this reference
	 * @param o the object to be disassociated
	 */
	public void removeAssociate( IAdaptable o ) {
		associates.remove(o);
	}
	
}
