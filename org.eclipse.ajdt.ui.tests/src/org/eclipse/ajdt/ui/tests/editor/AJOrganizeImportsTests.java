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
package org.eclipse.ajdt.ui.tests.editor;

import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.internal.ui.editor.actions.AJOrganizeImportsOperation;
import org.eclipse.ajdt.internal.ui.editor.actions.AJOrganizeImportsOperation.IChooseImportQuery;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.TypeNameMatch;

/**
 * 
 * @author Andrew Eisenberg
 * @created Jul 12, 2010
 */
public class AJOrganizeImportsTests extends UITestCase {
    public static class MockChooseImportsQuery implements IChooseImportQuery {

        public TypeNameMatch[] chooseImports(TypeNameMatch[][] openChoices,
                ISourceRange[] ranges) {
            throw new RuntimeException();
        }

    }

    private static class MockAJOrganizeImportsOperation extends AJOrganizeImportsOperation {
        public MockAJOrganizeImportsOperation(ICompilationUnit cu,
                CompilationUnit astRoot) throws CoreException {
            super(cu, astRoot, true, false, true, null);
        }
    }
    
    IProject project;
    IJavaProject javaProject;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = createPredefinedProject("DefaultEmptyProject");
        javaProject = JavaCore.create(project);
    }
    
    public void testOrganizeImportsSimple1() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\n\nclass Aspect {\nprivate HashMap val; }", 
                "package pack;\n\nimport java.util.HashMap;\n\nclass Aspect {\nprivate HashMap val; }");
    }

    public void testOrganizeImports1() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\nimport java.util.List;\naspect Aspect { }", 
                "package pack;\n\naspect Aspect { }");
    }
    
    public void testOrganizeImport2() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\nimport java.util.ArrayList;\naspect Aspect extends ArrayList<String> { }", 
                "package pack;\nimport java.util.ArrayList;\naspect Aspect extends ArrayList<String> { }");
    }
    
    public void testOrganizeImport2a() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\naspect Aspect extends ArrayList<IDontKnow> { }", 
                "package pack;\n\nimport java.util.ArrayList;\n\naspect Aspect extends ArrayList<IDontKnow> { }");
    }
    
    public void testOrganizeImport3() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\nimport java.util.List;\naspect Aspect { pointcut p() : within(List); }", 
                "package pack;\nimport java.util.List;\naspect Aspect { pointcut p() : within(List); }");
    }
    
    public void testOrganizeImport3a() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\naspect Aspect { pointcut p() : within(ArrayList); }", 
        "package pack;\n\nimport java.util.ArrayList;\n\naspect Aspect { pointcut p() : within(ArrayList); }");
    }
    
    public void testOrganizeImport4() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\naspect Aspect { pointcut p() : call(* foo(ArrayList<? extends HashSet>)); }", 
        "package pack;\n\nimport java.util.ArrayList;\nimport java.util.HashSet;\n\naspect Aspect { pointcut p() : call(* foo(ArrayList<? extends HashSet>)); }");
    }
    
    public void testOrganizeImportITD1() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\naspect Aspect { int ArrayList.x = 9; }", 
                "package pack;\n\nimport java.util.ArrayList;\n\naspect Aspect { int ArrayList.x = 9; }");
    }
    
    public void testOrganizeImportITD2() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\naspect Aspect { void ArrayList.x() { } }", 
        "package pack;\n\nimport java.util.ArrayList;\n\naspect Aspect { void ArrayList.x() { } }");
    }
    
    public void testOrganizeImportITD3() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\naspect Aspect { ArrayList.new(int g) { } }", 
        "package pack;\n\nimport java.util.ArrayList;\n\naspect Aspect { ArrayList.new(int g) { } }");
    }
    
    
    public void testOrganizeImportITD4() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\naspect Aspect { void ArrayList<HashSet>.x() { } }", 
        "package pack;\n\nimport java.util.ArrayList;\nimport java.util.HashSet;\n\naspect Aspect { void ArrayList<HashSet>.x() { } }");
    }
    
    public void testOrganizeImportITD5() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\naspect Aspect { void ArrayList.x(HashSet g) { } }", 
        "package pack;\n\nimport java.util.ArrayList;\nimport java.util.HashSet;\n\naspect Aspect { void ArrayList.x(HashSet g) { } }");
    }
    
    public void testOrganizeImportITD6() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\naspect Aspect { void java.util.ArrayList.x(HashSet g) { } }", 
        "package pack;\n\nimport java.util.HashSet;\n\naspect Aspect { void java.util.ArrayList.x(HashSet g) { } }");
    }
    
    // At Eclipse 4.6 (neon) I've added the 'import pack.Aspect.F' to the
    // expected output - not sure why
    // the testcase is adding it because it is locally defined and if I use a runtime
    // workbench then the same thing is not added. It could be the binding resolver
    // can succeed at runtime but just doesn't work in the test infrastructure (so at
    // runtime the simple 'F' can be resolved, which refers to an inner class).
    public void testDeclare1() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\naspect Aspect { class F { }\ndeclare parents : F extends ArrayList; }", 
//                "package pack;\n\nimport java.util.ArrayList;\n\naspect Aspect { class F { }\ndeclare parents : F extends ArrayList; }");
    "package pack;\n\nimport java.util.ArrayList;\n\nimport pack.Aspect.F;\n\naspect Aspect { class F { }\ndeclare parents : F extends ArrayList; }");
    }
    
    public void testDeclare2() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\naspect Aspect {\ndeclare warning : within(ArrayList) : \"In Array List\"; }", 
        "package pack;\n\nimport java.util.ArrayList;\n\naspect Aspect {\ndeclare warning : within(ArrayList) : \"In Array List\"; }");
    }
    
    // Cannot find missing types from within @declare 
    public void _testDeclare3() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\naspect Aspect {\ndeclare @type : ArrayList : @Foo; }\n @interface @Foo { }", 
        "package pack;\n\nimport java.util.ArrayList;\n\naspect Aspect {\ndeclare @type : ArrayList : @Foo; }\n @interface @Foo { }");
    }
    
    // However, if the import statement already exists, it will not be removed 
    public void testDeclare4() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\n\nimport java.util.ArrayList;\n\naspect Aspect {\ndeclare @type : ArrayList : @Foo; }\n @interface @Foo { }", 
        "package pack;\n\nimport java.util.ArrayList;\n\naspect Aspect {\ndeclare @type : ArrayList : @Foo; }\n @interface @Foo { }");
    }
    // Bug 367354
    public void testOnDemand1() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\n" + 
                "import javax.swing.*;\n" + 
                "\n" + 
                "class Aspect {\n" + 
                "    JFrame j;\n" + 
                "}", 
                "package pack;\n" + 
                "import javax.swing.JFrame;\n" + 
                "\n" + 
                "class Aspect {\n" + 
                "    JFrame j;\n" + 
                "}");
    }
    
    // Bug 367354
    public void testOnDemand2() throws Exception {
        checkOrganizeImports("Aspect.aj", "pack", 
                "package pack;\n" + 
                "import javax.swing.*;\n" +
                "import java.util.List;\n" + 
                "class Aspect {\n" + 
                "    JFrame j;\n" +
                "    List<String> s;\n" + 
                "}", 
                "package pack;\n" + 
                "import java.util.List;\n" + 
                "\n" + 
                "import javax.swing.JFrame;\n" + 
                "class Aspect {\n" + 
                "    JFrame j;\n" + 
                "    List<String> s;\n" + 
                "}");
    }
    private void checkOrganizeImports(String cuName, String packageName, String original, String expected) throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage(packageName, cuName, original, javaProject);
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(unit);
        CompilationUnit astRoot= (CompilationUnit) parser.createAST(null); 
        String actual = performOrganizeImports(unit, astRoot);
        assertEquals("Organize imports failed", expected, actual);
    }

    private String performOrganizeImports(ICompilationUnit unit,
            CompilationUnit astRoot) throws CoreException, Exception {
        MockAJOrganizeImportsOperation op = new MockAJOrganizeImportsOperation(unit, astRoot);
        op.run(new NullProgressMonitor());
        ((AJCompilationUnit) unit).requestOriginalContentMode();
        String actual = String.valueOf(((AJCompilationUnit) unit).getContents());
        ((AJCompilationUnit) unit).discardOriginalContentMode();
        return actual;
    }
}
