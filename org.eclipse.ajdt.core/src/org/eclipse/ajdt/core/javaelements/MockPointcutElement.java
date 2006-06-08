/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IProgramElement.Accessibility;
import org.aspectj.asm.IProgramElement.ExtraInformation;
import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * A mock pointcut element represents a pointcut contained in an aspect that
 * is in a .java file. 'Mock' because it's created when the model is created
 * and not when the file is parsed.
 */
public class MockPointcutElement extends PointcutElement implements IMockElement {

	private ExtraInformation extraInfo;

	private PointcutElementInfo elementInfo;

	public MockPointcutElement(JavaElement parent, String name, String[] parameterTypes, IProgramElement.ExtraInformation extraInfo, PointcutElementInfo elementInfo) {
		super(parent, name, parameterTypes);
		this.elementInfo = elementInfo;
		this.extraInfo = extraInfo;
	}

	public MockPointcutElement(JavaElement parent, String name, String[] parameterTypes, int offset, String accessibility) {
		super(parent, name, parameterTypes);
		elementInfo = new PointcutElementInfo();
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
		elementInfo.setSourceRangeStart(offset);	
		elementInfo.setName(name.toCharArray());
		elementInfo.setAJKind(Kind.POINTCUT);
	}

	public ExtraInformation getAJExtraInformation() throws JavaModelException {
		return extraInfo;
	}
	
	public Object getElementInfo() throws JavaModelException {
		return elementInfo;
	}
	
	public String getHandleIdentifier() {
		return super.getHandleIdentifier() 
			+ AspectElement.JEM_EXTRA_INFO + elementInfo.getSourceRange().getOffset()
			+ AspectElement.JEM_EXTRA_INFO + elementInfo.accessibility.toString();
	}
}
