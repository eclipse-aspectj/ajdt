/*******************************************************************************
 * Copyright (c) 2010 SpringSource Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Andrew Eisenberg = Initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.model;


import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * 
 * @author Andrew Eisenberg
 * @created Oct 29, 2010
 */
public class AJModelTest6 extends AbstractModelTest {

    IProject project;
    IJavaProject javaProject;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = createPredefinedProject("DefaultEmptyProject", true);
        javaProject = JavaCore.create(project);
    }
    
    public void testBug329067() throws Exception {
        createCompilationUnitAndPackage("pack", "Java.java", "package pack;\npublic class Java { }", javaProject);
        createCompilationUnitAndPackage("pack2", "Aspect.aj", "package pack2;\nimport pack.Java;\npublic aspect Aspect {\n " +
        		"public int Java.x;\n" +
        		"public int Java.x() { return x; } }", javaProject);
        checkHandles(javaProject);
    }

}
