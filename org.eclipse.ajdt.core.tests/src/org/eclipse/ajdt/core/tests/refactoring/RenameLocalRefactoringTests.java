/*******************************************************************************
 * Copyright (c) 2010  SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.refactoring;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Test that renaming local variables proceed correctly even when there are ITDs applied to target type
 * This is all around Bug 384314
 */
public class RenameLocalRefactoringTests extends AbstractAJDTRefactoringTest {
    
    
    /**
     * Test the basics
     * @throws Exception
     */
    public void testSimple() throws Exception {
        performRefactoringAndUndo(
                "xx", 
                "xxNEW", 
        new String[] { 
                "p", 
        }, 
        new String[] {
                "Java1.java", 
        }, new String[] {
                "package p;\npublic class Java1 { void meth() { int xx = 9; } }", 
        }, new String[] { 
                "package p;\npublic class Java1 { void meth() { int xxNEW = 9; } }", 
        });
    }
    
    public void testRenameWithITD() throws Exception {
        performRefactoringAndUndo(
                "xx", 
                "xxNEW", 
                new String[] { 
                        "p", 
                        "p",
                }, 
                new String[] {
                        "Java1.java",
                        "Aspect1.aj"
                }, new String[] {
                        "package p;\npublic class Java1 { void meth() { int xx = 9; } }", 
                        "package p;\npublic aspect Aspect1 { void Java1.meth2() { int xx = 9; } }", 
                }, new String[] { 
                        "package p;\npublic class Java1 { void meth() { int xxNEW = 9; } }", 
                        "package p;\npublic aspect Aspect1 { void Java1.meth2() { int xx = 9; } }", 
                });
    }
    
    public void testRenameWithDeclareParents() throws Exception {
        performRefactoringAndUndo(
                "xx", 
                "xxNEW", 
                new String[] { 
                        "p", 
                        "p",
                }, 
                new String[] {
                        "Java1.java",
                        "Aspect1.aj"
                }, new String[] {
                        "package p;\npublic class Java1 { void meth() { int xx = 9; } }", 
                        "package p;\npublic aspect Aspect1 { declare parents : Java1 implements java.io.Serializable; }", 
                }, new String[] { 
                        "package p;\npublic class Java1 { void meth() { int xxNEW = 9; } }", 
                        "package p;\npublic aspect Aspect1 { declare parents : Java1 implements java.io.Serializable; }", 
                });
    }
    
    public void testRenameWithDeclareParentsAndITD() throws Exception {
        performRefactoringAndUndo(
                "xx", 
                "xxNEW", 
                new String[] { 
                        "p", 
                        "p",
                }, 
                new String[] {
                        "Java1.java",
                        "Aspect1.aj"
                }, new String[] {
                        "package p;\npublic class Java1 { void meth() { int xx = 9; xx++; } }", 
                        "package p;\npublic aspect Aspect1 { declare parents : Java1 implements java.io.Serializable;\nvoid Java1.meth2() { int xx = 9; } }", 
                }, new String[] { 
                        "package p;\npublic class Java1 { void meth() { int xxNEW = 9; xxNEW++; } }", 
                        "package p;\npublic aspect Aspect1 { declare parents : Java1 implements java.io.Serializable;\nvoid Java1.meth2() { int xx = 9; } }", 
                });
    }
    public void testRenameWithDeclareParentsAndITDAndDeclareAnnotation() throws Exception {
        performRefactoringAndUndo(
                "xx", 
                "xxNEW", 
                new String[] { 
                        "p", 
                        "p",
                }, 
                new String[] {
                        "Java1.java",
                        "Aspect1.aj"
                }, new String[] {
                        "package p;\npublic class Java1 { void meth() { int xx = 9; xx++; } }", 
                        "package p;\npublic aspect Aspect1 { declare parents : Java1 implements java.io.Serializable;\nvoid Java1.meth2() { int xx = 9; }\ndeclare @type : Java1 : @Deprecated; }", 
                }, new String[] { 
                        "package p;\npublic class Java1 { void meth() { int xxNEW = 9; xxNEW++; } }", 
                        "package p;\npublic aspect Aspect1 { declare parents : Java1 implements java.io.Serializable;\nvoid Java1.meth2() { int xx = 9; }\ndeclare @type : Java1 : @Deprecated; }", 
                });
    }
    
    public void testRenameInITD() throws Exception {
        performRefactoringAndUndo(
                "xx", 
                "xxNEW", 
                new String[] { 
                        "p", 
                        "p",
                }, 
                new String[] {
                        "Aspect1.aj",
                        "Java1.java",
                }, new String[] {
                        "package p;\npublic aspect Aspect1 { void Java1.meth2() { int xx = 9; } }", 
                        "package p;\npublic class Java1 { void meth() { int xx = 9; } }", 
                }, new String[] { 
                        "package p;\npublic aspect Aspect1 { void Java1.meth2() { int xxNEW = 9; } }", 
                        "package p;\npublic class Java1 { void meth() { int xx = 9; } }", 
                });
    }
    
    // assume local variable is in the first CU's first type and first method and has a type of 'int'
    private void performRefactoringAndUndo(String oldVariableName, String newVariableName, String[] packNames, String[] cuNames, String[] initialContents, String[] finalContents) throws Exception {
        ICompilationUnit[] origUnits = createUnits(packNames, cuNames, initialContents);

        
        RenameJavaElementDescriptor descriptor = RefactoringSignatureDescriptorFactory
                                        .createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_LOCAL_VARIABLE);
        descriptor.setNewName(newVariableName);
        
        int start = initialContents[0].indexOf(oldVariableName);
        int end = start + oldVariableName.length() - 1;
        ILocalVariable var = new LocalVariable((JavaElement) getFirstMethod(origUnits[0]), oldVariableName, start, end, start, 
                end, "I", new Annotation[0], 0, false);
        descriptor.setJavaElement(var);
        descriptor.setUpdateReferences(true);
        
        Refactoring refactoring = createRefactoring(descriptor);
        RefactoringStatus result = performRefactoring(refactoring, true, true);
        
        result = ignoreKnownErrors(result);

        assertTrue("Refactoring produced an error: " + result, result.isOK());
        
        assertContents(origUnits, finalContents);
        
        // undo
        assertTrue("anythingToUndo", RefactoringCore.getUndoManager()
                .anythingToUndo());
        assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager()
                .anythingToRedo());

        RefactoringCore.getUndoManager().performUndo(null,
                new NullProgressMonitor());
        
        assertContents(origUnits, initialContents);

        // redo
        assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager()
                .anythingToUndo());
        assertTrue("anythingToRedo", RefactoringCore.getUndoManager()
                .anythingToRedo());
        RefactoringCore.getUndoManager().performRedo(null,
                new NullProgressMonitor());
        assertContents(origUnits, finalContents);
    }
}