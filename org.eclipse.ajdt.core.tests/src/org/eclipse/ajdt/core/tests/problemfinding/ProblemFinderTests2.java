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
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * These tests ensure that even when there is no model,
 * the eager parser does not contribute spurious errors
 * due to ITDs.
 * 
 * We have to be liberal in what we cut out.  If we are
 * unsure if an error is due to an ITD, just remove it to be
 * safe
 * 
 * @author andrew
 *
 */
public class ProblemFinderTests2 extends AJDTCoreTestCase {
    AJCompilationUnit demoCU;
    AJCompilationUnit myAspectCU;
    AJCompilationUnit otherClassCU;
    AJCompilationUnit myAspectCU2;
    AJCompilationUnit otherClassCU2;
    private IProject proj;
    protected void setUp() throws Exception {
        IFile demoFile;
        IFile myAspectFile;
        IFile otherClassFile;
        IFile myAspectFile2;
        IFile otherClassFile2;
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
        setAutobuilding(false);
        // remove the model
        proj.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
    }
    public void testNoModelAndNoProblems1() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(myAspectCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + myAspectCU + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }
    
    public void testNoModelAndNoProblems2() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(otherClassCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + otherClassCU + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }
    
    public void testNoModelAndNoProblems3() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(demoCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + demoCU + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }
    
    public void testNoModelAndNoProblems4() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(myAspectCU2, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + myAspectCU2 + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }
    
    public void testNoModelAndNoProblems5() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(otherClassCU2, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + otherClassCU2 + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }
}
