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
		IProject libProject = (IProject)getWorkspaceRoot().findMember("MyAspectLibrary");
		if (libProject==null) {
			libProject = createPredefinedProject("MyAspectLibrary");
		}
		IProject weaveMeProject = createPredefinedProject("WeaveMe");
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] { AJRelationshipManager.ADVISED_BY };
			List allRels = AJModel.getInstance().getAllRelationships(
					weaveMeProject, rels);
			boolean gotBinaryAdvice = false;
			for (Iterator iter = allRels.iterator(); iter.hasNext();) {
				AJRelationship rel = (AJRelationship) iter.next();
				IJavaElement source = rel.getSource();
				if (source.getElementName().equals("main")) {
					IJavaElement target = rel.getTarget();
					if (target.getElementName().indexOf("injar aspect") != -1) {
						gotBinaryAdvice = true;
					}
				}
			}
			assertTrue("Didn't find main element advised by an injar aspect",
					gotBinaryAdvice);
		} finally {
			deleteProject(weaveMeProject);
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
		IProject project = createPredefinedProject("MarkersTest");
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] { AJRelationshipManager.ADVISED_BY };
			List allRels = AJModel.getInstance().getAllRelationships(project,
					rels);
			boolean gotBeforeAdviceWithoutRuntimeTest = false;
			boolean gotAroundAdviceWithRuntimeTest = false;
			for (Iterator iter = allRels.iterator(); iter.hasNext();) {
				AJRelationship rel = (AJRelationship) iter.next();
				IJavaElement source = rel.getSource();
				if (source.getElementName().equals("bar")) {
					IJavaElement target = rel.getTarget();
					if (target.getElementName().equals("before")
							&& !rel.hasRuntimeTest()) {
						gotBeforeAdviceWithoutRuntimeTest = true;
					} else if (target.getElementName().equals("around")
							&& rel.hasRuntimeTest()) {
						gotAroundAdviceWithRuntimeTest = true;
					}
				}
			}
			assertTrue(
					"Didn't find \"bar\" element advised by before advice without a runtime test",
					gotBeforeAdviceWithoutRuntimeTest);
			assertTrue(
					"Didn't find \"bar\" element advised by around advice with a runtime test",
					gotAroundAdviceWithRuntimeTest);
		} finally {
			deleteProject(project);
		}
	}
}