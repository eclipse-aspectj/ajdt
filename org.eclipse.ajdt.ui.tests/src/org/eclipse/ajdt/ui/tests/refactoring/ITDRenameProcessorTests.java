/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
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

import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.tests.refactoring.AbstractAJDTRefactoringTest;
import org.eclipse.ajdt.internal.ui.refactoring.ITDRenameRefactoringContribution;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

/**
 * @author Andrew Eisenberg
 * @created Apr 23, 2010
 *
 */
public class ITDRenameProcessorTests extends
AbstractAJDTRefactoringTest {

    public void testMethodRenameOnInterfaceWithDeclareParents1() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj",
                "G.java"
        }, 
        new String[] {
                "aspect F {\n void A.xx() { }; \ninterface A { } \n declare parents : G implements A; }",
                "class G {\n { xx(); } }"
        }, 
        new String[] {
                "aspect F {\n void A.xxx() { }; \ninterface A { } \n declare parents : G implements A; }",
                "class G {\n { xxx(); } }"
        });
    }

    public void testMethodRenameOnInterfaceWithDeclareParents2() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj",
                "G.java"
        }, 
        new String[] {
                "aspect F {\n void A.xx() { }; \ninterface A { } \n declare parents : G implements A; }",
                "class G {\n { xx(); } public void xx() { } }"
        }, 
        new String[] {
                "aspect F {\n void A.xxx() { }; \ninterface A { } \n declare parents : G implements A; }",
                "class G {\n { xxx(); } public void xxx() { } }"
        });
    }



    public void testSimpleRenameField1() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj"
        }, 
        new String[] {
                "aspect F {\n int A.xx; \nclass A { }}"
        }, 
        new String[] {
                "aspect F {\n int A.xxx; \nclass A { }}"
        });
    }

    public void testSimpleRenameField2() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "a"
        },
        new String[] {
                "F.aj"
        }, 
        new String[] {
                "package a; aspect F {\n int A.xx; \nclass A { \n{ xx = 0; } }}"
        }, 
        new String[] {
                "package a; aspect F {\n int A.xxx; \nclass A { \n{ xxx = 0; } }}"
        });
    }

    public void testSimpleRenameField3() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "a"
        },
        new String[] {
                "F.aj"
        }, 
        new String[] {
                "package a; aspect F {\n int A.xx;\n void A.foo() { xx++; }\n int xx;\nclass A { \n{ xx = 0; } }}"
        }, 
        new String[] {
                "package a; aspect F {\n int A.xxx;\n void A.foo() { xxx++; }\n int xx;\nclass A { \n{ xxx = 0; } }}"
        });
    }

    public void testMultiFileRenameField1() throws Exception {
        performRefactoringAndUndo("G.xxx",
                new String[] {
                "a",
                "b",
                "c"
        },
        new String[] {
                "F.aj",
                "G.java",
                "H.aj",
        }, 
        new String[] {
                "package a; import b.G;\n public aspect F {\n public int G.xx; }",
                "package b;\n public class G {\n public void foo() { xx++; this.xx++; } }",
                "package c; import b.G;\n public aspect H {\n int xx = new G().xx++;\n G.new(int xx) { this.xx = xx; } }",
                "package d; import b.G;\n public class I extends G {\n void foo() { xx++ } }",
                "package e; import b.G;\n public class J extends G {\n int xx; \n void foo() { xx++ } }"
        }, 
        new String[] {
                "package a; import b.G;\n public aspect F {\n public int G.xxx; }",
                "package b;\n public class G {\n public void foo() { xxx++; this.xxx++; } }",
                "package c; import b.G;\n public aspect H {\n int xx = new G().xxx++;\n G.new(int xx) { this.xxx = xx; } }",
                "package d; import b.G;\n public class I extends G {\n void foo() { xxx++ } }",
                "package e; import b.G;\n public class J extends G {\n int xx; \n void foo() { xx++ } }"
        });
    }

    public void testPrivateRenameField1() throws Exception {
        performRefactoringAndUndo("G.xxx",
                new String[] {
                "a",
                "b"
        },
        new String[] {
                "F.aj",
                "G.java"
        }, 
        new String[] {
                "package a; import b.G;\n public aspect F {\n private int G.xx; \n int xx = new G().xx; \n void G.baz() {\n xx++; this.xx++; } }",
                "package b;\n public class G { \n int xx; }"
        }, 
        new String[] {
                "package a; import b.G;\n public aspect F {\n private int G.xxx; \n int xx = new G().xxx; \n void G.baz() {\n xxx++; this.xxx++; } }",
                "package b;\n public class G { \n int xx; }"
        });
    }

    public void testRenameMethodNoArgs1() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj"
        }, 
        new String[] {
                "aspect F {\n void A.xx() {\n xx(); this.xx(); } \nclass A { \n { xx(); this.xx(); }}}"
        }, 
        new String[] {
                "aspect F {\n void A.xxx() {\n xxx(); this.xxx(); } \nclass A { \n { xxx(); this.xxx(); }}}"
        });
    }

    public void testRenameMethodNoArgs2() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj",
                "A.java"
        }, 
        new String[] {
                "aspect F {\n void A.xx() {\n xx(); this.xx(); } \nclass A { \n { xx(); this.xx(); }}}",
                "class A { \n void xx() { } }"
        }, 
        new String[] {
                "aspect F {\n void A.xxx() {\n xxx(); this.xxx(); } \nclass A { \n { xxx(); this.xxx(); }}}",
                "class A { \n void xx() { } }"
        });
    }

    public void testRenameMethodNoArgs3() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj",
                "A.java",
                "B.java",
                "C.java",
                "D.java",
                "E.java"
        }, 
        new String[] {
                "aspect F {\n void A.xx() {\n xx(); this.xx(); }; }",
                "class A { }",
                "class B extends A { void xx() { xx(); this.xx(); super.xx(); }}",
                "class C implements D { public void xx() { xx(); new A().xx(); new B().xx(); }}",
                "interface D {\n void xx(); }",
                "class E { \n void foo() { new B().xx(); new A() {}.xx(); } }"
        }, 
        new String[] {
                "aspect F {\n void A.xxx() {\n xxx(); this.xxx(); }; }",
                "class A { }",
                "class B extends A { void xxx() { xxx(); this.xxx(); super.xxx(); }}",
                "class C implements D { public void xx() { xx(); new A().xxx(); new B().xxx(); }}",
                "interface D {\n void xx(); }",
                "class E { \n void foo() { new B().xxx(); new A() {}.xxx(); } }"
        });
    }


    public void testRenameMethodPrimitiveArg1() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj"
        }, 
        new String[] {
                "aspect F {\n void A.xx(int x) {\n xx(5); this.xx(5); }; \nclass A { \n { xx(5); this.xx(5); }}}"
        }, 
        new String[] {
                "aspect F {\n void A.xxx(int x) {\n xxx(5); this.xxx(5); }; \nclass A { \n { xxx(5); this.xxx(5); }}}"
        });
    }

    public void testRenameMethodPrimitiveArg2() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj",
                "A.java"
        }, 
        new String[] {
                "aspect F {\n void A.xx(int x) {\n xx(5); this.xx(5); }; \nclass A { \n { xx(5); this.xx(5); }}}",
                "class A { \n void xx(int x) { xx(5); } }"
        }, 
        new String[] {
                "aspect F {\n void A.xxx(int x) {\n xxx(5); this.xxx(5); }; \nclass A { \n { xxx(5); this.xxx(5); }}}",
                "class A { \n void xx(int x) { xx(5); } }"
        });
    }

    public void testRenameMethodPrimitiveArg3() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj",
                "A.java",
                "B.java",
                "C.java",
                "D.java",
                "E.java"
        }, 
        new String[] {
                "aspect F {\n void A.xx(int x) {\n xx(5); this.xx(5); }; }",
                "class A { }",
                "class B extends A { void xx(int x) { xx(5); this.xx(5); super.xx(5); }}",
                "class C implements D { public void xx(int x) { xx(5); new A().xx(5); new B().xx(5); }}",
                "interface D {\n void xx(int x); }",
                "class E { \n void foo() { new B().xx(5); new A() {}.xx(5); } }"
        }, 
        new String[] {
                "aspect F {\n void A.xxx(int x) {\n xxx(5); this.xxx(5); }; }",
                "class A { }",
                "class B extends A { void xxx(int x) { xxx(5); this.xxx(5); super.xxx(5); }}",
                "class C implements D { public void xx(int x) { xx(5); new A().xxx(5); new B().xxx(5); }}",
                "interface D {\n void xx(int x); }",
                "class E { \n void foo() { new B().xxx(5); new A() {}.xxx(5); } }"
        });
    }

    public void testRenameMethodStringArg1() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj"
        }, 
        new String[] {
                "aspect F {\n void A.xx(String x) {\n xx(\"5\"); this.xx(\"5\"); }; \nclass A { \n { xx(\"5\"); this.xx(\"5\"); }}}"
        }, 
        new String[] {
                "aspect F {\n void A.xxx(String x) {\n xxx(\"5\"); this.xxx(\"5\"); }; \nclass A { \n { xxx(\"5\"); this.xxx(\"5\"); }}}"
        });
    }

    public void testRenameMethodStringArg2() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj",
                "A.java"
        }, 
        new String[] {
                "aspect F {\n void A.xx(String x) {\n xx(\"5\"); this.xx(\"5\"); }; \nclass A { \n { xx(\"5\"); this.xx(\"5\"); }}}",
                "class A { \n void xx(String x) { xx(\"5\"); } }"
        }, 
        new String[] {
                "aspect F {\n void A.xxx(String x) {\n xxx(\"5\"); this.xxx(\"5\"); }; \nclass A { \n { xxx(\"5\"); this.xxx(\"5\"); }}}",
                "class A { \n void xx(String x) { xx(\"5\"); } }"
        });
    }

    public void testRenameMethodStringArg3() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj",
                "A.java",
                "B.java",
                "C.java",
                "D.java",
                "E.java"
        }, 
        new String[] {
                "aspect F {\n void A.xx(String x) {\n xx(\"5\"); this.xx(\"5\"); }; }",
                "class A { }",
                "class B extends A { void xx(String x) { xx(\"5\"); this.xx(\"5\"); super.xx(\"5\"); }}",
                "class C implements D { public void xx(String x) { xx(\"5\"); new A().xx(\"5\"); new B().xx(\"5\"); }}",
                "interface D {\n void xx(String x); }",
                "class E { \n void foo() { new B().xx(\"5\"); new A() {}.xx(\"5\"); } }"
        }, 
        new String[] {
                "aspect F {\n void A.xxx(String x) {\n xxx(\"5\"); this.xxx(\"5\"); }; }",
                "class A { }",
                "class B extends A { void xxx(String x) { xxx(\"5\"); this.xxx(\"5\"); super.xxx(\"5\"); }}",
                "class C implements D { public void xx(String x) { xx(\"5\"); new A().xxx(\"5\"); new B().xxx(\"5\"); }}",
                "interface D {\n void xx(String x); }",
                "class E { \n void foo() { new B().xxx(\"5\"); new A() {}.xxx(\"5\"); } }"
        });
    }

    public void testRenameMethodTwoArgs1() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj"
        }, 
        new String[] {
                "aspect F {\n void A.xx(String x, int y) {\n xx(\"5\", 5); this.xx(\"5\", 5); }; \nclass A { \n { xx(\"5\", 5); this.xx(\"5\", 5); }}}"
        }, 
        new String[] {
                "aspect F {\n void A.xxx(String x, int y) {\n xxx(\"5\", 5); this.xxx(\"5\", 5); }; \nclass A { \n { xxx(\"5\", 5); this.xxx(\"5\", 5); }}}"
        });
    }

    public void testRenameMethodTwoArgs2() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj",
                "A.java"
        }, 
        new String[] {
                "aspect F {\n void A.xx(String x, int y) {\n xx(\"5\", 5); this.xx(\"5\", 5); }; \nclass A { \n { xx(\"5\", 5); this.xx(\"5\", 5); }}}",
                "class A { \n void xx(String x, int y) { xx(\"5\", 5); } }"
        }, 
        new String[] {
                "aspect F {\n void A.xxx(String x, int y) {\n xxx(\"5\", 5); this.xxx(\"5\", 5); }; \nclass A { \n { xxx(\"5\", 5); this.xxx(\"5\", 5); }}}",
                "class A { \n void xx(String x, int y) { xx(\"5\", 5); } }"
        });
    }

    public void testRenameMethodTwoArgs3() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj",
                "A.java",
                "B.java",
                "C.java",
                "D.java",
                "E.java"
        }, 
        new String[] {
                "aspect F {\n void A.xx(String x, int y) {\n xx(\"5\", 5); this.xx(\"5\", 5); }; }",
                "class A { }",
                "class B extends A { void xx(String x, int y) { xx(\"5\", 5); this.xx(\"5\", 5); super.xx(\"5\", 5); }}",
                "class C implements D { public void xx(String x, int y) { xx(\"5\", 5); new A().xx(\"5\", 5); new B().xx(\"5\", 5); }}",
                "interface D {\n void xx(String x, int y); }",
                "class E { \n void foo() { new B().xx(\"5\", 5); new A() {}.xx(\"5\", 5); } }"
        }, 
        new String[] {
                "aspect F {\n void A.xxx(String x, int y) {\n xxx(\"5\", 5); this.xxx(\"5\", 5); }; }",
                "class A { }",
                "class B extends A { void xxx(String x, int y) { xxx(\"5\", 5); this.xxx(\"5\", 5); super.xxx(\"5\", 5); }}",
                "class C implements D { public void xx(String x, int y) { xx(\"5\", 5); new A().xxx(\"5\", 5); new B().xxx(\"5\", 5); }}",
                "interface D {\n void xx(String x, int y); }",
                "class E { \n void foo() { new B().xxx(\"5\", 5); new A() {}.xxx(\"5\", 5); } }"
        });
    }

    public void testRenameMethodListOfStringArg1() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj"
        }, 
        new String[] {
                "import java.util.List;\n aspect F {\n void A.xx(List<String> x) {\n xx(null); this.xx(null); }; \nclass A { \n { xx(null); this.xx(null); }}}"
        }, 
        new String[] {
                "import java.util.List;\n aspect F {\n void A.xxx(List<String> x) {\n xxx(null); this.xxx(null); }; \nclass A { \n { xxx(null); this.xxx(null); }}}"
        });
    }

    public void testRenameMethodListOfStringArg2() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj",
                "A.java"
        }, 
        new String[] {
                "import java.util.List;\n aspect F {\n void A.xx(List<String> x) {\n xx(null); this.xx(null); }; \nclass A { \n { xx(null); this.xx(null); }}}",
                "import java.util.List;\n class A { \n void xx(List<String> x) { xx(null); } }"
        }, 
        new String[] {
                "import java.util.List;\n aspect F {\n void A.xxx(List<String> x) {\n xxx(null); this.xxx(null); }; \nclass A { \n { xxx(null); this.xxx(null); }}}",
                "import java.util.List;\n class A { \n void xx(List<String> x) { xx(null); } }"
        });
    }

    public void testRenameMethodListOfStringArg3() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj",
                "A.java",
                "B.java",
                "C.java",
                "D.java",
                "E.java"
        }, 
        new String[] {
                "import java.util.List;\n aspect F {\n void A.xx(List<String> x) {\n xx(null); this.xx(null); }; }",
                "import java.util.List;\n class A { }",
                "import java.util.List;\n class B extends A { void xx(List<String> x) { xx(null); this.xx(null); super.xx(null); }}",
                "import java.util.List;\n class C implements D { public void xx(List<String> x) { xx(null); new A().xx(null); new B().xx(null); }}",
                "import java.util.List;\n interface D {\n void xx(List<String> x); }",
                "import java.util.List;\n class E { \n void foo() { new B().xx(null); new A() {}.xx(null); } }"
        }, 
        new String[] {
                "import java.util.List;\n aspect F {\n void A.xxx(List<String> x) {\n xxx(null); this.xxx(null); }; }",
                "import java.util.List;\n class A { }",
                "import java.util.List;\n class B extends A { void xxx(List<String> x) { xxx(null); this.xxx(null); super.xxx(null); }}",
                "import java.util.List;\n class C implements D { public void xx(List<String> x) { xx(null); new A().xxx(null); new B().xxx(null); }}",
                "import java.util.List;\n interface D {\n void xx(List<String> x); }",
                "import java.util.List;\n class E { \n void foo() { new B().xxx(null); new A() {}.xxx(null); } }"
        });
    }

    public void testRenameMethodQualifiedListArg1() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj"
        }, 
        new String[] {
                "aspect F {\n void A.xx(java.util.List<java.util.List<Object>> x) {\n xx(null); this.xx(null); }; \nclass A { \n { xx(null); this.xx(null); }}}"
        }, 
        new String[] {
                "aspect F {\n void A.xxx(java.util.List<java.util.List<Object>> x) {\n xxx(null); this.xxx(null); }; \nclass A { \n { xxx(null); this.xxx(null); }}}"
        });
    }

    public void testRenameMethodQualifiedListArg2() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj",
                "A.java"
        }, 
        new String[] {
                "aspect F {\n void A.xx(java.util.List<java.util.List<Object>> x) {\n xx(null); this.xx(null); }; \nclass A { \n { xx(null); this.xx(null); }}}",
                "class A { \n void xx(java.util.List<java.util.List<Object>> x) { xx(null); } }"
        }, 
        new String[] {
                "aspect F {\n void A.xxx(java.util.List<java.util.List<Object>> x) {\n xxx(null); this.xxx(null); }; \nclass A { \n { xxx(null); this.xxx(null); }}}",
                "class A { \n void xx(java.util.List<java.util.List<Object>> x) { xx(null); } }"
        });
    }

    public void testRenameMethodQualifiedListArg3() throws Exception {
        performRefactoringAndUndo("A.xxx",
                new String[] {
                "F.aj",
                "A.java",
                "B.java",
                "C.java",
                "D.java",
                "E.java"
        }, 
        new String[] {
                "aspect F {\n void A.xx(java.util.List<java.util.List<Object>> x) {\n xx(null); this.xx(null); }; }",
                "class A { }",
                "class B extends A { void xx(java.util.List<java.util.List<Object>> x) { xx(null); this.xx(null); super.xx(null); }}",
                "class C implements D { public void xx(java.util.List<java.util.List<Object>> x) { xx(null); new A().xx(null); new B().xx(null); }}",
                "interface D {\n void xx(java.util.List<java.util.List<Object>> x); }",
                "class E { \n void foo() { new B().xx(null); new A() {}.xx(null); } }"
        }, 
        new String[] {
                "aspect F {\n void A.xxx(java.util.List<java.util.List<Object>> x) {\n xxx(null); this.xxx(null); }; }",
                "class A { }",
                "class B extends A { void xxx(java.util.List<java.util.List<Object>> x) { xxx(null); this.xxx(null); super.xxx(null); }}",
                "class C implements D { public void xx(java.util.List<java.util.List<Object>> x) { xx(null); new A().xxx(null); new B().xxx(null); }}",
                "interface D {\n void xx(java.util.List<java.util.List<Object>> x); }",
                "class E { \n void foo() { new B().xxx(null); new A() {}.xxx(null); } }"
        });
    }

    /**
     * Assumes that we are renaming the first itd of the first CU
     * @throws Exception 
     */
    private void performRefactoringAndUndo(String newName, String[] cuNames, String[] initialContents, String[] finalContents) throws Exception {
        String[] packNames = new String[cuNames.length];
        Arrays.fill(packNames, "");
        performRefactoringAndUndo(newName, packNames, cuNames, initialContents, finalContents);
    }
    private void performRefactoringAndUndo(String newName, String[] packNames, String[] cuNames, String[] initialContents, String[] finalContents) throws Exception {
        ICompilationUnit[] units = createUnits(packNames, cuNames, initialContents);

        IntertypeElement itd = getFirstIntertypeElement(units);

        RenameJavaElementDescriptor descriptor = (RenameJavaElementDescriptor) new ITDRenameRefactoringContribution().createDescriptor();
        descriptor.setJavaElement(itd);
        descriptor.setNewName(newName);
        descriptor.setUpdateReferences(true);

        RenameRefactoring refactoring = (RenameRefactoring) createRefactoring(descriptor);
        RefactoringStatus result = performRefactoring(refactoring, true, false);

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
