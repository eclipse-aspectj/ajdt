/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * @author Luzius Meisser
 */
public class IntertypeElement extends AspectJMemberElement {
	public IntertypeElement(JavaElement parent, String name, String[] parameterTypes) {
		super(parent, name, parameterTypes);
	}
	
	public char[] getTargetType() throws JavaModelException{
		return ((IntertypeElementInfo)getElementInfo()).getTargetType();
	}
	
	/**
	 * @see JavaElement#getHandleMemento()
	 */
	protected char getHandleMementoDelimiter() {
		return AspectElement.JEM_ITD;
	}
	
}
