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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;

/**
 * @author Luzius Meisser
 */
public class AspectElementInfo extends SourceTypeElementInfo implements IAspectJElementInfo{

	protected Kind kind;
	protected Accessibility accessibility;
	protected List modifiers;
	protected ExtraInformation extra;
	
	public void setHandle(IType handle) {
		this.handle = handle;
	}
	/**
	 * Sets the name of the package this type is declared in.
	 */
//	public void setPackageName(char[] name) {
//		this.packageName= name;
//	}
	/**
	 * Sets the name of the source file this type is declared in.
	 */
//	public void setSourceFileName(char[] name) {
//		this.sourceFileName= name;
//	}
	/**
	 * Sets the (unqualified) name of this type's superclass
	 */
	public void setSuperclassName(char[] superclassName) {
		this.superclassName = superclassName;
	}
	/**
	 * Sets the (unqualified) names of the interfaces this type implements or extends
	 */
	public void setSuperInterfaceNames(char[][] superInterfaceNames) {
		this.superInterfaceNames = superInterfaceNames;
	}
	
	public void setFlags(int flags) {
		this.flags = flags;
	}
	/**
	 * Sets this member's name
	 */
	//public void setName(char[] name) {
	//	this.name= name;
	//}
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
	
	public void setSourceRangeEnd(int end) {
		fSourceRangeEnd = end;
	}
	public void setSourceRangeStart(int start) {
		fSourceRangeStart = start;
	}

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
}
