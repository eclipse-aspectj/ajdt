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
import org.eclipse.jdt.internal.corext.refactoring.code.PromoteTempToFieldRefactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author Andrew Eisenberg
 * @created Apr 23, 2010
 *
 */
public class ConvertLocalToFieldTests extends AbstractAJDTRefactoringTest {
    
    public void testConvertLocalToFieldSimple1() throws Exception {
        String initialContents = "class Foo { void x() { int hh = 9 + 8; } }";
        String toExtract = "hh";
        String finalContents = "class Foo { private int xx;\n\nvoid x() { xx = 9 + 8; } }";
        performRefactoringAndUndo("xx", "Foo.java", initialContents, finalContents, initialContents.indexOf(toExtract), toExtract.length());
    }
    public void testConvertLocalToFieldSimple2() throws Exception {
        String initialContents = "class Foo { void x() { int hh = 9 + 8; } }";
        String toExtract = "hh";
        String finalContents = "class Foo { private int xx;\n\nvoid x() { xx = 9 + 8; } }";
        performRefactoringAndUndo("xx", "Foo.aj", initialContents, finalContents, initialContents.indexOf(toExtract), toExtract.length());
    }
    
    public void testConvertLocalToFieldAspect1() throws Exception {
        String initialContents = "aspect Foo { void x() { int hh = 9 + 8; } }";
        String toExtract = "hh";
        String finalContents = "aspect Foo { private int xx;\n\nvoid x() { xx = 9 + 8; } }";
        performRefactoringAndUndo("xx", "Foo.aj", initialContents, finalContents, initialContents.indexOf(toExtract), toExtract.length());
    }

    public void testConvertLocalToFieldAspect2() throws Exception {
        String initialContents = "package p; aspect Foo { void x() { int hh = 9 + 8; } }";
        String toExtract = "hh";
        String finalContents = "package p; aspect Foo { private int xx;\n\nvoid x() { xx = 9 + 8; } }";
        performRefactoringAndUndo("xx", "p", "Foo.aj", initialContents, finalContents, initialContents.indexOf(toExtract), toExtract.length());
    }
    
    public void testConvertLocalToFieldAspect3() throws Exception {
        String initialContents = "package p; aspect Foo { void x() { int hh = 9 + 8; } }";
        String toExtract = "hh";
        String finalContents = "package p; aspect Foo { private int xx;\n\nvoid x() { xx = 9 + 8; } }";
        performRefactoringAndUndo("xx", "p", "Foo.aj", initialContents, finalContents, initialContents.indexOf(toExtract), toExtract.length());
    }
    
    public void testConvertLocalToFieldAspect4() throws Exception {
        String initialContents = "package p; aspect Foo { void X.x() { int hh = 9 + 8; } class X { } }";
        String toExtract = "hh";
        String finalContents = "package p; aspect Foo { private int xx;\nvoid X.x() { xx = 9 + 8; } class X { } }";
        performRefactoringAndUndo("xx", "p", "Foo.aj", initialContents, finalContents, initialContents.indexOf(toExtract), toExtract.length());
    }
    
    public void testConvertLocalToFieldAspect5() throws Exception {
        String initialContents = "package p; aspect Foo { before() : execution(* X.*(..)) { int hh = 9 + 8; } class X { } }";
        String toExtract = "hh";
        String finalContents = "package p; aspect Foo { private int xx;\nbefore() : execution(* X.*(..)) { xx = 9 + 8; } class X { } }";
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
        PromoteTempToFieldRefactoring convertLocal = new PromoteTempToFieldRefactoring(unit, start, length);
        convertLocal.setFieldName(varName);
        RefactoringStatus result = performRefactoring(convertLocal, true, false);

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
