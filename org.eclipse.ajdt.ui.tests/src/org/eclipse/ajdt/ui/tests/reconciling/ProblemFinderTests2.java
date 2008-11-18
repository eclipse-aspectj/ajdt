package org.eclipse.ajdt.ui.tests.reconciling;

import java.util.HashMap;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.parserbridge.AJCompilationUnitProblemFinder;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.ajdt.ui.tests.UITestCase;
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
public class ProblemFinderTests2 extends UITestCase {
    AJCompilationUnit demoCU;
    AJCompilationUnit myAspectCU;
    AJCompilationUnit otherClassCU;
    AJCompilationUnit myAspectCU2;
    AJCompilationUnit otherClassCU2;
    private IFile demoFile;
    private IFile myAspectFile;
    private IFile otherClassFile;
    private IFile myAspectFile2;
    private IFile otherClassFile2;
    private IProject proj;
    protected void setUp() throws Exception {
        super.setUp();
        proj = createPredefinedProject("ITDTesting");
        waitForJobsToComplete();
        setAutobuilding(false);
        demoFile = proj.getFile("src/test/Demo.aj");
        demoCU = new AJCompilationUnit(demoFile);
        myAspectFile = proj.getFile("src/test/MyAspect.aj");
        myAspectCU = new AJCompilationUnit(myAspectFile);
        otherClassFile = proj.getFile("src/test/OtherClass.aj");
        otherClassCU = new AJCompilationUnit(otherClassFile);
        myAspectFile2 = proj.getFile("src/test2/MyAspect2.aj");
        myAspectCU2 = new AJCompilationUnit(myAspectFile2);
        otherClassFile2 = proj.getFile("src/test2/OtherClass2.aj");
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
        
        assertEquals("Should not have any problems", 0, problems.size());
    }
    
    public void testNoModelAndNoProblems2() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(otherClassCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        assertEquals("Should not have any problems", 0, problems.size());
    }
    
    public void testNoModelAndNoProblems3() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(demoCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        assertEquals("Should not have any problems", 0, problems.size());
    }
    
    public void testNoModelAndNoProblems4() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(myAspectCU2, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        assertEquals("Should not have any problems", 0, problems.size());
    }
    
    public void testNoModelAndNoProblems5() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(otherClassCU2, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        assertEquals("Should not have any problems", 0, problems.size());
    }
}
