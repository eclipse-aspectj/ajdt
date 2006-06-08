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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * A mock advice element represents advice contained in an aspect that
 * is in a .java file. 'Mock' because it's created when the model is created
 * and not when the file is parsed.
 */
public class MockAdviceElement extends AdviceElement implements IMockElement {
	
	private ExtraInformation extraInfo;

	private AdviceElementInfo elementInfo;

	public MockAdviceElement(JavaElement parent, String name, String[] parameterTypes, IProgramElement.ExtraInformation extraInfo, AdviceElementInfo elementInfo) {
		super(parent, name, parameterTypes);
		this.elementInfo = elementInfo;
		this.extraInfo = extraInfo;
	}

	public MockAdviceElement(JavaElement parent, int offset, String name, String[] parameterTypes) {
		super(parent, name, parameterTypes);
		this.elementInfo = new AdviceElementInfo();
		elementInfo.setSourceRangeStart(offset);
		elementInfo.setName(name.toCharArray());
		elementInfo.setAJKind(IProgramElement.Kind.ADVICE);
		this.extraInfo = new ExtraInformation();
		this.extraInfo.setExtraAdviceInformation(name);
	}
		
	public ExtraInformation getAJExtraInformation() throws JavaModelException {
		return extraInfo;
	}
	
	public boolean equals(Object o) {
		if(o instanceof MockAdviceElement) {
			return super.equals(o) && ((MockAdviceElement)o).elementInfo == elementInfo;
		}
		return false;
	}
	
	public int hashCode() {
		return super.hashCode() + elementInfo.hashCode();
	}
	
	public Object getElementInfo() throws JavaModelException {
		return elementInfo;
	}
	
	public String getHandleIdentifier() {
		return super.getHandleIdentifier() + AspectElement.JEM_EXTRA_INFO + elementInfo.getSourceRange().getOffset();
	}
}
