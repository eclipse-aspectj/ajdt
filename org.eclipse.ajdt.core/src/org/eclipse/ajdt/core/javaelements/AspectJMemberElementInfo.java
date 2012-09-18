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

import java.util.List;

import org.aspectj.asm.IProgramElement.Modifiers;
import org.aspectj.asm.IProgramElement.Accessibility;
import org.aspectj.asm.IProgramElement.ExtraInformation;
import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.internal.core.Annotation;
import org.eclipse.jdt.internal.core.SourceMethodElementInfo;

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
	private char[][] argumentTypeNames;
	
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
	public void setAJModifiers(List<Modifiers> mods) {
		modifiers = mods;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.javamodel.javaelements.IAspectJElementInfo#getAJModifiers()
	 */
	public List<Modifiers> getAJModifiers() {
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
	
	public void setArgumentTypeNames(char[][] types) {
		this.argumentTypeNames = types;
	}
	public void setConstructor(boolean isConstructor) {
		this.isConstructor = isConstructor;
	}
	
	// make public
	public void setExceptionTypeNames(char[][] types) {
		this.exceptionTypes = types;
	}

	// make public
	public void setReturnType(char[] type) {
	    // bug 310704, remove starting dot if it exists
	    if (type != null && type.length > 0 && type[0] == '.') {
	        char[] newType = new char[type.length-1];
	        System.arraycopy(type, 1, newType, 0, type.length-1);
	        type = newType;
	    }
		this.returnType = type;
	}
	
    // make public
	public void setArgumentNames(char[][] names) {
	    this.argumentNames = names;
	}

	public ISourceRange getSourceRange() {
		/* AJDT 1.7 Begin */
		if (sourceRangeEnd == 0)
			return new SourceRange(sourceRangeStart, name != null ? name.length : 0);
		return new SourceRange(sourceRangeStart, sourceRangeEnd - sourceRangeStart + 1);
		/* AJDT 1.7 End */
	}

	public void setSourceRangeEnd(int end) {
		// JDT's CompilationUnitStructureRequestor.exitMethod() can call
		// us with an end value of zero for pointcuts, which would result
		// in a truncated source (as shown in the pointcut source hover for
		// example).
		if ((this instanceof PointcutElementInfo) && (end == 0)) {
			return;
		}
		/* AJDT 1.7 */
		sourceRangeEnd = end;
	}
	
	public void setSourceRangeStart(int start) {
		/* AJDT 1.7 */
		sourceRangeStart = start;
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
	
	public char[][] getArgumentTypeNames() {
        return argumentTypeNames;
    }
		
	public boolean isConstructor() {
		return isConstructor;
	}
	
	public boolean isAnnotationMethod() {
		return false;
	}
	
	public char[] getReturnTypeName() {
		return returnType;
	}	
	
	public void setAnnotations(IAnnotation[] annotations) {
	    if (annotations == null) {
	        this.annotations = Annotation.NO_ANNOTATIONS;
	    } else {
	        this.annotations = annotations;
	    }
	}
	
	public void setArguments(ILocalVariable[] arguments) {
	    this.arguments = arguments;
	}
	
	public ILocalVariable[] getArguments() {
	    return super.arguments;
	}
	
	/* AJDT 1.7 */
	protected IJavaElement[] children;
	public IJavaElement[] getChildren() {
	    return super.getChildren();
	}
	public void setChildren(IJavaElement[] children) {
        this.children = children;
    }
	/* AJDT 1.7 end */
	
}
