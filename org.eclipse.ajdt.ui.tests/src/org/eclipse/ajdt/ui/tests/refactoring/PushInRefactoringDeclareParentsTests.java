/*******************************************************************************
 * Copyright (c) 2011 SpringSource and others.
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
import java.util.Collections;
import java.util.List;

import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.tests.refactoring.AbstractAJDTRefactoringTest;
import org.eclipse.ajdt.internal.ui.refactoring.PushInRefactoring;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * 
 * @author Andrew Eisenberg
 * @created Nov 26, 2010
 */
public class PushInRefactoringDeclareParentsTests extends AbstractAJDTRefactoringTest {

    private enum ToPushIn { ALL, FIRST }


    //    interface only
    public void testDefaultPackage1() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "",
                        "",
                        "",
                },
                new String[] {
                        "Aspect.aj",
                        "Interface.java",
                        "Target.java",
                },
                new String[] {
                        "public aspect Aspect {\n" +
                        "  declare parents : Target implements Interface;\n" +
                        "}",
                        "public interface Interface { }",
                        "public class Target { }",
                },
                new String[] {
                        null,
                        "public interface Interface { }",
                        "public class Target implements Interface { }",
                }, ToPushIn.FIRST
                );
    }

    //    class only
    public void testDefaultPackage2() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "",
                        "",
                        "",
                },
                new String[] {
                        "Aspect.aj",
                        "Clazz.java",
                        "Target.java",
                },
                new String[] {
                        "public aspect Aspect {\n" +
                        "  declare parents : Target implements Clazz;\n" + // incorrect declare
                        "}",
                        "public class Clazz { }",
                        "public class Target { }",
                },
                new String[] {
                        null,
                        "public class Clazz { }",
                        "public class Target extends Clazz { }",
                }, ToPushIn.FIRST
                );
    }

    //  class and interface, only class pushed in
    public void testDefaultPackage3() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "",
                        "",
                        "",
                        "",
                },
                new String[] {
                        "Aspect.aj",
                        "Clazz.java",
                        "Interface.java",
                        "Target.java",
                },
                new String[] {
                        "public aspect Aspect {\n" +
                        "  declare parents : Target extends Clazz;\n" +
                        "  declare parents : Target extends Interface;\n" + // incorrect declare
                        "}",
                        "public class Clazz { }",
                        "public interface Interface { }",
                        "public class Target { }",
                },
                new String[] {
                        null,
                        "public class Clazz { }",
                        "public interface Interface { }",
                        "public class Target extends Clazz implements Interface { }",
                }, ToPushIn.ALL
                );
    }

    //class and interface, only class pushed in
    public void testDefaultPackage4() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "",
                        "",
                        "",
                        "",
                },
                new String[] {
                        "Aspect.aj",
                        "Clazz.java",
                        "Interface.java",
                        "Target.java",
                },
                new String[] {
                        "public aspect Aspect {\n" +
                        "  declare parents : Target extends Clazz;\n" +
                        "  declare parents : Target extends Interface;\n" + // incorrect declare
                        "}",
                        "public class Clazz { }",
                        "public interface Interface { }",
                        "public class Target { }",
                },
                new String[] {
                        "public aspect Aspect {\n" +
                        "  \n" +
                        "  declare parents : Target extends Interface;\n" + // incorrect declare
                        "}",
                        "public class Clazz { }",
                        "public interface Interface { }",
                        "public class Target extends Clazz { }",
                }, ToPushIn.FIRST
                );
    }
    //class and interface only interface pushed in
    public void testDefaultPackage5() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "",
                        "",
                        "",
                        "",
                },
                new String[] {
                        "Aspect.aj",
                        "Clazz.java",
                        "Interface.java",
                        "Target.java",
                },
                new String[] {
                        "public aspect Aspect {\n" +
                        "  declare parents : Target extends Interface;\n" + // incorrect declare
                        "  declare parents : Target extends Clazz;\n" +
                        "}",
                        "public class Clazz { }",
                        "public interface Interface { }",
                        "public class Target { }",
                },
                new String[] {
                        "public aspect Aspect {\n" +
                        "  \n" +
                        "  declare parents : Target extends Clazz;\n" +
                        "}",
                        "public class Clazz { }",
                        "public interface Interface { }",
                        "public class Target implements Interface { }",
                }, ToPushIn.FIRST
                );
    }
    
    // interface on interface
    public void testDefaultPackage6() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "",
                        "",
                        "",
                },
                new String[] {
                        "Aspect.aj",
                        "Interface.java",
                        "Target.java",
                },
                new String[] {
                        "public aspect Aspect {\n" +
                        "  declare parents : Target extends Interface;\n" +
                        "}",
                        "public interface Interface { }",
                        "public interface Target { }",
                },
                new String[] {
                        null,
                        "public interface Interface { }",
                        "public interface Target extends Interface { }",
                }, ToPushIn.FIRST
                );
    }
    
    // two interfaces on interface
    public void testDefaultPackage7() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "",
                        "",
                        "",
                        "",
                },
                new String[] {
                        "Aspect.aj",
                        "Interface.java",
                        "Interface2.java",
                        "Target.java",
                },
                new String[] {
                        "public aspect Aspect {\n" +
                        "  declare parents : Target extends Interface;\n" +
                        "  declare parents : Target implements Interface2;" + // incorrect declare
                        "}",
                        "public interface Interface { }",
                        "public interface Interface2 { }",
                        "public interface Target { }",
                },
                new String[] {
                        null,
                        "public interface Interface { }",
                        "public interface Interface2 { }",
                        "public interface Target extends Interface, Interface2 { }",
                }, ToPushIn.ALL
                );
    }
    
    // two interfaces on interface, one pushed in
    public void testDefaultPackage8() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "",
                        "",
                        "",
                        "",
                },
                new String[] {
                        "Aspect.aj",
                        "Interface.java",
                        "Interface2.java",
                        "Target.java",
                },
                new String[] {
                        "public aspect Aspect {\n" +
                        "  declare parents : Target extends Interface;\n" +
                        "  declare parents : Target extends Interface2;\n" +
                        "}",
                        "public interface Interface { }",
                        "public interface Interface2 { }",
                        "public interface Target { }",
                },
                new String[] {
                        "public aspect Aspect {\n" +
                        "  \n" +
                        "  declare parents : Target extends Interface2;\n" +
                        "}",
                        "public interface Interface { }",
                        "public interface Interface2 { }",
                        "public interface Target extends Interface { }",
                }, ToPushIn.FIRST
                );
    }
    
    //  class and interface, only class pushed in
    public void testSamePackage1() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack1",
                        "pack1",
                        "pack1",
                },
                new String[] {
                        "Aspect.aj",
                        "Clazz.java",
                        "Interface.java",
                        "Target.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "public aspect Aspect {\n" +
                        "  declare parents : Target extends Clazz;\n" +
                        "  declare parents : Target extends Interface;\n" + // incorrect declare
                        "}",
                        "package pack1;\n" +
                        "public class Clazz { }",
                        "package pack1;\n" +
                        "public interface Interface { }",
                        "package pack1;\n" +
                        "public class Target { }",
                },
                new String[] {
                        null,
                        "package pack1;\n" +
                        "public class Clazz { }",
                        "package pack1;\n" +
                        "public interface Interface { }",
                        "package pack1;\n" +
                        "public class Target extends Clazz implements Interface { }",
                }, ToPushIn.ALL
                );
    }
    
    //  class and two interfaces, only class pushed in
    public void testSamePackage2() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack1",
                        "pack1",
                        "pack1",
                        "pack1",
                },
                new String[] {
                        "Aspect.aj",
                        "Clazz.java",
                        "Interface.java",
                        "Interface2.java",
                        "Target.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "public aspect Aspect {\n" +
                        "  declare parents : Target extends Clazz;\n" +
                        "  declare parents : Target extends Interface;\n" + // incorrect declare
                        "  declare parents : Target extends Interface2;" + // incorrect declare
                        "}",
                        "package pack1;\n" +
                        "public class Clazz { }",
                        "package pack1;\n" +
                        "public interface Interface { }",
                        "package pack1;\n" +
                        "public interface Interface2 { }",
                        "package pack1;\n" +
                        "public class Target { }",
                },
                new String[] {
                        null,
                        "package pack1;\n" +
                        "public class Clazz { }",
                        "package pack1;\n" +
                        "public interface Interface { }",
                        "package pack1;\n" +
                        "public interface Interface2 { }",
                        "package pack1;\n" +
                        "public class Target extends Clazz implements Interface, Interface2 { }",
                }, ToPushIn.ALL
                );
    }
    
    //  class and interface, existing superclass
    public void testSamePackage3() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack1",
                        "pack1",
                        "pack1",
                        "pack1",
                },
                new String[] {
                        "Aspect.aj",
                        "Clazz.java",
                        "Sub.java",
                        "Interface.java",
                        "Target.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "public aspect Aspect {\n" +
                        "  declare parents : Target extends Sub;\n" +
                        "  declare parents : Target extends Interface;\n" + // incorrect declare
                        "}",
                        "package pack1;\n" +
                        "public class Clazz { }",
                        "package pack1;\n" +
                        "public class Sub extends Clazz { }",
                        "package pack1;\n" +
                        "public interface Interface { }",
                        "package pack1;\n" +
                        "public class Target extends Clazz { }",
                },
                new String[] {
                        null,
                        "package pack1;\n" +
                        "public class Clazz { }",
                        "package pack1;\n" +
                        "public class Sub extends Clazz { }",
                        "package pack1;\n" +
                        "public interface Interface { }",
                        "package pack1;\n" +
                        "public class Target extends Sub implements Interface { }",
                }, ToPushIn.ALL
                );
    }
    
    //  multiple targets
    public void testOtherPackage1() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack1",
                        "pack1",
                        "pack2",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj",
                        "Clazz.java",
                        "Interface.java",
                        "Target1.java",
                        "Target2.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "public aspect Aspect {\n" +
                        "  declare parents : pack2.* extends Clazz;\n" +
                        "  declare parents : pack2.* extends Interface;\n" + // incorrect declare
                        "}",
                        "package pack1;\n" +
                        "public class Clazz { }",
                        "package pack1;\n" +
                        "public interface Interface { }",
                        "package pack2;\n" +
                        "public class Target1 { }",
                        "package pack2;\n" +
                        "public class Target2 { }",
                },
                new String[] {
                        null,
                        "package pack1;\n" +
                        "public class Clazz { }",
                        "package pack1;\n" +
                        "public interface Interface { }",
                        "package pack2;\n" +
                        "\n" +
                        "import pack1.Clazz;\n" +
                        "import pack1.Interface;\n" +
                        "\n" +
                        "public class Target1 extends Clazz implements Interface { }",
                        "package pack2;\n" +
                        "\n" +
                        "import pack1.Clazz;\n" +
                        "import pack1.Interface;\n" +
                        "\n" +
                        "public class Target2 extends Clazz implements Interface { }",
                }, ToPushIn.ALL
                );
    }
    
    //  multiple targets, no import required
    public void testOtherPackage2() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack1",
                        "pack1",
                        "pack2",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj",
                        "Clazz.java",
                        "Interface.java",
                        "Target1.java",
                        "Target2.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "public aspect Aspect {\n" +
                        "  declare parents : pack2.* extends Clazz;\n" +
                        "  declare parents : pack2.* extends Interface;\n" + // incorrect declare
                        "}",
                        "package pack1;\n" +
                        "public class Clazz { }",
                        "package pack1;\n" +
                        "public interface Interface { }",
                        "package pack2;\n" +
                        "import pack1.Clazz;\n" +
                        "import pack1.Interface;\n" +
                        "\n" +
                        "public class Target1 { }",
                        "package pack2;\n" +
                        "import pack1.Clazz;\n" +
                        "import pack1.Interface;\n" +
                        "\n" +
                        "public class Target2 { }",
                },
                new String[] {
                        null,
                        "package pack1;\n" +
                        "public class Clazz { }",
                        "package pack1;\n" +
                        "public interface Interface { }",
                        "package pack2;\n" +
                        "import pack1.Clazz;\n" +
                        "import pack1.Interface;\n" +
                        "\n" +
                        "public class Target1 extends Clazz implements Interface { }",
                        "package pack2;\n" +
                        "import pack1.Clazz;\n" +
                        "import pack1.Interface;\n" +
                        "\n" +
                        "public class Target2 extends Clazz implements Interface { }",
                }, ToPushIn.ALL
                );
    }
    
    //  multiple targets
    public void testGenerics1() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj",
                        "Target1.java",
                        "Target2.java",
                },
                new String[] {
                        "package pack1;\n" + 
                        "import java.util.List;\n" + 
                        "public aspect Aspect {\n" + 
                        "  declare parents : pack2.* extends I<String,   List<String>   >;\n" + 
                        "  public static interface I<A, B> {\n" + 
                        "  }\n" + 
                        "}",
                        "package pack2;\n" +
                        "public class Target1 { }",
                        "package pack2;\n" +
                        "public interface Target2 { }",
                },
                new String[] {
                        "package pack1;\n" +
                        "import java.util.List;\n" +
                        "public aspect Aspect {\n" +
                        "  \n" +
                        "  public static interface I<A, B> {\n" +
                        "  }\n" +
                        "}",
                        "package pack2;\n" +
                        "\n" +
                        "import java.util.List;\n" +
                        "import pack1.Aspect.I;\n" +
                        "\n" +
                        "public class Target1 implements I<String, List<String>> { }",
                        "package pack2;\n" +
                        "\n" +
                        "import java.util.List;\n" +
                        "import pack1.Aspect.I;\n" +
                        "\n" +
                        "public interface Target2 extends I<String, List<String>> { }",
                }, ToPushIn.FIRST
                );
    }

    // First compilation unit contains the elements to push in.
    private void performRefactoringAndUndo(String[] packNames, String[] cuNames, String[] initialContents, String[] finalContents, ToPushIn toPush) throws Exception {
        ICompilationUnit[] units = createUnits(packNames, cuNames, initialContents);
        List<IMember> itds;
        if (toPush == ToPushIn.ALL) {
            itds = new ArrayList<IMember>();
            for (IJavaElement elt : units[0].getTypes()[0].getChildren()) {
                if (elt instanceof IAspectJElement || elt instanceof IType) {
                    itds.add((IMember) elt);
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
