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

import java.util.Arrays;

import org.eclipse.ajdt.core.tests.refactoring.AbstractAJDTRefactoringTest;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.refactoring.descriptors.MoveDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Test that it is possible to move a type into an aspect or an AJ compilation unit 
 * without errors
 */
public class MoveTypeIntoAspectRefactoringTests extends AbstractAJDTRefactoringTest {
    
    
    /**
     * Test that the basic stuff works
     * @throws Exception
     */
    public void testSimple1() throws Exception {
        performRefactoringAndUndo( 
                new String[] { 
                        "Class1.java", 
                        "Class2.java", 
                } ,
                new String[] { 
                        "class Class1 { }\nclass Inner { }", 
                        "class Class2 { }", 
                }, 
                new String[] {
                        "class Class1 { }", 
                        "class Class2 {\n\n\tclass Inner { } }", 
                },
                true);
    }
    
    public void testSimple2() throws Exception {
        performRefactoringAndUndo( 
                new String[] { 
                        "Class1.java", 
                        "Class2.java", 
                } ,
                new String[] { 
                        "class Class1 { }\nclass Inner { }", 
                        "class Class2 { }", 
                }, 
                new String[] {
                        "class Class1 { }", 
                        "class Class2 { }\n\nclass Inner { }", 
                },
                false);
    }
    
    public void testSimple3() throws Exception {
        performRefactoringAndUndo( 
                new String[] { 
                        "Class1.java", 
                        "Aspect1.aj", 
                } ,
                new String[] { 
                        "class Class1 { }\nclass Inner { }", 
                        "aspect Aspect1 { }", 
                }, 
                new String[] {
                        "class Class1 { }", 
                        "aspect Aspect1 {\n\n\tclass Inner { } }", 
                },
                true);
    }
    
    public void testSimple4() throws Exception {
        performRefactoringAndUndo( 
                new String[] { 
                        "Class1.java", 
                        "Aspect1.aj", 
                } ,
                new String[] { 
                        "class Class1 { }\nclass Inner { }", 
                        "aspect Aspect1 { }", 
                }, 
                new String[] {
                        "class Class1 { }", 
                        "aspect Aspect1 { }\n\nclass Inner { }", 
                },
                false);
    }
    
    public void testAspectWithITD1() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "p",
                        "p",
                        "p2",
                },
                new String[] { 
                        "Class1.java", 
                        "Aspect1.aj", 
                        "Third.java", 
                } ,
                new String[] { 
                        "package p;\nclass Class1 { }\nclass Inner { }", 
                        "package p;\nimport p2.Third;\naspect Aspect1 {\nint Third.x = 9; }",
                        "package p2;\npublic class Third { }",
                }, 
                new String[] {
                        "package p;\nclass Class1 { }", 
                        "package p;\nimport p2.Third;\naspect Aspect1 {\nclass Inner { }\n\nint Third.x = 9; }",
                        "package p2;\npublic class Third { }",
                },
                true);
    }

    public void testAspectWithITD2() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "p",
                        "p",
                        "p2",
                },
                new String[] { 
                        "Class1.java", 
                        "Aspect1.aj", 
                        "Third.java", 
                } ,
                new String[] { 
                        "package p;\nclass Class1 { }\nclass Inner { }", 
                        "package p;\nimport p2.Third;\naspect Aspect1 {\nint Third.x = 9; }",
                        "package p2;\npublic class Third { }",
                }, 
                new String[] {
                        "package p;\nclass Class1 { }", 
                        "package p;\nimport p2.Third;\naspect Aspect1 {\nint Third.x = 9; }\n\nclass Inner { }",
                        "package p2;\npublic class Third { }",
                },
                false);
    }
    
    
    private void performRefactoringAndUndo(String[] cuNames, String[] initialContents, String[] finalContents, boolean intoType) throws Exception {
        String[] packNames = new String[cuNames.length];
        Arrays.fill(packNames, "");
        performRefactoringAndUndo(packNames, cuNames, initialContents, finalContents, intoType);
    }

    
    // assume we are moving the second type in the first CU into the second CU or first type in second CU (depending on last argument)
    private void performRefactoringAndUndo(String[] packNames, String[] cuNames, String[] initialContents, String[] finalContents, boolean intoType) throws Exception {
        ICompilationUnit[] units = createUnits(packNames, cuNames, initialContents);
        
        MoveDescriptor descriptor = RefactoringSignatureDescriptorFactory
                .createMoveDescriptor();
        descriptor.setDestination(intoType ? units[1].getTypes()[0] : units[1]);
        descriptor.setUpdateReferences(true);
        descriptor.setProject(project.getElementName());
        descriptor.setUpdateQualifiedNames(true);
        descriptor.setMoveMembers(new IMember[] { units[0].getTypes()[1] });
        
        Refactoring refactoring = createRefactoring(descriptor);
        RefactoringStatus result = performRefactoring(refactoring, true, true);
        
        result = ignoreKnownErrors(result);

        assertTrue("Refactoring produced an error: " + result, result.isOK());
        
        assertContents(units, finalContents);
        
        // undo
        assertTrue("anythingToUndo", RefactoringCore.getUndoManager()
                .anythingToUndo());
        assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager()
                .anythingToRedo());

        RefactoringCore.getUndoManager().performUndo(null,
                new NullProgressMonitor());
        
        assertContents(units, initialContents);

        // redo
        assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager()
                .anythingToUndo());
        assertTrue("anythingToRedo", RefactoringCore.getUndoManager()
                .anythingToRedo());
        RefactoringCore.getUndoManager().performRedo(null,
                new NullProgressMonitor());
        assertContents(units, finalContents);
    }

}