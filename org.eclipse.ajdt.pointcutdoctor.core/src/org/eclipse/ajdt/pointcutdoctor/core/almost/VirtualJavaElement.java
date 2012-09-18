/*******************************************************************************
 * Copyright (c) 2007 Linton Ye.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.core.almost;

import java.util.HashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;

public class VirtualJavaElement extends JavaElement {

	protected VirtualJavaElement()
			throws IllegalArgumentException {
		//XXX this will cause an NPE whenever the parent is needed.  How to work around the problem?
		super(null);
	}

	@Override
	protected void closing(Object info) throws JavaModelException {
	}

	@Override
	protected Object createElementInfo() {
		return null;
	}

	@Override
	protected void generateInfos(Object info, @SuppressWarnings("rawtypes") HashMap newElements,
			IProgressMonitor pm) throws JavaModelException {
	}

	@Override
	public IJavaElement getHandleFromMemento(String token,
			MementoTokenizer memento, WorkingCopyOwner owner) {
		return null;
	}

	@Override
	protected char getHandleMementoDelimiter() {
		return 0;
	}

	public IResource getCorrespondingResource() throws JavaModelException {
		return null;
	}

	public int getElementType() {
		return JEM_LOCALVARIABLE;  // same as AJCodeElement
	}

	public IPath getPath() {
		return null;
	}

	public IResource getResource() {
		return null;
	}

	public IResource getUnderlyingResource() throws JavaModelException {
		return null;
	}

	public boolean isStructureKnown() throws JavaModelException {
		return false;
	}

	@Override
	public IResource resource() {
		return null;
	}

}
