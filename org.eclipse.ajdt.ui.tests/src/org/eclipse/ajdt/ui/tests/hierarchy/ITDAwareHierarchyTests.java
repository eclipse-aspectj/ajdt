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

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
public class ITDAwareHierarchyTests extends UITestCase {
    AJCompilationUnit ship;
    ICompilationUnit yacht;
    IJavaProject shipProj;
    
    // for some reason, the primary owner is from jdt ui, and this is giving us problems
    // set it to null, so that the ui plugin is not triggered here.
    WorkingCopyOwner primaryOwner;
    
    protected void setUp() throws Exception {
        super.setUp();
        primaryOwner = DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider;
        DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider = null;
        
        IProject proj = createPredefinedProject("ITDAwareHierarchy");
        IFile shipFile = proj.getFile("src/ships/Ship.aj");
        ship = (AJCompilationUnit) AspectJCore.create(shipFile);
        ship.becomeWorkingCopy(null);
        IFile yachtFile = proj.getFile("src/ships/Yacht.aj");
        yacht = (ICompilationUnit) AspectJCore.create(yachtFile);
        yacht.becomeWorkingCopy(null);
        shipProj = JavaCore.create(proj);
        
        // ensure the project is indexed so that hierarchy building can occur
        IndexManager manager = JavaModelManager.getIndexManager();
        manager.indexAll(proj);
    }
    
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider = primaryOwner;
        }
    }
    
    /**
     * Tests that ITD super types and super interfaces are included in the type hierarchy
     * when they are declared on the focus type
     */
    public void testSuperTypeHierarchyOfFocusType() throws Exception {
        TypeHierarchy hierarchy = new TypeHierarchy(ship.findPrimaryType(), new ICompilationUnit[] {ship, yacht}, shipProj, false);
        hierarchy.refresh(null);
        IType[] allClasses = hierarchy.getAllClasses();
        assertEquals(3, allClasses.length);
        arrayContains("Object", allClasses);
        arrayContains("FloatingThing", allClasses);
        arrayContains("Ship", allClasses);
        
        IType[] allInterfaces = hierarchy.getAllInterfaces();
        assertEquals(1, allInterfaces.length);
        arrayContains("Log", allInterfaces);
    }
    
    /**
     * Tests that ITD super types and super interfaces are included in the type hierarchy
     * when they are declared on the non-focus type
     */
    public void testSuperTypeHierarchyOfNonFocusType() throws Exception {
        TypeHierarchy hierarchy = new TypeHierarchy(yacht.findPrimaryType(), new ICompilationUnit[] {ship, yacht}, shipProj, false);
        hierarchy.refresh(null);
        IType[] allClasses = hierarchy.getAllClasses();
        assertEquals(4, allClasses.length);
        arrayContains("Object", allClasses);
        arrayContains("FloatingThing", allClasses);
        arrayContains("Ship", allClasses);
        arrayContains("Yacht", allClasses);
        
        IType[] allInterfaces = hierarchy.getAllInterfaces();
        assertEquals(1, allInterfaces.length);
        arrayContains("Log", allInterfaces);
    }
    
    /**
     * We don't implement this yet
     */
    public void testSubTypeHierarchy() {
         System.out.println("ITD Aware Sub Type Hierarchies not implemented");
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
