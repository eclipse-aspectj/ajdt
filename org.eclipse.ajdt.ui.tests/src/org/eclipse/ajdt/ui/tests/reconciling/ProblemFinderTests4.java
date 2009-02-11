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

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.parserbridge.AJCompilationUnitProblemFinder;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Tests AJCompilationUnitProblemFinder
 * 
 * Tests bug 256989
 * @author andrew
 *
 */
public class ProblemFinderTests4 extends UITestCase {
    private AJCompilationUnit inAspectFileCU;
    private AJCompilationUnit inJavaFileCU;
    private AJCompilationUnit inSwitchFileCU;
    private IProject proj;
    protected void setUp() throws Exception {
        IFile inAspectFile;
        IFile inJavaFile;
        IFile inSwitchFile;
        super.setUp();
        proj = createPredefinedProject("bug256989"); //$NON-NLS-1$
        waitForJobsToComplete();
        setAutobuilding(false);
        inAspectFile = proj.getFile("src/none/AspectWithThis.aj"); //$NON-NLS-1$
        inAspectFileCU = new AJCompilationUnit(inAspectFile);

        inJavaFile = proj.getFile("src/none/AspectWithThisJava.java"); //$NON-NLS-1$
        inJavaFileCU = new AJCompilationUnit(inJavaFile);
        
        inSwitchFile = proj.getFile("src/none/AspectWithSwitch.aj"); //$NON-NLS-1$
        inSwitchFileCU = new AJCompilationUnit(inSwitchFile);
        
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        setAutobuilding(true);
    }
 
    public void testAspectFile() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(inAspectFileCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + inAspectFileCU + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }

    // Requires JDT Weaving
    // XXX This test is failing on the server, but passing locally
    // why?  try uncommenting again later 
    public void testJavaFile() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(inJavaFileCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + inJavaFileCU + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }
    
    // tests for switch statements, bug 258685
    public void testSwitchStatement() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(inSwitchFileCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + inSwitchFileCU + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }
}