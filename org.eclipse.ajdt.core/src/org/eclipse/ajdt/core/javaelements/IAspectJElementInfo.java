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
	public abstract Kind getAJKind();
	
	public abstract IProgramElement.ExtraInformation getAJExtraInfo();

	public abstract Accessibility getAJAccessibility();
	
	public abstract List<Modifiers> getAJModifiers();

	public abstract void setAJKind(Kind kind);

	public abstract void setAJAccessibility(Accessibility accessibility);
	
	public abstract void setAJModifiers(List<Modifiers> mods);
	
	public abstract void setAJExtraInfo(IProgramElement.ExtraInformation extra);
}