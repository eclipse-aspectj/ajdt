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
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * @author Luzius Meisser
 */
public class PointcutElement extends AspectJMemberElement {

	public PointcutElement(JavaElement parent, String name, String[] parameterTypes) {
		super(parent, name, parameterTypes);
	}
	
	protected char getHandleMementoDelimiter() {
		return AspectElement.JEM_POINTCUT;
	}
	
	protected Object createElementInfo() {
        IProgramElement ipe = AJProjectModelFactory.getInstance().getModelForJavaElement(this)
                .javaElementToProgramElement(this);
	    
	    PointcutElementInfo info = new PointcutElementInfo();
	    info.setAJKind(IProgramElement.Kind.POINTCUT);
	    info.setName(this.getElementName().toCharArray());
	    info.setAJAccessibility(ipe.getAccessibility());
        ISourceLocation sourceLocation = ipe.getSourceLocation();
        if (sourceLocation != null) {
            info.setSourceRangeStart(sourceLocation.getOffset());
            info.setNameSourceStart(sourceLocation.getOffset());
            info.setNameSourceEnd(sourceLocation.getOffset() + ipe.getName().length());
        }
	    return info;
	}
	
}
