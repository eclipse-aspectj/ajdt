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
import org.aspectj.asm.IProgramElement.Accessibility;
import org.aspectj.asm.IProgramElement.Kind;
import org.aspectj.asm.IProgramElement.Modifiers;

/**
 * @author Luzius Meisser
 */
public interface IAspectJElementInfo {
	Kind getAJKind();

	IProgramElement.ExtraInformation getAJExtraInfo();

	Accessibility getAJAccessibility();

	List<Modifiers> getAJModifiers();

	void setAJKind(Kind kind);

	void setAJAccessibility(Accessibility accessibility);

	void setAJModifiers(List<Modifiers> mods);

	void setAJExtraInfo(IProgramElement.ExtraInformation extra);
}
