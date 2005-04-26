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
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.internal.EclipseVersion;
import org.eclipse.core.resources.IProject;
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

		// each entry in the array contains:
		// <handle> <name of element> <containing resource> <class name of
		// element>
		// note that the elements referred to by the handles need to exist in
		// the workspace
		String[][] testHandles = {
				{ "=TJP Example/src<tjp{Demo.java", "Demo.java", "Demo.java",
						"CompilationUnit" },
				{ "=TJP Example/src<tjp{Demo.java[Demo~main", "main",
						"Demo.java", "SourceMethod" },
				{ "=TJP Example/src<tjp{GetInfo.aj", "GetInfo.aj",
						"GetInfo.aj", "AJCompilationUnit" },
				{ "=TJP Example/src<tjp{GetInfo.aj}GetInfo", "GetInfo",
						"GetInfo.aj", "AspectElement" },
				{ "=TJP Example/src<tjp{GetInfo.aj}GetInfo~println", "println",
						"GetInfo.aj", "SourceMethod" },
				{ "=TJP Example/src<tjp{GetInfo.aj}GetInfo&around", "around",
						"GetInfo.aj", "AdviceElement" } };
		compareWithHandles(testHandles);

		deleteProject(project);
	}

	/**
	 * Test that AspectJCore.create() can form appropriate Java elements from a
	 * variety of handle identifiers
	 * 
	 * @throws Exception
	 */
	public void testCreateElementFromHandle2() throws Exception {
		IProject project = createPredefinedProject("Bean Example");

		String methodHandle = "=Bean Example/src<bean{Demo.java[Demo~main~\\[QString;?method-call(void bean.Point.setX(int))!37!0!0!0!I";
		if ((EclipseVersion.MAJOR_VERSION == 3)
				&& (EclipseVersion.MINOR_VERSION == 0)) {
			// the handle identifiers for method signatures changed after
			// eclipes 3.0: note the lack of escape character ~[QString; instead
			// of ~\[QString;
			methodHandle = "=Bean Example/src<bean{Demo.java[Demo~main~[QString;?method-call(void bean.Point.setX(int))!37!0!0!0!I";
		}

		// each entry in the array contains:
		// <handle> <name of element> <containing resource> <class name of
		// element>
		// note that the elements referred to by the handles need to exist in
		// the workspace
		String[][] testHandles = {
				{ methodHandle, "method-call(void bean.Point.setX(int))",
						"Demo.java", "AJCodeElement" },
				{
						"=Bean Example/src<bean{BoundPoint.aj}BoundPoint&around&QPoint;",
						"around", "BoundPoint.aj", "AdviceElement" },
				{
						"=Bean Example/src<bean{BoundPoint.aj}BoundPoint¬Point.hasListeners¬QString;",
						"Point.hasListeners", "BoundPoint.aj", "IntertypeElement" },
				{
						"=Bean Example/src<bean{BoundPoint.aj}BoundPoint`declare parents",
						"declare parents", "BoundPoint.aj", "DeclareElement" }

		};
		compareWithHandles(testHandles);

		deleteProject(project);
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

		AJRelationshipType[] rels = new AJRelationshipType[] {
				AJRelationshipManager.ADVISED_BY, AJRelationshipManager.ADVISES };
		compareElementsFromRelationships(rels, project);

		deleteProject(project);
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

		AJRelationshipType[] rels = new AJRelationshipType[] {
				AJRelationshipManager.ADVISED_BY,
				AJRelationshipManager.ADVISES,
				AJRelationshipManager.DECLARED_ON,
				AJRelationshipManager.ASPECT_DECLARATIONS };
		compareElementsFromRelationships(rels, project);

		deleteProject(project);
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

		AJRelationshipType[] rels = new AJRelationshipType[] {
				AJRelationshipManager.ADVISED_BY,
				AJRelationshipManager.ADVISES,
				AJRelationshipManager.DECLARED_ON,
				AJRelationshipManager.ASPECT_DECLARATIONS,
				AJRelationshipManager.MATCHED_BY,
				AJRelationshipManager.MATCHES_DECLARE };
		compareElementsFromRelationships(rels, project);

		deleteProject(project);
	}

	private String getSimpleClassName(Object obj) {
		String longName = obj.getClass().getName();
		int index = longName.lastIndexOf('.');
		if (index == -1) {
			return longName;
		}
		return longName.substring(index + 1);
	}

	private void compareElementsFromRelationships(AJRelationshipType[] rels,
			IProject project) {
		List allRels = AJModel.getInstance().getAllRelationships(project, rels);
		if (allRels.size() == 0) {
			// if the project or model didn't build properly we'd get no relationships
			// and the test would blindly pass without this check
			fail("No relationships found for project "+project.getName());
		}
		for (Iterator iter = allRels.iterator(); iter.hasNext();) {
			AJRelationship rel = (AJRelationship) iter.next();
			IJavaElement source = rel.getSource();
			//System.out.println("source: " + source);
			String sourceHandle = source.getHandleIdentifier();
			//System.out.println("source handle: " + sourceHandle);
			IJavaElement recreated = AspectJCore.create(sourceHandle);
			//System.out.println("recreated: " + recreated);
			String recreatedHandle = recreated.getHandleIdentifier();
			//System.out.println("recreated handle: " + recreatedHandle);

			assertEquals(
					"Handle identifier of created element doesn't match original",
					sourceHandle, recreatedHandle);
			assertEquals("Name of created element doesn't match original",
					source.getElementName(), recreated.getElementName());
			assertEquals(
					"Name of created element resource doesn't match original",
					source.getResource().getName(), recreated.getResource()
							.getName());
			assertEquals("Name of created element doesn't match original",
					getSimpleClassName(source), getSimpleClassName(recreated));

			// test line number of AJCodeElements
			if ((source instanceof AJCodeElement)
					&& (recreated instanceof AJCodeElement)) {
				AJCodeElement sourceCodeEl = (AJCodeElement) source;
				AJCodeElement recreatedCodeEl = (AJCodeElement) recreated;
				assertEquals(
						"Line number of created AJCodeElement doesn't match original",
						sourceCodeEl.getLine(), recreatedCodeEl.getLine());
			}
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