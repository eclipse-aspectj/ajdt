/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.contribution.jdt.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author Andrew Eisenberg
 * @created May 21, 2010
 * Provides hooks for refactoring support 
 */
public interface IRefactoringProvider {

    boolean isInterestingElement(IJavaElement element);
    
    void performRefactoring(IJavaElement element, boolean lightweight) throws CoreException;
    
    boolean shouldCheckResultForCompileProblems(ICompilationUnit unit);
}
