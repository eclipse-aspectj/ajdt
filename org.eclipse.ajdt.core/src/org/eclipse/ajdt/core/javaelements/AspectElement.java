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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceType;

/**
 * @author Luzius Meisser
 */
public class AspectElement extends SourceType implements IAspectJElement {
	public IMethod createMethod(String contents, IJavaElement sibling,
			boolean force, IProgressMonitor monitor) throws JavaModelException {
//		AJCompilationUnit ajunit = (AJCompilationUnit)this.getCompilationUnit();
//		ajunit.discardJavaParserCompatibilityMode();
		IMethod result = super.createMethod(contents, sibling, force, monitor);
//		ajunit.requestJavaParserCompatibilityMode();
		return result;
	}
	public AspectElement(JavaElement parent, String name) {
		super(parent, name);
	}

	public int getType() {
		return TYPE;
	}

	//TODO: forward call to ElementInfo (only for cosmetical reasons)
	public Kind getAJKind() throws JavaModelException  {
		IAspectJElementInfo info = (IAspectJElementInfo) getElementInfo();
		return info.getAJKind();
	}

	//TODO
	public Accessibility getAJAccessibility() throws JavaModelException {
		IAspectJElementInfo info = (IAspectJElementInfo) getElementInfo();
		return info.getAJAccessibility();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.javamodel.javaelements.IAspectJElement#getAJModifiers()
	 */
	public List getAJModifiers() throws JavaModelException {
		IAspectJElementInfo info = (IAspectJElementInfo) getElementInfo();
		return info.getAJModifiers();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.javamodel.javaelements.IAspectJElement#getAJExtraInformation()
	 */
	public ExtraInformation getAJExtraInformation() throws JavaModelException {
		IAspectJElementInfo info = (IAspectJElementInfo) getElementInfo();
		return info.getAJExtraInfo();
	}
	
}
