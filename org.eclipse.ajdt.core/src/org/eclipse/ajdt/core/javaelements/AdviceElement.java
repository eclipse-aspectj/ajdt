/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * @author Luzius Meisser
 */
public class AdviceElement extends AspectJMemberElement implements IAspectJElement {

	public AdviceElement(JavaElement parent, String name, String[] parameterTypes) {
		super(parent, name, parameterTypes);
	}
	
	/**
	 */
	public String readableName() {

		StringBuffer buffer = new StringBuffer(super.readableName());
		buffer.append('(');
		String[] parameterTypes = this.getParameterTypes();
		int length;
		if (parameterTypes != null && (length = parameterTypes.length) > 0) {
			for (int i = 0; i < length; i++) {
				buffer.append(Signature.toString(parameterTypes[i]));
				if (i < length - 1) {
					buffer.append(", "); //$NON-NLS-1$
				}
			}
		}
		buffer.append(')');
		return buffer.toString();
	}
	
	protected void toStringName(StringBuffer buffer) {
		buffer.append(getElementName());
		buffer.append('(');
		String[] parameters = this.getParameterTypes();
		int length;
		if (parameters != null && (length = parameters.length) > 0) {
			for (int i = 0; i < length; i++) {
				buffer.append(Signature.toString(parameters[i]));
				if (i < length - 1) {
					buffer.append(", "); //$NON-NLS-1$
				}
			}
		}
		buffer.append(')');
		if (this.occurrenceCount > 1) {
			buffer.append("#"); //$NON-NLS-1$
			buffer.append(this.occurrenceCount);
		}
	}
}
