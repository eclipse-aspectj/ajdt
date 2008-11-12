/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.core.tests.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder;
import org.aspectj.asm.IRelationship;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.testutils.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * More tests for mapping between IProgramElement and IJavaElements.
 */
public class AJModelTest2 extends AJDTCoreTestCase {

	/**
	 * Tests for a injar/binary relationship for an element advised by advice in
	 * another project (by adding that project's bin directory to the
	 * aspectpath)
	 * 
	 * @throws Exception
	 */
	public void testAspectPathDirWeaving() throws Exception {
		createPredefinedProject14("MyAspectLibrary"); //$NON-NLS-1$
		IProject weaveMeProject = createPredefinedProject("WeaveMe"); //$NON-NLS-1$
		AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(weaveMeProject);
        
		AJRelationshipType[] rels = new AJRelationshipType[] { AJRelationshipManager.ADVISED_BY };
		List/*IRelationship*/ allRels = model.getRelationshipsForProject( rels);
		boolean gotBinaryAdvice = false;
		for (Iterator iter = allRels.iterator(); iter.hasNext();) {
		    IRelationship rel = (IRelationship) iter.next();
			IJavaElement source = model.programElementToJavaElement(rel.getSourceHandle());
			if (source.getElementName().equals("main")) { //$NON-NLS-1$
			    for (Iterator targetIter = rel.getTargets().iterator(); targetIter.hasNext(); ) {
	                IJavaElement target = model.programElementToJavaElement(
	                        (String) targetIter.next());
//	                if (BinaryWeavingSupport.isActive) {
	                    if (target.getElementName().indexOf("before") != -1) { //$NON-NLS-1$
	                        gotBinaryAdvice = true;
	                    }
//	                } else {
//	                    if (target.getElementName().indexOf("binary aspect") != -1) { //$NON-NLS-1$
//	                        gotBinaryAdvice = true;
//	                    }
//	                }
			    }
			}
		}
//		if (BinaryWeavingSupport.isActive) {
			assertTrue("Didn't find main element advised by before advice", //$NON-NLS-1$
					gotBinaryAdvice);
//		} else {
//			assertTrue("Didn't find main element advised by a binary aspect", //$NON-NLS-1$
//					gotBinaryAdvice);
//		}
	}

	/**
	 * Tests for the existence of a particular "advised by" relationship with a
	 * runtime test, and one without.
	 * 
	 * @throws Exception
	 */
	public void testHasRuntimeTest() throws Exception {
		IProject project = createPredefinedProject("MarkersTest"); //$NON-NLS-1$
		AJRelationshipType[] rels = new AJRelationshipType[] { AJRelationshipManager.ADVISED_BY };
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(project);
        List/*IRelationship*/ allRels = model.getRelationshipsForProject(rels);
		boolean gotBeforeAdviceWithoutRuntimeTest = false;
		boolean gotAroundAdviceWithRuntimeTest = false;
		for (Iterator iter = allRels.iterator(); iter.hasNext();) {
		    IRelationship rel = (IRelationship) iter.next();
            IJavaElement source = model.programElementToJavaElement(rel.getSourceHandle());
			if (source.getElementName().equals("bar")) { //$NON-NLS-1$
                for (Iterator targetIter = rel.getTargets().iterator(); targetIter.hasNext(); ) {
                    IJavaElement target = model.programElementToJavaElement(
                            (String) targetIter.next());
    				if (target.getElementName().equals("before") //$NON-NLS-1$
    						&& !rel.hasRuntimeTest()) {
    					gotBeforeAdviceWithoutRuntimeTest = true;
    				} else if (target.getElementName().equals("around") //$NON-NLS-1$
    						&& rel.hasRuntimeTest()) {
    					gotAroundAdviceWithRuntimeTest = true;
    				}
                }
			}
		}
		assertTrue(
				"Didn't find \"bar\" element advised by before advice without a runtime test", //$NON-NLS-1$
				gotBeforeAdviceWithoutRuntimeTest);
		assertTrue(
				"Didn't find \"bar\" element advised by around advice with a runtime test", //$NON-NLS-1$
				gotAroundAdviceWithRuntimeTest);
	}
	
	   
    /**
     * Tests that the backwards compatibility of Using AJModel works properly
     * 
     * This uses the old style of AJModel.
     * The AJModel api will go away in the future
     * @throws Exception
     */
    public void testBug253245() throws Exception {
        Utils.setAutobuilding(false);
        IProject project = createPredefinedProject("ITDTesting"); //$NON-NLS-1$
        IJavaProject jProject = JavaCore.create(project);
        project.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        
        
        IType demo = jProject.findType("test.Demo");
        IType myAspect = jProject.findType("test.MyAspect");
        IType otherClass = jProject.findType("test.OtherClass");

        // no build yet. should return empty sets for each
        Set demoSet = getDeclaredMethods(demo);
        Set myAspectSet = getDeclaredMethods(myAspect);
        Set otherClassSet = getDeclaredMethods(otherClass);
        
        assertEquals("Project hasn't been built, so no relationships should have been found.", 0, demoSet.size());
        assertEquals("Project hasn't been built, so no relationships should have been found.", 0, myAspectSet.size());
        assertEquals("Project hasn't been built, so no relationships should have been found.", 0, otherClassSet.size());
        
        Utils.setAutobuilding(true);
        waitForAutoBuild();

        // ensure that all classes have the proper declare methods on it
        demoSet = getDeclaredMethods(demo);
        myAspectSet = getDeclaredMethods(myAspect);
        otherClassSet = getDeclaredMethods(otherClass);
        
        assertEquals("ITDs have not been found.", 5, demoSet.size());
        assertEquals("Shouldn't have any ITDs", 0, myAspectSet.size());
        assertEquals("Shouldn't have any ITDs.", 0, otherClassSet.size());
    }

    public static Set/*IMethod*/ getDeclaredMethods(IType type) throws JavaModelException {
        Set methods = new HashSet/*IMethod*/();
        AJRelationshipType[] types = new AJRelationshipType[] { AJRelationshipManager.DECLARED_ON };
        List/*AJRelationship*/ rels = AJModel.getInstance().getAllRelationships(
                type.getResource().getProject(), types);
        for (Iterator relIter = rels.iterator(); relIter.hasNext();) {
            AJRelationship rel = (AJRelationship) relIter.next();
            if (rel.getTarget().equals(type)) {
                IntertypeElement iType = (IntertypeElement) rel.getSource();
                methods.add(iType);
            }
        }
        return methods;
    }
}