/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.contribution.jdt.refactoring;

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.contribution.jdt.preferences.WeavableProjectListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractConstantRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractTempRefactoring;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

/**
 * Provides some general hooks into refactoring support
 * @author Andrew Eisenberg
 * @created Aug 13, 2010
 */
public privileged aspect RefactoringHooksAspect {


    /**
     * Hook into the checking of final conditions for extract local variables.
     * This refactoring performs a check to see if any errors were introduced 
     * by the refactoring, but grabs the contents from the underlying IFile, 
     * not from the compilation unit, and so the contents are not translated.
     */
    pointcut extractTempFinalConditions(ExtractTempRefactoring refactoring) : execution(public RefactoringStatus ExtractTempRefactoring.checkFinalConditions(IProgressMonitor) throws CoreException)
            && this(refactoring);
    
    /**
     * Allow clients to disable the errors check.
     */
    before(ExtractTempRefactoring refactoring) : extractTempFinalConditions(refactoring) {
        boolean result = maybeDisableChecking(refactoring, refactoring.fCu);
        refactoring.setCheckResultForCompileProblems(result);
    }

    /**
     * Hook into the checking of final conditions for extract constant.
     * This refactoring performs a check to see if any errors were introduced 
     * by the refactoring, but grabs the contents from the underlying IFile, 
     * not from the compilation unit, and so the contents are not translated.
     */
    pointcut extractConstantFinalConditions(ExtractConstantRefactoring refactoring) : execution(public RefactoringStatus ExtractConstantRefactoring.checkFinalConditions(IProgressMonitor) throws CoreException)
    && this(refactoring);
    
    /**
     * Allow clients to disable the errors check.
     */
    before(ExtractConstantRefactoring refactoring) : extractConstantFinalConditions(refactoring) {
        boolean result = maybeDisableChecking(refactoring, refactoring.fCu);
        refactoring.setCheckResultForCompileProblems(result);
    }

    /**
     * @param refactoring
     */
    private boolean  maybeDisableChecking(Refactoring refactoring, ICompilationUnit cu) {
        try {
            IRefactoringProvider provider = RefactoringAdapter.getInstance().getProvider();
            if (provider != null && isInterestingProject(cu)) {
                return provider.shouldCheckResultForCompileProblems(cu);
            }
        } catch (Exception e) {
            JDTWeavingPlugin.logException(e);
        }
        return true;
    }
    
    private boolean isInterestingProject(ICompilationUnit unit) {
        return unit != null &&  
                WeavableProjectListener.getInstance().isWeavableProject(unit.getJavaProject().getProject());
    }
    
    
    /**
     * Ensure that the {@link RefactoringASTParser} is based on the constant size transformed source.
     * We need this because the {@link RefactoringASTParser} is used to base source locations.  So,
     * we cannot mess with them.
     */
    pointcut refactoringASTParserParseWithASTProvider(ITypeRoot root) : execution(public static CompilationUnit 
            RefactoringASTParser.parseWithASTProvider(ITypeRoot, boolean, IProgressMonitor)) 
            && args(root, ..);
    
    /**
     * Another common entrance into the {@link RefactoringASTParser}.
     */
    pointcut refactoringASTParserParse(ITypeRoot root) : execution(public CompilationUnit 
            RefactoringASTParser.parse(ITypeRoot, boolean, IProgressMonitor)) 
            && args(root, ..);
    
    CompilationUnit around(ITypeRoot root) : 
        refactoringASTParserParseWithASTProvider(root) ||
        refactoringASTParserParse(root) {
        IRefactoringProvider provider = RefactoringAdapter.getInstance().getProvider();
        if (provider != null && provider.belongsToInterestingCompilationUnit(root)) {
            try {
                CompilationUnit maybeUnit = provider.createASTForRefactoring(root);
                if (maybeUnit != null) {
                    return maybeUnit;
                }
            } catch (Exception e) {
                JDTWeavingPlugin.logException(e);
            }
        } 
        return proceed(root);
    }
}
