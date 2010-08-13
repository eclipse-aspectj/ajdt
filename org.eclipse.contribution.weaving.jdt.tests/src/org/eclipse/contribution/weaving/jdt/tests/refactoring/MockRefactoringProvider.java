/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.contribution.weaving.jdt.tests.refactoring;

import org.eclipse.contribution.jdt.refactoring.IRefactoringProvider;
import org.eclipse.contribution.weaving.jdt.tests.MockCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;

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

}
