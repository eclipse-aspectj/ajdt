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
import org.eclipse.ajdt.core.model.AJProjectModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.ModelComparison;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

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
		IProject project = createPredefinedProject("Spacewar Example");
		try {
			IResource ajmap = project.findMember("Spacewar Example.ajmap");
			assertNotNull("Couldn't find ajmap file", ajmap);

			IResource debug = project.findMember("src/spacewar/Debug.aj");
			assertNotNull("Couldn't find Debug.aj file", debug);

			AJProjectModel currentModel = AJModel.getInstance()
					.getModelForProject(project);
			assertNotNull("Project model should not be null", currentModel);

			// load model from .ajmap file
			AJProjectModel mapModel = new AJProjectModel(project);
			mapModel.loadModel(ajmap.getLocation());
			assertNotNull("Loaded model should not be null", mapModel);

			// compare the two models: there should be added relationships
			List[] results = ModelComparison.compare(mapModel, currentModel);
			List added = results[0];
			if ((added == null) || (added.size() == 0)) {
				fail("List of added relationships should not be empty or null");
			}
			boolean found = false;
			for (Iterator iter = added.iterator(); !found && iter.hasNext();) {
				AJRelationship rel = (AJRelationship) iter.next();
				if (rel.getRelationship() == AJRelationshipManager.ADVISES) {
					if (currentModel.getJavaElementLinkName(rel.getSource())
							.indexOf("afterReturning") != -1) {
						if (currentModel
								.getJavaElementLinkName(rel.getTarget())
								.indexOf("fire()") != -1) {
							found = true;
						}
					}
				}
			}
			assertTrue("Missing added relationship: Expected afterReturning advises fire()",found);
			List removed = results[1];
			if ((removed != null) && (removed.size() > 0)) {
				fail("List of removed relationships should be empty or null. Found "
						+ removed.size() + " relationships");
			}

			// now delete Debug.aj file (should just exclude it from the build
			// config, but that currently requires the UI plugin)
			debug.delete(true, null);
			waitForAutoBuild();
			currentModel = AJModel.getInstance().getModelForProject(project);
			assertNotNull("Project model should not be null", currentModel);

			// compare the two models: they should now be the same
			results = ModelComparison.compare(mapModel, currentModel);
			added = results[0];
			if ((added != null) && (added.size() > 0)) {
				fail("List of added relationships should be empty or null. Found "
						+ added.size() + " relationships");
			}
			removed = results[1];
			if ((removed != null) && (removed.size() > 0)) {
				fail("List of removed relationships should be empty or null. Found "
						+ removed.size() + " relationships");
			}

		} finally {
			deleteProject(project);
		}
	}

}