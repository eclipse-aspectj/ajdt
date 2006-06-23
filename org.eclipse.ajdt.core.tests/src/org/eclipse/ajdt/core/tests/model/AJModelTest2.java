/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.core.tests.model;

import java.util.Iterator;
import java.util.List;

import org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;

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
		IProject libProject = (IProject)getWorkspaceRoot().findMember("MyAspectLibrary"); //$NON-NLS-1$
		if (libProject==null) {
			libProject = createPredefinedProject("MyAspectLibrary"); //$NON-NLS-1$
		}
		IProject weaveMeProject = createPredefinedProject("WeaveMe"); //$NON-NLS-1$
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] { AJRelationshipManager.ADVISED_BY };
			List allRels = AJModel.getInstance().getAllRelationships(
					weaveMeProject, rels);
			boolean gotBinaryAdvice = false;
			for (Iterator iter = allRels.iterator(); iter.hasNext();) {
				AJRelationship rel = (AJRelationship) iter.next();
				IJavaElement source = rel.getSource();
				if (source.getElementName().equals("main")) { //$NON-NLS-1$
					IJavaElement target = rel.getTarget();
					if (target.getElementName().indexOf("binary aspect") != -1) { //$NON-NLS-1$
						gotBinaryAdvice = true;
					}
				}
			}
			assertTrue("Didn't find main element advised by an injar aspect", //$NON-NLS-1$
					gotBinaryAdvice);
		} finally {
			deleteProject(weaveMeProject);
			// TODO: reinstate the deletion when we can make it work reliably
			//deleteProject(libProject);
		}
	}

	/**
	 * Tests for the existence of a particular "advised by" relationship with a
	 * runtime test, and one without.
	 * 
	 * @throws Exception
	 */
	public void testHasRuntimeTest() throws Exception {
		IProject project = createPredefinedProject("MarkersTest"); //$NON-NLS-1$
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] { AJRelationshipManager.ADVISED_BY };
			List allRels = AJModel.getInstance().getAllRelationships(project,
					rels);
			boolean gotBeforeAdviceWithoutRuntimeTest = false;
			boolean gotAroundAdviceWithRuntimeTest = false;
			for (Iterator iter = allRels.iterator(); iter.hasNext();) {
				AJRelationship rel = (AJRelationship) iter.next();
				IJavaElement source = rel.getSource();
				if (source.getElementName().equals("bar")) { //$NON-NLS-1$
					IJavaElement target = rel.getTarget();
					if (target.getElementName().equals("before") //$NON-NLS-1$
							&& !rel.hasRuntimeTest()) {
						gotBeforeAdviceWithoutRuntimeTest = true;
					} else if (target.getElementName().equals("around") //$NON-NLS-1$
							&& rel.hasRuntimeTest()) {
						gotAroundAdviceWithRuntimeTest = true;
					}
				}
			}
			assertTrue(
					"Didn't find \"bar\" element advised by before advice without a runtime test", //$NON-NLS-1$
					gotBeforeAdviceWithoutRuntimeTest);
			assertTrue(
					"Didn't find \"bar\" element advised by around advice with a runtime test", //$NON-NLS-1$
					gotAroundAdviceWithRuntimeTest);
		} finally {
			deleteProject(project);
		}
	}
	
	public void testUsesPointcutRels() throws Exception {
		// pr148027 - if we're not generating the uses pointcut relationship
		// then just return
		if (!AsmHierarchyBuilder.shouldAddUsesPointcut) return; 
		IProject project = createPredefinedProject("TJP Example"); //$NON-NLS-1$
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] { AJRelationshipManager.USES_POINTCUT };
			List usesRels = AJModel.getInstance().getAllRelationships(project,
					rels);
			boolean gotUsesDemoExces = false;
			boolean gotUsesGoCut = false;
			for (Iterator iter = usesRels.iterator(); iter.hasNext();) {
				AJRelationship rel = (AJRelationship) iter.next();
				IJavaElement source = rel.getSource();
				if (source.getElementName().equals("around")) { //$NON-NLS-1$
					IJavaElement target = rel.getTarget();
					if (target.getElementName().equals("demoExecs")) { //$NON-NLS-1$
						gotUsesDemoExces = true;
					} else if (target.getElementName().equals("goCut")) { //$NON-NLS-1$
						gotUsesGoCut = true;
					}
				}
			}
			assertTrue(
					"Didn't find relationship for around advice using the demoExecs pointcut", //$NON-NLS-1$
					gotUsesDemoExces);
			assertTrue(
					"Didn't find relationship for around advice using the goCut pointcut", //$NON-NLS-1$
					gotUsesGoCut);
			
			// now test pointcut used by relationships
			rels = new AJRelationshipType[] { AJRelationshipManager.POINTCUT_USED_BY };
			List usedByRels = AJModel.getInstance().getAllRelationships(project,
					rels);
			boolean gotUsedByDemoExces = false;
			boolean gotUsedByGoCut = false;
			for (Iterator iter = usedByRels.iterator(); iter.hasNext();) {
				AJRelationship rel = (AJRelationship) iter.next();
				IJavaElement target = rel.getTarget();
				if (target.getElementName().equals("around")) { //$NON-NLS-1$
					IJavaElement source = rel.getSource();
					if (source.getElementName().equals("demoExecs")) { //$NON-NLS-1$
						gotUsedByDemoExces = true;
					} else if (source.getElementName().equals("goCut")) { //$NON-NLS-1$
						gotUsedByGoCut = true;
					}
				}
			}
			assertTrue(
					"Didn't find relationship for demoExecs pointcut used by around advice", //$NON-NLS-1$
					gotUsedByDemoExces);
			assertTrue(
					"Didn't find relationship for goCut pointcut used by around advice", //$NON-NLS-1$
					gotUsedByGoCut);
		} finally {
			deleteProject(project);
		}
	}
}