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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.descriptors.MoveDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Test that renaming types works well inside of Aspects.
 */
public class MoveCURefactoringTests extends AbstractAJDTRefactoringTest {
    
    // still no tests for:
    // move target has same simple name as something else and there is a complex pattern
    
    
    /**
     * Test the basics
     * @throws Exception
     */
    public void testSimpleMove1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
        }, 
        new String[] {
                "Java.java", 
        }, new String[] {
                "package p;\npublic class Java { }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
        });
    }
    
    public void testSimpleMove2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
        }, 
        new String[] {
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic aspect Aspect { }", 
        }, new String[] { 
                "package NEW;\npublic aspect Aspect { }", 
        });
    }
    
    public void testSimpleMove3() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect extends Java { }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package p;\n\nimport NEW.Java;\n\npublic aspect Aspect extends Java { }", 
        });
    }
    
    // Disable...failing on build server
    public void _testSimpleMove4() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect extends p.Java { }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package p;\npublic aspect Aspect extends NEW.Java { }", 
        });
    }
    
    // from same package to different
    public void testMoveTypeInITDField1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\npublic int Java.x; }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package p;\n\nimport NEW.Java;\n\npublic aspect Aspect {\npublic int Java.x; }", 
        });
    }
    
    public void testMoveTypeInITDMethod1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\npublic void Java.x() { } }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package p;\n\nimport NEW.Java;\n\npublic aspect Aspect {\npublic void Java.x() { } }", 
        });
    }
    
    public void testMoveTypeInITDConstructor1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\npublic Java.new(int x) {\n this(); } }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package p;\n\nimport NEW.Java;\n\npublic aspect Aspect {\npublic Java.new(int x) {\n this(); } }", 
        });
    }
    
    public void testMoveTypeInDeclareParents1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package p;\npublic class Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : Java extends Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package p;\npublic class Other { }", 
                "package p;\n\nimport NEW.Java;\n\npublic aspect Aspect {\ndeclare parents : Java extends Other; }", 
        });
    }
    
    public void testMoveTypeInDeclareParentsOther1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Other.java", 
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Other { }", 
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : Java extends Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Other { }", 
                "package p;\npublic class Java { }", 
                "package p;\n\nimport NEW.Other;\n\npublic aspect Aspect {\ndeclare parents : Java extends Other; }", 
        });
    }
    
    public void testMoveTypeInDeclareParentsComplex1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Annotation.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic @interface Annotation { }", 
                "package p;\npublic class Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : (@Annotation *) extends Other; }", 
        }, new String[] { 
                "package NEW;\npublic @interface Annotation { }", 
                "package p;\npublic class Other { }", 
                "package p;\n\nimport NEW.Annotation;\n\npublic aspect Aspect {\ndeclare parents : (@Annotation *) extends Other; }", 
        });
    }
    
    public void testMoveTypeInDeclareAtType1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @type : Java : @Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package p;\npublic @interface Other { }", 
                "package p;\n\nimport NEW.Java;\n\npublic aspect Aspect {\ndeclare @type : Java : @Other; }", 
        });
    }
    
    public void testMoveTypeInDeclareAtTypeAnnotation1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Other.java", 
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic @interface Other { }", 
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\ndeclare @type : Java : @Other; }", 
        }, new String[] { 
                "package NEW;\npublic @interface Other { }", 
                "package p;\npublic class Java { }", 
                "package p;\n\nimport NEW.Other;\n\npublic aspect Aspect {\ndeclare @type : Java : @Other; }", 
        });
    }
    
    public void testMoveTypeInDeclareAtMethod1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java {\n public void x() { }}", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @method : public void Java.x() : @Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Java {\n public void x() { }}", 
                "package p;\npublic @interface Other { }", 
                "package p;\n\nimport NEW.Java;\n\npublic aspect Aspect {\ndeclare @method : public void Java.x() : @Other; }", 
        });
    }
    
    public void testMoveTypeInDeclareAtConstructor1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { \npublic Java() { } }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @constructor : public Java.new() : @Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Java { \npublic Java() { } }", 
                "package p;\npublic @interface Other { }", 
                "package p;\n\nimport NEW.Java;\n\npublic aspect Aspect {\ndeclare @constructor : public Java.new() : @Other; }", 
        });
    }

    public void testMoveTypeInDeclareAtField1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java {\npublic int x; }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @field : public int Java.x : @Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Java {\npublic int x; }", 
                "package p;\npublic @interface Other { }", 
                "package p;\n\nimport NEW.Java;\n\npublic aspect Aspect {\ndeclare @field : public int Java.x : @Other; }", 
        });
    }

    // from same package to different, fully qualified
    public void testMoveTypeInITDField2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\npublic int p.Java.x; }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\npublic int NEW.Java.x; }", 
        });
    }
    
    public void testMoveTypeInITDMethod2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\npublic void p.Java.x() { } }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\npublic void NEW.Java.x() { } }", 
        });
    }
    
    public void testMoveTypeInITDConstructor2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\npublic p.Java.new(int x) {\n this(); } }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\npublic NEW.Java.new(int x) {\n this(); } }", 
        });
    }
    
    public void testMoveTypeInDeclareParents2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package p;\npublic class Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : p.Java extends Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package p;\npublic class Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : NEW.Java extends Other; }", 
        });
    }
    
    public void testMoveTypeInDeclareParentsOther2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Other.java", 
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Other { }", 
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : Java extends p.Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Other { }", 
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : Java extends NEW.Other; }", 
        });
    }
    
    // FAIL!!!
    public void testMoveTypeInDeclareParentsComplex2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Annotation.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic @interface Annotation { }", 
                "package p;\npublic class Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : (@p.Annotation *) extends Other; }", 
        }, new String[] { 
                "package NEW;\npublic @interface Annotation { }", 
                "package p;\npublic class Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : (@NEW.Annotation *) extends Other; }", 
        });
    }
    
    public void testMoveTypeInDeclareAtType2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @type : p.Java : @Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @type : NEW.Java : @Other; }", 
        });
    }
    
    // FAIL
    public void testMoveTypeInDeclareAtTypeAnnotation2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Other.java", 
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic @interface Other { }", 
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\ndeclare @type : Java : @p.Other; }", 
        }, new String[] { 
                "package NEW;\npublic @interface Other { }", 
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\ndeclare @type : Java : @NEW.Other; }", 
        });
    }
    
    // FAIL 
    public void testMoveTypeInDeclareAtMethod2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java {\n public void x() { }}", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @method : public void p.Java.x() : @Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Java {\n public void x() { }}", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @method : public void NEW.Java.x() : @Other; }", 
        });
    }
    
    //FAIL
    public void testMoveTypeInDeclareAtConstructor2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { \npublic Java() { } }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @constructor : public p.Java.new() : @Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Java { \npublic Java() { } }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @constructor : public NEW.Java.new() : @Other; }", 
        });
    }

    // FAIL
    public void testMoveTypeInDeclareAtField2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java {\npublic int x; }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @field : public int p.Java.x : @Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Java {\npublic int x; }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @field : public int NEW.Java.x : @Other; }", 
        });
    }

    
    // from different package to new package
    public void testMoveTypeInITDField3() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "q", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package q;\n\nimport p.Java;\n\npublic aspect Aspect {\npublic int Java.x; }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package q;\n\nimport NEW.Java;\n\npublic aspect Aspect {\npublic int Java.x; }", 
        });
    }
    
    public void testMoveTypeInITDMethod3() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "q", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package q;\n\nimport p.Java;\n\npublic aspect Aspect {\npublic void Java.x() { } }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package q;\n\nimport NEW.Java;\n\npublic aspect Aspect {\npublic void Java.x() { } }", 
        });
    }
    
    public void testMoveTypeInITDConstructor3() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "q", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package q;\n\nimport p.Java;\n\npublic aspect Aspect {\npublic Java.new(int x) {\n this(); } }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package q;\n\nimport NEW.Java;\n\npublic aspect Aspect {\npublic Java.new(int x) {\n this(); } }", 
        });
    }
    
    // from different package to new different, fully qualified
    public void testMoveTypeInITDField4() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "q", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package q;\npublic aspect Aspect {\npublic int p.Java.x; }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package q;\npublic aspect Aspect {\npublic int NEW.Java.x; }", 
        });
    }
    
    public void testMoveTypeInITDMethod4() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "q", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package q;\npublic aspect Aspect {\npublic void p.Java.x() { } }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package q;\npublic aspect Aspect {\npublic void NEW.Java.x() { } }", 
        });
    }
    
    public void testMoveTypeInITDConstructor4() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "q", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package q;\npublic aspect Aspect {\npublic p.Java.new(int x) {\n this(); } }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package q;\npublic aspect Aspect {\npublic NEW.Java.new(int x) {\n this(); } }", 
        });
    }
    
//    // from different package to same package
//    public void testMoveTypeInITDField5() throws Exception {
//        performRefactoringAndUndo("q", 
//                new String[] { 
//                "p", 
//                "q", 
//        }, 
//        new String[] {
//                "Java.java", 
//                "Aspect.aj", 
//        }, new String[] {
//                "package p;\npublic class Java { }", 
//                "package q;\n\nimport p.Java;\n\npublic aspect Aspect {\npublic int Java.x; }", 
//        }, new String[] { 
//                "package q;\npublic class Java { }", 
//                "package q;\n\n\npublic aspect Aspect {\npublic int Java.x; }", 
//        });
//    }
    
//    public void testMoveTypeInITDMethod5() throws Exception {
//        performRefactoringAndUndo("q", 
//                new String[] { 
//                "p", 
//                "q", 
//        }, 
//        new String[] {
//                "Java.java", 
//                "Aspect.aj", 
//        }, new String[] {
//                "package p;\npublic class Java { }", 
//                "package q;\n\nimport p.Java;\n\npublic aspect Aspect {\npublic void Java.x() { } }", 
//        }, new String[] { 
//                "package q;\npublic class Java { }", 
//                "package q;\n\n\npublic aspect Aspect {\npublic void Java.x() { } }", 
//        });
//    }
    
//    public void testMoveTypeInITDConstructor5() throws Exception {
//        performRefactoringAndUndo("q", 
//                new String[] { 
//                "p", 
//                "q", 
//        }, 
//        new String[] {
//                "Java.java", 
//                "Aspect.aj", 
//        }, new String[] {
//                "package p;\npublic class Java { }", 
//                "package q;\n\nimport p.Java;\n\npublic aspect Aspect {\npublic Java.new(int x) {\n this(); } }", 
//        }, new String[] { 
//                "package q;\npublic class Java { }", 
//                "package q;\n\n\npublic aspect Aspect {\npublic Java.new(int x) {\n this(); } }", 
//        });
//    }
    
    // from different package to same package, fully qualified
    public void testMoveTypeInITDField6() throws Exception {
        performRefactoringAndUndo("q", 
                new String[] { 
                "p", 
                "q", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package q;\npublic aspect Aspect {\npublic int p.Java.x; }", 
        }, new String[] { 
                "package q;\npublic class Java { }", 
                "package q;\npublic aspect Aspect {\npublic int q.Java.x; }", 
        });
    }
    
    public void testMoveTypeInITDMethod6() throws Exception {
        performRefactoringAndUndo("q", 
                new String[] { 
                "p", 
                "q", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package q;\npublic aspect Aspect {\npublic void p.Java.x() { } }", 
        }, new String[] { 
                "package q;\npublic class Java { }", 
                "package q;\npublic aspect Aspect {\npublic void q.Java.x() { } }", 
        });
    }
    
    public void testMoveTypeInITDConstructor6() throws Exception {
        performRefactoringAndUndo("q", 
                new String[] { 
                "p", 
                "q", 
        }, 
        new String[] {
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package q;\npublic aspect Aspect {\npublic p.Java.new(int x) {\n this(); } }", 
        }, new String[] { 
                "package q;\npublic class Java { }", 
                "package q;\npublic aspect Aspect {\npublic q.Java.new(int x) {\n this(); } }", 
        });
    }
    
    public void testMoveTypeInDeclareParents6() throws Exception {
        performRefactoringAndUndo("p", 
                new String[] { 
                "q", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package q;\npublic class Java { }", 
                "package p;\npublic class Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : q.Java extends Other; }", 
        }, new String[] { 
                "package p;\npublic class Java { }", 
                "package p;\npublic class Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : p.Java extends Other; }", 
        });
    }
    
    public void testMoveTypeInDeclareParentsOther6() throws Exception {
        performRefactoringAndUndo("p", 
                new String[] { 
                "q", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Other.java", 
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package q;\npublic class Other { }", 
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : Java extends q.Other; }", 
        }, new String[] { 
                "package p;\npublic class Other { }", 
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : Java extends p.Other; }", 
        });
    }
    
    // FAIL
    public void testMoveTypeInDeclareParentsComplex6() throws Exception {
        performRefactoringAndUndo("p", 
                new String[] { 
                "q", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Annotation.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package q;\npublic @interface Annotation { }", 
                "package p;\npublic class Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : (@q.Annotation *) extends Other; }", 
        }, new String[] { 
                "package p;\npublic @interface Annotation { }", 
                "package p;\npublic class Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare parents : (@p.Annotation *) extends Other; }", 
        });
    }
    
    public void testMoveTypeInDeclareAtType6() throws Exception {
        performRefactoringAndUndo("p", 
                new String[] { 
                "q", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package q;\npublic class Java { }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @type : q.Java : @Other; }", 
        }, new String[] { 
                "package p;\npublic class Java { }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @type : p.Java : @Other; }", 
        });
    }
    
    // FAIL
    public void testMoveTypeInDeclareAtTypeAnnotation6() throws Exception {
        performRefactoringAndUndo("p", 
                new String[] { 
                "q", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Other.java", 
                "Java.java", 
                "Aspect.aj", 
        }, new String[] {
                "package q;\npublic @interface Other { }", 
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\ndeclare @type : Java : @q.Other; }", 
        }, new String[] { 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic class Java { }", 
                "package p;\npublic aspect Aspect {\ndeclare @type : Java : @p.Other; }", 
        });
    }
    
    // FAIL
    public void testMoveTypeInDeclareAtMethod6() throws Exception {
        performRefactoringAndUndo("p", 
                new String[] { 
                "q", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package q;\npublic class Java {\n public void x() { }}", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @method : public void q.Java.x() : @Other; }", 
        }, new String[] { 
                "package p;\npublic class Java {\n public void x() { }}", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @method : public void p.Java.x() : @Other; }", 
        });
    }
    
    // FAIL
    public void testMoveTypeInDeclareAtConstructor6() throws Exception {
        performRefactoringAndUndo("p", 
                new String[] { 
                "q", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package q;\npublic class Java { \npublic Java() { } }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @constructor : public q.Java.new() : @Other; }", 
        }, new String[] { 
                "package p;\npublic class Java { \npublic Java() { } }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @constructor : public p.Java.new() : @Other; }", 
        });
    }

    // FAIL
    public void testMoveTypeInDeclareAtField6() throws Exception {
        performRefactoringAndUndo("p", 
                new String[] { 
                "q", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package q;\npublic class Java {\npublic int x; }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @field : public int q.Java.x : @Other; }", 
        }, new String[] { 
                "package p;\npublic class Java {\npublic int x; }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @field : public int p.Java.x : @Other; }", 
        });
    }

    // FAIL
    public void testMoveTypeWhenMultipleTypesOfSameSimpleNameExist1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "q", 
                "r", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package q;\npublic class Java { public int x; }", 
                "package r;\npublic class Java { public int x; }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @field : public int q.Java.x : @Other; \ndeclare @field : public int r.Java.x : @Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Java { public int x; }", 
                "package r;\npublic class Java { public int x; }", 
                "package p;\npublic @interface Other { }", 
                "package p;\npublic aspect Aspect {\ndeclare @field : public int NEW.Java.x : @Other; \ndeclare @field : public int r.Java.x : @Other; }", 
        });
    }
    
    public void testMoveTypeWhenMultipleTypesOfSameSimpleNameExist2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "q", 
                "r", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package q;\npublic class Java { public int x; }", 
                "package r;\npublic class Java { public int x; }", 
                "package p;\npublic @interface Other { }", 
                "package p;\n\nimport q.Java;\n\npublic aspect Aspect {\ndeclare @field : public int Java.x : @Other; \ndeclare @field : public int r.Java.x : @Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Java { public int x; }", 
                "package r;\npublic class Java { public int x; }", 
                "package p;\npublic @interface Other { }", 
                "package p;\n\nimport NEW.Java;\n\npublic aspect Aspect {\ndeclare @field : public int Java.x : @Other; \ndeclare @field : public int r.Java.x : @Other; }", 
        });
    }
    
    // FAIL
    public void testMoveTypeWhenMultipleTypesOfSameSimpleNameExist3() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "q", 
                "r", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package q;\npublic class Java { public int x; }", 
                "package r;\npublic class Java { public int x; }", 
                "package p;\npublic @interface Other { }", 
                "package p;\n\nimport r.Java;\n\npublic aspect Aspect {\ndeclare @field : public int q.Java.x : @Other; \ndeclare @field : public int Java.x : @Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Java { public int x; }", 
                "package r;\npublic class Java { public int x; }", 
                "package p;\npublic @interface Other { }", 
                "package p;\n\nimport r.Java;\n\npublic aspect Aspect {\ndeclare @field : public int NEW.Java.x : @Other; \ndeclare @field : public int Java.x : @Other; }", 
        });
    }
    
    // FAIL
    // should be no change in the aspect
    public void testMoveTypeWhenMultipleTypesOfSameSimpleNameExist4() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "q", 
                "r", 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Java.java", 
                "Other.java", 
                "Aspect.aj", 
        }, new String[] {
                "package q;\npublic class Java { public int x; }", 
                "package r;\npublic class Java { public int x; }", 
                "package p;\npublic @interface Other { }", 
                "package p;\n\nimport r.Java;\n\npublic aspect Aspect {\ndeclare @field : public int Java.x : @Other; }", 
        }, new String[] { 
                "package NEW;\npublic class Java { public int x; }", 
                "package r;\npublic class Java { public int x; }", 
                "package p;\npublic @interface Other { }", 
                "package p;\n\nimport r.Java;\n\npublic aspect Aspect {\ndeclare @field : public int Java.x : @Other; }", 
        });
    }
    
    // assume we are moving the first CU to the new specified package
    private void performRefactoringAndUndo(String newPackageName, String[] packNames, String[] cuNames, String[] initialContents, String[] finalContents) throws Exception {
        IPackageFragment newPackage = createPackage(newPackageName, project);
        ICompilationUnit[] units = createUnits(packNames, cuNames, initialContents);
        
        MoveDescriptor descriptor = RefactoringSignatureDescriptorFactory
                .createMoveDescriptor();
        descriptor.setDestination(newPackage);
        descriptor.setUpdateReferences(true);
        descriptor.setProject(project.getElementName());
        descriptor.setUpdateQualifiedNames(true);
        descriptor.setMoveResources(new IFile[0], new IFolder[0], new ICompilationUnit[] { units[0] });
        
        Refactoring refactoring = createRefactoring(descriptor);
        RefactoringStatus result = performRefactoring(refactoring, true, true);
        
        result = ignoreKnownErrors(result);

        assertTrue("Refactoring produced an error: " + result, result.isOK());
        
        ICompilationUnit newUnit = getNewUnit(newPackageName, cuNames[0]);
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

    private ICompilationUnit getNewUnit(String newPackName, String name) throws JavaModelException {
        int dotIndex = name.indexOf('.');
        String typeName = name.substring(0, dotIndex); 
        String qualName = newPackName.length() > 0 ? newPackName + "." + typeName : typeName;
        return project.findType(qualName).getCompilationUnit();
    }
}