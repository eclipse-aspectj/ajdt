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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractConstantRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractTempRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.PromoteTempToFieldRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameLocalVariableProcessor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;

/**
 * 
 * @author Andrew Eisenberg
 * @created Aug 13, 2010
 */
public class RefactoringHooksTests extends AbstractWeavingRefactoringTest {

    private IRefactoringProvider orig;
    private MockRefactoringProvider mock = new MockRefactoringProvider();
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        orig = RefactoringAdapter.getInstance().getProvider();
        RefactoringAdapter.getInstance().setProvider(mock);
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        RefactoringAdapter.getInstance().setProvider(orig);
        mock.reset();
    }
    
    
    public void testCreateSourceConvertedAST1() throws Exception {
        checkRenameLocalRefactoring("Something.mock");
        assertNotNull("Create Source Converted AST advice not executed", mock.createSourceConvertedAST);
        assertTrue("Create Source Converted AST advice should be true", mock.createSourceConvertedAST);
    }

    public void testCreateSourceConvertedAST2() throws Exception {
        checkRenameLocalRefactoring("Something.java");
        assertNotNull("Create Source Converted AST advice not executed", mock.createSourceConvertedAST);
        assertFalse("Create Source Converted AST advice should be false", mock.createSourceConvertedAST);
    }

    private RefactoringStatus checkRenameLocalRefactoring(String unitName) throws CoreException {
        String source = "package p; \n class Something { \nvoid f() { int x = 9 + 8; } }";
        ICompilationUnit unit = createCompilationUnitAndPackage("p", unitName, source, project);
        String toRefactor = "x";
        int start = source.indexOf(toRefactor);
        int end = start;
        RenameLocalVariableProcessor refactoring = new RenameLocalVariableProcessor(
                new LocalVariable(extractFirstMethod(unit), toRefactor, start, end, start, end, "I", new Annotation[0], 0, false));
        refactoring.setNewElementName("other");
        RefactoringStatus status = refactoring.checkInitialConditions(monitor);
        assertTrue("Refactoring returns with messages.\n" + status, status.isOK());
        CheckConditionsContext context = new CheckConditionsContext();
        ResourceChangeChecker checker = new ResourceChangeChecker();
        context.add(checker);
        status = refactoring.checkFinalConditions(new NullProgressMonitor(), context);
        assertTrue("Refactoring returns with messages.\n" + status, status.isOK());
        return status;
    }

    private JavaElement extractFirstMethod(ICompilationUnit unit) {
        return (JavaElement) unit.getType("Something").getMethod("f", new String[0]);
    }

    /**
     * Ensures that the associated advice is hit when performing local variable extraction
     * @throws Exception
     */
    public void testExtractLocalVariableInMock() throws Exception {
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
     * Ensures that the associated advice is hit when performing local variable extraction
     * @throws Exception
     */
    public void testExtractLocalVariableInJava() throws Exception {
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
    /**
     * Ensures that the associated advice is hit when performing constant extraction
     * @throws Exception
     */
    public void testExtractConstantInMock() throws Exception {
        String source = "class Something { \nvoid f() { int x = 9 + 8; } }";
        String toRefactor = "9 + 8";
        ICompilationUnit unit = createCompilationUnitAndPackage("", "Something.mock", source, project);
        assertTrue("Weaving not set up properly", unit instanceof MockCompilationUnit);
        
        ExtractConstantRefactoring refactoring = new ExtractConstantRefactoring(unit, source.indexOf(toRefactor), toRefactor.length());
        refactoring.setConstantName("xx");
        RefactoringStatus status = super.performRefactoring(refactoring, true, false);
        assertTrue("Refactoring should not have failed", status.isOK());
        
        assertNotNull("Check results advice not executed", mock.checkResults);
        assertFalse("Check results should be false", mock.checkResults);
    }
    /**
     * Ensures that the associated advice is hit when performing convert local to field
     * @throws Exception
     */
    public void testConvertLocalInJava() throws Exception {
        String source = "package p; \n class Something { \nvoid f() { int x = 9 + 8; } }";
        String toRefactor = "9 + 8";
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Something.java", source, project);
        
        ExtractConstantRefactoring refactoring = new ExtractConstantRefactoring(unit, source.indexOf(toRefactor), toRefactor.length());
        refactoring.setConstantName("xx");
        RefactoringStatus status = super.performRefactoring(refactoring, true, false);
        assertTrue("Refactoring should not have failed", status.isOK());
        
        assertNotNull("Check results advice not executed", mock.checkResults);
        assertTrue("Check results should be true", mock.checkResults);
    }
    /**
     * Ensures that the associated advice is hit when performing constant extraction
     * @throws Exception
     */
    public void testConvertLocalInMock() throws Exception {
        String source = "class Something { \nvoid f() { int x = 9 + 8; } }";
        String toRefactor = "x";
        ICompilationUnit unit = createCompilationUnitAndPackage("", "Something.mock", source, project);
        assertTrue("Weaving not set up properly", unit instanceof MockCompilationUnit);
        
        PromoteTempToFieldRefactoring refactoring = new PromoteTempToFieldRefactoring(unit, source.indexOf(toRefactor), toRefactor.length());
        refactoring.setFieldName("xx");
        RefactoringStatus status = super.performRefactoring(refactoring, true, false);
        assertTrue("Refactoring should not have failed", status.isOK());
        
        assertNotNull("Create AST For refactoring advice not executed", mock.createASTForRefactoring);
        assertFalse("Create AST For refactoring should be false", mock.createASTForRefactoring);
    }
    /**
     * Ensures that the associated advice is hit when performing convert local to field
     * @throws Exception
     */
    public void testExtractConstantInJava() throws Exception {
        String source = "package p; \n class Something { \nvoid f() { int x = 9 + 8; } }";
        String toRefactor = "x";
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Something.java", source, project);
        
        PromoteTempToFieldRefactoring refactoring = new PromoteTempToFieldRefactoring(unit, source.indexOf(toRefactor), toRefactor.length());
        refactoring.setFieldName("xx");
        RefactoringStatus status = super.performRefactoring(refactoring, true, false);
        assertTrue("Refactoring should not have failed", status.isOK());
        
        assertNotNull("Create AST For refactoring advice not executed", mock.createASTForRefactoring);
        assertTrue("Create AST For refactoring should be true", mock.createASTForRefactoring);
    }
}
