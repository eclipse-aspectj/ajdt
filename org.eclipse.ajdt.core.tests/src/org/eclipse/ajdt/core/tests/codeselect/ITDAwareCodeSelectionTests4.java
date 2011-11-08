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
package org.eclipse.ajdt.core.tests.codeselect;

import org.eclipse.contribution.jdt.itdawareness.INameEnvironmentProvider;
import org.eclipse.contribution.jdt.itdawareness.NameEnvironmentAdapter;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Tests that selecting the ITD name itself will appropriately 
 * select either the ITD or the target type
 * @author Andrew Eisenberg
 * @created Aug 16, 2010
 */
public class ITDAwareCodeSelectionTests4 extends
        AbstractITDAwareCodeSelectionTests {
    
    IJavaProject project;
    
    // need to set a NameEnviromentProvider, since this is typically
    // set by AJDT.UI
    INameEnvironmentProvider origProvider;
    INameEnvironmentProvider mockProvider = new MockNameEnvironmentProvider();
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        origProvider = NameEnvironmentAdapter.getInstance().getProvider();
        NameEnvironmentAdapter.getInstance().setProvider(mockProvider);
        super.setUp();
        project = JavaCore.create(createPredefinedProject("DefaultEmptyProject"));
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            NameEnvironmentAdapter.getInstance().setProvider(origProvider);
        }
    }
    
    // bug 361876
    public void testSelectITDMethod1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", 
                "package p;\n" + 
        		"aspect Aspect {\n" + 
        		"    public void Test.method(String arg1) { }\n" + 
        		"    public void Test.method() { }\n" + 
        		"}", project);
        createCompilationUnitAndPackage("p", "Test.java", 
                "package p;\n" + 
                "public class Test { }", project);
        validateCodeSelect(unit, findRegion(unit, "method", 1), "Test.method", false, 1);
        validateCodeSelect(unit, findRegion(unit, "method", 2), "Test.method", false, 0);
    }
    // bug 361876
    public void testSelectITDMethod2() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", 
                "package p;\n" + 
                "aspect Aspect {\n" + 
                "    public void Test.caller() {\n" + 
                "        this.method(null);\n" + 
                "        this.method();\n" + 
                "    }\n" + 
                "    public void caller(Test t) {\n" + 
                "        t.method(null);\n" + 
                "        t.method();\n" + 
                "    }\n" + 
                "    public void Test.method(String arg1) { }\n" + 
                "    public void Test.method() { }\n" + 
                "}", project);
        createCompilationUnitAndPackage("p", "Test.java", 
                "package p;\n" + 
                "public class Test { }", project);
        validateCodeSelect(unit, findRegion(unit, "method", 1), "Test.method", false, 1);
        validateCodeSelect(unit, findRegion(unit, "method", 2), "Test.method", false, 0);
        validateCodeSelect(unit, findRegion(unit, "method", 3), "Test.method", false, 1);
        validateCodeSelect(unit, findRegion(unit, "method", 4), "Test.method", false, 0);
    }
    // bug 361876
    public void testSelectITDMethod3() throws Exception {
        createCompilationUnitAndPackage("p", "Aspect.aj", 
                "package p;\n" + 
                "aspect Aspect {\n" + 
                "    public void Test.method(String arg1) { }\n" + 
                "    public void Test.method() { }\n" + 
                "}", project);
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Test.java", 
                "package p;\n" + 
        		"public class Test {\n" + 
        		"    void xx() {\n" + 
        		"        method(null);\n" + 
        		"        method();\n" + 
        		"    }\n" + 
        		"}", project);
        validateCodeSelect(unit, findRegion(unit, "method", 1), "Test.method", false, 1);
        validateCodeSelect(unit, findRegion(unit, "method", 2), "Test.method", false, 0);
    }
    
    public void testSelectTargetTypeNameField1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nint AClass.x;\nclass AClass { } }", project);
        validateCodeSelect(unit, findRegion(unit, "AClass", 1), "AClass");
    }
    public void testSelectTargetTypeNameField2() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nint AClass.x; }\nclass AClass { }", project);
        validateCodeSelect(unit, findRegion(unit, "AClass", 1), "AClass");
    }
    public void testSelectTargetTypeNameField3() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\nimport q.AClass;\naspect Aspect {\nint AClass.x; }", project);
        createCompilationUnitAndPackage("q", "AClass.java", "package q;\n public class AClass { }", project);
        validateCodeSelect(unit, findRegion(unit, "AClass", 1), "AClass");
    }

    public void testSelectTargetTypeNameMethod1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nvoid AClass.x() { }\nclass AClass { } }", project);
        validateCodeSelect(unit, findRegion(unit, "AClass", 1), "AClass");
    }
    public void testSelectTargetTypeNameMethod2() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nvoid AClass.x() { } }\nclass AClass { }", project);
        validateCodeSelect(unit, findRegion(unit, "AClass", 1), "AClass");
    }
    public void testSelectTargetTypeNameMethod3() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\nimport q.AClass;\naspect Aspect {\nvoid AClass.x() { } }", project);
        createCompilationUnitAndPackage("q", "AClass.java", "package q;\n public class AClass { }", project);
        validateCodeSelect(unit, findRegion(unit, "AClass", 1), "AClass");
    }
    
    // should select the class declaration
    public void testSelectTargetTypeNameConstructor1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nAClass.new() { } }\nclass AClass { }", project);
        validateCodeSelect(unit, findRegion(unit, "AClass", 1), "AClass");
    }
    
    // should select the ITD itself
    public void testSelectTargetTypeNameConstructor2() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nAClass.new() { }\nclass AClass { } }", project);
        validateCodeSelect(unit, findRegion(unit, "new", 1), "AClass.AClass_new", true);  // expecting a compilation problem
    }



    public void testSelectITDNameField1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nint AClass.x;\nclass AClass { } }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "AClass.x");
    }
    public void testSelectITDNameField2() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nint AClass.x; }\nclass AClass { }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "AClass.x");
    }
    public void testSelectITDNameField3() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\nimport q.AClass;\naspect Aspect {\nint AClass.x; }", project);
        createCompilationUnitAndPackage("q", "AClass.java", "package q;\n public class AClass { }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "AClass.x");
    }

    public void testSelectITDNameMethod1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nvoid AClass.x() { }\nclass AClass { } }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "AClass.x");
    }
    public void testSelectITDNameMethod2() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nvoid AClass.x() { } }\nclass AClass { }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "AClass.x");
    }
    public void testSelectITDNameMethod3() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\nimport q.AClass;\naspect Aspect {\nvoid AClass.x() { } }", project);
        createCompilationUnitAndPackage("q", "AClass.java", "package q;\n public class AClass { }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "AClass.x");
    }

    public void testSelectQualifiedTargetTypeNameField1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nint p.Aspect.AClass.x;\nclass AClass { } }", project);
        validateCodeSelect(unit, findRegion(unit, "AClass", 1), "AClass");
    }
    public void testSelectQualifiedTargetTypeNameMethod1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nvoid p.Aspect.AClass.x() { }\nclass AClass { } }", project);
        validateCodeSelect(unit, findRegion(unit, "AClass", 1), "AClass");
    }
    public void testSelectQualifiedTargetTypeNameConstructor1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\np.AClass.new() { } }\nclass AClass { }", project);
        validateCodeSelect(unit, findRegion(unit, "AClass", 1), "AClass");
    }

    
    public void testSelectQualifiedITDNameField1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nint p.Aspect.AClass.x;\nclass AClass { } }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "p.Aspect.AClass.x");
    }
    public void testSelectQualifiedITDNameMethod1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nvoid p.Aspect.AClass.x() { }\nclass AClass { } }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "p.Aspect.AClass.x");
    }
}
