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

import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
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
	
	protected Object createElementInfo() {
	    IProgramElement ipe = AJProjectModelFactory.getInstance().getModelForJavaElement(this).javaElementToProgramElement(this);
	    IntertypeElementInfo info = new IntertypeElementInfo();
        info.setAJExtraInfo(ipe.getExtraInfo());
        info.setName(name.toCharArray());
        info.setAJKind(ipe.getKind());
        info.setAJModifiers(ipe.getModifiers());
        info.setAJAccessibility(ipe.getAccessibility());
        ISourceLocation sourceLocation = ipe.getSourceLocation();
        info.setSourceRangeStart(sourceLocation.getOffset());
        info.setNameSourceStart(sourceLocation.getOffset());
        info.setNameSourceEnd(sourceLocation.getOffset() + ipe.getName().length());
        
	    return info;
	}
	
//	protected Object createElementInfo() {
//	    IProgramElement ipe = AJProjectModelFactory.getInstance().getModelForJavaElement(this).javaElementToProgramElement(this);
//	    ipe.get
//	}
	
	/**
	 * @see JavaElement#getHandleMemento()
	 */
	protected char getHandleMementoDelimiter() {
		return AspectElement.JEM_ITD;
	}
	
//    public String getHandleIdentifier() {
//        try {
//            IntertypeElementInfo info = (IntertypeElementInfo) getElementInfo();
//            return super.getHandleIdentifier() 
//                + AspectElement.JEM_EXTRA_INFO + info.getSourceRange().getOffset() 
//                + AspectElement.JEM_EXTRA_INFO + info.getAJKind().toString()
//                + AspectElement.JEM_EXTRA_INFO + info.getAJAccessibility().toString();
//        } catch (JavaModelException e) {
//            return super.getHandleIdentifier();
//        } 
//    }	
}
