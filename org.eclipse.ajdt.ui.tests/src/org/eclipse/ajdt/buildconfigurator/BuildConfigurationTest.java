/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.buildconfigurator;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.test.utils.JavaTestProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author hawkinsh
 *
 */
public class BuildConfigurationTest extends TestCase {

	IProject javaProject = null;
	IProject ajProject = null;
	JavaTestProject tp,tp2;

	IFile fileA;
	IFile fileB;
	IFile fileDef;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		//create JavaProject
		tp = new JavaTestProject("Java Project created by BuildConfigurationTest");
		javaProject = tp.getProject();
		
		tp2 = new JavaTestProject("AJ Project created by BuildConfigurationTest");
		ajProject = tp2.getProject();
		AJDTUtils.addAspectJNature(ajProject);		
		
		this.waitForJobsToComplete(ajProject);
		setupSandboxSourceFolder();
		waitForJobsToComplete(ajProject);

	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		try {
			tp.dispose();
			tp2.dispose();
		} catch (CoreException e) {
			// do nothing - don't care if problems occur here....
		}
	}

	private void waitForJobsToComplete(IProject pro){
		Job job = new Job("Dummy Job"){
			public IStatus run(IProgressMonitor m){
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.DECORATE);
		job.setRule(pro);
	    job.schedule();
	    try {
			job.join();
		} catch (InterruptedException e) {
			// Do nothing
		}
	}
	
	private void setupSandboxSourceFolder() throws Exception{
		IFolder src = ajProject.getFolder("testSrcPath");
		if (!src.exists()){
			src.create(true, true, null);
		}
		IJavaProject jp = JavaCore.create(ajProject);
		IClasspathEntry[] cpes = jp.getRawClasspath();
		IClasspathEntry[] newCpes = new IClasspathEntry[cpes.length + 1];
		
		boolean alreadyThere = false;
		for (int i=0; i<cpes.length; i++){
			newCpes[i] = cpes[i];
			if (cpes[i].getPath().equals(src.getFullPath()))
				alreadyThere = true;
		}
		if (!alreadyThere){
			newCpes[cpes.length] = JavaCore.newSourceEntry(src.getFullPath());
			jp.setRawClasspath(newCpes, null);
		}
		
		fileDef = src.getFile("InDefaultPack.java");
		if (!fileDef.exists()) {
			//fileDef.create(new StringBufferInputStream("public class InDefaultPack{}"), true, null);
			String content = "public class InDefaultPack{}";
			ByteArrayInputStream source = new ByteArrayInputStream(content.getBytes());
			fileDef.create(source,true,null);
		}
		IFolder pack = src.getFolder("package1");
		if (!pack.exists()){
			pack.create(true, true, null);
		}

		fileA = pack.getFile("A.java"); 
		if (!fileA.exists()) {
			//fileA.create(new StringBufferInputStream("package package1;\npublic class A{}"), true, null);
			String content = "package package1;\npublic class A{}";
			ByteArrayInputStream source = new ByteArrayInputStream(content.getBytes());
			fileA.create(source,true,null);
		}

		fileB = pack.getFile("B.java"); 
		if (!fileB.exists()) {
			//fileB.create(new StringBufferInputStream("package package1;\npublic class B{}"), true, null);
			String content = "package package1;\npublic class B{}";
			ByteArrayInputStream source = new ByteArrayInputStream(content.getBytes());
			fileB.create(source,true,null);
		}
	}
	
	public void testBuildConfigurationIsincluded(){
		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc;
		
		pbc = conf.getProjectBuildConfigurator(ajProject);
		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
		
		//test: are all new files included?
		waitForJobsToComplete(ajProject);
		if (!bc.isIncluded(fileA))
			fail("A.java was not included.");
		if (!bc.isIncluded(fileB))
			fail("B.java was not included.");
		if (!bc.isIncluded(fileDef))
			fail("InDefaultPackage.java was not included.");
		
	}
	
	public void testBuildConfigurationExclude() throws CoreException{
		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc;
		
		pbc = conf.getProjectBuildConfigurator(ajProject);
		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
		
		//prerequisite
		testBuildConfigurationIsincluded();
			
			waitForJobsToComplete(ajProject);
			List l = new ArrayList(3);
			l.add(fileA);
			bc.excludeFiles(l);
			waitForJobsToComplete(ajProject);
			if (bc.isIncluded(fileA)){
				fail("Exclude failed. A.java still included.");
			}
			if (!bc.isIncluded(fileB))
				fail("Exclude failed. B.java should be included.");
			if (!bc.isIncluded(fileDef))
				fail("Exclude failed. InDefaultPackage.java should be included.");
			
			l.clear();
			l.add(fileA.getParent());
			bc.excludeFiles(l);
			waitForJobsToComplete(ajProject);
			if (bc.isIncluded(fileA))
				fail("Exclude failed. A.java still included.");
			if (bc.isIncluded(fileB))
				fail("Exclude failed. B.java still included.");
			if (!bc.isIncluded(fileDef))
				fail("Exclude failed. InDefaultPackage.java should be included.");
			
			
			bc.includeFiles(l);
			waitForJobsToComplete(ajProject);
			if (!bc.isIncluded(fileA))
				fail("Reinclude failed. A.java should be included.");
			if (!bc.isIncluded(fileB))
				fail("Reinclude failed. B.java should be included.");
			if (!bc.isIncluded(fileDef))
				fail("Reinclude failed. InDefaultPackage.java should be included.");
		
			l.clear();
			l.add(fileA);
			l.add(fileB);
			bc.excludeFiles(l);
			waitForJobsToComplete(ajProject);
			if (bc.isIncluded(fileA))
				fail("Exclude failed. A.java still included.");
			if (bc.isIncluded(fileB))
				fail("Exclude failed. B.java still included.");
			if (!bc.isIncluded(fileDef))
				fail("Exclude failed. InDefaultPackage.java should be included.");
			
	}
	
	public void testBuildConfigurationInclude() throws CoreException{
		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc;
		
		pbc = conf.getProjectBuildConfigurator(ajProject);
		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
			
			waitForJobsToComplete(ajProject);
			List l = new ArrayList(3);
			l.add(fileA.getParent());
			l.add(fileDef);
			bc.excludeFiles(l);
			waitForJobsToComplete(ajProject);
			if (bc.isIncluded(fileA))
				fail("Exclude failed. A.java should be excluded.");
			if (bc.isIncluded(fileB))
				fail("Exclude failed. B.java should be excluded.");
			if (bc.isIncluded(fileDef))
				fail("Exclude failed. InDefaultPackage.java should be excluded.");
			
			l.clear();
			l.add(fileDef);
			bc.includeFiles(l);
			waitForJobsToComplete(ajProject);
			if (bc.isIncluded(fileA))
				fail("Include failed. A.java should be excluded.");
			if (bc.isIncluded(fileB))
				fail("Include failed. B.java should be excluded.");
			if (!bc.isIncluded(fileDef))
				fail("Include failed. InDefaultPackage.java should be included.");
			

			l.clear();
			l.add(fileA.getParent());
			bc.includeFiles(l);
			waitForJobsToComplete(ajProject);
			if (!bc.isIncluded(fileA))
				fail("Include failed. A.java is not included.");
			if (!bc.isIncluded(fileB))
				fail("Include failed. B.java is not included.");
			if (!bc.isIncluded(fileDef))
				fail("Include failed. InDefaultPackage.java is not included.");
				
	}
}

