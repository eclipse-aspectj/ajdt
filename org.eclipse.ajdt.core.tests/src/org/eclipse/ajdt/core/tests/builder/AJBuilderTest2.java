/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.builder;

import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.testutils.TestLogger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class AJBuilderTest2 extends AJDTCoreTestCase {


	/**
	 * Part of Bug 91420 - if the output folder has changed then 
	 * need to do a build
	 */
	public void testChangeInOutputDirCausesReBuild() throws Exception {
		TestLogger testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		IProject project = createPredefinedProject("bug91420"); //$NON-NLS-1$
		try {
			IJavaProject javaProject = JavaCore.create(project);
			IPath origOutput = javaProject.getOutputLocation();
			
			IPath newOutput = new Path("/bug91420/newBin"); //$NON-NLS-1$
			assertFalse("should be setting output dir to new place", //$NON-NLS-1$
					origOutput.toString().equals(newOutput.toString()));
			
			javaProject.setOutputLocation(newOutput,null);
			waitForAutoBuild();
			assertNotSame("should have set output directory to new place",origOutput,newOutput); //$NON-NLS-1$

			List log = testLog.getMostRecentEntries(3);
			// print log to the screen for test case development purposes
			testLog.printLog();
			
			assertTrue("output dir has changed so should have spent time in AJDE", ((String)log.get(0)).indexOf("Total time spent in AJDE") != -1); //$NON-NLS-1$ //$NON-NLS-2$  
			assertTrue("output dir has changed so should have created element map", ((String)log.get(1)).indexOf("Create element map") != -1);  //$NON-NLS-1$ //$NON-NLS-2$
			assertTrue("output dir has changed so should have spent time in AJBuilder.build()", ((String)log.get(2)).indexOf("Total time spent in AJBuilder.build()") != -1); //$NON-NLS-1$ //$NON-NLS-2$
			
			// reset the output dir back to its original setting
			javaProject.setOutputLocation(origOutput,null);
			waitForAutoBuild();
			assertEquals("should have reset the output directory",origOutput.toString(),javaProject.getOutputLocation().toString()); //$NON-NLS-1$
			
		} finally {
			AspectJPlugin.getDefault().setAJLogger(null);
			deleteProject(project);
		}
	}
	
	/**
	 * Part of Bug 91420 - if a library has been added to the classpath
	 * then we need to do a rebuild.
	 */
	public void testChangeInRequiredLibsCausesReBuild() throws Exception {
		TestLogger testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		IProject project = createPredefinedProject("bug91420"); //$NON-NLS-1$
		try {
			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] origClasspath = javaProject.getRawClasspath();

			// add a library to the classpath
			addLibraryToClasspath(project,"testJar.jar"); //$NON-NLS-1$
			waitForAutoBuild();
			// check it's been added to the classpath
			assertTrue("library should have been added to classpath",projectHasLibraryOnClasspath(javaProject,"testJar.jar")); //$NON-NLS-1$ //$NON-NLS-2$
			
			// check that a build has in fact occured
			List log = testLog.getMostRecentEntries(3);
			// print log to the screen for test case development purposes
			testLog.printLog();

			assertTrue("classpath has changed (new required library) so should have spent time in AJDE", ((String)log.get(0)).indexOf("Total time spent in AJDE") != -1);   //$NON-NLS-1$ //$NON-NLS-2$
			assertTrue("classpath has changed (new required library) so should have created element map", ((String)log.get(1)).indexOf("Create element map") != -1);  //$NON-NLS-1$ //$NON-NLS-2$
			assertTrue("classpath has changed (new required library) so should have spent time in AJBuilder.build()", ((String)log.get(2)).indexOf("Total time spent in AJBuilder.build()") != -1);  //$NON-NLS-1$ //$NON-NLS-2$

			// reset the changes
			javaProject.setRawClasspath(origClasspath,null);
			waitForAutoBuild();
			assertFalse("library should no longer be on the classpath",projectHasLibraryOnClasspath(javaProject,"testJar.jar")); //$NON-NLS-1$ //$NON-NLS-2$
			
		} finally {
			AspectJPlugin.getDefault().setAJLogger(null);
			deleteProject(project);
		}
	}
	
	/**
	 * Part of Bug 91420 - if there has been a change in required
	 * projects then need to to a rebuild
	 */
	public void testChangeInRequiredProjectsCausesReBuild() throws Exception {
		TestLogger testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		IProject project = createPredefinedProject("bug91420"); //$NON-NLS-1$
		IProject project2 = createPredefinedProject("bug101481"); //$NON-NLS-1$
		try {
			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] origClasspath = javaProject.getRawClasspath();
			
			addProjectDependency(project, project2);
			waitForAutoBuild();
			// check the dependency is there
			assertTrue("project bug91420 should have a project dependency on project bug101481",  //$NON-NLS-1$
					projectHasProjectDependency(javaProject, project2));
			
			List log = testLog.getMostRecentEntries(3);
			// print log to the screen for test case development purposes
			testLog.printLog();

			assertTrue("classpath has changed (new project dependency) so should have spent time in AJDE", ((String)log.get(0)).indexOf("Total time spent in AJDE") != -1); //$NON-NLS-1$ //$NON-NLS-2$  
			assertTrue("classpath has changed (new project dependency) so should have created element map", ((String)log.get(1)).indexOf("Create element map") != -1);  //$NON-NLS-1$ //$NON-NLS-2$
			assertTrue("classpath has changed (new project dependency) so should have spent time in AJBuilder.build()", ((String)log.get(2)).indexOf("Total time spent in AJBuilder.build()") != -1); //$NON-NLS-1$ //$NON-NLS-2$ 
		
			// reset the changes
			javaProject.setRawClasspath(origClasspath,null);
			waitForAutoBuild();
			assertFalse("project dependencies should have been removed", //$NON-NLS-1$
					projectHasProjectDependency(javaProject, project2));
			
		} finally {
			AspectJPlugin.getDefault().setAJLogger(null);
			deleteProject(project);
		}
	}

	private void addLibraryToClasspath(IProject project, String libName) throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);
		
		IClasspathEntry[] originalCP = javaProject.getRawClasspath();
		IClasspathEntry ajrtLIB = JavaCore.newLibraryEntry(project.getLocation()
				.append(libName).makeAbsolute(), // library location
				null, // no source
				null // no source
				);
		int originalCPLength = originalCP.length;
		IClasspathEntry[] newCP = new IClasspathEntry[originalCPLength + 1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCPLength);
		newCP[originalCPLength] = ajrtLIB;
		javaProject.setRawClasspath(newCP, null);
	}
	
	private boolean projectHasLibraryOnClasspath(IJavaProject proj, String libName) throws JavaModelException {
		IClasspathEntry[] entries = proj.getRawClasspath();
		IPath libPath = proj.getProject().getLocation().append(libName);
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				if (entry.getPath().equals(libPath)
						|| entry.getPath().equals(libPath.makeAbsolute())) {
					return true;
				}			
			}
		}
		return false;
	}

	private void addProjectDependency(IProject project, IProject projectDependedOn) throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] originalCP = javaProject.getRawClasspath();
		IClasspathEntry newEntry = JavaCore.newProjectEntry(projectDependedOn.getFullPath());
		int originalCPLength = originalCP.length;
		IClasspathEntry[] newCP = new IClasspathEntry[originalCPLength + 1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCPLength);
		newCP[originalCPLength] = newEntry;
		javaProject.setRawClasspath(newCP, null);
	}
	
	private boolean projectHasProjectDependency(IJavaProject proj, IProject projectDependedOn) throws JavaModelException {
		IClasspathEntry[] entries = proj.getRawClasspath();
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				if (entry.getPath().equals(projectDependedOn.getFullPath())
						|| entry.getPath().equals(projectDependedOn.getFullPath().makeAbsolute())) {
					return true;
				}			
			}
		}
		return false;
	}
	
}
