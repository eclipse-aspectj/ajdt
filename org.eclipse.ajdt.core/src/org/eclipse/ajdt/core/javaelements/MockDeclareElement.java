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
import org.aspectj.asm.IProgramElement.ExtraInformation;
import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * A mock declare element represents a declare statement contained in an aspect that
 * is in a .java file. 'Mock' because it's created when the model is created
 * and not when the file is parsed.
 */
public class MockDeclareElement extends DeclareElement implements IMockElement {

		
	private ExtraInformation extraInfo;

	private DeclareElementInfo elementInfo;

	public MockDeclareElement(JavaElement parent, String name, String[] parameterTypes, IProgramElement.ExtraInformation extraInfo, DeclareElementInfo elementInfo) {
		super(parent, name, parameterTypes);
		this.elementInfo = elementInfo;
		this.extraInfo = extraInfo;
	}
	
	public MockDeclareElement(JavaElement parent, int offset, String name) {
		super(parent, name, null);
		this.elementInfo = new DeclareElementInfo();
		elementInfo.setSourceRangeStart(offset);
		elementInfo.setName(name.toCharArray());
		elementInfo.setAJKind(getKindForString(name));
	}
	
	public ExtraInformation getAJExtraInformation() throws JavaModelException {
		return extraInfo;
	}
	
	public Object getElementInfo() throws JavaModelException {
		return elementInfo;
	}
	
	public String getHandleIdentifier() {
		return super.getHandleIdentifier() + AspectElement.JEM_EXTRA_INFO + elementInfo.getSourceRange().getOffset();
	}
	
	public boolean equals(Object o) {
		if(o instanceof MockDeclareElement) {
			return super.equals(o) && ((MockDeclareElement)o).elementInfo == elementInfo;
		}
		return false;
	}
	
	public int hashCode() {
		return super.hashCode() + elementInfo.hashCode();
	}	
	
	public Kind getKindForString(String kindString) {
		for (int i = 0; i < IProgramElement.Kind.ALL.length; i++) {
			if (kindString.startsWith(IProgramElement.Kind.ALL[i].toString())) return IProgramElement.Kind.ALL[i];	
		}
		
		// TODO: Remove when IProgramElement.Kind.ALL is updated to include these
		if(kindString.startsWith("declare @constructor")) { //$NON-NLS-1$
			return IProgramElement.Kind.DECLARE_ANNOTATION_AT_CONSTRUCTOR;
		}
		if(kindString.startsWith("declare @method")) { //$NON-NLS-1$
			return IProgramElement.Kind.DECLARE_ANNOTATION_AT_METHOD;
		}
		if(kindString.startsWith("declare @field")) { //$NON-NLS-1$
			return IProgramElement.Kind.DECLARE_ANNOTATION_AT_FIELD;
		}
		if(kindString.startsWith("declare @type")) { //$NON-NLS-1$
			return IProgramElement.Kind.DECLARE_ANNOTATION_AT_TYPE;
		}
		
		return IProgramElement.Kind.ERROR;
	}
}
