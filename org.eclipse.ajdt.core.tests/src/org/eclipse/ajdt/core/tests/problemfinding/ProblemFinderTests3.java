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

/**
 * Tests AJCompilationUnitProblemFinder
 * 
 * Ensures generics in ITDs are working properly
 * @author andrew
 *
 */
public class ProblemFinderTests3 extends AJDTCoreTestCase {
    AJCompilationUnit actionExecutorCU;
    AJCompilationUnit deleteActionCU;
    AJCompilationUnit deleteActionAspectCU;
    private IProject proj;
    protected void setUp() throws Exception {
        IFile actionExecutorFile;
        IFile deleteActionFile;
        IFile deleteActionAspectFile;
        super.setUp();
        proj = createPredefinedProject("ITDTesting2"); //$NON-NLS-1$
        joinBackgroudActivities();
        setAutobuilding(false);
        actionExecutorFile = proj.getFile("src/generics/ActionExecutor.java"); //$NON-NLS-1$
        actionExecutorCU = new AJCompilationUnit(actionExecutorFile);

        deleteActionFile = proj.getFile("src/generics/DeleteAction.java"); //$NON-NLS-1$
        deleteActionCU = new AJCompilationUnit(deleteActionFile);
        
        deleteActionAspectFile = proj.getFile("src/generics/DeleteAction.java"); //$NON-NLS-1$
        deleteActionAspectCU = new AJCompilationUnit(deleteActionAspectFile);
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        setAutobuilding(true);
    }
 
    public void testNoProblemsActionExecutor() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(actionExecutorCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + actionExecutorCU + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }

    public void testNoProblemsDeleteAction() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(deleteActionCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + deleteActionCU + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }

    public void testNoProblemsDeleteActionAspect() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(deleteActionAspectCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + deleteActionAspectCU + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }

     
 
}