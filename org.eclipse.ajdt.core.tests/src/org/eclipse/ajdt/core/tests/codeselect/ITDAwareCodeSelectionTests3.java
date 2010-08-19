/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *     Kris De Volder - Bug 318509 related
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.codeselect;

import org.eclipse.contribution.jdt.itdawareness.INameEnvironmentProvider;
import org.eclipse.contribution.jdt.itdawareness.NameEnvironmentAdapter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Andrew Eisenberg
 * @created Jun 6, 2009
 * Tests code selection for ITDs
 * Further testing of ITD hyperlinks.
 * Ensure that hyperlinking works when target aspect is in separate project
 */
public class ITDAwareCodeSelectionTests3 extends AbstractITDAwareCodeSelectionTests {
    
    // need to set a NameEnviromentProvider, since this is typically
    // set by AJDT.UI
    // BAD!!!
    INameEnvironmentProvider origProvider;
    INameEnvironmentProvider mockProvider = new MockNameEnvironmentProvider();

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        origProvider = NameEnvironmentAdapter.getInstance().getProvider();
        NameEnvironmentAdapter.getInstance().setProvider(mockProvider);
        super.setUp();
    }

    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            NameEnvironmentAdapter.getInstance().setProvider(origProvider);
        }
    }

    public void testBug318509MethodAndITDWithSameNumberOfArgs() throws Exception {
    	IProject project = createPredefinedProject("Bug318509MethodAndITDWithSameNumberOfArgs");
    	ICompilationUnit main = getCompilationUnit(project);
    	project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
    	
    	//Tests for methods:
    	validateCodeSelect(main, findRegion(main, "method", 1), "src/MyClass.java", "MyClass.method(int)");
    	validateCodeSelect(main, findRegion(main, "method", 2), "src/MyAspect.aj", "MyAspect.MyClass.method(String)");
    	//Tests for constructors:
    	validateCodeSelect(main, findRegion(main, "MyClass", 2), "src/MyClass.java", "MyClass.MyClass(int)");
    	validateCodeSelect(main, findRegion(main, "MyClass", 3), "src/MyAspect.aj", "MyAspect.MyClass.MyClass_new(String)");
    }

	private ICompilationUnit getCompilationUnit(IProject project) {
		ICompilationUnit main = JavaCore.createCompilationUnitFrom(project.getFile("src/Main.java"));
		return main;
	}
}
