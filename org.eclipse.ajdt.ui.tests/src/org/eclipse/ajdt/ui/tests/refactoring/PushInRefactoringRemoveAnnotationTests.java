/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.tests.refactoring.AbstractAJDTRefactoringTest;
import org.eclipse.ajdt.internal.ui.refactoring.PushInRefactoring;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * 
 * @author Andrew Eisenberg
 * @created Nov 26, 2010
 */
public class PushInRefactoringRemoveAnnotationTests extends AbstractAJDTRefactoringTest {

    private enum ToPushIn { ALL, FIRST }
    
    public void testPushInRemoveFieldSimple() throws Exception {
        performRefactoringAndUndo(
        new String[] {
            "Aspect.aj", 
            "Java.java",
            "Foo.java"
        },
        new String[] {
            "aspect Aspect {\n  declare @field : public int Java.i : -@Foo; }",
            "class Java {\n  @Foo\n  public int i = 9;}",
            "@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
        },
        new String[] {
                null,
                "class Java {\n  \n  public int i = 9;}",
                "@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
        }, ToPushIn.FIRST
        );
    }
    
    public void testPushInRemoveFieldDifferentPackages() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                        "pack3"
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                        "Foo.java"
                },
                new String[] {
                        "package pack1;\n import pack2.Java;\n import pack3.Foo;\n public aspect Aspect {\n  declare @field : public int Java.i : -@Foo; }",
                        "package pack2;\n import pack3.Foo;\n public class Java {\n  @Foo\n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                },
                new String[] {
                        null,
                        "package pack2;\n import pack3.Foo;\n public class Java {\n  \n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                }, ToPushIn.FIRST
        );
    }

    public void testPushInRemoveFieldAspectFullyQualified() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                        "pack3"
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                        "Foo.java"
                },
                new String[] {
                        "package pack1;\n public aspect Aspect {\n  declare @field : int pack2.Java.i : -@pack3.Foo; }",
                        "package pack2;\n import pack3.Foo;\n public class Java {\n  @Foo\n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                },
                new String[] {
                        null,
                        "package pack2;\n import pack3.Foo;\n public class Java {\n  \n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                }, ToPushIn.FIRST
        );
    }
    
    public void testPushInRemoveFieldClassFullyQualified() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                        "pack3"
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                        "Foo.java"
                },
                new String[] {
                        "package pack1;\n import pack2.Java;\n import pack3.Foo;\n public aspect Aspect {\n  declare @field : public int Java.i : -@Foo; }",
                        "package pack2;\n public class Java {\n  @pack3.Foo\n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                },
                new String[] {
                        null,
                        "package pack2;\n public class Java {\n  \n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                }, ToPushIn.FIRST
        );
    }

    public void testPushInRemoveFieldBothFullyQualified() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                        "pack3"
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                        "Foo.java"
                },
                new String[] {
                        "package pack1;\n public aspect Aspect {\n  declare @field : int pack2.Java.i : -@pack3.Foo; }",
                        "package pack2;\n public class Java {\n  @pack3.Foo\n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                },
                new String[] {
                        null,
                        "package pack2;\n public class Java {\n  \n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                }, ToPushIn.FIRST
        );
    }

    public void testPushInRemoveFieldClassFullyQualifiedMultiple1() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                        "pack3",
                        "pack4"
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                        "Foo.java",
                        "Foo.java"
                },
                new String[] {
                        "package pack1;\n import pack2.Java;\n import pack3.Foo;\n public aspect Aspect {\n  declare @field : public int Java.i : -@Foo; }",
                        "package pack2;\n import pack4.Foo;\n public class Java {\n  @pack3.Foo\n @Foo\n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }",
                        "package pack4;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                },
                new String[] {
                        null,
                        "package pack2;\n import pack4.Foo;\n public class Java {\n  \n @Foo\n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }",
                        "package pack4;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                }, ToPushIn.FIRST
        );
    }

    public void testPushInRemoveFieldClassFullyQualifiedMultiple2() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                        "pack3",
                        "pack4"
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                        "Foo.java",
                        "Foo.java"
                },
                new String[] {
                        "package pack1;\n import pack2.Java;\n import pack3.Foo;\n public aspect Aspect {\n  declare @field : public int Java.i : -@Foo; }",
                        "package pack2;\n import pack3.Foo;\n public class Java {\n  @pack4.Foo\n @Foo\n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }",
                        "package pack4;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                },
                new String[] {
                        null,
                        "package pack2;\n import pack3.Foo;\n public class Java {\n  @pack4.Foo\n \n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }",
                        "package pack4;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                }, ToPushIn.FIRST
        );
    }
    
    public void testPushInRemoveFieldMultiple() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                        "pack3",
                        "pack4"
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                        "Foo.java",
                        "Foo.java"
                },
                new String[] {
                        "package pack1;\n import pack2.Java;\n import pack3.Foo;\n public aspect Aspect {\n  declare @field : public int Java.i : -@Foo;\n  declare @field : public int Java.i : -@pack4.Foo; }",
                        "package pack2;\n import pack3.Foo;\n public class Java {\n  @pack4.Foo\n @Foo\n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }",
                        "package pack4;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                },
                new String[] {
                        null,
                        "package pack2;\n import pack3.Foo;\n public class Java {\n  \n \n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }",
                        "package pack4;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                }, ToPushIn.ALL
        );
    }
    
    public void testPushInRemoveFieldMultiple2() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                        "pack3",
                        "pack4"
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                        "Foo.java",
                        "Foo.java"
                },
                new String[] {
                        "package pack1;\n import pack2.Java;\n import pack3.Foo;\n public aspect Aspect {\n  declare @field : public int Java.i : -@Foo;\n  declare @field : public int Java.i : -@pack4.Foo; }",
                        "package pack2;\n import pack3.Foo;\n public class Java {\n  @pack4.Foo\n @Foo\n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }",
                        "package pack4;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                },
                new String[] {
                        "package pack1;\n import pack2.Java;\n import pack3.Foo;\n public aspect Aspect {\n    declare @field : public int Java.i : -@pack4.Foo; }",
                        "package pack2;\n import pack3.Foo;\n public class Java {\n  @pack4.Foo\n \n  public int i = 9;}",
                        "package pack3;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }",
                        "package pack4;\n @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                }, ToPushIn.FIRST
        );
    }
    
    
    public void _testPushInRemoveMethodSimple() throws Exception {
        performRefactoringAndUndo(
        new String[] {
            "Aspect.aj", 
            "Java.java",
            "Foo.java"
        },
        new String[] {
            "aspect Aspect {\n  declare @method : public void Java.i() : -@Foo; }",
            "class Java {\n  @Foo\n  public void i() { } }",
            "@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
        },
        new String[] {
                null,
                "class Java {\n  \n  public void i() { } }",
                "@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
        }, ToPushIn.FIRST
        );
    }

    public void _testPushInRemoveTypeSimple() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                        "Foo.java"
                },
                new String[] {
                        "aspect Aspect {\n  declare @type : Java : -@Foo; }",
                        "@Foo class Java {\n }",
                        "@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                },
                new String[] {
                        null,
                        " class Java {\n }",
                        "@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Foo { }"
                }, ToPushIn.FIRST
        );
    }
    
    
    // First compilation unit contains the elements to push in.
    private void performRefactoringAndUndo(String[] cuNames, String[] initialContents, String[] finalContents, ToPushIn toPush) throws Exception {
        String[] packNames = new String[cuNames.length];
        Arrays.fill(packNames, "");
        performRefactoringAndUndo(packNames, cuNames, initialContents, finalContents, toPush);
    }
    private void performRefactoringAndUndo(String[] packNames, String[] cuNames, String[] initialContents, String[] finalContents, ToPushIn toPush) throws Exception {
        ICompilationUnit[] units = createUnits(packNames, cuNames, initialContents);
        List<IMember> itds;
        if (toPush == ToPushIn.ALL) {
            itds = new ArrayList<IMember>();
            for (IJavaElement elt : units[0].getTypes()[0].getChildren()) {
                if (elt instanceof IAspectJElement) {
                    itds.add((IAspectJElement) elt);
                }
            }
        } else {
            itds = Collections.singletonList((IMember) units[0].getTypes()[0].getChildren()[0]);
        }
        
        PushInRefactoring refactoring = new PushInRefactoring();
        refactoring.setITDs(itds);
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
