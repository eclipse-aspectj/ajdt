/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.debug.ui.actions;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Copied computeTypeName method from
 * org.eclipse.jdt.internal.debug.ui.actions.ValidBreakpointLocationLocator 
 * to make visible to ToggleBreakpointAdapter in this package.
 */
public class ValidBreakpointLocationLocator {

	/**
	 * Compute the name of the type which contains this node.
	 * Result will be the name of the type or the inner type which contains this node, but not of the local or anonymous type.
	 */
	static protected String computeTypeName(ASTNode node) {
		String typeName = null;
		while (!(node instanceof org.eclipse.jdt.core.dom.CompilationUnit)) {
			if (node instanceof org.eclipse.jdt.core.dom.AbstractTypeDeclaration) {
				String identifier= ((org.eclipse.jdt.core.dom.AbstractTypeDeclaration)node).getName().getIdentifier();
				if (typeName == null) {
					typeName= identifier;
				} else {
					typeName= identifier + "$" + typeName; //$NON-NLS-1$
				}
			} else {
				typeName= null;
			}
			node= node.getParent();
		}
		org.eclipse.jdt.core.dom.PackageDeclaration packageDecl= ((org.eclipse.jdt.core.dom.CompilationUnit)node).getPackage();
		String packageIdentifier= ""; //$NON-NLS-1$
		if (packageDecl != null) {
			org.eclipse.jdt.core.dom.Name packageName= packageDecl.getName();
			while (packageName.isQualifiedName()) {
				org.eclipse.jdt.core.dom.QualifiedName qualifiedName= (org.eclipse.jdt.core.dom.QualifiedName) packageName;
				packageIdentifier= qualifiedName.getName().getIdentifier() + "." + packageIdentifier; //$NON-NLS-1$
				packageName= qualifiedName.getQualifier();
			}
			packageIdentifier= ((org.eclipse.jdt.core.dom.SimpleName)packageName).getIdentifier() + "." + packageIdentifier; //$NON-NLS-1$
		}
		return packageIdentifier + typeName;
	}

}
