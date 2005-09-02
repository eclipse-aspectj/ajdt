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
package org.eclipse.ajdt.core.tests;

import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.EclipseVersion;
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Tests for AspectJCore.create()
 */
public class AJCoreTest extends AJDTCoreTestCase {

	/**
	 * Test that AspectJCore.create() can form appropriate Java elements from a
	 * variety of handle identifiers
	 * 
	 * @throws Exception
	 */
	public void testCreateElementFromHandle() throws Exception {
		IProject project = createPredefinedProject("TJP Example");
		try {
			// each entry in the array contains:
			// <handle> <name of element> <containing resource> <class name of
			// element>
			// note that the elements referred to by the handles need to exist
			// in the workspace
			String[][] testHandles = {
					{ "=TJP Example/src<tjp{Demo.java", "Demo.java",
							"Demo.java", "CompilationUnit" },
					{ "=TJP Example/src<tjp{Demo.java[Demo~main", "main",
							"Demo.java", "SourceMethod" },
					{ "=TJP Example/src<tjp*GetInfo.aj", "GetInfo.aj",
							"GetInfo.aj", "AJCompilationUnit" },
					{ "=TJP Example/src<tjp*GetInfo.aj}GetInfo", "GetInfo",
							"GetInfo.aj", "AspectElement" },
					{ "=TJP Example/src<tjp*GetInfo.aj}GetInfo~println",
							"println", "GetInfo.aj", "SourceMethod" },
					{ "=TJP Example/src<tjp*GetInfo.aj}GetInfo&around",
							"around", "GetInfo.aj", "AdviceElement" } };
			compareWithHandles(testHandles);
		} finally {
			deleteProject(project);
		}
	}

	/**
	 * Test that AspectJCore.create() can form appropriate Java elements from a
	 * variety of handle identifiers
	 * 
	 * @throws Exception
	 */
	public void testCreateElementFromHandle2() throws Exception {
		IProject project = createPredefinedProject("Bean Example");
		try {
			String methodHandle = "=Bean Example/src<bean{Demo.java[Demo~main~\\[QString;?method-call(void bean.Point.setX(int))!37!0!0!0!I";
			if ((EclipseVersion.MAJOR_VERSION == 3)
					&& (EclipseVersion.MINOR_VERSION == 0)) {
				// the handle identifiers for method signatures changed after
				// eclipes 3.0: note the lack of escape character ~[QString;
				// instead of ~\[QString;
				methodHandle = "=Bean Example/src<bean{Demo.java[Demo~main~[QString;?method-call(void bean.Point.setX(int))!37!0!0!0!I";
			}

			// each entry in the array contains:
			// <handle> <name of element> <containing resource> <class name of
			// element>
			// note that the elements referred to by the handles need to exist
			// in the workspace
			String[][] testHandles = {
					{ methodHandle, "method-call(void bean.Point.setX(int))",
							"Demo.java", "AJCodeElement" },
					{
							"=Bean Example/src<bean*BoundPoint.aj}BoundPoint&around&QPoint;",
							"around", "BoundPoint.aj", "AdviceElement" },
					{
							"=Bean Example/src<bean*BoundPoint.aj}BoundPoint¬Point.hasListeners¬QString;",
							"Point.hasListeners", "BoundPoint.aj",
							"IntertypeElement" },
					{
							"=Bean Example/src<bean*BoundPoint.aj}BoundPoint`declare parents",
							"declare parents", "BoundPoint.aj",
							"DeclareElement" },
					{
							"=Bean Example/src<bean*BoundPoint.aj}BoundPoint`declare parents!2",
							"declare parents", "BoundPoint.aj",
							"DeclareElement" }

			};
			compareWithHandles(testHandles);
		} finally {
			deleteProject(project);
		}
	}

	/**
	 * Test that going from an IJavaElement to its handle identifier then back
	 * to an IJavaElement using AspectJCore.create() results in a element that
	 * is equivalent to the original (not necessarily identical).
	 * 
	 * @throws Exception
	 */
	public void testHandleCreateRoundtrip() throws Exception {
		IProject project = createPredefinedProject("TJP Example");
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] {
					AJRelationshipManager.ADVISED_BY,
					AJRelationshipManager.ADVISES };
			compareElementsFromRelationships(rels, project);
		} finally {
			deleteProject(project);
		}
	}

	/**
	 * Test that going from an IJavaElement to its handle identifier then back
	 * to an IJavaElement using AspectJCore.create() results in a element that
	 * is equivalent to the original (not necessarily identical).
	 * 
	 * @throws Exception
	 */
	public void testHandleCreateRoundtrip2() throws Exception {
		IProject project = createPredefinedProject("Bean Example");
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] {
					AJRelationshipManager.ADVISED_BY,
					AJRelationshipManager.ADVISES,
					AJRelationshipManager.DECLARED_ON,
					AJRelationshipManager.ASPECT_DECLARATIONS };
			compareElementsFromRelationships(rels, project);
		} finally {
			deleteProject(project);
		}
	}

	/**
	 * Test that going from an IJavaElement to its handle identifier then back
	 * to an IJavaElement using AspectJCore.create() results in a element that
	 * is equivalent to the original (not necessarily identical).
	 * 
	 * @throws Exception
	 */
	public void testHandleCreateRoundtrip3() throws Exception {
		IProject project = createPredefinedProject("MarkersTest");
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] {
					AJRelationshipManager.ADVISED_BY,
					AJRelationshipManager.ADVISES,
					AJRelationshipManager.DECLARED_ON,
					AJRelationshipManager.ASPECT_DECLARATIONS,
					AJRelationshipManager.MATCHED_BY,
					AJRelationshipManager.MATCHES_DECLARE };
			compareElementsFromRelationships(rels, project);
		} finally {
			deleteProject(project);
		}
	}

	/**
	 * Test that going from an IJavaElement to its handle identifier then back
	 * to an IJavaElement using AspectJCore.create() results in a element that
	 * is equivalent to the original (not necessarily identical).
	 * 
	 * @throws Exception
	 */
	public void testHandleCreateRoundtrip4() throws Exception {
		IProject project = createPredefinedProject("Spacewar Example");
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] {
					AJRelationshipManager.ADVISED_BY,
					AJRelationshipManager.ADVISES,
					AJRelationshipManager.DECLARED_ON,
					AJRelationshipManager.ASPECT_DECLARATIONS,
					AJRelationshipManager.MATCHED_BY,
					AJRelationshipManager.MATCHES_DECLARE };
			compareElementsFromRelationships(rels, project);
		} finally {
			deleteProject(project);
		}
	}

	/**
	 * Test that going from an IJavaElement to its handle identifier then back
	 * to an IJavaElement using AspectJCore.create() results in a element that
	 * is equivalent to the original (not necessarily identical).
	 * 
	 * @throws Exception
	 */
	public void testHandleCreateRoundtrip5() throws Exception {
		IProject libProject = (IProject)getWorkspaceRoot().findMember("MyAspectLibrary");
		if (libProject==null) {
			libProject = createPredefinedProject("MyAspectLibrary");
		}
		IProject weaveMeProject = createPredefinedProject("WeaveMe");
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] { AJRelationshipManager.ADVISED_BY };
			compareElementsFromRelationships(rels, weaveMeProject);
		} finally {
			deleteProject(weaveMeProject);
			//deleteProject(libProject);
		}
	}

	/**
	 * Test that going from an IJavaElement to its handle identifier then back
	 * to an IJavaElement using AspectJCore.create() results in a element that
	 * is equivalent to the original (not necessarily identical).
	 * 
	 * @throws Exception
	 */
	public void testHandleCreateRoundtripBug94107() throws Exception {
		IProject project = createPredefinedProject("bug94107");
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] { AJRelationshipManager.ADVISED_BY };
			compareElementsFromRelationships(rels, project);
		} finally {
			deleteProject(project);
		}
	}
	
	private static String getSimpleClassName(Object obj) {
		String longName = obj.getClass().getName();
		int index = longName.lastIndexOf('.');
		if (index == -1) {
			return longName;
		}
		return longName.substring(index + 1);
	}

	static void compareElementsFromRelationships(AJRelationshipType[] rels,
			IProject project) {
		List allRels = AJModel.getInstance().getAllRelationships(project, rels);
		if (allRels.size() == 0) {
			// if the project or model didn't build properly we'd get no
			// relationships
			// and the test would blindly pass without this check
			fail("No relationships found for project " + project.getName());
		}
		for (Iterator iter = allRels.iterator(); iter.hasNext();) {
			AJRelationship rel = (AJRelationship) iter.next();
			compareElementWithRecreated(rel.getSource());
			compareElementWithRecreated(rel.getTarget());
		}
	}

	private static void compareElementWithRecreated(IJavaElement element) {
		String handle = element.getHandleIdentifier();
		IJavaElement recreated = AspectJCore.create(handle);
		String recreatedHandle = recreated.getHandleIdentifier();
		
		assertEquals(
				"Handle identifier of created element doesn't match original",
				handle, recreatedHandle);
		assertEquals("Name of created element doesn't match original", element
				.getElementName(), recreated.getElementName());
		IResource res = element.getResource();
		if (res != null) {
			// only do this test if the original has a valid resource
			assertEquals(
					"Name of created element resource doesn't match original",
					res.getName(), recreated.getResource().getName());
		}

		assertEquals("Name of created element doesn't match original",
				getSimpleClassName(element), getSimpleClassName(recreated));

		// test line number of AJCodeElements
		if ((element instanceof AJCodeElement)
				&& (recreated instanceof AJCodeElement)) {
			AJCodeElement sourceCodeEl = (AJCodeElement) element;
			AJCodeElement recreatedCodeEl = (AJCodeElement) recreated;
			assertEquals(
					"Line number of created AJCodeElement doesn't match original",
					sourceCodeEl.getLine(), recreatedCodeEl.getLine());
		}
	}

	private void compareWithHandles(String[][] testHandles) {
		for (int i = 0; i < testHandles.length; i++) {
			IJavaElement el = AspectJCore.create(testHandles[i][0]);
			assertEquals(
					"Handle identifier of created element doesn't match original",
					testHandles[i][0], el.getHandleIdentifier());
			assertEquals("Name of created element doesn't match expected",
					testHandles[i][1], el.getElementName());
			assertEquals(
					"Name of created element resource doesn't match expected",
					testHandles[i][2], el.getResource().getName());
			assertEquals("Created element is not of the expected class type",
					testHandles[i][3], getSimpleClassName(el));
		}
	}
}