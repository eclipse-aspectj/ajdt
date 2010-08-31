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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Test that renaming types works well inside of Aspects.
 */
public class RenamePackageRefactoringTests extends AbstractAJDTRefactoringTest {
    
    // still no tests for:
    // complex patterns are not implemented yet
    
    /**
     * Test the basics
     * @throws Exception
     */
    public void testSimpleMove1() throws Exception {
        performRefactoringAndUndo(
                "p.orig", 
                "p.NEW", 
        new String[] { 
                "p", 
                "p.orig", 
        }, 
        new String[] {
                "Java1.java", 
                "Java2.java", 
        }, new String[] {
                "package p;\nimport p.orig.Java2;\npublic class Java1 extends Java2 { }", 
                "package p.orig;\npublic class Java2 { }", 
        }, new String[] { 
                "package p;\nimport p.NEW.Java2;\npublic class Java1 extends Java2 { }", 
                "package p.NEW;\npublic class Java2 { }", 
        });
    }
    
    public void testSimpleMove2() throws Exception {
        performRefactoringAndUndo(
                "p.orig", 
                "p.NEW", 
                new String[] { 
                        "p", 
                        "p.orig", 
                }, 
                new String[] {
                        "Aspect1.aj", 
                        "Java2.java", 
                }, new String[] {
                        "package p;\nimport p.orig.Java2;\npublic aspect Aspect1 extends Java2 { }", 
                        "package p.orig;\npublic class Java2 { }", 
                }, new String[] { 
                        "package p;\nimport p.NEW.Java2;\npublic aspect Aspect1 extends Java2 { }", 
                        "package p.NEW;\npublic class Java2 { }", 
                });
    }
    
    /**
     * Although not really basic, all the renaming happens inside of the import declarations, 
     * so shouldn't really be affected by being inside an aspect
     * @throws Exception
     */
    public void testSimpleMove3() throws Exception {
        performRefactoringAndUndo(
                "p.orig", 
                "p.NEW", 
                new String[] { 
                        "p", 
                        "p.orig", 
                        "p.orig", 
                }, 
                new String[] {
                        "Aspect1.aj", 
                        "Java2.java", 
                        "Java3.java", 
                }, new String[] {
                        "package p;\nimport p.orig.Java2;\nimport p.orig.Java3;\npublic aspect Aspect1 {\n declare parents : Java2 extends Java3; }", 
                        "package p.orig;\npublic class Java2 { }", 
                        "package p.orig;\npublic class Java3 { }", 
                }, new String[] { 
                        "package p;\nimport p.NEW.Java2;\nimport p.NEW.Java3;\npublic aspect Aspect1 {\n declare parents : Java2 extends Java3; }", 
                        "package p.NEW;\npublic class Java2 { }", 
                        "package p.NEW;\npublic class Java3 { }", 
                });
    }
    
    public void testITDFieldPackageMove1() throws Exception {
        performRefactoringAndUndo(
                "p.orig", 
                "p.NEW", 
                new String[] { 
                        "p", 
                        "p.orig", 
                }, 
                new String[] {
                        "Aspect1.aj", 
                        "Java2.java", 
                }, new String[] {
                        "package p;\npublic aspect Aspect1 {\n int p.orig.Java2.x = 9; }", 
                        "package p.orig;\npublic class Java2 { }", 
                }, new String[] { 
                        "package p;\npublic aspect Aspect1 {\n int p.NEW.Java2.x = 9; }", 
                        "package p.NEW;\npublic class Java2 { }", 
                });
    }
    
    public void testITDFieldPackageMove2() throws Exception {
        performRefactoringAndUndo(
                "p.orig", 
                "p.NEW", 
                new String[] {
                        "p",
                        "p.orig", 
                        "p.orig", 
                }, 
                new String[] {
                        "Dummy.java",
                        "Aspect1.aj", 
                        "Java2.java", 
                }, new String[] {
                        "package p;\nclass Dummy{ }",  
                        "package p.orig;\npublic aspect Aspect1 {\n int p.orig.Java2.x = 9; }", 
                        "package p.orig;\npublic class Java2 { }", 
                }, new String[] { 
                        "package p;\nclass Dummy{ }",  
                        "package p.NEW;\npublic aspect Aspect1 {\n int p.NEW.Java2.x = 9; }", 
                        "package p.NEW;\npublic class Java2 { }", 
                });
    }
    
    public void testITDMethodPackageMove1() throws Exception {
        performRefactoringAndUndo(
                "p.orig", 
                "p.NEW", 
                new String[] { 
                        "p", 
                        "p.orig", 
                }, 
                new String[] {
                        "Aspect1.aj", 
                        "Java2.java", 
                }, new String[] {
                        "package p;\npublic aspect Aspect1 {\n void p.orig.Java2.x() { } }", 
                        "package p.orig;\npublic class Java2 { }", 
                }, new String[] { 
                        "package p;\npublic aspect Aspect1 {\n void p.NEW.Java2.x() { } }", 
                        "package p.NEW;\npublic class Java2 { }", 
                });
    }
    
    public void testITDConstructorPackageMove1() throws Exception {
        performRefactoringAndUndo(
                "p.orig", 
                "p.NEW", 
                new String[] { 
                        "p", 
                        "p.orig", 
                }, 
                new String[] {
                        "Aspect1.aj", 
                        "Java2.java", 
                }, new String[] {
                        "package p;\npublic aspect Aspect1 {\n p.orig.Java2.new() {\n this(); } }", 
                        "package p.orig;\npublic class Java2 { }", 
                }, new String[] { 
                        "package p;\npublic aspect Aspect1 {\n p.NEW.Java2.new() {\n this(); } }", 
                        "package p.NEW;\npublic class Java2 { }", 
                });
    }
    
    public void testDeclareParents1() throws Exception {
        performRefactoringAndUndo(
                "p.orig", 
                "p.NEW", 
                new String[] { 
                        "p", 
                        "p.orig", 
                        "p.orig", 
                }, 
                new String[] {
                        "Aspect1.aj", 
                        "Java2.java", 
                        "Java3.java", 
                }, new String[] {
                        "package p;\npublic aspect Aspect1 {\n declare parents : p.orig.Java2 extends p.orig.Java3; }", 
                        "package p.orig;\npublic class Java2 { }", 
                        "package p.orig;\npublic class Java3 { }", 
                }, new String[] { 
                        "package p;\npublic aspect Aspect1 {\n declare parents : p.NEW.Java2 extends p.NEW.Java3; }", 
                        "package p.NEW;\npublic class Java2 { }", 
                        "package p.NEW;\npublic class Java3 { }", 
                });
    }
    
    public void testDeclareParents2() throws Exception {
        performRefactoringAndUndo(
                "orig", 
                "aNEW", 
                new String[] { 
                        "p", 
                        "orig", 
                        "orig", 
                }, 
                new String[] {
                        "Aspect1.aj", 
                        "Java2.java", 
                        "Java3.java", 
                }, new String[] {
                        "package p;\npublic aspect Aspect1 {\n declare parents : orig.Java2 extends orig.Java3; }", 
                        "package orig;\npublic class Java2 { }", 
                        "package orig;\npublic class Java3 { }", 
                }, new String[] { 
                        "package p;\npublic aspect Aspect1 {\n declare parents : aNEW.Java2 extends aNEW.Java3; }", 
                        "package aNEW;\npublic class Java2 { }", 
                        "package aNEW;\npublic class Java3 { }", 
                });
    }
    
    public void testDeclareAnnotation1() throws Exception {
        performRefactoringAndUndo(
                "p.orig", 
                "p.NEW", 
                new String[] { 
                        "p", 
                        "p.orig", 
                        "p.orig", 
                }, 
                new String[] {
                        "Aspect1.aj", 
                        "Java2.java", 
                        "Java3.java", 
                }, new String[] {
                        "package p;\npublic aspect Aspect1 {\n declare @type : p.orig.Java2 : @p.orig.Java3; }", 
                        "package p.orig;\npublic class Java2 { }", 
                        "package p.orig;\npublic @interface Java3 { }", 
                }, new String[] { 
                        "package p;\npublic aspect Aspect1 {\n declare @type : p.NEW.Java2 : @p.NEW.Java3; }", 
                        "package p.NEW;\npublic class Java2 { }", 
                        "package p.NEW;\npublic @interface Java3 { }", 
                });
    }
    

    
    // assume first unit stays in same package and all others are moved
    private void performRefactoringAndUndo(String oldPackageName, String newPackageName, String[] packNames, String[] cuNames, String[] initialContents, String[] finalContents) throws Exception {
        IPackageFragment oldPackage = createPackage(oldPackageName, project);
        ICompilationUnit[] origUnits = createUnits(packNames, cuNames, initialContents);

        
        RenameJavaElementDescriptor descriptor = RefactoringSignatureDescriptorFactory
                                        .createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_PACKAGE);
        descriptor.setNewName(newPackageName);
        descriptor.setJavaElement(oldPackage);
        descriptor.setUpdateReferences(true);
        
        Refactoring refactoring = createRefactoring(descriptor);
        RefactoringStatus result = performRefactoring(refactoring, true, true);
        
        result = ignoreKnownErrors(result);

        assertTrue("Refactoring produced an error: " + result, result.isOK());
        
        // create the new units
        ICompilationUnit[] newUnits = new ICompilationUnit[origUnits.length];
        newUnits[0] = origUnits[0];
        for (int i = 1; i < origUnits.length; i++) {
            newUnits[i] = getNewUnit(newPackageName, origUnits[i].getElementName());
        }
        
        assertContents(newUnits, finalContents);
        
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
        assertContents(newUnits, finalContents);
    }

    private ICompilationUnit getNewUnit(String newPackName, String name) throws JavaModelException {
        int dotIndex = name.indexOf('.');
        String typeName = name.substring(0, dotIndex); 
        String qualName = newPackName.length() > 0 ? newPackName + "." + typeName : typeName;
        return project.findType(qualName).getCompilationUnit();
    }
}