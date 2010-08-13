package org.eclipse.contribution.weaving.jdt.tests.refactoring;

import org.eclipse.contribution.weaving.jdt.tests.WeavingTestCase;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

/**
 * @author Andrew Eisenberg
 * @created Apr 23, 2010
 *
 */
public abstract class AbstractWeavingRefactoringTest extends WeavingTestCase {
    protected IJavaProject project;
    protected IPackageFragment p;
    protected void setUp() throws Exception {
        super.setUp();
        project = JavaCore.create(createPredefinedProject("DefaultEmptyProject"));
        p = createPackage("p", project);
    }
    
    protected ICompilationUnit[] createUnits(String[] packages, String[] cuNames, String[] cuContents) throws CoreException {
        ICompilationUnit[] units = new ICompilationUnit[cuNames.length];
        for (int i = 0; i < units.length; i++) {
            units[i] = createCompilationUnitAndPackage(packages[i], cuNames[i], cuContents[i], project);
        }
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
        waitForManualBuild();
        waitForAutoBuild();
        assertNoProblems(project.getProject());
        return units;
    }
    
    protected void assertContents(ICompilationUnit[] existingUnits, String[] expectedContents) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < existingUnits.length; i++) {
            char[] contents = ((CompilationUnit) existingUnits[i]).getContents();
            String actualContents = String.valueOf(contents);
            if (!actualContents.equals(expectedContents[i])) {
                sb.append("\n-----EXPECTING-----\n");
                sb.append(expectedContents[i]);
                sb.append("\n--------WAS--------\n");
                sb.append(actualContents);
            }
        }
        if (sb.length() > 0) {
            fail("Refactoring produced unexpected results:" + sb.toString());
        }
    }
    
    protected IField getFirstField(ICompilationUnit[] units)
            throws JavaModelException {
        return (IField) units[0].getTypes()[0].getChildren()[0];
    }
    
    protected RefactoringStatus performRefactoring(Refactoring ref, boolean providesUndo, boolean performOnFail) throws Exception {
        // force updating of indexes
        super.buildProject(project);
        performDummySearch();
        IUndoManager undoManager= getUndoManager();
        final CreateChangeOperation create= new CreateChangeOperation(
            new CheckConditionsOperation(ref, CheckConditionsOperation.ALL_CONDITIONS),
            RefactoringStatus.FATAL);
        final PerformChangeOperation perform= new PerformChangeOperation(create);
        perform.setUndoManager(undoManager, ref.getName());
        IWorkspace workspace= ResourcesPlugin.getWorkspace();
        executePerformOperation(perform, workspace);
        RefactoringStatus status= create.getConditionCheckingStatus();
        assertTrue("Change wasn't executed", perform.changeExecuted() || ! perform.changeExecutionFailed());
        Change undo= perform.getUndoChange();
        if (providesUndo) {
            assertNotNull("Undo doesn't exist", undo);
            assertTrue("Undo manager is empty", undoManager.anythingToUndo());
        } else {
            assertNull("Undo manager contains undo but shouldn't", undo);
        }
        return status;
    }

    
    /**
     * Can ignore all errors that don't have anything to do with us.
     */
    protected RefactoringStatus ignoreKnownErrors(
            RefactoringStatus result) {
        if (result.getSeverity() != RefactoringStatus.ERROR) {
            return result;
        }
        
        RefactoringStatusEntry[] entries = result.getEntries();
        for (int i = 0; i < entries.length; i++) {
            // if this entries is known or it isn't an error,
            // then it can be ignored.
            // otherwise not OK.
            if (!checkStringForKnownErrors(entries[i].getMessage()) &&
                    entries[i].isError()) {
                return result;
            }
        }
        return new RefactoringStatus();
    }

    private boolean checkStringForKnownErrors(String resultString) {
        return resultString.indexOf("Found potential matches") >= 0 ||
        resultString.indexOf("Method breakpoint participant") >= 0 ||
        resultString.indexOf("Watchpoint participant") >= 0 || 
        resultString.indexOf("Breakpoint participant") >= 0 || 
        resultString.indexOf("Launch configuration participant") >= 0;
    }

    protected void performDummySearch() throws Exception {
        performDummySearch(p);
    }
    
    
    protected final Refactoring createRefactoring(RefactoringDescriptor descriptor) throws CoreException {
        RefactoringStatus status= new RefactoringStatus();
        Refactoring refactoring= descriptor.createRefactoring(status);
        assertNotNull("refactoring should not be null", refactoring);
        assertTrue("status should be ok", status.isOK());
        return refactoring;
    }

    protected IUndoManager getUndoManager() {
        IUndoManager undoManager= RefactoringCore.getUndoManager();
        undoManager.flush();
        return undoManager;
    }
    
    protected void executePerformOperation(final PerformChangeOperation perform, IWorkspace workspace) throws CoreException {
        workspace.run(perform, new NullProgressMonitor());
    }
}
