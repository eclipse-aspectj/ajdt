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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.refactoring.actions.RenameJavaElementAction;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

/**
 * This aspect ensures that ITDs are renamed with the proper rename refactoring
 * @author Andrew Eisenberg
 * @created May 21, 2010
 */
public aspect RenameJavaElementActionAspect {
    
    /**
     * This will be null if AJDT is not installed (ie- JDT Weaving installed, but no AJDT)
     */
    RefactoringAdapter adapter = RefactoringAdapter.getInstance();
    
    pointcut renameInvoked(IJavaElement element, boolean lightweight) : 
            execution(private void RenameJavaElementAction.run(
                    IJavaElement, boolean) 
                    throws CoreException) && args (element, lightweight) && 
                    within(RenameJavaElementAction);
    
    void around(IJavaElement element, boolean lightweight) throws CoreException : 
            renameInvoked(element, lightweight) {
        IRefactoringProvider provider = adapter.getProvider();
        if (provider != null && provider.isInterestingElement(element)) {
            provider.performRefactoring(element, lightweight);
        } else {
            if (provider != null && provider.belongsToInterestingCompilationUnit(element)) {
                // must disable inline renaming for AJCompilationUnits.  They are not good with 
                // inline renaming due to differences in source locations.
                lightweight = false;
            }
            proceed(element, lightweight);
        }
    }
    
    //////////////////////////////////////////////
    // Refactoring parse
    //////////////////////////////////////////////
    /**
     * Captures calls to a {@link RefactoringASTParser} when it is making an AST to validate
     * the results of a refactoring
     * 
     * @param contents
     * @param unit
     * @param resolveBindings
     * @param statementsRecovery
     * @param monitor
     */
    pointcut refactoringParse(String contents, ICompilationUnit unit, boolean resolveBindings, boolean statementsRecovery, IProgressMonitor monitor) : execution(CompilationUnit RefactoringASTParser.parse(String, 
            ICompilationUnit, boolean, boolean, IProgressMonitor)) && args(contents, unit, resolveBindings, statementsRecovery, monitor);
    
    CompilationUnit around(String contents, ICompilationUnit unit, boolean resolveBindings, boolean statementsRecovery, IProgressMonitor monitor) : refactoringParse(contents, unit, resolveBindings, statementsRecovery, monitor) {
        IRefactoringProvider provider = adapter.getProvider();
        if (provider != null && provider.inInterestingProject(unit)) {
            CompilationUnit ast = provider.createSourceConvertedAST(contents, unit, resolveBindings, statementsRecovery, true, monitor);
            return ast;
        } else {
            return proceed(contents, unit, resolveBindings, statementsRecovery, monitor);
        }
        
    }

}
