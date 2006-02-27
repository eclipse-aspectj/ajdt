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
import org.eclipse.core.resources.IncrementalProjectBuilder;

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
			List[] results = ModelComparison.compare(mapModel, currentModel);
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
			assertTrue("Missing added relationship: Expected afterReturning advises fire()",found); //$NON-NLS-1$
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
			results = ModelComparison.compare(mapModel, currentModel);
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

}