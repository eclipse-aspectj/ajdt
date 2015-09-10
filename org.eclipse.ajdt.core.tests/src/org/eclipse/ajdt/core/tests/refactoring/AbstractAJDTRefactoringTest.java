/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *     Kris De Volder - minor changes to visibility modifiers
 *******************************************************************************/

package org.eclipse.ajdt.core.tests.refactoring;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
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
public abstract class AbstractAJDTRefactoringTest extends AJDTCoreTestCase {
    protected IJavaProject project;
    protected IPackageFragment p;
    protected void setUp() throws Exception {
        super.setUp();
        IProject maybeProject = ResourcesPlugin.getWorkspace().getRoot().getProject("DefaultEmptyProject");
        if (!maybeProject.exists()) {
            project = JavaCore.create(createPredefinedProject("DefaultEmptyProject"));
        } else {
            project = JavaCore.create(maybeProject);
        }
        p = createPackage("p", project);
    }
    
    protected ICompilationUnit[] createUnits(String[] packages, String[] cuNames, String[] cuContents) throws CoreException {
        return super.createUnits(packages, cuNames, cuContents, project);
    }
    
    protected ICompilationUnit createUnit(String pkg, String cuName, String cuContents) throws CoreException {
        return super.createUnit(pkg, cuName, cuContents, project);
    }
    
    protected void assertContents(ICompilationUnit[] existingUnits, String[] expectedContents) throws JavaModelException {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < existingUnits.length; i++) {
            if (expectedContents[i] != null) {
                char[] contents = extractContents(existingUnits[i]);
                
                String actualContents = String.valueOf(contents);
                if (!actualContents.equals(expectedContents[i])) {
                    sb.append("\n-----EXPECTING-----\n");
                    sb.append(expectedContents[i]);
                    sb.append("\n--------WAS--------\n");
                    sb.append(actualContents);
                    sb.append("\nExpected length="+expectedContents[i].length());
                    sb.append("\nActual length="+actualContents.length());
                    sb.append("\n");
                }
            } else {
                // unit should have been deleted
                if (existingUnits[i].exists()) {
                    sb.append("\nUnit " + existingUnits[i].getElementName() + " should have been deleted.\n");
                    sb.append("Instead had the following contents:\n");
                    sb.append(extractContents(existingUnits[i]));
                }
            }
        }
        if (sb.length() > 0) {
            fail("Refactoring produced unexpected results:" + sb.toString());
        }
    }

    public char[] extractContents(ICompilationUnit unit)
            throws JavaModelException {
        char[] contents;
        if (unit instanceof AJCompilationUnit) {
            ((AJCompilationUnit) unit).requestOriginalContentMode();
        }
        contents = ((CompilationUnit) unit).getContents();
        if (unit instanceof AJCompilationUnit) {
            ((AJCompilationUnit) unit).discardOriginalContentMode();
        }
        return contents;
    }

    protected void assertContents(ICompilationUnit existingUnits, String expectedContents) throws JavaModelException {
        StringBuffer sb = new StringBuffer();
        char[] contents;
        if (existingUnits instanceof AJCompilationUnit) {
            ((AJCompilationUnit) existingUnits).requestOriginalContentMode();
        }
        contents = ((CompilationUnit) existingUnits).getContents();
        if (existingUnits instanceof AJCompilationUnit) {
            ((AJCompilationUnit) existingUnits).discardOriginalContentMode();
        }
        String actualContents = String.valueOf(contents);
        if (!actualContents.equals(expectedContents)) {
            sb.append("\n-----EXPECTING-----\n");
            sb.append(expectedContents);
            sb.append("\n--------WAS--------\n");
            sb.append(actualContents);
        }
        if (sb.length() > 0) {
            fail("Refactoring produced unexpected results:" + sb.toString());
        }
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
        performDummySearch(project);
    }
    
    
    protected final Refactoring createRefactoring(RefactoringDescriptor descriptor) throws CoreException {
        RefactoringStatus status= new RefactoringStatus();
        Refactoring refactoring= descriptor.createRefactoring(status);
        assertNotNull("refactoring should not be null", refactoring);
        assertTrue("status should be ok, but was: " + status, status.isOK());
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
