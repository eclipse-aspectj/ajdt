/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *     Andrew Eisenberg - completely rewritten for 2.1.0
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.refactoring;

import java.util.Arrays;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public class AspectRenameParticipantTest extends AbstractAJDTRefactoringTest {
    
    public void testSimpleRename1() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { "Class.java" } ,
                new String[] { "class Class { Class() { } }" }, 
                new String[] { "class XXX { XXX() { } }" });
    }
    
    public void testSimpleRename2() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                    "Class.java", 
                    "Aspect.aj", 
                } ,
                new String[] { 
                    "class Class { Class() { } }", 
                    "aspect Aspect { Class clazz; }", 
                }, 
                new String[] { 
                    "class XXX { XXX() { } }",
                    "aspect Aspect { XXX clazz; }", 
                });
    }
    
    /* E35 failing on build server, but passing locally */
    public void _testRenameITD1() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "class Class {\n Class() { } }", 
                "aspect Aspect {\n Class Class.clazz; }", 
        }, 
        new String[] { 
                "class XXX {\n XXX() { } }",
                "aspect Aspect {\n XXX XXX.clazz; }", 
        });
    }
    
    /* E35 failing on build server, but passing locally */
    public void _testRenameITD2() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "class Class {\n Class() { } }", 
                "aspect Aspect {\n Class Class.clazz; }", 
        }, 
        new String[] { 
                "class XXX {\n XXX() { } }",
                "aspect Aspect {\n XXX XXX.clazz; }", 
        });
    }
    
    public void testRenameDeclare1() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "class Class {\n Class() { } }", 
                "aspect Aspect {\n declare parents : Class extends Object; }", 
        }, 
        new String[] { 
                "class XXX {\n XXX() { } }",
                "aspect Aspect {\n declare parents : XXX extends Object; }", 
        });
    }
    
    public void testRenamePointcut1() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "class Class {\n Class() { } }", 
                "aspect Aspect {\n before() : within(Class) { } }", 
        }, 
        new String[] { 
                "class XXX {\n XXX() { } }",
                "aspect Aspect {\n before() : within(XXX) { } }", 
        });
    }
    
    
    // assume we are renaming the first type in the first CU
    private void performRefactoringAndUndo(String newName, String[] cuNames, String[] initialContents, String[] finalContents) throws Exception {
        String[] packNames = new String[cuNames.length];
        Arrays.fill(packNames, "");
        performRefactoringAndUndo(newName, packNames, cuNames, initialContents, finalContents);
    }
    private void performRefactoringAndUndo(String newName, String[] packNames, String[] cuNames, String[] initialContents, String[] finalContents) throws Exception {
        ICompilationUnit[] units = createUnits(packNames, cuNames, initialContents);
        
        IType type = units[0].getAllTypes()[0];
        
        RenameJavaElementDescriptor descriptor = RefactoringSignatureDescriptorFactory
                .createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_TYPE);
        descriptor.setUpdateReferences(true);
        descriptor.setJavaElement(type);
        descriptor.setNewName(newName);
        
        RenameRefactoring refactoring = (RenameRefactoring) createRefactoring(descriptor);
        RefactoringStatus result = performRefactoring(refactoring, true, true);
        
        result = ignoreKnownErrors(result);

        assertTrue("Refactoring produced an error: " + result, result.isOK());
        
        ICompilationUnit newUnit = getNewUnit(newName, packNames[0]);
        ICompilationUnit origUnit = units[0];
        units[0] = newUnit;
        assertContents(units, finalContents);
        
        // undo
        assertTrue("anythingToUndo", RefactoringCore.getUndoManager()
                .anythingToUndo());
        assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager()
                .anythingToRedo());

        RefactoringCore.getUndoManager().performUndo(null,
                new NullProgressMonitor());
        
        units[0] = origUnit;
        assertContents(units, initialContents);

        // redo
        assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager()
                .anythingToUndo());
        assertTrue("anythingToRedo", RefactoringCore.getUndoManager()
                .anythingToRedo());
        RefactoringCore.getUndoManager().performRedo(null,
                new NullProgressMonitor());
        units[0] = newUnit;
        assertContents(units, finalContents);
    }

    private ICompilationUnit getNewUnit(String newName, String packName) throws JavaModelException {
        String qualName = packName.length() > 0 ? packName + "." + newName : newName;
        return project.findType(qualName).getCompilationUnit();
    }

    

}