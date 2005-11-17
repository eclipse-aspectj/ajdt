/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import java.util.List;

import org.aspectj.asm.IProgramElement.Accessibility;
import org.aspectj.asm.IProgramElement.ExtraInformation;
import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceMethod;

/**
 *
 */
public class MockSourceMethod extends SourceMethod implements IMockElement {

	private MethodElementInfo elementInfo;

	/**
	 * @param parent
	 * @param name
	 * @param parameterTypes
	 */
	public MockSourceMethod(JavaElement parent, String name,
			String[] parameterTypes, MethodElementInfo elementInfo) {
		super(parent, name, parameterTypes);
		this.elementInfo = elementInfo;
	}
	
	public MockSourceMethod(JavaElement parent, String name, String[] parameterTypes, int offset, String accessibility) {
		super(parent, name, parameterTypes);
		elementInfo = new MethodElementInfo();
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
		elementInfo.setAJKind(Kind.METHOD);
	}

	public Object getElementInfo() throws JavaModelException {
		return elementInfo;
	}
	
	public String getHandleIdentifier() {
		return super.getHandleIdentifier() 
			+ AspectElement.JEM_EXTRA_INFO + elementInfo.getSourceRange().getOffset()
			+ AspectElement.JEM_EXTRA_INFO + elementInfo.accessibility.toString();
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

	public static class MethodElementInfo extends AspectJMemberElementInfo {

	}
}

