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

package org.eclipse.ajdt.ui.tests.refactoring;

import org.eclipse.ajdt.core.tests.refactoring.AbstractAJDTRefactoringTest;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractTempRefactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author Andrew Eisenberg
 * @created Apr 23, 2010
 *
 */
public class ExtractLocalTests extends AbstractAJDTRefactoringTest {
    
    public void testExtractLocalSimple1() throws Exception {
        String initialContents = "class Foo { void x() { int x = 9 + 8; } }";
        String toExtract = "9 + 8";
        String finalContents = "class Foo { void x() { int xx = 9 + 8;\nint x = xx; } }";
        performRefactoringAndUndo("xx", "Foo.java", initialContents, finalContents, initialContents.indexOf(toExtract), toExtract.length());
    }
    public void testExtractLocalSimple2() throws Exception {
        String initialContents = "class Foo { void x() { int x = 9 + 8; } }";
        String toExtract = "9 + 8";
        String finalContents = "class Foo { void x() { int xx = 9 + 8;\nint x = xx; } }";
        performRefactoringAndUndo("xx", "Foo.aj", initialContents, finalContents, initialContents.indexOf(toExtract), toExtract.length());
    }
    
    public void testExtractLocalAspect1() throws Exception {
        String initialContents = "aspect Foo { void x() { int x = 9 + 8; } }";
        String toExtract = "9 + 8";
        String finalContents = "aspect Foo { void x() { int xx = 9 + 8;\nint x = xx; } }";
        performRefactoringAndUndo("xx", "Foo.aj", initialContents, finalContents, initialContents.indexOf(toExtract), toExtract.length());
    }

    public void testExtractLocalAspect2() throws Exception {
        String initialContents = "package p; aspect Foo { void x() { int x = 9 + 8; } }";
        String toExtract = "9 + 8";
        String finalContents = "package p; aspect Foo { void x() { int xx = 9 + 8;\nint x = xx; } }";
        performRefactoringAndUndo("xx", "p", "Foo.aj", initialContents, finalContents, initialContents.indexOf(toExtract), toExtract.length());
    }
    
    public void testExtractLocalAspect3() throws Exception {
        String initialContents = "package p; aspect Foo { void x() { int x = 9 + 8; } }";
        String toExtract = "9 + 8";
        String finalContents = "package p; aspect Foo { void x() { int xx = 9 + 8;\nint x = xx; } }";
        performRefactoringAndUndo("xx", "p", "Foo.aj", initialContents, finalContents, initialContents.indexOf(toExtract), toExtract.length());
    }
    
    public void testExtractLocalAspect4() throws Exception {
        String initialContents = "package p; aspect Foo { void X.x() { int x = 9 + 8; } class X { } }";
        String toExtract = "9 + 8";
        String finalContents = "package p; aspect Foo { void X.x() { int xx = 9 + 8;\nint x = xx; } class X { } }";
        performRefactoringAndUndo("xx", "p", "Foo.aj", initialContents, finalContents, initialContents.indexOf(toExtract), toExtract.length());
    }
    
    public void testExtractLocalAspect5() throws Exception {
        String initialContents = "package p; aspect Foo { before() : execution(* X.*(..)) { int x = 9 + 8; } class X { } }";
        String toExtract = "9 + 8";
        String finalContents = "package p; aspect Foo { before() : execution(* X.*(..)) { int xx = 9 + 8;\nint x = xx; } class X { } }";
        performRefactoringAndUndo("xx", "p", "Foo.aj", initialContents, finalContents, initialContents.indexOf(toExtract), toExtract.length());
    }
    
    /**
     * Assumes that we are renaming the first itd of the first CU
     * @throws Exception 
     */
    private void performRefactoringAndUndo(String varName, String cuName, String initialContents, String finalContents, int start, int length) throws Exception {
        performRefactoringAndUndo(varName, "", cuName, initialContents, finalContents, start, length);
    }
    private void performRefactoringAndUndo(String varName, String packName, String cuName, String initialContents, String finalContents, int start, int length) throws Exception {
        ICompilationUnit unit = createUnit(packName, cuName, initialContents);
        ExtractTempRefactoring temp = new ExtractTempRefactoring(unit, start, length);
        temp.setTempName(varName);
        RefactoringStatus result = performRefactoring(temp, true, false);

        result = ignoreKnownErrors(result);

        assertTrue("Refactoring produced an error: " + result, result.isOK());

        assertContents(unit, finalContents);

        // undo
        assertTrue("anythingToUndo", RefactoringCore.getUndoManager()
                .anythingToUndo());
        assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager()
                .anythingToRedo());

        RefactoringCore.getUndoManager().performUndo(null,
                new NullProgressMonitor());
        assertContents(unit, initialContents);

        // redo
        assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager()
                .anythingToUndo());
        assertTrue("anythingToRedo", RefactoringCore.getUndoManager()
                .anythingToRedo());
        RefactoringCore.getUndoManager().performRedo(null,
                new NullProgressMonitor());
        assertContents(unit, finalContents);
    }
}
