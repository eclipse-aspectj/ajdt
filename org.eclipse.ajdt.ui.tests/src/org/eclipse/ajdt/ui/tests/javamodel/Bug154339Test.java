/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Ford  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.javamodel;

import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

public class Bug154339Test extends UITestCase {

	public void testWhenProjectIsDeleted() throws CoreException {
		IProject project = createPredefinedProject("Simple Project"); //$NON-NLS-1$
		waitForJobsToComplete();

		Object object = IncrementalStateManager.retrieveStateFor(AspectJPlugin
				.getBuildConfigurationFile(project));
		assertNotNull(
				"IncrementalStateManager didn't find state for project", object); //$NON-NLS-1$
		deleteProject(project);
		object = IncrementalStateManager.retrieveStateFor(AspectJPlugin
				.getBuildConfigurationFile(project));
		waitForJobsToComplete();
		assertNull(
				"IncrementalStateManager should not find state for a deleted project", object); //$NON-NLS-1$
	}

	public void testWhenProjectIsClosed() throws CoreException {
		IProject project = createPredefinedProject("Simple Project"); //$NON-NLS-1$
		waitForJobsToComplete();

		Object object = IncrementalStateManager.retrieveStateFor(AspectJPlugin
				.getBuildConfigurationFile(project));
		assertNotNull(
				"IncrementalStateManager didn't find state for project", object); //$NON-NLS-1$
		project.close(null);
		waitForJobsToComplete();
		object = IncrementalStateManager.retrieveStateFor(AspectJPlugin
				.getBuildConfigurationFile(project));
		assertNull(
				"IncrementalStateManager should not find state for a closed project", object); //$NON-NLS-1$
		project.open(null);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobsToComplete();
		object = IncrementalStateManager.retrieveStateFor(AspectJPlugin
				.getBuildConfigurationFile(project));
		assertNotNull(
				"IncrementalStateManager didn't find state after opening and building a closed project", object); //$NON-NLS-1$		
	}
	
	public void testWhenProjectIsConverted() throws CoreException {
		IProject project = createPredefinedProject("Simple AJ Project"); //$NON-NLS-1$
		waitForJobsToComplete();
		
		Object object = IncrementalStateManager.retrieveStateFor(AspectJPlugin
				.getBuildConfigurationFile(project));
		assertNotNull(
				"IncrementalStateManager didn't find state for project", object); //$NON-NLS-1$
		
		AspectJUIPlugin.convertFromAspectJProject(project);
		waitForJobsToComplete();
		object = IncrementalStateManager.retrieveStateFor(AspectJPlugin
				.getBuildConfigurationFile(project));
		assertNull(
				"IncrementalStateManager should not find state for project", object); //$NON-NLS-1$
		
		AspectJUIPlugin.convertToAspectJProject(project);
		waitForJobsToComplete();
		object = IncrementalStateManager.retrieveStateFor(AspectJPlugin
				.getBuildConfigurationFile(project));
		assertNotNull(
				"IncrementalStateManager didn't find state for project", object); //$NON-NLS-1$
	}

}
