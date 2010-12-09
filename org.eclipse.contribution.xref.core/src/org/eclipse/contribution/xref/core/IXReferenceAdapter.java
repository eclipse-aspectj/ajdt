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

import java.util.Collection;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Adapter returned by a call to 
 * <code>IAdaptable.getAdapter(IXReferenceAdapter.class)</code>.
 * Defined for IResources and IJavaElements.
 * 
 * <p>An IXReferenceAdapter provides access to the set of known cross references
 * for the object it adapts. </p>
 * 
 * <p>To connect a cross reference adapter to a cross reference repository 
 * service or to add cross references to all instances of a type use an 
 * IXReferenceProvider.
 * </p>
 */
public interface IXReferenceAdapter extends IAdaptable {
	
	/** 
	 * The adaptable object (IResource or IJavaElement) whose cross references
	 * are represented by this instance. 
	 * @return the source of all cross references represented by this adapter
	 */
	IAdaptable getReferenceSource();
	
	public IJavaElement[] getExtraChildren(IJavaElement je);
	
	/**
	 * The set of {@link IXReference}s the source participates in. 
	 * @return the set of all cross references contributed by one or more 
     * {@link IXReferenceProvider}s defined for the cross reference source.
	 */
	Collection<IXReference> getXReferences();

}
