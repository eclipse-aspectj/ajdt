/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ISourceRange;

/**
 * Used to represent injar aspects (which have no parent)
 * 
 * I don't think this class is used any more
 */
public class AJInjarElement extends AJCodeElement implements IAJCodeElement {
	
	/**
	 * @param parent
	 * @param name
	 * @param parameterTypes
	 */
	public AJInjarElement(String name) {
		super(null, name); 
	}
	
	public ISourceRange getNameRange() {
	    return new SourceRange(this.nameStart, this.nameEnd-this.nameStart+1);
	}
	
	public IResource getResource() {
		if (getParent() == null) {
			// injar aspect, which has no parent
			return null;
		} else {
			return super.getResource(); 
		}
	}
	
	protected void getHandleMemento(StringBuffer buff) {
		if (getParent() == null) {
			// injar aspect, which has no parent
			buff.append(getHandleMementoDelimiter());
			buff.append(getName());
			if (this.occurrenceCount > 1) {
				buff.append(JEM_COUNT);
				buff.append(this.occurrenceCount);
			}
		} else {
			super.getHandleMemento(buff);
		}
	}
}
