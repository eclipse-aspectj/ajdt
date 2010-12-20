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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * Adapter factory to support Cross References 
 *
 */
public class XReferenceAdapterFactory implements IAdapterFactory {
 
	private static final Class<?>[] adapterList = new Class[]{IXReferenceAdapter.class};
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (!adapterType.equals(IXReferenceAdapter.class) ) {
			return null;
		} else if (adaptableObject instanceof XReferenceAdapter) {
		    return adaptableObject;
		} else if (adaptableObject instanceof IAdaptable) {
		    return new XReferenceAdapter((IAdaptable) adaptableObject);
		} else {
		    return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@SuppressWarnings("rawtypes")
    public Class[] getAdapterList() {
		return adapterList;
	}

}
