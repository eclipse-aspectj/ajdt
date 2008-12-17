package org.eclipse.ajdt.core.tests.weaving;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyBuilder;
import org.eclipse.jdt.internal.core.hierarchy.IndexBasedHierarchyBuilder;
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
public class ITDAwareHierarchyTests extends AJDTCoreTestCase {
    AJCompilationUnit ship;
    ICompilationUnit yacht;
    IJavaProject shipProj;
    
    protected void setUp() throws Exception {
        super.setUp();
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
    
    /**
     * Tests that ITD super types and super interfaces are included in the type hierarchy
     * when they are declared on the focus type
     */
    public void testSuperTypeHierarchyOfFocusType() throws Exception {
        TypeHierarchy hierarchy = new TypeHierarchy(ship.findPrimaryType(), new ICompilationUnit[] {ship, yacht}, shipProj, false);
        hierarchy.refresh(null);
        IType[] allClasses = hierarchy.getAllClasses();
        assertEquals(3, allClasses.length);
        assertEquals("Object", allClasses[0].getElementName());
        assertEquals("FloatingThing", allClasses[1].getElementName());
        assertEquals("Ship", allClasses[2].getElementName());
        
        IType[] allInterfaces = hierarchy.getAllInterfaces();
        assertEquals(1, allInterfaces.length);
        assertEquals("Log", allInterfaces[0].getElementName());
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
        assertEquals("Object", allClasses[0].getElementName());
        assertEquals("FloatingThing", allClasses[1].getElementName());
        assertEquals("Ship", allClasses[2].getElementName());
        assertEquals("Yacht", allClasses[3].getElementName());
        
        IType[] allInterfaces = hierarchy.getAllInterfaces();
        assertEquals(1, allInterfaces.length);
        assertEquals("Log", allInterfaces[0].getElementName());
    }

    /**
     * We don't implement this yet
     */
    public void testSubTypeHierarchy() {
         System.out.println("ITD Aware Sub Type Hierarchies not implemented");
    }
    
}
