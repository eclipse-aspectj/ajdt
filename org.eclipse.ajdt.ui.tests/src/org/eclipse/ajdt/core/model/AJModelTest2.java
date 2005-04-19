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

package org.eclipse.ajdt.core.model;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.ajdt.test.AllTests;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;

/**
 * More tests for mapping between IProgramElement and IJavaElements
 * 
 * @author Matt Chapman
 */
public class AJModelTest2 extends TestCase {

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		AllTests.setupAJDTPlugin();
	}

	/**
	 * Tests for a injar/binary relationship for an element advised by advice in
	 * another project (by adding that project's bin directory to the
	 * aspectpath)
	 * 
	 * @throws Exception
	 */
	public void testAspectPathDirWeaving() throws Exception {
		IProject libProject = Utils.createPredefinedProject("MyAspectLibrary");
		IProject weaveMeProject = Utils.createPredefinedProject("WeaveMe");

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

		Utils.deleteProject(weaveMeProject);
		Utils.deleteProject(libProject);
	}

	/**
	 * Tests for the existence of a particular "advised by" relationship with a
	 * runtime test, and one without.
	 * 
	 * @throws Exception
	 */
	public void testHasRuntimeTest() throws Exception {
		IProject project = Utils.createPredefinedProject("MarkersTest");

		AJRelationshipType[] rels = new AJRelationshipType[] { AJRelationshipManager.ADVISED_BY };
		List allRels = AJModel.getInstance().getAllRelationships(project, rels);
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

		Utils.deleteProject(project);
	}
}