/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January    - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.newbuildconfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import org.eclipse.ajdt.core.BuildConfig;
import org.eclipse.ajdt.internal.ui.refactoring.ReaderInputStream;
import org.eclipse.ajdt.ui.buildpath.BuildConfigurationUtils;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class BuildConfigurationTest extends UITestCase {

	IProject project;
	
	protected void setUp() throws Exception {
		super.setUp();		
		project = createPredefinedProject("Tracing Example"); //$NON-NLS-1$		
		waitForJobsToComplete();	
	}
	
	public void testApplyAndSaveNoTrace() throws Exception {
		checkIncluded(12);
		IFile propertiesFile = project.getFile("notrace.ajproperties"); //$NON-NLS-1$
		assertNotNull(propertiesFile);
		assertTrue(propertiesFile.exists());
		
		BuildConfigurationUtils.applyBuildConfiguration(propertiesFile);
		waitForJobsToComplete();
		checkIncluded(4);
		
		IFile newPropertiesFile = project.getFile("notrace2.ajproperties"); //$NON-NLS-1$
		BuildConfigurationUtils.saveBuildConfiguration(newPropertiesFile);
		newPropertiesFile.refreshLocal(1, null);
		waitForJobsToComplete();
		compareFiles(propertiesFile, newPropertiesFile);			
	}

	public void testApplyAndSaveTraceLib() throws Exception {
		checkIncluded(12);
		IFile propertiesFile = project.getFile("tracelib.ajproperties"); //$NON-NLS-1$
		assertNotNull(propertiesFile);
		assertTrue(propertiesFile.exists());
		
		BuildConfigurationUtils.applyBuildConfiguration(propertiesFile);
		waitForJobsToComplete();
		checkIncluded(6);
		checkFileIncluded("src/tracing/lib/TraceMyClasses.aj"); //$NON-NLS-1$
		checkFileNotIncluded("src/tracing/version1/Trace.java"); //$NON-NLS-1$
		
		IFile newPropertiesFile = project.getFile("tracelib2.ajproperties"); //$NON-NLS-1$
		BuildConfigurationUtils.saveBuildConfiguration(newPropertiesFile);
		newPropertiesFile.refreshLocal(1, null);
		waitForJobsToComplete();
		
		compareFiles(propertiesFile, newPropertiesFile);
	}

	public void testApplyTraceV1() {
		checkIncluded(12);
		IFile propertiesFile = project.getFile("tracev1.ajproperties"); //$NON-NLS-1$
		assertNotNull(propertiesFile);
		assertTrue(propertiesFile.exists());
		
		BuildConfigurationUtils.applyBuildConfiguration(propertiesFile);
		waitForJobsToComplete();
		checkIncluded(6);
		checkFileNotIncluded("src/tracing/lib/TraceMyClasses.aj"); //$NON-NLS-1$
		checkFileIncluded("src/tracing/version1/Trace.java"); //$NON-NLS-1$
	}
	
	public void testApplyTraceV2() {
		checkIncluded(12);
		IFile propertiesFile = project.getFile("tracev2.ajproperties"); //$NON-NLS-1$
		assertNotNull(propertiesFile);
		assertTrue(propertiesFile.exists());
		
		BuildConfigurationUtils.applyBuildConfiguration(propertiesFile);
		waitForJobsToComplete();
		checkIncluded(6);
		checkFileNotIncluded("src/tracing/lib/TraceMyClasses.aj"); //$NON-NLS-1$
		checkFileIncluded("src/tracing/version2/Trace.aj"); //$NON-NLS-1$
	}
	
	public void testApplyTraceV3() {
		checkIncluded(12);
		IFile propertiesFile = project.getFile("tracev3.ajproperties"); //$NON-NLS-1$
		assertNotNull(propertiesFile);
		assertTrue(propertiesFile.exists());
		
		BuildConfigurationUtils.applyBuildConfiguration(propertiesFile);
		waitForJobsToComplete();
		checkIncluded(6);
		checkFileNotIncluded("src/tracing/lib/TraceMyClasses.aj"); //$NON-NLS-1$
		checkFileIncluded("src/tracing/version3/Trace.aj"); //$NON-NLS-1$
		
	}
	
	public void testExcludeAll() throws Exception {
		checkIncluded(12);
		IFile propertiesFile = project.getFile("none.ajproperties"); //$NON-NLS-1$
		propertiesFile.create(new ReaderInputStream(new StringReader("src.excludes = src/")), true, null); //$NON-NLS-1$
		waitForJobsToComplete();
		assertNotNull(propertiesFile);
		assertTrue(propertiesFile.exists());
		
		BuildConfigurationUtils.applyBuildConfiguration(propertiesFile);
		waitForJobsToComplete();
		checkIncluded(0);
		
		propertiesFile = project.getFile("none2.ajproperties"); //$NON-NLS-1$
		propertiesFile.create(new ReaderInputStream(new StringReader("src.includes = / \n" + //$NON-NLS-1$
				"src.excludes = src/tracing/")), true, null); //$NON-NLS-1$
		waitForJobsToComplete();
		assertNotNull(propertiesFile);
		assertTrue(propertiesFile.exists());
		
		BuildConfigurationUtils.applyBuildConfiguration(propertiesFile);
		waitForJobsToComplete();
		checkIncluded(0);
		
		IFile newPropertiesFile = project.getFile("none3.ajproperties"); //$NON-NLS-1$
		BuildConfigurationUtils.saveBuildConfiguration(newPropertiesFile);
		newPropertiesFile.refreshLocal(1, null);
		waitForJobsToComplete();
		compareFiles(propertiesFile, newPropertiesFile);	
	}

	private void compareFiles(IFile propertiesFile, IFile newPropertiesFile) throws CoreException, IOException {
		BufferedReader br = null;
		BufferedReader br2 = null;
		try {
			br = new BufferedReader(new InputStreamReader(propertiesFile.getContents()));
			br2 = new BufferedReader(new InputStreamReader(newPropertiesFile.getContents()));
			String line1 = br.readLine();
			String line2 = br2.readLine();
			while(line1 != null && line2 != null) {
				assertEquals(line1.trim(), line2.trim());
				line1 = br.readLine();
				line2 = br2.readLine();				
			}
		} finally {
			if(br != null) {
				br.close();
			}
			if(br2 != null) {
				br2.close();
			}
		}
	}
	
	private void checkIncluded(int numFiles) {
		List included = BuildConfig.getIncludedSourceFiles(project);
		assertEquals(numFiles, included.size());
	}
	
	
	private void checkFileNotIncluded(String filename) {
		IFile file = project.getFile(filename);
		assertNotNull(file);
		assertTrue(file.exists());
		assertFalse(BuildConfig.isIncluded(file));
	}

	private void checkFileIncluded(String filename) {
		IFile file = project.getFile(filename);
		assertNotNull(file);
		assertTrue(file.exists());
		assertTrue(BuildConfig.isIncluded(file));
	}
}
