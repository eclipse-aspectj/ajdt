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

import java.util.List;

import org.aspectj.asm.IProgramElement.Accessibility;
import org.aspectj.asm.IProgramElement.ExtraInformation;
import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.SourceMethodElementInfo;
import org.eclipse.jdt.internal.core.SourceRange;

/**
 * @author Luzius Meisser
 */
public class AspectJMemberElementInfo extends SourceMethodElementInfo implements IAspectJElementInfo {

	protected Kind kind;
	protected Accessibility accessibility;
	protected List modifiers;
	protected ExtraInformation extra;
	
	private char[] name;
	private char[] returnType;
	private boolean isConstructor;
	
	public Kind getAJKind() {
		return kind;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.javamodel.javaelements.IAspectJElement#getAccessibility()
	 */
	public Accessibility getAJAccessibility() {
		return accessibility;
	}

	public void setAJKind(Kind kind) {
		this.kind = kind;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.javamodel.javaelements.IAspectJElement#getAccessibility()
	 */
	public void setAJAccessibility(Accessibility accessibility) {
		this.accessibility = accessibility;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.javamodel.javaelements.IAspectJElementInfo#setModifiers(java.util.List)
	 */
	public void setAJModifiers(List mods) {
		modifiers = mods;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.javamodel.javaelements.IAspectJElementInfo#getAJModifiers()
	 */
	public List getAJModifiers() {
		return modifiers;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.javamodel.javaelements.IAspectJElementInfo#getAJExtraInfo()
	 */
	public ExtraInformation getAJExtraInfo() {
		return extra;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.javamodel.javaelements.IAspectJElementInfo#setAJExtraInfo(org.aspectj.asm.IProgramElement.ExtraInformation)
	 */
	public void setAJExtraInfo(ExtraInformation extra) {
		this.extra = extra;
	}
	
	public void setArgumentNames(char[][] names) {
		this.argumentNames = names;
	}
//	public void setArgumentTypeNames(char[][] types) {
//		this.argumentTypeNames = types;
//	}
	public void setConstructor(boolean isConstructor) {
		this.isConstructor = isConstructor;
	}
	public void setExceptionTypeNames(char[][] types) {
		this.exceptionTypes = types;
	}
	public void setReturnType(char[] type) {
		this.returnType = type;
	}
	public ISourceRange getSourceRange() {
		if (fSourceRangeEnd == 0)
			return new SourceRange(fSourceRangeStart, name.length);
		return new SourceRange(fSourceRangeStart, fSourceRangeEnd - fSourceRangeStart + 1);
	}
	public void setSourceRangeEnd(int end) {
		fSourceRangeEnd = end;
	}
	public void setSourceRangeStart(int start) {
		fSourceRangeStart = start;
	}
	
	public void setFlags(int flags) {
		this.flags = flags;
	}
	/**
	 * Sets this member's name
	 */
	public void setName(char[] name) {
		this.name= name;
	}
	/**
	 * Sets the last position of this member's name, relative
	 * to its openable's source buffer.
	 */
	public void setNameSourceEnd(int end) {
		this.nameEnd= end;
	}
	/**
	 * Sets the start position of this member's name, relative
	 * to its openable's source buffer.
	 */
	public void setNameSourceStart(int start) {
		this.nameStart= start;
	}
	
//	public String getSignature() {
//
//		String[] paramSignatures = new String[this.argumentTypeNames.length];
//		for (int i = 0; i < this.argumentTypeNames.length; ++i) {
//			paramSignatures[i] = Signature.createTypeSignature(this.argumentTypeNames[i], false);
//		}
//		return Signature.createMethodSignature(paramSignatures, Signature.createTypeSignature(this.returnType, false));
//	}
	
	public boolean isConstructor() {
		return isConstructor;
	}
	
	public boolean isAnnotationMethod() {
		return false;
	}
	
	public char[] getReturnTypeName() {
		return returnType;
	}
}
