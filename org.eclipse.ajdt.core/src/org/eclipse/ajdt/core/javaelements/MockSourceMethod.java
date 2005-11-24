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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.Member;


/**
 *
 */
public class MockSourceMethod extends Member implements IMethod, IMockElement {

	/**
	 * The parameter type signatures of the method - stored locally
	 * to perform equality test. <code>null</code> indicates no
	 * parameters.
	 */
	protected String[] parameterTypes;

	private MethodElementInfo elementInfo;

	/**
	 * @param parent
	 * @param name
	 * @param parameterTypes
	 */
	public MockSourceMethod(JavaElement parent, String name,
			String[] parameterTypes, MethodElementInfo elementInfo) {
		super(parent, name);
		this.parameterTypes = parameterTypes;
		this.elementInfo = elementInfo;
	}
	
	public MockSourceMethod(JavaElement parent, String name, String[] parameterTypes, int offset, String accessibility) {
		super(parent, name);
		this.parameterTypes = parameterTypes;
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

	public Object getElementInfo() {
		return elementInfo;
	}
	
	/**
	 * @see JavaElement#getHandleMemento()
	 */
	protected char getHandleMementoDelimiter() {
		return JavaElement.JEM_METHOD;
	}
	
	public String getHandleIdentifier() {
		return super.getHandleIdentifier() 
			+ AspectElement.JEM_EXTRA_INFO + elementInfo.getSourceRange().getOffset()
			+ AspectElement.JEM_EXTRA_INFO + elementInfo.accessibility.toString();
	}
	
	public Kind getAJKind() {
		return Kind.METHOD;
	}
	
	public Accessibility getAJAccessibility() {
		return elementInfo.accessibility;
	}
	
	public List getAJModifiers() {
		return elementInfo.modifiers;
	}
	
	public ExtraInformation getAJExtraInformation() {
		return elementInfo.extra;
	}

	public static class MethodElementInfo extends AspectJMemberElementInfo {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getExceptionTypes()
	 */
	public String[] getExceptionTypes() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getTypeParameterSignatures()
	 */
	public String[] getTypeParameterSignatures() {
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getNumberOfParameters()
	 */
	public int getNumberOfParameters() {
		return parameterTypes.length;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getParameterNames()
	 */
	public String[] getParameterNames() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getParameterTypes()
	 */
	public String[] getParameterTypes() {
		return parameterTypes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getReturnType()
	 */
	public String getReturnType() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getSignature()
	 */
	public String getSignature() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#isConstructor()
	 */
	public boolean isConstructor() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#isMainMethod()
	 */
	public boolean isMainMethod() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#isSimilar(org.eclipse.jdt.core.IMethod)
	 */
	public boolean isSimilar(IMethod method) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getElementType()
	 */
	public int getElementType() {
		return METHOD;
	}
}

