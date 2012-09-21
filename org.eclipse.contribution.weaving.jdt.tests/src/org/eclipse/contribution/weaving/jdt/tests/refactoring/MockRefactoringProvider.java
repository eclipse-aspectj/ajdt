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
import org.eclipse.contribution.weaving.jdt.tests.MockNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

/**
 * 
 * @author Andrew Eisenberg
 * @created Aug 13, 2010
 */
public class MockRefactoringProvider implements IRefactoringProvider {
    
    Boolean checkResults = null;
    
    Boolean createASTForRefactoring = null;
    
    Boolean createSourceConvertedAST = null;

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
        createASTForRefactoring = null;
        createSourceConvertedAST = null;
    }

    public boolean belongsToInterestingCompilationUnit(IJavaElement elt) {
        try {
            return elt.getJavaProject().getProject().hasNature(MockNature.ID_NATURE);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    public CompilationUnit createASTForRefactoring(ITypeRoot root) {
        createASTForRefactoring = Boolean.valueOf(!(root instanceof MockCompilationUnit));
        return null;
    }

    public boolean inInterestingProject(ICompilationUnit unit) {
        return createSourceConvertedAST = unit.getElementName().endsWith("mock");
    }

    public CompilationUnit createSourceConvertedAST(String contents, ICompilationUnit unit, boolean resolveBindings, boolean statementsRecovery, boolean bindingsRecovery, IProgressMonitor monitor) {
        ASTParser fParser = ASTParser.newParser(AST.JLS4);
        fParser.setResolveBindings(resolveBindings);
        fParser.setStatementsRecovery(statementsRecovery);
        fParser.setBindingsRecovery(bindingsRecovery);
        fParser.setSource(contents.toCharArray());
        String cfName= unit.getElementName();
        fParser.setUnitName(cfName.substring(0, cfName.length() - 6) + JavaModelUtil.DEFAULT_CU_SUFFIX);
        fParser.setProject(unit.getJavaProject());
        fParser.setCompilerOptions(unit.getJavaProject().getOptions(true));
        CompilationUnit newCUNode= (CompilationUnit) fParser.createAST(monitor);
        return newCUNode;
    }
}
