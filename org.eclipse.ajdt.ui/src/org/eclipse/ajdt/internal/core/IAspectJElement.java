/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.core;

import java.util.List;

import org.aspectj.asm.IProgramElement;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * IAspectJElement represents an element in the parse tree of an AspectJ
 * source file. 
 */
public interface IAspectJElement extends IAdaptable, IWorkbenchAdapter {

	public static final int CATEGORY_IMPORT = 0;
	public static final int CATEGORY_OTHER=1;
	public static final int CATEGORY_DECLARATION = 2;
	public static final int CATEGORY_INTRODUCTION = 3;
	public static final int CATEGORY_POINTCUT = 4;
	public static final int CATEGORY_ADVICE = 5;

	/**
	 * Get the location of this element within the project source files
	 */
	public IMarker getLocationMarker( );

	/**
	 * Get the category into which this element fits
	 */
	public int category( );
	
	/**
	 * Get the list of modifiers for this element
	 * (may return null if not a program element)
	 * Constants returned are defined in org.aspectj.asm.ProgramElementNode
	 */
	public List getModifiers( );
	
	/**
	 * Get the list of accessibility qualifiers for this element
	 * (may return null if not a program element)
	 * Constants returned are defined in org.aspectj.asm.ProgramElementNode
	 */
	public IProgramElement.Accessibility getAccessibility( );

	/**
	 * Does this element represent a field?
	 */
	public boolean isField( );	
		
}
