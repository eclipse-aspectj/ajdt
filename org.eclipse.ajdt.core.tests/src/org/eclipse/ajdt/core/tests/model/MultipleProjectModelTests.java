/*******************************************************************************
 * Copyright (c) 2010 SprinSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.core.tests.model;

import java.util.List;

import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Tests for mapping between IProgramElement and IJavaElements
 * 
 * This class tests that aj references across projects are working
 */
public class MultipleProjectModelTests extends AJDTCoreTestCase {

    private IJavaProject commons;
    private IJavaProject dependent;
    private AJProjectModelFacade model;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        commons = JavaCore.create(createPredefinedProject("DefaultEmptyProject"));
        dependent = JavaCore.create(createPredefinedProject("DefaultEmptyProjectDependent"));
        model = AJProjectModelFactory.getInstance().getModelForJavaElement(dependent);
    }
    
    public void testITDFieldInOtherProject() throws Exception {
        ICompilationUnit other = createCompilationUnitAndPackage("common", "Aspect.aj", 
                "package common;\n" +
                "public aspect Aspect {\n" +
                "  public int ITD.x;\n" +
                "  public void ITD.x2() { };\n" +
                "  declare parents : dependent.* implements ITD;\n" +
                "  declare @type : dependent.* : @Foo;\n" +
                "  interface ITD { }\n" +
                "  public @interface Foo{ }" +
                "}", commons);
        ICompilationUnit unit = createCompilationUnitAndPackage("dependent", "Java.java", 
                "package dependent;\n" +
                "public class Java {\n" +
                "}", dependent);

        assertNoProblems(commons.getProject());
        assertNoProblems(dependent.getProject());
        IType type = unit.getType("Java");
        IType otherType = other.getType("Aspect");
        
        List<IJavaElement> elts = model.getRelationshipsForElement(type,  AJRelationshipManager.ASPECT_DECLARATIONS);

        // the ITDs + the declare statement
        assertEquals("Wrong number of aspect declarations found "+ type, 3, elts.size());
        for (IJavaElement orig : elts) {
            roundTripCheck(otherType, orig);
        }

        // AspectJ bug 327057  Not working yet
//        elts = model.getRelationshipsForElement(type,  AJRelationshipManager.ANNOTATED_BY);
//
//        // one declare annotation
//        assertEquals("Wrong number of aspect declarations found "+ type, 1, elts.size());
//        for (IJavaElement orig : elts) {
//            roundTripCheck(otherType, orig);
//        }
    }

    private void roundTripCheck(IType otherType, IJavaElement orig) throws JavaModelException {
        assertTrue("Should exist: " + orig.getHandleIdentifier(), orig.exists());
        assertEquals(otherType, orig.getParent());
        assertTrue("" + orig + " should be a child of " + otherType, isAChildOf(otherType, orig));
        
        IProgramElement ipe = model.javaElementToProgramElement(orig);
        assertNotSame(IHierarchy.NO_STRUCTURE, ipe);
        
        orig = model.programElementToJavaElement(ipe);
        assertTrue("Should exist: " + orig.getHandleIdentifier(), orig.exists());
        assertEquals(otherType, orig.getParent());
        assertTrue("" + orig + " should be a child of " + otherType, isAChildOf(otherType, orig));
    }
    
    private boolean isAChildOf(IParent parent, IJavaElement maybeChild) throws JavaModelException {
        IJavaElement[] children = parent.getChildren();
        for (IJavaElement child : children) {
            if (child.equals(maybeChild)) {
                return true;
            }
        }
        return false;
    }
}