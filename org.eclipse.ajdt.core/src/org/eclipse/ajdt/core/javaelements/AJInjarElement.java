/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import org.eclipse.core.resources.IResource;

/**
 * Used to represent injar aspects (which have no parent)
 */
public class AJInjarElement extends AJCodeElement implements IAJCodeElement {
	
	/**
	 * @param parent
	 * @param name
	 * @param parameterTypes
	 */
	public AJInjarElement(String name) {
		super(null,0,name); //$NON-NLS-1$
	}
	
	public IResource getResource() {
		if (getParent() == null) {
			// injar aspect, which has no parent
			return null;
		}
		return super.getResource(); 
	}
	
	public String getHandleMemento(){
		if (getParent() == null) {
			// injar aspect, which has no parent
			StringBuffer buff= new StringBuffer();
			buff.append(getHandleMementoDelimiter());
			buff.append(getName());
			if (this.occurrenceCount > 1) {
				buff.append(JEM_COUNT);
				buff.append(this.occurrenceCount);
			}
			return buff.toString();
		} 
		return getHandleMemento();
	}
}
