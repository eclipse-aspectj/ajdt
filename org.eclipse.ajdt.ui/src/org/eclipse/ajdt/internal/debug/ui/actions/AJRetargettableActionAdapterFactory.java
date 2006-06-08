/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/
package org.eclipse.ajdt.internal.debug.ui.actions;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;

/**
 * 
 */
public class AJRetargettableActionAdapterFactory implements IAdapterFactory {


	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IToggleBreakpointsTarget.class) {
			return new ToggleBreakpointAdapter();
		} 
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[]{IToggleBreakpointsTarget.class};
	}

}
