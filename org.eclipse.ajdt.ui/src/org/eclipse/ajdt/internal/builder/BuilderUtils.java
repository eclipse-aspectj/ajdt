/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 ******************************************************************************/
package org.eclipse.ajdt.internal.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

/**
 * Builder Utilities
 */
public class BuilderUtils {

	public static void updateTypesCache(final IJavaProject jp) {
//		 TODO: Remove when we branch - there is no types cache in 3.1
	}
	
	
	public static void updateTypesCache(IWorkspace workspace) {
//		 TODO: Remove when we branch - there is no types cache in 3.1
	}
	
	/**
	 * @param types
	 * @param j
	 * @return
	 */
	public static char[][] getEnclosingTypes(IType startType) {
		char[][] enclosingTypes = new char[0][];
		IType type = startType.getDeclaringType();
		List enclosingTypeList = new ArrayList();
		while(type != null) {
			char[] typeName = type.getElementName().toCharArray();
			enclosingTypeList.add(0, typeName);
			type = type.getDeclaringType();
		}
		if(enclosingTypeList.size() > 0) {
			enclosingTypes = new char[enclosingTypeList.size()][];
			for (int k = 0; k < enclosingTypeList.size(); k++) {
				char[] typeName = (char[]) enclosingTypeList.get(k);
				enclosingTypes[k] = typeName;
			}
		}
		return enclosingTypes;
	}
	

	public static void initTypesCache() {
		// TODO: Remove when we branch - there is no types cache in 3.1
	}
}
