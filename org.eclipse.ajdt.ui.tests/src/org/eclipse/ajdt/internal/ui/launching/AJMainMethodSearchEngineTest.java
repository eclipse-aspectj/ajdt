package org.eclipse.ajdt.internal.ui.launching;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.ajdt.buildconfigurator.BuildConfiguration;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.launching.AJMainMethodSearchEngine;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.jdt.internal.core.util.Util;

import junit.framework.TestCase;

public class AJMainMethodSearchEngineTest extends TestCase {

	
	
	public void testWithTracingExample() throws Exception {
		// This is a workaround to avoid an exception being thrown on Eclipse 3.1,
		// but does not affect the test.
		Field field = null;
		try {
			field = Util.class.getField("JAVA_LIKE_EXTENSIONS");
		} catch (NoSuchFieldException nsfe) {
			// do nothing - we are on eclipse 3.0
		}
		if(field != null) {
			char[][] extensions = new char[4][];
			extensions[0] = new char[] {'.', 'j', 'a', 'v', 'a'};
			extensions[1] = new char[] {'.', 'J', 'A', 'V', 'A'};
			extensions[2] = new char[] {'.', 'a', 'j'};
			extensions[3] = new char[] {'.', 'A', 'J'};
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
		JavaSearchScope scope = new JavaSearchScope();
		scope.add(jp);
		Object[] results = searchEngine.searchMainMethodsIncludingAspects(new NullProgressMonitor(), scope, IJavaSearchScope.SOURCES, true);
		assertTrue("There should be one result", results.length == 1);
		pbc.setActiveBuildConfiguration(tracev1);
		Utils.waitForJobsToComplete();
		Object[] results2 = searchEngine.searchMainMethodsIncludingAspects(new NullProgressMonitor(), scope, IJavaSearchScope.SOURCES, true);
		assertTrue("There should be two results", results2.length == 2);
		Utils.deleteProject(project);
		if(field != null) {
			char[][] extensions = new char[2][];
			extensions[0] = new char[] {'.', 'j', 'a', 'v', 'a'};
			extensions[1] = new char[] {'.', 'J', 'A', 'V', 'A'};
			field.set(null, extensions);
		}
	}
	
}

