/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/
package org.eclipse.ajdt.internal.builder;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.ajdt.buildconfigurator.BuildConfiguration;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurator;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 *  Test for bug 75373: problem-markers created by other builders on java-resources disappear
 */
public class ProblemMarkerTest extends TestCase {

	private IProject myProject;

	protected void setUp() throws Exception {
		super.setUp();
		myProject = Utils.getPredefinedProject("Simple AJ Project", true);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		Utils.deleteProject(myProject);
	}
	
	public void testMarkerIsNotRemoved() throws CoreException {
		IFile f = myProject.getFile("src/p1/Main.java");
		IMarker marker = f.createMarker(IMarker.PROBLEM);
		marker.setAttribute(IMarker.MESSAGE, "hello");
		myProject.build(IncrementalProjectBuilder.FULL_BUILD,
				new NullProgressMonitor());
		Utils.waitForJobsToComplete();
		assertTrue("Marker we created should still exist after a build", marker
				.exists());
	}
	
	/**
	 * Problem markers should only be added when the file
	 * is included in the active build configuration
	 * 
	 * @throws Exception
	 */
	public void testExludedFromBuildConfig() throws Exception {
		IFolder src = myProject.getFolder("src");
		if (!src.exists()){
			src.create(true, true, null);
		}
		IFolder p1 = src.getFolder("p1");
		if (!p1.exists()){
			p1.create(true, true, null);
		}
		assertNotNull("src folder should not be null", src);
		assertNotNull("package p1 should not be null", p1);
		 
		IFile newAspect = createFile(p1,"newAspect.aj","blah blah blah");
		assertNotNull("newAspect should not be null", newAspect);
		
		ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator().getProjectBuildConfigurator(myProject);
		BuildConfiguration buildConfig = pbc.getActiveBuildConfiguration();

		List newFiles = new ArrayList();
		newFiles.add(newAspect);
		
		buildConfig.excludeFiles(newFiles);
		Utils.waitForJobsToComplete();
		
		assertFalse("new aspect shouldn't be included in active build configuration",
				buildConfig.isIncluded(newAspect));
		
		myProject.build(IncrementalProjectBuilder.FULL_BUILD,new NullProgressMonitor());
		Utils.waitForJobsToComplete();
		assertFalse("new aspect shouldn't be included in active build configuration",
				buildConfig.isIncluded(newAspect));
		
		assertFalse("no problem markers should be against the project since the file isn't included in the build config", 
				ProjectDependenciesUtils.projectIsMarkedWithError(myProject,null));
		
		buildConfig.includeFiles(newFiles);

		myProject.build(IncrementalProjectBuilder.FULL_BUILD,new NullProgressMonitor());
		Utils.waitForJobsToComplete();
		assertTrue("problem markers should be against the project since file is included in the build config", 
				ProjectDependenciesUtils.projectIsMarkedWithError(myProject,null));

		
	}
	

	public IFile createFile(IFolder inFolder,String name, String content) throws CoreException {
		IFile file = inFolder.getFile(name);
		if (file.exists()) {
			file.delete(0,null);
		}
		ByteArrayInputStream source = new ByteArrayInputStream(content.getBytes());
		file.create(source,true,null);		
		return file;
	}
	
}