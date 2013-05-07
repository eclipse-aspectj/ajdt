/*******************************************************************************
 * Copyright (c) 2006. 2010 IBM Corporation, SpringSource and others.
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

/**
 * Test that renaming types works well inside of Aspects.
 */
public class AspectRenameRefactoringTests extends AbstractAJDTRefactoringTest {
    
    /** Just testing that the refactoring infrastructure is working for standard situations */
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
    
    public void testRenameITD1() throws Exception {
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
    
    public void testRenameITD2() throws Exception {
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
    
    public void testRenameITD3() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "class Class {\n Class() { } }", 
                "aspect Aspect {\n /** {@link Class} */\n Class Class.clazz; }", 
        }, 
        new String[] { 
                "class XXX {\n XXX() { } }",
                "aspect Aspect {\n /** {@link XXX} */\n XXX XXX.clazz; }", 
        });
    }
    
    public void testRenameITD4() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "class Class {\n Class() { } }", 
                "aspect Aspect {\n void Class.clazz() { } }", 
        }, 
        new String[] { 
                "class XXX {\n XXX() { } }",
                "aspect Aspect {\n void XXX.clazz() { } }", 
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
    
    public void testRenameDeclare2() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "class Class {\n Class() { } }", 
                "aspect Aspect {\n declare parents : Foo extends Class;\n class Foo { } }", 
        }, 
        new String[] { 
                "class XXX {\n XXX() { } }",
                "aspect Aspect {\n declare parents : Foo extends XXX;\n class Foo { } }", 
        });
    }
    
    public void testRenameDeclare3() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "class Class {\n Class() { } }", 
                "aspect Aspect {\n declare @type : Class : @Foo;\n @interface Foo { } }", 
        }, 
        new String[] { 
                "class XXX {\n XXX() { } }",
                "aspect Aspect {\n declare @type : XXX : @Foo;\n @interface Foo { } }", 
        });
    }
    
    public void testRenameDeclare4() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "Annotation.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "@interface Annotation { }", 
                "aspect Aspect {\n declare @type : Foo : @Annotation;\n class Foo { } }", 
        }, 
        new String[] { 
                "@interface XXX { }", 
                "aspect Aspect {\n declare @type : Foo : @XXX;\n class Foo { } }", 
        });
    }
    
    public void testRenameDeclare5() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "Annotation.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "@interface Annotation { }", 
                "aspect Aspect {\n declare parents : (@Annotation *) extends Foo;\n class Foo { } }", 
        }, 
        new String[] { 
                "@interface XXX { }", 
                "aspect Aspect {\n declare parents : (@XXX *) extends Foo;\n class Foo { } }", 
        });
    }
    
    public void testRenameDeclare6() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "class Class {\n void xxx() { } }", 
                "aspect Aspect {\n declare @method : void Class.xxx() : @Foo;\n @interface Foo { } }", 
        }, 
        new String[] { 
                "class XXX {\n void xxx() { } }",
                "aspect Aspect {\n declare @method : void XXX.xxx() : @Foo;\n @interface Foo { } }", 
        });
    }
    
    public void testRenameDeclare7() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "class Class {\n Class() { } }", 
                "aspect Aspect {\n declare @constructor : Class.new() : @Foo;\n @interface Foo { } }", 
        }, 
        new String[] { 
                "class XXX {\n XXX() { } }",
                "aspect Aspect {\n declare @constructor : XXX.new() : @Foo;\n @interface Foo { } }", 
        });
    }
    
    public void testRenameDeclare8() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "class Class {\n int xxx; { } }", 
                "aspect Aspect {\n declare @field : int Class.xxx : @Foo;\n @interface Foo { } }", 
        }, 
        new String[] { 
                "class XXX {\n int xxx; { } }", 
                "aspect Aspect {\n declare @field : int XXX.xxx : @Foo;\n @interface Foo { } }", 
        });
    }
    
    /**
     * renaming within PCDs are not completely supported yet. 
     * @throws Exception
     */
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
    
    // now we do a bunch of tests that ensure renaming occurs properly with fully qualified names and types in different packages
    // can't test pointcuts, but can test decp, and dec ann
    
    public void testRenameITDPackages1() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "q", 
        } ,
        new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\n public class Class {\n Class() { } }", 
                "package q;\nimport p.Class;\n aspect Aspect {\n /** {@link Class} */\n Class Class.clazz; }", 
        }, 
        new String[] { 
                "package p;\n public class XXX {\n XXX() { } }", 
                "package q;\nimport p.XXX;\n aspect Aspect {\n /** {@link XXX} */\n XXX XXX.clazz; }", 
        });
    }

    public void testRenameITDPackages2() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "q", 
        } ,
        new String[] { 
                "AClass.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\n public class AClass {\n AClass() { } }", 
                "package q;\nimport p.*;\n aspect Aspect {\n /** {@link AClass} */\n AClass AClass.clazz; }", 
        }, 
        new String[] { 
                "package p;\n public class XXX {\n XXX() { } }", 
                "package q;\nimport p.*;\n aspect Aspect {\n /** {@link XXX} */\n XXX XXX.clazz; }", 
        });
    }
    
    // Failing on build server...disable
    public void _testRenameITDPackages3() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "q", 
        } ,
        new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\npublic class Class {\n Class() { } }", 
                "package q;\naspect Aspect {\n /** {@link p.Class} */\n p.Class p.Class.clazz; }", 
        }, 
        new String[] { 
                "package p;\npublic class XXX {\n XXX() { } }", 
                "package q;\naspect Aspect {\n /** {@link p.XXX} */\n p.XXX p.XXX.clazz; }", 
        });
    }
    
    public void testRenameITDPackages4() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "q", 
        } ,
        new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\npublic class Class {\n Class() { } }", 
                "package q;\naspect Aspect {\n void p.Class.clazz() { } }", 
        }, 
        new String[] { 
                "package p;\npublic class XXX {\n XXX() { } }", 
                "package q;\naspect Aspect {\n void p.XXX.clazz() { } }", 
        });
    }
    
    public void testRenameITDPackages5() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "q", 
        } ,
        new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\npublic class Class {\n public Class() { } }", 
                "package q;\naspect Aspect {\n p.Class.new(int x) {\n this(); } }", 
        }, 
        new String[] { 
                "package p;\npublic class XXX {\n public XXX() { } }", 
                "package q;\naspect Aspect {\n p.XXX.new(int x) {\n this(); } }", 
        });
    }
    
    public void testRenameDeclarePackage1() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "q", 
        } ,
        new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\npublic class Class {\n Class() { } }", 
                "package q;\naspect Aspect {\n declare parents : p.Class extends Object; }", 
        }, 
        new String[] { 
                "package p;\npublic class XXX {\n XXX() { } }",
                "package q;\naspect Aspect {\n declare parents : p.XXX extends Object; }", 
        });
    }
    
    public void testRenameDeclarePackage2() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "q", 
        } ,
        new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\npublic class Class {\n public Class() { } }", 
                "package q;\naspect Aspect {\n declare parents : Foo extends p.Class;\n class Foo { } }", 
        }, 
        new String[] { 
                "package p;\npublic class XXX {\n public XXX() { } }",
                "package q;\naspect Aspect {\n declare parents : Foo extends p.XXX;\n class Foo { } }", 
        });
    }
    
    public void testRenameDeclarePackage3() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "q", 
        } ,
        new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\npublic class Class {\n Class() { } }", 
                "package q;\naspect Aspect {\n declare @type : p.Class : @Foo;\n @interface Foo { } }", 
        }, 
        new String[] { 
                "package p;\npublic class XXX {\n XXX() { } }",
                "package q;\naspect Aspect {\n declare @type : p.XXX : @Foo;\n @interface Foo { } }", 
        });
    }
    
    public void testRenameDeclarePackage4() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "q", 
        } ,
        new String[] { 
                "Annotation.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\npublic @interface Annotation { }", 
                "package q;\naspect Aspect {\n declare @type : Foo : @p.Annotation;\n class Foo { } }", 
        }, 
        new String[] { 
                "package p;\npublic @interface XXX { }", 
                "package q;\naspect Aspect {\n declare @type : Foo : @p.XXX;\n class Foo { } }", 
        });
    }
    
    public void testRenameDeclarePackage5() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "q", 
        } ,
        new String[] { 
                "Annotation.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\npublic @interface Annotation { }", 
                "package q;\naspect Aspect {\n declare parents : (@p.Annotation *) extends Foo;\n class Foo { } }", 
        }, 
        new String[] { 
                "package p;\npublic @interface XXX { }", 
                "package q;\naspect Aspect {\n declare parents : (@p.XXX *) extends Foo;\n class Foo { } }", 
        });
    }
    
    public void testRenameDeclarePackage6() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "q", 
        } ,
        new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\npublic class Class {\n public void xxx() { } }", 
                "package q;\naspect Aspect {\n declare @method : public void p.Class.xxx() : @Foo;\n @interface Foo { } }", 
        }, 
        new String[] { 
                "package p;\npublic class XXX {\n public void xxx() { } }", 
                "package q;\naspect Aspect {\n declare @method : public void p.XXX.xxx() : @Foo;\n @interface Foo { } }", 
        });
    }
    
    public void testRenameDeclarePackage7() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "q", 
        } ,
        new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\npublic class Class {\n Class() { } }", 
                "package q;\naspect Aspect {\n declare @constructor : p.Class.new() : @Foo;\n @interface Foo { } }", 
        }, 
        new String[] { 
                "package p;\npublic class XXX {\n XXX() { } }", 
                "package q;\naspect Aspect {\n declare @constructor : p.XXX.new() : @Foo;\n @interface Foo { } }", 
        });
    }

    public void testRenameDeclarePackage8() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "q", 
        } ,
        new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\npublic class Class {\n public int xxx; { } }", 
                "package q;\naspect Aspect {\n declare @field : public int p.Class.xxx : @Foo;\n @interface Foo { } }", 
        }, 
        new String[] { 
                "package p;\npublic class XXX {\n public int xxx; { } }", 
                "package q;\naspect Aspect {\n declare @field : public int p.XXX.xxx : @Foo;\n @interface Foo { } }", 
        });
    }
    
    public void testRenameTypeInITD1() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "p", 
        } ,
        new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\npublic class Class {  }", 
                "package p;\naspect Aspect {\n  Class Class.bar() { Class a = new Class(); return a; } }", 
        }, 
        new String[] { 
                "package p;\npublic class XXX {  }", 
                "package p;\naspect Aspect {\n  XXX XXX.bar() { XXX a = new XXX(); return a; } }", 
        });
    }
    
    public void testRenameTypeInITD2() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "p", 
        } ,
        new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\npublic class Class {  }", 
                "package p;\naspect Aspect {\n  Class Class.bar= new Class(); }", 
        }, 
        new String[] { 
                "package p;\npublic class XXX {  }", 
                "package p;\naspect Aspect {\n  XXX XXX.bar= new XXX(); }", 
        });
    }
    
    public void testRenameTypeInAdvice1() throws Exception {
        performRefactoringAndUndo("XXX", 
                new String[] { 
                "p", 
                "p", 
        } ,
        new String[] { 
                "Class.java", 
                "Aspect.aj", 
        } ,
        new String[] { 
                "package p;\npublic class Class {  }", 
                "package p;\naspect Aspect {\n  before() : adviceexecution() { Class c = new Class(); } }", 
        }, 
        new String[] { 
                "package p;\npublic class XXX {  }", 
                "package p;\naspect Aspect {\n  before() : adviceexecution() { XXX c = new XXX(); } }", 
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