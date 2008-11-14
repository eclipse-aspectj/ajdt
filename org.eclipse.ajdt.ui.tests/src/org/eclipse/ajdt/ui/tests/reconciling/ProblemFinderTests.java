package org.eclipse.ajdt.ui.tests.reconciling;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.parserbridge.AJCompilationUnitProblemFinder;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;

/**
 * Tests AJCompilationUnitProblemFinder
 * @author andrew
 *
 */
public class ProblemFinderTests extends UITestCase {
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
        
        assertEquals("Should not have any problems", 0, problems.size());
    }
    public void testNoProblemsOtherClass() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(otherClassCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);

        assertEquals("Should not have any problems", 0, problems.size());
    }
    public void testNoProblemsDemo() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(demoCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);

        assertEquals("Should not have any problems", 0, problems.size());
    }
    public void testNoProblemsMyAspectCU() throws Exception {
        HashMap problems = new HashMap();
        // these next two test super classes and interfaces
        AJCompilationUnitProblemFinder.processAJ(myAspectCU2, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        assertEquals("Should not have any problems", 0, problems.size());
    }
    public void testNoProblemsOtherClass2() throws Exception {
        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(otherClassCU2, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);

        assertEquals("Should not have any problems", 0, problems.size());

    }
    
    /**
     * project should have no problems at first
     * @throws Exception
     */
    public void testSyntaxError() throws Exception {
        otherClassCU.getBuffer().setContents(otherClassCU.getBuffer().getContents() + "gggg");

        HashMap problems = new HashMap();
        AJCompilationUnitProblemFinder.processAJ(otherClassCU, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);

        assertEquals("Should have one syntax error", 1, problems.size());
    }
    
    public void testNoReturnTypeError() throws Exception {
        String contents = otherClassCU.getBuffer().getContents();
        try {
            otherClassCU.getBuffer().setContents(contents.substring(0, contents.length()-2) + "t() { } }\n");
    
            HashMap problems = new HashMap();
            AJCompilationUnitProblemFinder.processAJ(otherClassCU, 
                    AJWorkingCopyOwner.INSTANCE, problems, true, 
                    ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
    
            assertEquals("Should have one syntax error.", 1, problems.size());
            CategorizedProblem prob = ((CategorizedProblem[]) problems.values().iterator().next())[0];
            assertEquals("Return type for the method is missing", prob.getMessage());
        } finally {
            // reset contents
            otherClassCU.getBuffer().setContents(contents);
        }
    }
    
    
    
    public void testReconciler() throws Exception {
        otherClassCU.becomeWorkingCopy(new MockProblemRequestor(), null);
        otherClassCU.reconcile(AST.JLS3, true, true, null, null);
        MockProblemRequestor requestor = (MockProblemRequestor) otherClassCU.getPerWorkingCopyInfo().getProblemRequestor();
        assertEquals("Problem requestor should have found no problems: " + requestor.problemString(), 0, requestor.problems.size());
    }
    
    public void testReconcilerWithErrors() throws Exception {
        String contents = otherClassCU.getBuffer().getContents();
        try {
            otherClassCU.becomeWorkingCopy(new MockProblemRequestor(), null);
            otherClassCU.getBuffer().setContents(contents + "gggg");
            
            otherClassCU.reconcile(AST.JLS3, true, true, null, null);
            MockProblemRequestor requestor = (MockProblemRequestor) otherClassCU.getPerWorkingCopyInfo().getProblemRequestor();
            assertEquals("Problem requestor should have found one problem: " + requestor.problemString(), 1, requestor.problems.size());
        } finally {
            // reset contents
            otherClassCU.getBuffer().setContents(contents);
        }

    }

    // not doing ITD aware problem finding
//    public void testNoMethodFound() throws Exception {
//        String contents = demoCU.getBuffer().getContents();
//        try {
//            demoCU.becomeWorkingCopy(new MockProblemRequestor(), null);
//            String s = contents;
//            s = s.replaceFirst("foo", "fffffff");
//            demoCU.getBuffer().setContents(s);
//            demoCU.reconcile(AST.JLS3, true, true, null, null);
//            MockProblemRequestor requestor = (MockProblemRequestor) demoCU.getPerWorkingCopyInfo().getProblemRequestor();
//            assertEquals("Problem requestor should have found one problem: " + requestor.problemString(), 1, requestor.problems.size());
//        } finally {
//            demoCU.getBuffer().setContents(contents);
//        }
//    }
    
    private class MockProblemRequestor implements IProblemRequestor {

        List problems = new LinkedList();
        
        public void acceptProblem(IProblem problem) {
            problems.add(problem);
        }

        public String problemString() {
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            for (Iterator probIter = problems.iterator(); probIter.hasNext();) {
                DefaultProblem prob = (DefaultProblem) probIter.next();
                sb.append("\n\t" + prob.toString());
            }
            sb.append("\n]");
            return sb.toString();
        }

        public void beginReporting() {
        }

        public void endReporting() {
        }

        public boolean isActive() {
            return true;
        }
        
        
        
    }
}