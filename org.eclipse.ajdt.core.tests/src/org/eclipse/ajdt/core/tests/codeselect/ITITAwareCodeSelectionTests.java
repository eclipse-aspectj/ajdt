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
 * Tests that code selection opccurs correctly on intertype inner type references
 * @author Andrew Eisenberg
 * @created Aug 16, 2010
 */
public class ITITAwareCodeSelectionTests extends
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
    

    public void testNothingYet() throws Exception {
        // stub
    }
    //    public void testSelectTargetTypeNameField3() throws Exception {
//        ICompilationUnit unit = createCompilationUnitAndPackage("p", "Aspect.aj", "package p;\nimport q.AClass;\naspect Aspect {\nint AClass.x; }", project);
//        createCompilationUnitAndPackage("q", "AClass.java", "package q;\n public class AClass { }", project);
//        validateCodeSelect(unit, findRegion(unit, "AClass", 1), "AClass");
//    }

}
