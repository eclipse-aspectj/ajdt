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
package org.eclipse.contribution.weaving.jdt.tests.refactoring;

import org.eclipse.contribution.jdt.refactoring.IRefactoringProvider;
import org.eclipse.contribution.weaving.jdt.tests.MockCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;

/**
 * 
 * @author Andrew Eisenberg
 * @created Aug 13, 2010
 */
public class MockRefactoringProvider implements IRefactoringProvider {
    
    Boolean checkResults = null;

    public boolean isInterestingElement(IJavaElement element) {
        return element.getAncestor(IJavaElement.COMPILATION_UNIT) instanceof MockCompilationUnit;
    }

    public void performRefactoring(IJavaElement element, boolean lightweight)
            throws CoreException {
        // no-op
    }

    public boolean shouldCheckResultForCompileProblems(ICompilationUnit unit) {
        checkResults = Boolean.valueOf(!(unit instanceof MockCompilationUnit));
        return !(unit instanceof MockCompilationUnit);
    }
    
    void reset() {
        checkResults = null;
    }

    public boolean belongsToInterestingCompilationUnit(IJavaElement elt) {
        return false;
    }

    public ITypeRoot convertRoot(ITypeRoot root) {
        return root;
    }
}
