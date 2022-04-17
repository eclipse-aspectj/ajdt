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

import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IProgramElement.Modifiers;
import org.aspectj.asm.IProgramElement.Accessibility;
import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author Luzius Meisser
 */
public interface IAspectJElement extends IMember {

	Kind getAJKind() throws JavaModelException;

	Accessibility getAJAccessibility() throws JavaModelException;

	List<Modifiers> getAJModifiers() throws JavaModelException;

	IProgramElement.ExtraInformation getAJExtraInformation() throws JavaModelException;

}
