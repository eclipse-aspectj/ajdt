/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ben Dalziel     - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.buildconfig;

import java.io.File;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.ui.tests.AllUITests;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;

public class ResourceListenerTest extends UITestCase {
	
	private static final String MODEL_FILE = ".elementMap"; //$NON-NLS-1$
	private static final String LST_FILE = ".generated.lst"; //$NON-NLS-1$

	public void testBug101214() throws Exception {
		AllUITests.setupAJDTPlugin();
		IProject project = createPredefinedProject("Simple AJ Project"); //$NON-NLS-1$
		File lstFile = new File(getFileName(project, LST_FILE));
		assertTrue("LST file has not been created when project created", lstFile.exists()); //$NON-NLS-1$
		
		AJModel.getInstance().saveModel(project);
		File modelFile = new File(getFileName(project, MODEL_FILE));
		assertTrue("File has not been saved", modelFile.exists()); //$NON-NLS-1$

		deleteProject(project);
		
		waitForJobsToComplete();
					
		// Check that the files have been deleted
		assertFalse("LST File has not been deleted", lstFile.exists()); // Fix not implemented //$NON-NLS-1$
		assertFalse("Model File has not been deleted", modelFile.exists()); //$NON-NLS-1$
	}
	
	private String getFileName(IProject project, String FILE) {
		return AspectJPlugin.getDefault().getStateLocation().append(
				project.getName() + FILE).toOSString();
	}
}
