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

import org.eclipse.jdt.core.ISourceRange;

/**
 * @author Luzius Meisser
 */
public class IntertypeElementInfo extends AspectJMemberElementInfo {
    
    int declaredModifiers;
    private char[] qualifiedReturnType;
    int targetTypeStart;
    int targetTypeEnd;
    
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
    
    public void setQualifiedReturnType(char[] qualifiedReturnType) {
        this.qualifiedReturnType = qualifiedReturnType;
    }
    
    public char[] getQualifiedReturnType() {
        return qualifiedReturnType;
    }
    
    public ISourceRange getTargetTypeSourceRange() {
        return new SourceRange(targetTypeStart, targetTypeEnd - targetTypeStart);
    }
    
    public void setTargetTypeStart(int targetTypeStart) {
        this.targetTypeStart = targetTypeStart;
    }
    
    public void setTargetTypeEnd(int targetTypeEnd) {
        this.targetTypeEnd = targetTypeEnd;
    }
}