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
import org.eclipse.contribution.jdt.refactoring.RefactoringAdapter;
import org.eclipse.contribution.weaving.jdt.tests.MockCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractTempRefactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * 
 * @author Andrew Eisenberg
 * @created Aug 13, 2010
 */
public class RefactoringHooksTests extends AbstractWeavingRefactoringTest {

    private IRefactoringProvider orig;
    private MockRefactoringProvider mock;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        orig = RefactoringAdapter.getInstance().getProvider();
        mock = new MockRefactoringProvider();
        RefactoringAdapter.getInstance().setProvider(mock);
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        RefactoringAdapter.getInstance().setProvider(orig);
        mock.reset();
    }
    
    
    /**
     * Ensures that the associated advice is hit when performing local variable rename
     * @throws Exception
     */
    public void testLocalVariableRenameInMock() throws Exception {
        String source = "class Something { \nvoid f() { int x = 9 + 8; } }";
        String toRefactor = "9 + 8";
        ICompilationUnit unit = createCompilationUnitAndPackage("", "Something.mock", source, project);
        assertTrue("Weaving not set up properly", unit instanceof MockCompilationUnit);
        
        ExtractTempRefactoring refactoring = new ExtractTempRefactoring(unit, source.indexOf(toRefactor), toRefactor.length());
        refactoring.setTempName("xx");
        RefactoringStatus status = super.performRefactoring(refactoring, true, false);
        assertTrue("Refactoring should not have failed", status.isOK());
        
        assertNotNull("Check results advice not executed", mock.checkResults);
        assertFalse("Check results should be false", mock.checkResults);
    }
    /**
     * Ensures that the associated advice is hit when performing local variable rename
     * @throws Exception
     */
    public void testLocalVariableRenameInJava() throws Exception {
        String source = "package p; \n class Something { \nvoid f() { int x = 9 + 8; } }";
        String toRefactor = "9 + 8";
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Something.java", source, project);
        
        ExtractTempRefactoring refactoring = new ExtractTempRefactoring(unit, source.indexOf(toRefactor), toRefactor.length());
        refactoring.setTempName("xx");
        RefactoringStatus status = super.performRefactoring(refactoring, true, false);
        assertTrue("Refactoring should not have failed", status.isOK());
        
        assertNotNull("Check results advice not executed", mock.checkResults);
        assertTrue("Check results should be true", mock.checkResults);
    }
}
