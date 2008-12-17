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

/**
 * @author Luzius Meisser
 */
public class IntertypeElementInfo extends AspectJMemberElementInfo {
	
    int declaredModifiers;
    
    public IntertypeElementInfo() {
    }
    
	protected char[] targetType;

	public char[] getTargetType() {
		return targetType;
	}
	public void setTargetType(char[] targetType) {
		this.targetType = targetType;
	}
	
	public void setDeclaredModifiers(int declaredModifiers) {
        this.declaredModifiers = declaredModifiers;
    }
	public int getDeclaredModifiers() {
        return declaredModifiers;
    }
	
}
