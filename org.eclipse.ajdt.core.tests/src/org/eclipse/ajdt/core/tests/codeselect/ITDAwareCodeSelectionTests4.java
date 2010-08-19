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
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = JavaCore.create(createPredefinedProject("DefaultEmptyProject"));
    }
    
    public void testSelectTargetTypeNameField1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nint Class.x;\nclass Class { } }", project);
        validateCodeSelect(unit, findRegion(unit, "Class", 1), "Class");
    }
    public void testSelectTargetTypeNameField2() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nint Class.x; }\nclass Class { }", project);
        validateCodeSelect(unit, findRegion(unit, "Class", 1), "Class");
    }
    public void testSelectTargetTypeNameField3() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\nimport q.Class;\naspect Aspect {\nint Class.x; }", project);
        createCompilationUnitAndPackage("q", "Class.java", "package q;\n public class Class { }", project);
        validateCodeSelect(unit, findRegion(unit, "Class", 1), "Class");
    }

    public void testSelectTargetTypeNameMethod1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nvoid Class.x() { }\nclass Class { } }", project);
        validateCodeSelect(unit, findRegion(unit, "Class", 1), "Class");
    }
    public void testSelectTargetTypeNameMethod2() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nvoid Class.x() { } }\nclass Class { }", project);
        validateCodeSelect(unit, findRegion(unit, "Class", 1), "Class");
    }
    public void testSelectTargetTypeNameMethod3() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\nimport q.Class;\naspect Aspect {\nvoid Class.x() { } }", project);
        createCompilationUnitAndPackage("q", "Class.java", "package q;\n public class Class { }", project);
        validateCodeSelect(unit, findRegion(unit, "Class", 1), "Class");
    }
    
    public void testSelectTargetTypeNameConstructor1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nClass.new() { }\nclass Class { } }", project);
        validateCodeSelect(unit, findRegion(unit, "Class", 1), "Class.Class_new");
    }
    
    public void testSelectTargetTypeNameConstructor2() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nClass.new() { }\nclass Class { } }", project);
        validateCodeSelect(unit, findRegion(unit, "new", 1), "Class.Class_new");
    }



    public void testSelectITDNameField1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nint Class.x;\nclass Class { } }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "Class.x");
    }
    public void testSelectITDNameField2() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nint Class.x; }\nclass Class { }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "Class.x");
    }
    public void testSelectITDNameField3() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\nimport q.Class;\naspect Aspect {\nint Class.x; }", project);
        createCompilationUnitAndPackage("q", "Class.java", "package q;\n public class Class { }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "Class.x");
    }

    public void testSelectITDNameMethod1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nvoid Class.x() { }\nclass Class { } }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "Class.x");
    }
    public void testSelectITDNameMethod2() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nvoid Class.x() { } }\nclass Class { }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "Class.x");
    }
    public void testSelectITDNameMethod3() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\nimport q.Class;\naspect Aspect {\nvoid Class.x() { } }", project);
        createCompilationUnitAndPackage("q", "Class.java", "package q;\n public class Class { }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "Class.x");
    }

    public void testSelectQualifiedTargetTypeNameField1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nint p.Class.x;\nclass Class { } }", project);
        validateCodeSelect(unit, findRegion(unit, "Class", 1), "Class");
    }
    public void testSelectQualifiedTargetTypeNameMethod1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nvoid p.Class.x() { }\nclass Class { } }", project);
        validateCodeSelect(unit, findRegion(unit, "Class", 1), "Class");
    }
    public void testSelectQualifiedTargetTypeNameConstructor1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\np.Class.new() { }\nclass Class { } }", project);
        validateCodeSelect(unit, findRegion(unit, "Class", 1), "p.Class.p_Class_new");
    }

    
    public void testSelectQualifiedITDNameField1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nint p.Class.x;\nclass Class { } }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "p.Class.x");
    }
    public void testSelectQualifiedITDNameMethod1() throws Exception {
        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\naspect Aspect {\nvoid p.Class.x() { }\nclass Class { } }", project);
        validateCodeSelect(unit, findRegion(unit, "x", 1), "p.Class.x");
    }

}
