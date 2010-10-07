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
package org.eclipse.ajdt.ui.tests.hierarchy;

import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;



/**
 * Tests that the ITD Aware hierarchy builder works properly
 * This requires the JDT Weaving plugin to be present
 * @author andrew
 * @created Nov 24, 2008
 *
 *
 */
public class ITDAwareHierarchyTests2 extends UITestCase {
    IJavaProject proj;
    
    // for some reason, the primary owner is from jdt ui, and this is giving us problems
    // set it to null, so that the ui plugin is not triggered here.
    WorkingCopyOwner primaryOwner;
    
    protected void setUp() throws Exception {
        super.setUp();
        primaryOwner = DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider;
        DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider = null;
        
        proj = JavaCore.create(createPredefinedProject("DefaultEmptyProject"));
        
        // ensure the project is indexed so that hierarchy building can occur
        IndexManager manager = JavaModelManager.getIndexManager();
        manager.indexAll(proj.getProject());
    }
    
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider = primaryOwner;
        }
    }
    
    public void testAbstractAspectWithDeclare() throws Exception {
        ICompilationUnit[] units = createUnits(new String[] { "p" }, 
                new String[] { "AbstractAspect.aj" }, 
                new String[] {
                        "package p;\n" +
                        "public abstract aspect AbstractAspect {\n" +
                        "declare parents : Class extends X;\n" +
                        "declare parents : Class extends Y;\n" +
                        "}\n" +
                        "aspect Aspect extends AbstractAspect {\n" + 
                        "    void something(X x) {\n" +
                        "       something(new Class());\n" +
                        "    }\n" +
                        "    void something2(Y y) {\n" +
                        "        something2(new Class());\n" +
                        "    }\n" +
                        "}\n" +
                        "interface X { }\n" +
                        "interface Y { }\n" +
                        "class Class { }" }, proj);

        IType clazz = units[0].getType("Class");
        
        TypeHierarchy hierarchy = new TypeHierarchy(clazz, units, proj, false);
        hierarchy.refresh(null);
        IType[] allClasses = hierarchy.getAllClasses();
        assertEquals(2, allClasses.length);
        arrayContains("Object", allClasses);
        arrayContains("Class", allClasses);

        IType[] allInterfaces = hierarchy.getAllInterfaces();
        assertEquals(2, allInterfaces.length);
        arrayContains("X", allInterfaces);
        arrayContains("Y", allInterfaces);
    }
    
    public void testAbstractGenericAspectWithDeclare() throws Exception {
        ICompilationUnit[] units = createUnits(new String[] { "p" }, 
                new String[] { "AbstractAspect.aj" }, 
                new String[] {
                        "package p;\n" +
                        "public abstract aspect AbstractAspect<S, T> {\n" +
                        "declare parents : Class extends S;\n" +
                        "declare parents : Class extends T;\n" +
                        "}\n" +
                        "aspect Aspect extends AbstractAspect<X, Y> {\n" + 
                        "    void something(X x) {\n" +
                        "       something(new Class());\n" +
                        "    }\n" +
                        "    void something2(Y y) {\n" +
                        "        something2(new Class());\n" +
                        "    }\n" +
                        "}\n" +
                        "interface X { }\n" +
                        "interface Y { }\n" +
                        "class Class { }" }, proj);

        IType clazz = units[0].getType("Class");
        
        TypeHierarchy hierarchy = new TypeHierarchy(clazz, units, proj, false);
        hierarchy.refresh(null);
        IType[] allClasses = hierarchy.getAllClasses();
        assertEquals(2, allClasses.length);
        arrayContains("Object", allClasses);
        arrayContains("Class", allClasses);

        IType[] allInterfaces = hierarchy.getAllInterfaces();
        assertEquals(2, allInterfaces.length);
        arrayContains("X", allInterfaces);
        arrayContains("Y", allInterfaces);
    }
    
    
    private void arrayContains(String elementName, IType[] array) {
        boolean found = false;
        for (int i = 0; i < array.length; i++) {
            found |= array[i].getElementName().equals(elementName);
        }
        if (!found) {
            fail("Searching for " + elementName + " in ITD aware type hierarchy, but not found");
        }
    }
}
