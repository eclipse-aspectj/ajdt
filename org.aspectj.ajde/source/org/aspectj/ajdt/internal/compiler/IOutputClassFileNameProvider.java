/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.compiler;

import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;

/**
 * @author colyer
 *
 * Implementors of this interface know how to create an output destination name
 * for a given compilation result and class file. This interface capures the variation
 * in strategy between ajc command-line compiles and an AJDT driven compilation.
 */
public interface IOutputClassFileNameProvider {
	String getOutputClassFileName(char[] eclipseClassFileName, CompilationResult result);
}
