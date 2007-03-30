/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 ******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class ASTHolderAJCUInfo extends AJCompilationUnitInfo {

	int astLevel;
	boolean resolveBindings;
	int reconcileFlags;
	HashMap problems = null;
	CompilationUnit ast;

}
