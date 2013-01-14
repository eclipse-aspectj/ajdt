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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.CancelableNameEnvironment;
import org.eclipse.jdt.internal.core.JavaProject;

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
     * @param elt
     * @return true iff this CU is in a project type that we care about.
     */
    boolean inInterestingProject(IJavaElement elt);
    
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

    /**
     * Creates a name environment suitable for refactoring
     * @param project
     * @param owner
     * @param monitor2
     * @return
     * @throws JavaModelException 
     */
    CancelableNameEnvironment createNameEnvironment(JavaProject project,
            WorkingCopyOwner owner, IProgressMonitor monitor2) throws JavaModelException;
}
