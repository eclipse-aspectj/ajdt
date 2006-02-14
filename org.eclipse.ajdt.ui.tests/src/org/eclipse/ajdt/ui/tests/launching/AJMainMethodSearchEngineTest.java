/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/
package org.eclipse.ajdt.ui.tests.launching;

import org.eclipse.ajdt.internal.launching.AJMainMethodSearchEngine;
import org.eclipse.ajdt.ui.buildpath.BuildConfigurationUtils;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;

public class AJMainMethodSearchEngineTest extends UITestCase {

	
	
	public void testWithTracingExample() throws Exception {
		IProject project = createPredefinedProject("Tracing Example"); //$NON-NLS-1$
		waitForJobsToComplete();
		IJavaProject jp = JavaCore.create(project);
		IFile propertiesFile = project.getFile("notrace.ajproperties"); //$NON-NLS-1$
		assertNotNull(propertiesFile);
		assertTrue(propertiesFile.exists());		
		BuildConfigurationUtils.applyBuildConfiguration(propertiesFile);
		waitForJobsToComplete();
		AJMainMethodSearchEngine searchEngine = new AJMainMethodSearchEngine();
		IJavaElement[] elements = new IJavaElement[]{jp};
	
		int constraints = IJavaSearchScope.SOURCES;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elements, constraints);
		Object[] results = searchEngine.searchMainMethodsIncludingAspects(new NullProgressMonitor(), scope, true);
		assertTrue("There should be one result, found " + results.length, results.length == 1); //$NON-NLS-1$
		propertiesFile = project.getFile("tracev1.ajproperties"); //$NON-NLS-1$
		assertNotNull(propertiesFile);
		assertTrue(propertiesFile.exists());		
		BuildConfigurationUtils.applyBuildConfiguration(propertiesFile);
		waitForJobsToComplete();
		waitForJobsToComplete();
		Object[] results2 = searchEngine.searchMainMethodsIncludingAspects(new NullProgressMonitor(), scope, true);
		assertTrue("There should be two results, found " + results2.length, results2.length == 2); //$NON-NLS-1$
	}
	
}

