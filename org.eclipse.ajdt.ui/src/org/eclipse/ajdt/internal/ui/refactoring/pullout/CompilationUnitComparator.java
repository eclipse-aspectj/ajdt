/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kris De Volder - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.refactoring.pullout;

import java.util.Comparator;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * A comparator that can be used to sort or compare compilation units.
 * 
 * @author kdvolder
 */
public class CompilationUnitComparator implements Comparator<ICompilationUnit> {

	public static Comparator<ICompilationUnit> the = new CompilationUnitComparator();
	
	private CompilationUnitComparator() {}

	public int compare(ICompilationUnit o1, ICompilationUnit o2) {
		int result = o1.getElementName().compareTo(o2.getElementName());
		if (result==0) {
			// maybe same name but not actually same CU
			return o1.getPath().toString().compareTo(o2.getPath().toString());
		}
		else
			return result;
	}

}
