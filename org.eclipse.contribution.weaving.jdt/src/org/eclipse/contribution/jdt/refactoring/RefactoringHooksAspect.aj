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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractTempRefactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;

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
        try {
            IRefactoringProvider provider = RefactoringAdapter.getInstance().getProvider();
            if (provider != null && isInterestingProject(refactoring.fCu)) {
                boolean result = provider.shouldCheckResultForCompileProblems(refactoring.fCu);
                refactoring.setCheckResultForCompileProblems(result);
            }
        } catch (Exception e) {
            JDTWeavingPlugin.logException(e);
        }
    }
    
    private boolean isInterestingProject(ICompilationUnit unit) {
        return unit != null &&  
                WeavableProjectListener.getInstance().isWeavableProject(unit.getJavaProject().getProject());
    }

}
