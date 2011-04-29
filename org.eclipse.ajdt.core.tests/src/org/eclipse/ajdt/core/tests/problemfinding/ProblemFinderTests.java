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
package org.eclipse.ajdt.core.tests.problemfinding;

import java.util.HashMap;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.parserbridge.AJCompilationUnitProblemFinder;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.dom.AST;

/**
 * Tests AJCompilationUnitProblemFinder
 * @author andrew
 *
 */
public class ProblemFinderTests extends AJDTCoreTestCase {
    AJCompilationUnit demoCU;
    AJCompilationUnit myAspectCU;
    AJCompilationUnit otherClassCU;
    AJCompilationUnit myAspectCU2;
    AJCompilationUnit otherClassCU2;
    IFile otherClassFile2;
    private IProject proj;

    protected void setUp() throws Exception {
        IFile demoFile;
        IFile myAspectFile;
        IFile otherClassFile;
        IFile myAspectFile2;
        super.setUp();
        proj = createPredefinedProject("ITDTesting"); //$NON-NLS-1$
        joinBackgroudActivities();
        setAutobuilding(false);
        demoFile = proj.getFile("src/test/Demo.aj"); //$NON-NLS-1$
        demoCU = new AJCompilationUnit(demoFile);
        myAspectFile = proj.getFile("src/test/MyAspect.aj"); //$NON-NLS-1$
        myAspectCU = new AJCompilationUnit(myAspectFile);
        otherClassFile = proj.getFile("src/test/OtherClass.aj"); //$NON-NLS-1$
        otherClassCU = new AJCompilationUnit(otherClassFile);
        myAspectFile2 = proj.getFile("src/test2/MyAspect2.aj"); //$NON-NLS-1$
        myAspectCU2 = new AJCompilationUnit(myAspectFile2);
        otherClassFile2 = proj.getFile("src/test2/OtherClass2.aj"); //$NON-NLS-1$
        otherClassCU2 = new AJCompilationUnit(otherClassFile2);
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        setAutobuilding(true);
    }
    
    /**
     * project should have no problems at first
     * @throws Exception
     */
    public void testNoProblemsMyAspect() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(myAspectCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + myAspectCU + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }
    public void testNoProblemsOtherClass() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(otherClassCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);

        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + otherClassCU + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }
    public void testNoProblemsDemo() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(demoCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);

        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + demoCU + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }
    public void testNoProblemsMyAspectCU() throws Exception {
        HashMap problems = new HashMap();
        // these next two test super classes and interfaces
        AJCompilationUnitProblemFinder.processAJ(myAspectCU2, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + myAspectCU2 + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }
    public void testNoProblemsOtherClass2() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(otherClassCU2, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);

        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + otherClassCU2 + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$

    }
    
    /**
     * project should have no problems at first
     * @throws Exception
     */
    public void testSyntaxError() throws Exception {
        otherClassCU.getBuffer().setContents(otherClassCU.getBuffer().getContents() + "gggg"); //$NON-NLS-1$
        joinBackgroudActivities();

        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(otherClassCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);

        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should have 1 syntax error in " + otherClassCU + " but found:\n" + MockProblemRequestor.printProblems(problems), 1, problems.size()); //$NON-NLS-1$
    }
    
    public void testNoReturnTypeError() throws Exception {
        String contents = otherClassCU.getBuffer().getContents();
        try {
            otherClassCU.getBuffer().setContents(contents.substring(0, contents.length()-2) + "t() { } }\n"); //$NON-NLS-1$
            joinBackgroudActivities();

            HashMap problems = new HashMap();
            AJCompilationUnitProblemFinder.processAJ(otherClassCU, 
                    AJWorkingCopyOwner.INSTANCE, problems, true, 
                    ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
    
            assertEquals("Should have one syntax error.", 1, MockProblemRequestor.filterProblems(problems).size()); //$NON-NLS-1$
            CategorizedProblem prob = ((CategorizedProblem[]) problems.values().iterator().next())[0];
            assertEquals("Return type for the method is missing", prob.getMessage()); //$NON-NLS-1$
        } finally {
            // reset contents
            otherClassCU.getBuffer().setContents(contents);
        }
    }
    
    
    
    public void testReconciler() throws Exception {
        otherClassCU.becomeWorkingCopy(new MockProblemRequestor(), null);
        otherClassCU.reconcile(AST.JLS3, true, true, null, null);
        MockProblemRequestor requestor = (MockProblemRequestor) otherClassCU.getPerWorkingCopyInfo().getProblemRequestor();
        assertEquals("Problem requestor should have found no problems: " + requestor.problemString(), 0, MockProblemRequestor.filterProblems(requestor.problems).size()); //$NON-NLS-1$
    }
    
    public void testReconcilerWithErrors() throws Exception {
        String contents = otherClassCU.getBuffer().getContents();
        try {
            otherClassCU.becomeWorkingCopy(new MockProblemRequestor(), null);
            otherClassCU.getBuffer().setContents(contents + "gggg"); //$NON-NLS-1$
            joinBackgroudActivities();
            
            otherClassCU.reconcile(AST.JLS3, true, true, null, null);
            MockProblemRequestor requestor = (MockProblemRequestor) otherClassCU.getPerWorkingCopyInfo().getProblemRequestor();
            assertEquals("Problem requestor should have found one problem: " + requestor.problemString(), 1, MockProblemRequestor.filterProblems(requestor.problems).size()); //$NON-NLS-1$
        } finally {
            // reset contents
            otherClassCU.getBuffer().setContents(contents);
        }

    }

    public void testNoMethodFound() throws Exception {
        String contents = demoCU.getBuffer().getContents();
        try {
            demoCU.becomeWorkingCopy(new MockProblemRequestor(), null);
            String s = contents;
            s = s.replaceFirst("foo", "fffffff"); //$NON-NLS-1$ //$NON-NLS-2$
            demoCU.getBuffer().setContents(s);
            joinBackgroudActivities();
            
            demoCU.reconcile(AST.JLS3, true, true, null, null);
            MockProblemRequestor requestor = (MockProblemRequestor) demoCU.getPerWorkingCopyInfo().getProblemRequestor();
            assertEquals("Problem requestor should have found one problem: " + requestor.problemString(), 1, MockProblemRequestor.filterProblems(requestor.problems).size()); //$NON-NLS-1$
        } finally {
            demoCU.getBuffer().setContents(contents);
        }
    }
}