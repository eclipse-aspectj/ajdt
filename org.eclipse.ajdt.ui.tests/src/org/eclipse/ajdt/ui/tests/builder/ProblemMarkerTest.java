/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import java.io.ByteArrayInputStream;

import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;


public class ProblemMarkerTest extends UITestCase {

	/**
	 *  Test for bug 75373: problem-markers created by other builders on java-resources disappear
	 */
	public void testMarkerIsNotRemoved() throws CoreException {
		IProject myProject = createPredefinedProject("Simple AJ Project"); //$NON-NLS-1$
		IFile f = myProject.getFile("src/p1/Main.java"); //$NON-NLS-1$
		IMarker marker = f.createMarker(IMarker.PROBLEM);
		marker.setAttribute(IMarker.MESSAGE, "hello"); //$NON-NLS-1$
		myProject.build(IncrementalProjectBuilder.FULL_BUILD,
				new NullProgressMonitor());
		waitForJobsToComplete();
		assertTrue("Marker we created should still exist after a build", marker //$NON-NLS-1$
				.exists());
	}

	// this test fails because we are no longer placing error markers on in path projects
	/**
	 * An incremental build should not grow the list of problem markers against a Java project
	 * @throws CoreException
	 */
//	public void testBug111764() throws CoreException {
//		createPredefinedProject("InpathUsingAJProject"); //$NON-NLS-1$
//		IProject javaProject = createPredefinedProject("project.java.Y"); //$NON-NLS-1$
//		IFile f = javaProject.getFile("src/internal/stuff/MyBuilder.java"); //$NON-NLS-1$
//		IMarker[] markers = f.findMarkers(IAJModelMarker.AJDT_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
//		assertEquals("Should be 1 ADJT problem marker again file MyBuilder.java",1,markers.length); //$NON-NLS-1$
//
//		StringReader sr = new StringReader("/* blah blah blah */"); //$NON-NLS-1$
//		f.appendContents(new ReaderInputStream(sr), IResource.FORCE, null);
//
//		waitForJobsToComplete();
//
//		markers = f.findMarkers(IAJModelMarker.AJDT_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
//		assertEquals("Should still be only 1 ADJT problem marker again file MyBuilder.java",1,markers.length); //$NON-NLS-1$
//	}
	
// TODO: Do this another way with new build configs	
//	/**
//	 * Problem markers should only be added when the file
//	 * is included in the active build configuration
//	 * 
//	 * @throws Exception
//	 */
//	public void testExludedFromBuildConfig() throws Exception {
//	    IProject myProject = createPredefinedProject("Simple AJ Project"); //$NON-NLS-1$
//		IFolder src = myProject.getFolder("src"); //$NON-NLS-1$
//		if (!src.exists()){
//			src.create(true, true, null);
//		}
//		IFolder p1 = src.getFolder("p1"); //$NON-NLS-1$
//		if (!p1.exists()){
//			p1.create(true, true, null);
//		}
//		assertNotNull("src folder should not be null", src); //$NON-NLS-1$
//		assertNotNull("package p1 should not be null", p1); //$NON-NLS-1$
//		 
//		IFile newAspect = createFile(p1,"newAspect.aj","blah blah blah"); //$NON-NLS-1$ //$NON-NLS-2$
//		assertNotNull("newAspect should not be null", newAspect); //$NON-NLS-1$
//		
//		IProjectBuildConfigurator pbc = DefaultBuildConfigurator.getBuildConfigurator().getProjectBuildConfigurator(myProject);
//		BuildConfiguration buildConfig = (BuildConfiguration)pbc.getActiveBuildConfiguration();
//
//		List newFiles = new ArrayList();
//		newFiles.add(newAspect);
//		
//		buildConfig.excludeFiles(newFiles);
//		waitForJobsToComplete();
//		
//		assertFalse("new aspect shouldn't be included in active build configuration", //$NON-NLS-1$
//				buildConfig.isIncluded(newAspect));
//		
//		myProject.build(IncrementalProjectBuilder.FULL_BUILD,new NullProgressMonitor());
//		waitForJobsToComplete();
//		assertFalse("new aspect shouldn't be included in active build configuration", //$NON-NLS-1$
//				buildConfig.isIncluded(newAspect));
//		
//		assertFalse("no problem markers should be against the project since the file isn't included in the build config",  //$NON-NLS-1$
//				ProjectDependenciesUtils.projectIsMarkedWithError(myProject,null));
//		
//		buildConfig.includeFiles(newFiles);
//
//		myProject.build(IncrementalProjectBuilder.FULL_BUILD,new NullProgressMonitor());
//		waitForJobsToComplete();
//		assertTrue("problem markers should be against the project since file is included in the build config",  //$NON-NLS-1$
//				ProjectDependenciesUtils.projectIsMarkedWithError(myProject,null));
//
//		
//	}
	

	public IFile createFile(IFolder inFolder,String name, String content) throws CoreException {
		IFile file = inFolder.getFile(name);
		if (file.exists()) {
			file.delete(0,null);
		}
		ByteArrayInputStream source = new ByteArrayInputStream(content.getBytes());
		file.create(source,true,null);
		file.refreshLocal(IResource.DEPTH_INFINITE,null);
		return file;
	}
	
}