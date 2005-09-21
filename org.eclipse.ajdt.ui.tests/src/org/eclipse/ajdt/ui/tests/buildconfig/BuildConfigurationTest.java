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
package org.eclipse.ajdt.ui.tests.buildconfig;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author hawkinsh
 *
 */
public class BuildConfigurationTest extends UITestCase {

	IProject ajProject = null;

	IFile fileA;
	IFile fileB;
	IFile fileDef;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		ajProject = createPredefinedProject("AJ Project For BuildConfigurationTest"); //$NON-NLS-1$
		
		waitForJobsToComplete();
		setupSandboxSourceFolder();
		waitForJobsToComplete();
	}


	private void setupSandboxSourceFolder() throws Exception{
		IFolder src = ajProject.getFolder("testSrcPath"); //$NON-NLS-1$
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
		
		fileDef = src.getFile("InDefaultPack.java"); //$NON-NLS-1$
		if (!fileDef.exists()) {
			//fileDef.create(new StringBufferInputStream("public class InDefaultPack{}"), true, null);
			String content = "public class InDefaultPack{}"; //$NON-NLS-1$
			ByteArrayInputStream source = new ByteArrayInputStream(content.getBytes());
			fileDef.create(source,true,null);
		}
		IFolder pack = src.getFolder("package1"); //$NON-NLS-1$
		if (!pack.exists()){
			pack.create(true, true, null);
		}

		fileA = pack.getFile("A.java");  //$NON-NLS-1$
		if (!fileA.exists()) {
			//fileA.create(new StringBufferInputStream("package package1;\npublic class A{}"), true, null);
			String content = "package package1;\npublic class A{}"; //$NON-NLS-1$
			ByteArrayInputStream source = new ByteArrayInputStream(content.getBytes());
			fileA.create(source,true,null);
		}

		fileB = pack.getFile("B.java");  //$NON-NLS-1$
		if (!fileB.exists()) {
			//fileB.create(new StringBufferInputStream("package package1;\npublic class B{}"), true, null);
			String content = "package package1;\npublic class B{}"; //$NON-NLS-1$
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
		waitForJobsToComplete();
		if (!bc.isIncluded(fileA))
			fail("A.java was not included."); //$NON-NLS-1$
		if (!bc.isIncluded(fileB))
			fail("B.java was not included."); //$NON-NLS-1$
		if (!bc.isIncluded(fileDef))
			fail("InDefaultPackage.java was not included."); //$NON-NLS-1$
		
	}
	
	public void testBuildConfigurationExclude() throws CoreException{
		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc;
		
		pbc = conf.getProjectBuildConfigurator(ajProject);
		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
		
		//prerequisite
		testBuildConfigurationIsincluded();
			
		waitForJobsToComplete();
			List l = new ArrayList(3);
			l.add(fileA);
			bc.excludeFiles(l);
			waitForJobsToComplete();
			if (bc.isIncluded(fileA)){
				fail("Exclude failed. A.java still included."); //$NON-NLS-1$
			}
			if (!bc.isIncluded(fileB))
				fail("Exclude failed. B.java should be included."); //$NON-NLS-1$
			if (!bc.isIncluded(fileDef))
				fail("Exclude failed. InDefaultPackage.java should be included."); //$NON-NLS-1$
			
			l.clear();
			l.add(fileA.getParent());
			bc.excludeFiles(l);
			waitForJobsToComplete();
			if (bc.isIncluded(fileA))
				fail("Exclude failed. A.java still included."); //$NON-NLS-1$
			if (bc.isIncluded(fileB))
				fail("Exclude failed. B.java still included."); //$NON-NLS-1$
			if (!bc.isIncluded(fileDef))
				fail("Exclude failed. InDefaultPackage.java should be included."); //$NON-NLS-1$
			
			
			bc.includeFiles(l);
			waitForJobsToComplete();
			if (!bc.isIncluded(fileA))
				fail("Reinclude failed. A.java should be included."); //$NON-NLS-1$
			if (!bc.isIncluded(fileB))
				fail("Reinclude failed. B.java should be included."); //$NON-NLS-1$
			if (!bc.isIncluded(fileDef))
				fail("Reinclude failed. InDefaultPackage.java should be included."); //$NON-NLS-1$
		
			l.clear();
			l.add(fileA);
			l.add(fileB);
			bc.excludeFiles(l);
			waitForJobsToComplete();
			if (bc.isIncluded(fileA))
				fail("Exclude failed. A.java still included."); //$NON-NLS-1$
			if (bc.isIncluded(fileB))
				fail("Exclude failed. B.java still included."); //$NON-NLS-1$
			if (!bc.isIncluded(fileDef))
				fail("Exclude failed. InDefaultPackage.java should be included."); //$NON-NLS-1$
			
	}
	
	public void testBuildConfigurationInclude() throws CoreException{
		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc;
		
		pbc = conf.getProjectBuildConfigurator(ajProject);
		BuildConfiguration bc = pbc.getActiveBuildConfiguration();
			
		waitForJobsToComplete();
			List l = new ArrayList(3);
			l.add(fileA.getParent());
			l.add(fileDef);
			bc.excludeFiles(l);
			waitForJobsToComplete();
			if (bc.isIncluded(fileA))
				fail("Exclude failed. A.java should be excluded."); //$NON-NLS-1$
			if (bc.isIncluded(fileB))
				fail("Exclude failed. B.java should be excluded."); //$NON-NLS-1$
			if (bc.isIncluded(fileDef))
				fail("Exclude failed. InDefaultPackage.java should be excluded."); //$NON-NLS-1$
			
			l.clear();
			l.add(fileDef);
			bc.includeFiles(l);
			waitForJobsToComplete();
			if (bc.isIncluded(fileA))
				fail("Include failed. A.java should be excluded."); //$NON-NLS-1$
			if (bc.isIncluded(fileB))
				fail("Include failed. B.java should be excluded."); //$NON-NLS-1$
			if (!bc.isIncluded(fileDef))
				fail("Include failed. InDefaultPackage.java should be included."); //$NON-NLS-1$
			

			l.clear();
			l.add(fileA.getParent());
			bc.includeFiles(l);
			waitForJobsToComplete();
			if (!bc.isIncluded(fileA))
				fail("Include failed. A.java is not included."); //$NON-NLS-1$
			if (!bc.isIncluded(fileB))
				fail("Include failed. B.java is not included."); //$NON-NLS-1$
			if (!bc.isIncluded(fileDef))
				fail("Include failed. InDefaultPackage.java is not included."); //$NON-NLS-1$
				
	}
}

