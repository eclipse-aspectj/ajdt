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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.DeclareElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJProjectModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.ModelComparison;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

/**
 * Tests for comparing two project models
 */
public class ModelComparisonTest extends AJDTCoreTestCase {

	/**
	 * The .ajmap file in the test project contains the relationships in
	 * Spacewar excluding those from the Debug aspect, which is included by
	 * default. So the first comparison between the map file and the current
	 * build should find some added relationships, then we remove the Debug
	 * aspect, after which there should be no differences.
	 * 
	 * @throws Exception
	 */
	public void testCompareWithCurrent() throws Exception {
		IProject project = createPredefinedProject("Spacewar Example"); //$NON-NLS-1$
		try {
			IResource ajmap = project.findMember("Spacewar Example.ajmap"); //$NON-NLS-1$
			assertNotNull("Couldn't find ajmap file", ajmap); //$NON-NLS-1$

			IResource debug = project.findMember("src/spacewar/Debug.aj"); //$NON-NLS-1$
			assertNotNull("Couldn't find Debug.aj file", debug); //$NON-NLS-1$

			AJProjectModel currentModel = AJModel.getInstance()
					.getModelForProject(project);
			assertNotNull("Project model should not be null", currentModel); //$NON-NLS-1$

			// load model from .ajmap file
			AJProjectModel mapModel = new AJProjectModel(project);
			mapModel.loadModel(ajmap.getLocation());
			assertNotNull("Loaded model should not be null", mapModel); //$NON-NLS-1$

			// compare the two models: there should be added relationships
			List[] results = new ModelComparison(false).compareProjects(
					mapModel, currentModel);
			List added = results[0];
			if ((added == null) || (added.size() == 0)) {
				fail("List of added relationships should not be empty or null"); //$NON-NLS-1$
			}
			boolean found = false;
			for (Iterator iter = added.iterator(); !found && iter.hasNext();) {
				AJRelationship rel = (AJRelationship) iter.next();
				if (rel.getRelationship() == AJRelationshipManager.ADVISES) {
					if (currentModel.getJavaElementLinkName(rel.getSource())
							.indexOf("afterReturning") != -1) { //$NON-NLS-1$
						if (currentModel
								.getJavaElementLinkName(rel.getTarget())
								.indexOf("fire()") != -1) { //$NON-NLS-1$
							found = true;
						}
					}
				}
			}
			assertTrue(
					"Missing added relationship: Expected afterReturning advises fire()", found); //$NON-NLS-1$
			List removed = results[1];
			if ((removed != null) && (removed.size() > 0)) {
				fail("List of removed relationships should be empty or null. Found " //$NON-NLS-1$
						+ removed.size() + " relationships"); //$NON-NLS-1$
			}

			// now delete Debug.aj file (should just exclude it from the build
			// config, but that currently requires the UI plugin)
			debug.delete(true, null);
			waitForAutoBuild();
			project.build(IncrementalProjectBuilder.FULL_BUILD, null);
			currentModel = AJModel.getInstance().getModelForProject(project);
			assertNotNull("Project model should not be null", currentModel); //$NON-NLS-1$

			// compare the two models: they should now be the same
			results = new ModelComparison(false).compareProjects(mapModel,
					currentModel);
			added = results[0];
			if ((added != null) && (added.size() > 0)) {
				fail("List of added relationships should be empty or null. Found " //$NON-NLS-1$
						+ added.size() + " relationships"); //$NON-NLS-1$
			}
			removed = results[1];
			if ((removed != null) && (removed.size() > 0)) {
				fail("List of removed relationships should be empty or null. Found " //$NON-NLS-1$
						+ removed.size() + " relationships"); //$NON-NLS-1$
			}

		} finally {
			deleteProject(project);
		}
	}

	public void testCompareElements() throws Exception {
		IProject project = createPredefinedProject("Comparisons"); //$NON-NLS-1$
		try {
			IResource aspectFile = project.findMember("src/foo/MyAspect.aj"); //$NON-NLS-1$
			assertNotNull("Couldn't find MyAspect.aj file", aspectFile); //$NON-NLS-1$
			AJCompilationUnit ajcu = AJCompilationUnitManager.INSTANCE
					.getAJCompilationUnit((IFile) aspectFile);
			assertNotNull("Couldn't find AJCompilationUnit for file " //$NON-NLS-1$
					+ aspectFile, ajcu);
			IType[] types = ajcu.getAllTypes();
			assertTrue(
					"First contained type should be an AspectElement", types[0] instanceof AspectElement); //$NON-NLS-1$
			AspectElement aspect = (AspectElement) types[0];
			AdviceElement[] advice = aspect.getAdvice();
			assertEquals("Aspect should contain 2 advice elements", 2, //$NON-NLS-1$
					advice.length);
			IJavaElement before, after;
			if (advice[0].getElementName().indexOf("before") != -1) { //$NON-NLS-1$
				before = advice[0];
				after = advice[1];
			} else {
				before = advice[1];
				after = advice[0];
			}
			
			DeclareElement[] declares = aspect.getDeclares();
			assertEquals("Aspect should contain 1 declare element", 1, //$NON-NLS-1$
					declares.length);
			DeclareElement declare = declares[0];
			
			AJProjectModel model = AJModel.getInstance().getModelForProject(
					project);
			
			// compare after with before, should be 1 added
			List[] res = new ModelComparison(false).compareElements(model,
					model, after, before);
			List addedList = filterAdvisesMatchedByRels(res[0]);
			List removedList = filterAdvisesMatchedByRels(res[1]);
			assertEquals("AddedList should contain 1 element", 1, addedList.size()); //$NON-NLS-1$
			assertEquals("RemovedList should be empty", 0, removedList.size()); //$NON-NLS-1$
			AJRelationship rel = (AJRelationship)addedList.get(0);
			assertEquals("Wrong name for added source element","before",rel.getSource().getElementName()); //$NON-NLS-1$ //$NON-NLS-2$
			assertEquals("Wrong name for added target element","setup",rel.getTarget().getElementName()); //$NON-NLS-1$ //$NON-NLS-2$
			
			// compare before with after, should be 1 removed
			res = new ModelComparison(true).compareElements(model, model,
					before, after);
			addedList = filterAdvisesMatchedByRels(res[0]);
			removedList = filterAdvisesMatchedByRels(res[1]);			
			assertEquals("AddedList should be empty", 0, addedList.size()); //$NON-NLS-1$
			assertEquals("RemovedList should contain 1 element", 1, removedList.size()); //$NON-NLS-1$
			rel = (AJRelationship)removedList.get(0);
			assertEquals("Wrong name for removed source element","before",rel.getSource().getElementName());  //$NON-NLS-1$//$NON-NLS-2$
			assertEquals("Wrong name for removed target element","setup",rel.getTarget().getElementName()); //$NON-NLS-1$ //$NON-NLS-2$

			// compare declare with after, should be 2 added, 2 removed
			res = new ModelComparison(false).compareElements(model, model,
					declare, after);
			addedList = filterAdvisesMatchedByRels(res[0]);
			removedList = filterAdvisesMatchedByRels(res[1]);			
			assertEquals("AddedList should contain 2 elements", 2, addedList.size()); //$NON-NLS-1$
			assertEquals("RemovedList should contain 2 elements", 2, removedList.size()); //$NON-NLS-1$
			
			// propagate up this time, should be no changes
			res = new ModelComparison(true).compareElements(model, model,
					declare, after);
			addedList = filterAdvisesMatchedByRels(res[0]);
			removedList = filterAdvisesMatchedByRels(res[1]);			
			assertEquals("AddedList should be empty", 0, addedList.size()); //$NON-NLS-1$
			assertEquals("RemovedList should be empty", 0, removedList.size()); //$NON-NLS-1$
			
			// compare declare with before, should be 1 added
			res = new ModelComparison(true).compareElements(model, model,
					declare, before);
			addedList = filterAdvisesMatchedByRels(res[0]);
			removedList = filterAdvisesMatchedByRels(res[1]);			
			assertEquals("AddedList should contain 1 element", 1, addedList.size()); //$NON-NLS-1$
			assertEquals("RemovedList should be empty", 0, removedList.size()); //$NON-NLS-1$
			rel = (AJRelationship)addedList.get(0);
			assertEquals("Wrong name for added source element","before",rel.getSource().getElementName()); //$NON-NLS-1$ //$NON-NLS-2$
			assertEquals("Wrong name for added target element","setup",rel.getTarget().getElementName()); //$NON-NLS-1$ //$NON-NLS-2$
		} finally {
			deleteProject(project);
		}

	}
	
	private List filterAdvisesMatchedByRels(List relationshipList) {
		List filteredList = new ArrayList();
		for (Iterator iter = relationshipList.iterator(); iter.hasNext();) {
			AJRelationship element = (AJRelationship) iter.next();
			if ((element.getRelationship() == AJRelationshipManager.ADVISES)
					 || (element.getRelationship() == AJRelationshipManager.MATCHED_BY)) {
				filteredList.add(element);
			}
		}
		return filteredList;
	}
}