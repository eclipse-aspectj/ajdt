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

import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.dialogs.ITypeInfoFilterExtension;
import org.eclipse.jdt.ui.dialogs.ITypeInfoRequestor;
import org.eclipse.jdt.ui.dialogs.TypeSelectionExtension;

/**
 * This is a subclass of TypeSelectionExtension that can be used with
 * {@link JavaUI}.createTypeDialog to show only Aspects.
 * 
 * @author kdvolder
 */
public class AspectSelectionFilter extends TypeSelectionExtension {
	
	private IJavaProject project;
	private ITypeInfoFilterExtension theFilter = new ITypeInfoFilterExtension() {
		public boolean select(ITypeInfoRequestor typeInfoRequestor) {
			IType type;
			try {
				type = project.findType(typeInfoRequestor.getPackageName()+"."+typeInfoRequestor.getTypeName());
				return type!=null && type instanceof AspectElement;
			} catch (JavaModelException e) {
				return false;
			}
		}
	};
	
	public AspectSelectionFilter(IJavaProject project) { 
		this.project = project; 
	}

	@Override
	public ITypeInfoFilterExtension getFilterExtension() {
		return theFilter;
	}

}
