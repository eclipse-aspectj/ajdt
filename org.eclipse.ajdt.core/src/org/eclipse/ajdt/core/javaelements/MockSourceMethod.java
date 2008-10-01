/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import java.util.List;

import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IProgramElement.Accessibility;
import org.aspectj.asm.IProgramElement.ExtraInformation;
import org.aspectj.asm.IProgramElement.Kind;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceMethod;

/**
 * Mocks up a source method that is below an aspect declaration
 * 
 * hmmmm....what about binary methods?
 * what about fields?
 */
public class MockSourceMethod extends SourceMethod {

	private MethodElementInfo elementInfo;

	
	public MockSourceMethod(JavaElement parent, String name,
	        String[] parameterTypes) {
        super(parent, name, parameterTypes);
	    this.elementInfo = null;
	}
	
	public MockSourceMethod(JavaElement parent, String name,
			String[] parameterTypes, MethodElementInfo elementInfo) {
		super(parent, name, parameterTypes);
		this.elementInfo = elementInfo;
	}
	
	public MockSourceMethod(JavaElement parent, String name, String[] parameterTypes, int offset, String accessibility) {
		super(parent, name, parameterTypes);
		elementInfo = new MethodElementInfo();
		if (accessibility != null) {
    		if(accessibility.equals(Accessibility.PRIVATE.toString())) {
    			elementInfo.setAJAccessibility(Accessibility.PRIVATE);
    		} else if(accessibility.equals(Accessibility.PROTECTED.toString())) {
    			elementInfo.setAJAccessibility(Accessibility.PROTECTED);
    		} else if(accessibility.equals(Accessibility.PUBLIC.toString())) {
    			elementInfo.setAJAccessibility(Accessibility.PUBLIC);
    		} else if(accessibility.equals(Accessibility.PACKAGE.toString())) {
    			elementInfo.setAJAccessibility(Accessibility.PACKAGE);
    		} else if(accessibility.equals(Accessibility.PRIVILEGED.toString())) {
    			elementInfo.setAJAccessibility(Accessibility.PRIVILEGED);
    		}
		}
		elementInfo.setSourceRangeStart(offset);	
		elementInfo.setName(name.toCharArray());
		elementInfo.setAJKind(Kind.METHOD);
	}

	public Kind getAJKind() throws JavaModelException {
		return Kind.METHOD;
	}
	
	public Accessibility getAJAccessibility() throws JavaModelException {
		return elementInfo.accessibility;
	}
	
	public List getAJModifiers() throws JavaModelException {
		return elementInfo.modifiers;
	}
	
	public ExtraInformation getAJExtraInformation() throws JavaModelException {
		return elementInfo.extra;
	}
	
	protected Object createElementInfo() {
	    if (elementInfo != null) {
	        return elementInfo;
	    }
	    
        try {
            IProgramElement ipe = AJProjectModelFactory.getInstance().getModelForJavaElement(this)
                    .javaElementToProgramElement(this);
            
            elementInfo = new MethodElementInfo();
            ISourceLocation sourceLocation = ipe.getSourceLocation();
            elementInfo.setSourceRangeStart(sourceLocation.getOffset());
            elementInfo.setNameSourceStart(sourceLocation.getOffset());
            elementInfo.setNameSourceEnd(sourceLocation.getOffset() + ipe.getName().length());
            elementInfo.setAJExtraInfo(ipe.getExtraInfo());
            elementInfo.setName(name.toCharArray());
            elementInfo.setAJKind(IProgramElement.Kind.METHOD);
            elementInfo.setAJModifiers(ipe.getModifiers());
            elementInfo.setAJAccessibility(ipe.getAccessibility());
        
            return elementInfo;
        } catch (Exception e) {
            // can fail for any of a number of reasons.
            // return null so that we can try again later.
            return null;
        }
	}

	public static class MethodElementInfo extends AspectJMemberElementInfo {

	}
}

