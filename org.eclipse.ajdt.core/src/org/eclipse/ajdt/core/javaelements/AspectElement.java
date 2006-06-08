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
	
	// characters to use for handle identifiers, alongside the ones in JavaElement
	public static final char JEM_ASPECT_CU = '*';
	public static final char JEM_ADVICE = '&';
	public static final char JEM_ASPECT_TYPE = '}';
	public static final char JEM_CODEELEMENT = '?';
	public static final char JEM_ITD = ')';
	public static final char JEM_DECLARE = '`';
	public static final char JEM_POINTCUT = '+';
	// TYPE_PARAMETER is defined in Eclipse 3.1, but not 3.0
	public static final char JEM_TYPE_PARAMETER = ']';
	public static final char JEM_EXTRA_INFO = '>';
	
	public IMethod createMethod(String contents, IJavaElement sibling,
			boolean force, IProgressMonitor monitor) throws JavaModelException {
		IMethod result = super.createMethod(contents, sibling, force, monitor);
		return result;
	}
	public AspectElement(JavaElement parent, String name) {
		super(parent, name);
	}

	public int getType() {
		return TYPE;
	}

	/**
	 * Returns the pointcuts declared by this type. If this is a source type,
	 * the results are listed in the order in which they appear in the source,
	 * otherwise, the results are in no particular order.
	 * 
	 * @exception JavaModelException
	 *                if this element does not exist or if an exception occurs
	 *                while accessing its corresponding resource.
	 * @return the pointcuts declared by this type
	 */
	public PointcutElement[] getPointcuts() throws JavaModelException {
		// pointcuts appear as methods
		IMethod[] methods = getMethods();
		List list = new ArrayList();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i] instanceof PointcutElement) {
				list.add(methods[i]);
			}
		}
		PointcutElement[] array= new PointcutElement[list.size()];
		list.toArray(array);
		return array;
	}

	/**
	 * Returns the advice elements declared by this type. If this is a source
	 * type, the results are listed in the order in which they appear in the
	 * source, otherwise, the results are in no particular order.
	 * 
	 * @exception JavaModelException
	 *                if this element does not exist or if an exception occurs
	 *                while accessing its corresponding resource.
	 * @return the advice elements declared by this type
	 */
	public AdviceElement[] getAdvice() throws JavaModelException {
		// advice statements appear as methods
		IMethod[] methods = getMethods();
		List list = new ArrayList();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i] instanceof AdviceElement) {
				list.add(methods[i]);
			}
		}
		AdviceElement[] array = new AdviceElement[list.size()];
		list.toArray(array);
		return array;
	}

	/**
	 * Returns the declare elements declared by this type. If this is a source
	 * type, the results are listed in the order in which they appear in the
	 * source, otherwise, the results are in no particular order.
	 * 
	 * @exception JavaModelException
	 *                if this element does not exist or if an exception occurs
	 *                while accessing its corresponding resource.
	 * @return the declare elements declared by this type
	 */
	public DeclareElement[] getDeclares() throws JavaModelException {
		// declare statements appear as methods
		IMethod[] methods = getMethods();
		List list = new ArrayList();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i] instanceof DeclareElement) {
				list.add(methods[i]);
			}
		}
		DeclareElement[] array = new DeclareElement[list.size()];
		list.toArray(array);
		return array;
	}

	//TODO: forward call to ElementInfo (only for cosmetical reasons)
	public Kind getAJKind() throws JavaModelException  {
		IAspectJElementInfo info = (IAspectJElementInfo) getElementInfo();
		return info.getAJKind();
	}

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
	 * Added support for advice, ITDs, and declare statements
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
			if (token.charAt(0) == JavaElement.JEM_COUNT) {
				return advice.getHandleFromMemento(token, memento, workingCopyOwner);
			} 
			return advice.getHandleFromMemento(memento, workingCopyOwner);			
		} else if (token.charAt(0) == AspectElement.JEM_ITD) {
			String name = memento.nextToken();
			ArrayList params = new ArrayList();
			nextParam: while (memento.hasMoreTokens()) {
				token = memento.nextToken();
				switch (token.charAt(0)) {
					case JEM_TYPE:
					case JEM_TYPE_PARAMETER:
						break nextParam;
					case JEM_ITD:
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
			JavaElement itd = new IntertypeElement(this, name, parameters);
			return itd.getHandleFromMemento(memento, workingCopyOwner);
		} else if (token.charAt(0) == AspectElement.JEM_DECLARE) {
			String name = memento.nextToken();
			ArrayList params = new ArrayList();
			nextParam: while (memento.hasMoreTokens()) {
				token = memento.nextToken();
				switch (token.charAt(0)) {
					case JEM_TYPE:
					case JEM_TYPE_PARAMETER:
						break nextParam;
					case JEM_DECLARE:
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
			JavaElement itd = new DeclareElement(this, name, parameters);
			if (token.charAt(0) == JavaElement.JEM_COUNT) {
				return itd.getHandleFromMemento(token, memento, workingCopyOwner);
			}
			return itd.getHandleFromMemento(memento, workingCopyOwner);
		} else if (token.charAt(0) == AspectElement.JEM_POINTCUT) {
			String name = memento.nextToken();
			ArrayList params = new ArrayList();
			nextParam: while (memento.hasMoreTokens()) {
				token = memento.nextToken();
				switch (token.charAt(0)) {
					case JEM_TYPE:
					case JEM_TYPE_PARAMETER:
						break nextParam;
					case JEM_POINTCUT:
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
			JavaElement pointcut = new PointcutElement(this, name, parameters);
			return pointcut.getHandleFromMemento(memento, workingCopyOwner);
		}
		return super.getHandleFromMemento(token, memento, workingCopyOwner);
	}
}
