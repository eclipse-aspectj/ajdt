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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.launching.AJMainMethodSearchEngine;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.core.util.Util;

public class AJMainMethodSearchEngineTest extends TestCase {

	
	
	public void testWithTracingExample() throws Exception {
		// This is a workaround to avoid an exception being thrown on Eclipse 3.1,
		// but does not affect the test.
		Field field = null;
		try {
			field = Util.class.getDeclaredField("JAVA_LIKE_EXTENSIONS");
		} catch (NoSuchFieldException nsfe) {
			System.out.println("no such field..");
			// do nothing - we are on eclipse 3.0
		}
		if(field != null) {
			char[][] extensions = new char[2][];
			extensions[0] = new char[] {'.', 'j', 'a', 'v', 'a'};
			extensions[1] = new char[] {'.', 'a', 'j'};
			field.setAccessible(true);
			field.set(null, extensions);
		}
		
		IProject project = Utils.createPredefinedProject("Tracing Example");
		Utils.waitForJobsToComplete();
		IJavaProject jp = JavaCore.create(project);
		ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator().getProjectBuildConfigurator(jp);
		Collection bcs = pbc.getBuildConfigurations();
		BuildConfiguration notrace = null;
		BuildConfiguration tracev1 = null;
		for (Iterator iter = bcs.iterator(); iter.hasNext();) {
			BuildConfiguration bc = (BuildConfiguration) iter.next();
			if(bc.getName().equals("notrace")) {
				notrace = bc;
			} else if(bc.getName().equals("tracev1")) {
				tracev1 = bc;
			}
		}
		assertNotNull("Build configuration notrace should have been found", notrace);
		assertNotNull("Build configuration tracev1 should have been found", tracev1);
		pbc.setActiveBuildConfiguration(notrace);
		Utils.waitForJobsToComplete();
		AJMainMethodSearchEngine searchEngine = new AJMainMethodSearchEngine();
		IJavaElement[] elements = new IJavaElement[]{jp};
	
		int constraints = IJavaSearchScope.SOURCES;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elements, constraints);
		Object[] results = searchEngine.searchMainMethodsIncludingAspects(new NullProgressMonitor(), scope, true);
		assertTrue("There should be one result, found " + results.length, results.length == 1);
		pbc.setActiveBuildConfiguration(tracev1);
		Utils.waitForJobsToComplete();
		Object[] results2 = searchEngine.searchMainMethodsIncludingAspects(new NullProgressMonitor(), scope, true);
		assertTrue("There should be two results, found " + results2.length, results2.length == 2);
		Utils.deleteProject(project);
		if(field != null) {
			char[][] extensions = new char[2][];
			extensions[0] = new char[] {'.', 'j', 'a', 'v', 'a'};
			extensions[1] = new char[] {'.', 'J', 'A', 'V', 'A'};
			field.set(null, extensions);
		}
	}
	
}

