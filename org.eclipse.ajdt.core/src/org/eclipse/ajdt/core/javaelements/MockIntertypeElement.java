/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IProgramElement.Accessibility;
import org.aspectj.asm.IProgramElement.ExtraInformation;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * A mock intertype element represents an itd contained in an aspect that
 * is in a .java file. 'Mock' because it's created when the model is created
 * and not when the file is parsed.
 */
public class MockIntertypeElement extends IntertypeElement implements IMockElement {

	private ExtraInformation extraInfo;

	private IntertypeElementInfo elementInfo;

	public MockIntertypeElement(JavaElement parent, String name, String[] parameterTypes, ExtraInformation extraInfo, IntertypeElementInfo elementInfo	) {
		super(parent, name, parameterTypes);
		this.elementInfo = elementInfo;
		this.extraInfo = extraInfo;
	}

	public MockIntertypeElement(JavaElement parent, int offset, String name, String[] parameterTypes, String kind, String accessibility) {
		super(parent, name, parameterTypes);
		this.elementInfo = new IntertypeElementInfo();
		elementInfo.setSourceRangeStart(offset);
		elementInfo.setName(name.toCharArray());
		elementInfo.setAJKind(IProgramElement.Kind.getKindForString(kind));
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
	
	public ExtraInformation getAJExtraInformation() {
		return extraInfo;
	}
	
	public Object getElementInfo() {
		return elementInfo;
	}
	
	public String getHandleIdentifier() {
		return super.getHandleIdentifier() 
			+ AspectElement.JEM_EXTRA_INFO + elementInfo.getSourceRange().getOffset() 
			+ AspectElement.JEM_EXTRA_INFO + elementInfo.getAJKind().toString()
			+ AspectElement.JEM_EXTRA_INFO + elementInfo.getAJAccessibility().toString(); 
	}
}
