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

import java.util.ArrayList;
import java.util.List;

import org.aspectj.asm.IProgramElement.Accessibility;
import org.aspectj.asm.IProgramElement.ExtraInformation;
import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;

/**
 * @author Luzius Meisser
 */
public class AspectElement extends SourceType implements IAspectJElement {
	
	// characters to use for handle identifiers, alongside the ones in JavaCore
	public static final char JEM_ADVICE = '&';
	public static final char JEM_ASPECT_TYPE = '}';
	public static final char JEM_CODEELEMENT = '?';
	// TYPE_PARAMETER is defined in Eclipse 3.1, but not 3.0
	public static final char JEM_TYPE_PARAMETER = ']';
	
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
	
	/**
	 * @see JavaElement#getHandleMemento()
	 */
	protected char getHandleMementoDelimiter() {
		return AspectElement.JEM_ASPECT_TYPE;
	}
	
	/*
	 * Derived from JEM_METHOD clause in SourceType
	 * @see JavaElement
	 */
	public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner workingCopyOwner) {
		if (token.charAt(0) == AspectElement.JEM_ADVICE) {
			if (!memento.hasMoreTokens()) return this;
			String name = memento.nextToken();
			
			ArrayList params = new ArrayList();
			nextParam: while (memento.hasMoreTokens()) {
				token = memento.nextToken();
				switch (token.charAt(0)) {
					case JEM_TYPE:
					case JEM_TYPE_PARAMETER:
						break nextParam;
					case JEM_ADVICE:
						if (!memento.hasMoreTokens()) return this;
						String param = memento.nextToken();
						StringBuffer buffer = new StringBuffer();
						while (param.length() == 1 && Signature.C_ARRAY == param.charAt(0)) { // backward compatible with 3.0 mementos
							buffer.append(Signature.C_ARRAY);
							if (!memento.hasMoreTokens()) return this;
							param = memento.nextToken();
						}
						params.add(buffer.toString() + param);
						break;
					default:
						break nextParam;
				}
			}
			String[] parameters = new String[params.size()];
			params.toArray(parameters);
			
			JavaElement advice = new AdviceElement(this, name, parameters);
			return advice.getHandleFromMemento(memento, workingCopyOwner);
		}
		return super.getHandleFromMemento(token, memento, workingCopyOwner);
	}
}
