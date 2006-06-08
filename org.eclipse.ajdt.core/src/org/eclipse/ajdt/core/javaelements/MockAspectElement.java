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

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * A mock aspect element represents an aspect that
 * is in a .java file. 'Mock' because it's created when the model is created
 * and not when the file is parsed.
 */
public class MockAspectElement extends AspectElement implements IMockElement {

	private AspectElementInfo info;

	public MockAspectElement(JavaElement parent, String name, AspectElementInfo info) {
		super(parent, name);
		this.info = info;
		info.setHandle(this);
	}
	
	public Object getElementInfo() throws JavaModelException {
		return info;
	}


}
