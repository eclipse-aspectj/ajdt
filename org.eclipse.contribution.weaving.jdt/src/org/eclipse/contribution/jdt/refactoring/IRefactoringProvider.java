/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource and others.
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author Andrew Eisenberg
 * @created May 21, 2010
 * Provides hooks for refactoring support 
 */
public interface IRefactoringProvider {

    boolean isInterestingElement(IJavaElement element);
    
    void performRefactoring(IJavaElement element, boolean lightweight) throws CoreException;
    
    boolean shouldCheckResultForCompileProblems(ICompilationUnit unit);
    
    
    boolean belongsToInterestingCompilationUnit(IJavaElement elt);
    
    /**
     * Convert the type root into something that is parseable by JDT
     * @param root
     * @return
     */
    CompilationUnit createASTForRefactoring(ITypeRoot root);

    /**
     * @param unit
     * @return true iff this CU is in a project type that we care about.
     */
    boolean inInterestingProject(ICompilationUnit unit);
    
    /**
     * Creates an AST for the given compilation unit with source code translated.
     * The source locations are converted back to reflect the original source 
     * locations of the untranslated code.
     * 
     * @param contents
     * @param unit
     * @param resolveBindings
     * @param statementsRecovery
     * @param bindingsRecovery
     * @param monitor
     * @return
     */
    CompilationUnit createSourceConvertedAST(String contents, ICompilationUnit unit, boolean resolveBindings, boolean statementsRecovery, boolean bindingsRecovery, IProgressMonitor monitor);
}
