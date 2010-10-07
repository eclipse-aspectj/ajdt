/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Andrew Eisenberg - Initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.reconciling;

import java.util.HashMap;

import org.eclipse.ajdt.core.parserbridge.AJCompilationUnitProblemFinder;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.core.CompilationUnit;

/**
 * Tests AJCompilationUnitProblemFinder
 * @author andrew
 *
 */
public class GenericProblemFinderTests extends UITestCase {
    private IJavaProject proj;

    protected void setUp() throws Exception {
        proj = JavaCore.create(createPredefinedProject("DefaultEmptyProject")); //$NON-NLS-1$
        waitForJobsToComplete();
        setAutobuilding(false);
    }
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            setAutobuilding(true);
        }
    }
    
    public void testNoProblemsAbstractAspectDeclareParents() throws Exception {
        assertNoProblems(
                new String[] { "p" }, 
                new String[] { "AbstractAspect.aj" }, 
                new String[] {
                        "package p;\n" +
                        "public abstract aspect AbstractAspect {\n" +
                        "declare parents : Class extends X;\n" +
                        "declare parents : Class extends Y;\n" +
                        "}\n" +
                        "aspect Aspect extends AbstractAspect {\n" + 
                        "    void something(X x) {\n" +
                        "       something(new Class());\n" +
                        "    }\n" +
                        "    void something2(Y y) {\n" +
                        "        something2(new Class());\n" +
                        "    }\n" +
                        "}\n" +
                        "interface X { }\n" +
                        "interface Y { }\n" +
                        "class Class { }" });
    }
    public void testNoProblemsGenericAbstractAspectDeclareParents() throws Exception {
        assertNoProblems(
                new String[] { "p" }, 
                new String[] { "AbstractAspect.aj" }, 
                new String[] {
                        "package p;\n" +
                        "public abstract aspect AbstractAspect<S, T> {\n" +
                        "declare parents : Class extends S;\n" +
                        "declare parents : Class extends T;\n" +
                        "}\n" +
                        "aspect Aspect extends AbstractAspect<X, Y> {\n" + 
                        "    void something(X x) {\n" +
                        "       something(new Class());\n" +
                        "    }\n" +
                        "    void something2(Y y) {\n" +
                        "        something2(new Class());\n" +
                        "    }\n" +
                        "}\n" +
                        "interface X { }\n" +
                        "interface Y { }\n" +
                        "class Class { }" });
    }
    
    public void testNoProblemsGenericAbstractAspectDeclareParents2() throws Exception {
        assertNoProblems(
                new String[] { "p", "p", "p", "p", "p" }, 
                new String[] { "AbstractAspect.aj", "Aspect.aj", "X.java", "Y.java", "Class.java" }, 
                new String[] {
                        "package p;\n" +
                        "public abstract aspect AbstractAspect<S, T> {\n" +
                        "declare parents : Class extends S;\n" +
                        "declare parents : Class extends T;\n" +
                        "}",

                        "package p;\n" +
                        "aspect Aspect extends AbstractAspect<X, Y> {\n\n\n" + 
                        "    void something(X x) {\n" +
                        "       something(new Class());\n" +
                        "    }\n" +
                        "    void something2(Y y) {\n" +
                        "        something2(new Class());\n" +
                        "    }\n" +
                        "}",
                        
                        "package p;\n" +
                        "interface X { }\n",
                        
                        "package p;\n" +
                        "interface Y { }\n",
                        
                        "package p;\n" +
                        "class Class { }" });
    }

    private void assertNoProblems(String[] packages, String[] cuNames, String[] cuContents) throws CoreException {
        ICompilationUnit[] units = createUnits(packages, cuNames, cuContents, proj);
        buildProject(proj);
        for (ICompilationUnit unit : units) {
            assertNoProblems((CompilationUnit) unit);
        }
    }
    
    /**
     * @throws JavaModelException
     */
    private void assertNoProblems(CompilationUnit unit) throws JavaModelException {
        HashMap<String,CategorizedProblem[]> problems = new HashMap<String,CategorizedProblem[]>();
        AJCompilationUnitProblemFinder.processAJ(unit, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + unit + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }
}