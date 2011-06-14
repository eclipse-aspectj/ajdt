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
public class PushInRefactoringITITTests extends AbstractAJDTRefactoringTest {

    private enum ToPushIn { ALL, FIRST }
    
    // empty ITIT
    public void testEmptyITIT() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "import pack2.Java;" +
                        "public aspect Aspect {\n" +
                        "  public static final class Java.ITIT { }" +
                        "}",
                        "package pack2;\n public class Java {}",
                },
                new String[] {
                        null,
                        "package pack2;\n public class Java {\n" +
                        "\tpublic static final class ITIT { }\n" +
                        "}",
                }, ToPushIn.FIRST
        );
    }
    
    // empty fully qualified ITIT
    public void testEmptyFullyQualifiedITIT() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "public aspect Aspect {\n" +
                        "  public static final class pack2.Java.ITIT { }" +
                        "}",
                        "package pack2;\n public class Java {}",
                },
                new String[] {
                        null,
                        "package pack2;\n public class Java {\n" +
                        "\tpublic static final class ITIT { }\n" +
                        "}",
                }, ToPushIn.FIRST
        );
    }
    
    // single field no imports
    public void testOneFieldNoImports() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "import pack2.Java;" +
                        "public aspect Aspect {\n" +
                        "  public static final class Java.ITIT {\n" +
                        "    public static final int X = 9;\n" +
                        "  }" +
                        "}",
                        "package pack2;\n public class Java {}",
                },
                new String[] {
                        null,
                        "package pack2;\n public class Java {\n" +
                        "\tpublic static final class ITIT {\n" + 
                        "    public static final int X = 9;\n" + 
                        "  }\n" +
                        "}",
                }, ToPushIn.FIRST
        );
    }

    // two fields no imports
    public void testTwoFieldsNoImports() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "import pack2.Java;" +
                        "public aspect Aspect {\n" +
                        "  public static final class Java.ITIT {\n" +
                        "    public static final int X = 9;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }" +
                        "}",
                        "package pack2;\n public class Java {}",
                },
                new String[] {
                        null,
                        "package pack2;\n public class Java {\n" +
                        "\tpublic static final class ITIT {\n" + 
                        "    public static final int X = 9;\n" + 
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "}",
                }, ToPushIn.FIRST
        );
    }

    // import required
    public void testImportRequired() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "import pack2.Java;\n" +
                        "import java.util.List;\n" +
                        "public aspect Aspect {\n" +
                        "  public static final class Java.ITIT {\n" +
                        "    public static final List<String> X = null;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "}",
                        "package pack2;\n" +
                        "public class Java {}",
                },
                new String[] {
                        null,
                        "package pack2;\n\n" +
                        "import java.util.List;\n\n" +
                        "public class Java {\n" +
                        "\tpublic static final class ITIT {\n" + 
                        "    public static final List<String> X = null;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "}",
                }, ToPushIn.FIRST
        );
    }

    // import already there
    public void testImportAlreadyThere() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "import pack2.Java;\n" +
                        "import java.util.List;\n" +
                        "public aspect Aspect {\n" +
                        "  public static final class Java.ITIT {\n" +
                        "    public static final List<String> X = null;" +
                        "    public static final int X() { return 9; }" +
                        "  }" +
                        "}",
                        "package pack2;\n" +
                        "import java.util.List;\n" +
                        "public class Java {}",
                },
                new String[] {
                        null,
                        "package pack2;\n" +
                        "import java.util.List;\n" +
                        "public class Java {\n" +
                        "\tpublic static final class ITIT {\n" + 
                        "    public static final List<String> X = null;" +
                        "    public static final int X() { return 9; }" +
                        "  }\n" +
                        "}",
                }, ToPushIn.FIRST
        );
    }

    // import fully qualified
    public void testFullyQualifiedReference() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "import pack2.Java;\n" +
                        "public aspect Aspect {\n" +
                        "  public static final class Java.ITIT {\n" +
                        "    public static final java.util.List<String> X = null;" +
                        "    public static final int X() { return 9; }" +
                        "  }" +
                        "}",
                        "package pack2;\n" +
                        "public class Java {}",
                },
                new String[] {
                        null,
                        "package pack2;\n" +
                        "public class Java {\n" +
                        "\tpublic static final class ITIT {\n" + 
                        "    public static final java.util.List<String> X = null;" +
                        "    public static final int X() { return 9; }" +
                        "  }\n" +
                        "}",
                }, ToPushIn.FIRST
        );
    }

    // aspect not deleted
    public void testAspectNotDeleted() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "import pack2.Java;\n" +
                        "public aspect Aspect {\n" +
                        "  int x;\n" +
                        "  public static final class Java.ITIT {\n" +
                        "    public static final java.util.List<String> X = null;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "}",
                        "package pack2;\n" +
                        "public class Java {}",
                },
                new String[] {
                        "package pack1;\n" +
                        "import pack2.Java;\n" +
                        "public aspect Aspect {\n" +
                        "  int x;\n" +
                        "  }",
                        "package pack2;\n" +
                        "public class Java {\n" +
                        "\tpublic static final class ITIT {\n" + 
                        "    public static final java.util.List<String> X = null;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "}",
                }, ToPushIn.ALL
        );
    }

    // ITIT with no target
    // compile eror...not supported
    public void _testITITNoTarget() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "public aspect Aspect {\n" +
                        "  int x;" +
                        "  public static final class JavaOther.ITIT {\n" +
                        "    public static final java.util.List<String> X = null;" +
                        "    public static final int X() { return 9; }" +
                        "  }" +
                        "}",
                        "package pack2;\n" +
                        "public class Java {}",
                },
                new String[] {
                        "package pack1;\n" +
                        "public aspect Aspect {\n" +
                        "  int x;" +
                        "  public static final class JavaOther.ITIT {\n" +
                        "    public static final java.util.List<String> X = null;" +
                        "    public static final int X() { return 9; }" +
                        "  }" +
                        "}",
                        "package pack2;\n" +
                        "public class Java {}",
                }, ToPushIn.FIRST
        );
    }
    
    // Two ITITs one aspect same target
    public void testTwoITITsOneAspect() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "import pack2.Java;\n" +
                        "public aspect Aspect {\n" +
                        "  public static final class Java.ITIT {\n" +
                        "    public static final java.util.List<String> X = null;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "  public static final class Java.ITIT2 {\n" +
                        "    public static final java.util.List<String> X = null;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "}",
                        "package pack2;\n" +
                        "public class Java {}",
                },
                new String[] {
                        null,
                        "package pack2;\n" +
                        "public class Java {\n" +
                        "\tpublic static final class ITIT {\n" + 
                        "    public static final java.util.List<String> X = null;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "\n" +
                        "\tpublic static final class ITIT2 {\n" + 
                        "    public static final java.util.List<String> X = null;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "}",
                }, ToPushIn.ALL
        );
    }

    // Two ITITs one aspect same target
    public void testTwoITITsOneAspectFirstOnly() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "import pack2.Java;\n" +
                        "public aspect Aspect {\n" +
                        "  public static final class Java.ITIT {\n" +
                        "    public static final java.util.List<String> X = null;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "  public static final class Java.ITIT2 {\n" +
                        "    public static final java.util.List<String> X = null;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "}",
                        "package pack2;\n" +
                        "public class Java {}",
                },
                new String[] {
                        "package pack1;\n" +
                        "import pack2.Java;\n" +
                        "public aspect Aspect {\n" +
                        "    public static final class Java.ITIT2 {\n" +
                        "    public static final java.util.List<String> X = null;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "}",
                        "package pack2;\n" +
                        "public class Java {\n" +
                        "\tpublic static final class ITIT {\n" + 
                        "    public static final java.util.List<String> X = null;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "}",
                }, ToPushIn.FIRST
        );
    }

    // Two ITITs one aspect different targets
    public void testTwoITITsOneAspectDifferentTargets() throws Exception {
        performRefactoringAndUndo(
                new String[] {
                        "pack1",
                        "pack2",
                        "pack2",
                },
                new String[] {
                        "Aspect.aj", 
                        "Java.java",
                        "Java2.java",
                },
                new String[] {
                        "package pack1;\n" +
                        "import pack2.Java;\n" +
                        "import pack2.Java2;\n" +
                        "public aspect Aspect {\n" +
                        "  public static final class Java.ITIT {\n" +
                        "    public static final java.util.List<String> X = null;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "  public static final class Java2.ITIT2 {\n" +
                        "    public static final java.util.List<String> X2 = null;\n" +
                        "    public static final int X2() { return 9; }\n" +
                        "  }\n" +
                        "}",
                        "package pack2;\n" +
                        "public class Java {}",
                        "package pack2;\n" +
                        "public class Java2 {}",
                },
                new String[] {
                        null,
                        "package pack2;\n" +
                        "public class Java {\n" +
                        "\tpublic static final class ITIT {\n" + 
                        "    public static final java.util.List<String> X = null;\n" +
                        "    public static final int X() { return 9; }\n" +
                        "  }\n" +
                        "}",
                        "package pack2;\n" +
                        "public class Java2 {\n" +
                        "\tpublic static final class ITIT2 {\n" + 
                        "    public static final java.util.List<String> X2 = null;\n" +
                        "    public static final int X2() { return 9; }\n" +
                        "  }\n" +
                        "}",
                }, ToPushIn.ALL
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
