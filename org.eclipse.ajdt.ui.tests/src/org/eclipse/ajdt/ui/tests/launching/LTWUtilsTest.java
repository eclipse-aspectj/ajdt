/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins and Sian January - initial version
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests.launching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.launching.LTWUtils;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;

public class LTWUtilsTest extends UITestCase{
	
	private String line0Start = "<?xml version=\"1.0\"";
	private String aspectjBegin = "<aspectj>";
	private String aspectjEnd = "</aspectj>";
	private String aspectsBegin = "\t<aspects>";
	private String aspectsEnd = "\t</aspects>";
	private String aspects = "\t<aspects/>";
	
	// abstract aspects (which we don't want to include)
	public void testGetAspects() throws Exception {
		IProject project = createPredefinedProject("Tracing Example");
		waitForJobsToComplete();
		ProjectBuildConfigurator bc = BuildConfigurator.getBuildConfigurator().getProjectBuildConfigurator(project);
		Collection c = bc.getBuildConfigurations();
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			BuildConfiguration element = (BuildConfiguration) iter.next();
			if (element.getName().equals("tracelib")) {
				bc.setActiveBuildConfiguration(element);
			};
		}	
		waitForJobsToComplete();
		IJavaProject jp = JavaCore.create(project);
		IPackageFragmentRoot[] roots = jp.getAllPackageFragmentRoots();
		List srcRoots = new ArrayList();
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			if (!(root instanceof JarPackageFragmentRoot)) {
				srcRoots.add(root);
			}
		}
		assertEquals("There should be one src directory",1,srcRoots.size());
		List aspects = LTWUtils.getAspects((IPackageFragmentRoot)srcRoots.get(0));
		assertEquals("There should be two aspects",2,aspects.size());
		assertEquals("The first aspect should be called TraceMyClasses","TraceMyClasses",((AspectElement)aspects.get(0)).getElementName()); //$NON-NLS-2$
		assertEquals("The second aspect should be called AbstractTrace","AbstractTrace",((AspectElement)aspects.get(1)).getElementName()); //$NON-NLS-2$
		
	}
	
	// multiple source folders with no aspects
	public void testGenerateLTWConfigFile1() throws Exception {
		IProject project = createPredefinedProject("MultipleSourceFolders");
		waitForJobsToComplete();
		IJavaProject jp = JavaCore.create(project);
		LTWUtils.generateLTWConfigFile(jp);
		waitForJobsToComplete();
		
		IResource r1 = jp.getProject().findMember("src/" + LTWUtils.AOP_XML_LOCATION);
		IResource r2 = jp.getProject().findMember("src2/" + LTWUtils.AOP_XML_LOCATION);
		
		assertNull("aop.xml shouldn't exist in src directory because there are no aspects",r1);
		assertNull("aop.xml shouldn't exist in src2 directory because there are no aspects",r2);
	}
	
	// one source folder with aspects
	public void testGenerateLTWConfigFile2() throws Exception {
		IProject project = createPredefinedProject("Bean Example");
		waitForJobsToComplete();
		IJavaProject jp = JavaCore.create(project);
		LTWUtils.generateLTWConfigFile(jp);
		waitForJobsToComplete();
		
		IResource r1 = jp.getProject().findMember("src/" + LTWUtils.AOP_XML_LOCATION);		
		assertNotNull("aop.xml should exist in src directory because there are aspects",r1);
		
		IFile file = (IFile)r1;
		String[] expectedLines = new String[]{
				line0Start,
				aspectjBegin,
				aspectsBegin,
				"\t\t<aspect name=\"bean.BoundPoint\"/>",
				aspectsEnd,
				aspectjEnd};
		
		compareFileContentsWithExpected(file, expectedLines);

		waitForJobsToComplete();
		
		LTWUtils.generateLTWConfigFile(jp);
		waitForJobsToComplete();
				
		IResource r2 = jp.getProject().findMember("src/" + LTWUtils.AOP_XML_LOCATION);		
		assertNotNull("aop.xml should exist in src directory because there are aspects",r2);

		compareFileContentsWithExpected((IFile)r2, expectedLines);
	}
	
	// no src folders with aspects
	public void testGenerateLTWConfigFile3() throws Exception {
		IProject project = createPredefinedProject("WithoutSourceFolder");
		waitForJobsToComplete();
		IJavaProject jp = JavaCore.create(project);	
		LTWUtils.generateLTWConfigFile(jp);
		waitForJobsToComplete();
		
		IResource r1 = jp.getProject().findMember(LTWUtils.AOP_XML_LOCATION);		
		assertNotNull("aop.xml should exist in project directory because there are aspects",r1);

		IFile file = (IFile)r1;
		String[] expectedLines = new String[]{
				line0Start,
				aspectjBegin,
				aspectsBegin,
				"\t\t<aspect name=\"A\"/>",
				aspectsEnd,
				aspectjEnd};
		
		compareFileContentsWithExpected(file, expectedLines);
	
	}
	
	// no source folders, no aspects
	public void testGenerateLTWConfigFile4() throws Exception {
		IProject project = createPredefinedProject("Simple Project");
		waitForJobsToComplete();
		IJavaProject jp = JavaCore.create(project);	
		LTWUtils.generateLTWConfigFile(jp);
		waitForJobsToComplete();
		
		IResource r1 = jp.getProject().findMember(LTWUtils.AOP_XML_LOCATION);		
		assertNull("aop.xml should not exist in the project directory because there are no aspects",r1);

	}
	
	// multiple source folders with aspects
	public void testGenerateLTWConfigFile5() throws Exception {
		IProject project = createPredefinedProject("MultipleSourceFoldersWithAspects");
		waitForJobsToComplete();
		IJavaProject jp = JavaCore.create(project);
		LTWUtils.generateLTWConfigFile(jp);
		waitForJobsToComplete();

		IResource r1 = jp.getProject().findMember("src/" + LTWUtils.AOP_XML_LOCATION);
		IResource r2 = jp.getProject().findMember("src2/" + LTWUtils.AOP_XML_LOCATION);
		
		assertNotNull("aop.xml should exist in src directory because there are aspects",r1);
		assertNotNull("aop.xml should exist in src2 directory because there are aspects",r2);

		IFile file = (IFile)r1;
		String[] expectedLines = new String[]{
				line0Start,
				aspectjBegin,
				aspectsBegin,
				"\t\t<aspect name=\"pack.A1\"/>",
				aspectsEnd,
				aspectjEnd};
		
		compareFileContentsWithExpected(file, expectedLines);
		
		IFile file2 = (IFile)r2;
		String[] expectedLines2 = new String[]{
				line0Start,
				aspectjBegin,
				aspectsBegin,
				"\t\t<aspect name=\"pack.A2\"/>",
				aspectsEnd,
				aspectjEnd};
		
		compareFileContentsWithExpected(file2, expectedLines2);
	}
	
	// abstract aspects (which we don't want to include)
	public void testGenerateLTWConfigFile6() throws Exception {
		IProject project = createPredefinedProject("Tracing Example");
		waitForJobsToComplete();
		ProjectBuildConfigurator bc = BuildConfigurator.getBuildConfigurator().getProjectBuildConfigurator(project);
		Collection c = bc.getBuildConfigurations();
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			BuildConfiguration element = (BuildConfiguration) iter.next();
			if (element.getName().equals("tracelib")) {
				bc.setActiveBuildConfiguration(element);
			};
		}	
		
		IJavaProject jp = JavaCore.create(project);
		waitForJobsToComplete();
		LTWUtils.generateLTWConfigFile(jp);
		waitForJobsToComplete();

		IResource r1 = jp.getProject().findMember("src/" + LTWUtils.AOP_XML_LOCATION);
		assertNotNull("aop.xml should exist in src directory because there are aspects",r1);

		IFile file = (IFile)r1;
		String[] expectedLines = new String[]{
				line0Start,
				aspectjBegin,
				aspectsBegin,
				"\t\t<aspect name=\"tracing.lib.TraceMyClasses\"/>",
				"\t\t<aspect name=\"tracing.lib.AbstractTrace\"/>",
				aspectsEnd,
				aspectjEnd};
		
		compareFileContentsWithExpected(file, expectedLines);
		
		// activate a different build config to see if aop.xml updates correctly
		c = bc.getBuildConfigurations();
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			BuildConfiguration element = (BuildConfiguration) iter.next();
			if (element.getName().equals("tracev1")) {
				bc.setActiveBuildConfiguration(element);
			};
		}	
		waitForJobsToComplete();
		LTWUtils.generateLTWConfigFile(jp);
		waitForJobsToComplete();
		
		r1 = jp.getProject().findMember("src/" + LTWUtils.AOP_XML_LOCATION);
		assertNotNull("aop.xml should exist in src directory because there are aspects",r1);
		
		file = (IFile)r1;
		expectedLines = new String[]{
				line0Start,
				aspectjBegin,
				aspectsBegin,
				"\t\t<aspect name=\"tracing.version1.TraceMyClasses\"/>",
				aspectsEnd,
				aspectjEnd};
		
		compareFileContentsWithExpected(file, expectedLines);
		
		// activate a config where no aspects are included. This should
		// clear the "aspects" part of the aop.xml file.
		c = bc.getBuildConfigurations();
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			BuildConfiguration element = (BuildConfiguration) iter.next();
			if (element.getName().equals("notrace")) {
				bc.setActiveBuildConfiguration(element);
			};
		}	
		waitForJobsToComplete();
		LTWUtils.generateLTWConfigFile(jp);
		waitForJobsToComplete();
		
		r1 = jp.getProject().findMember("src/" + LTWUtils.AOP_XML_LOCATION);
		assertNotNull("aop.xml should exist in src directory because there are aspects",r1);
		
		file = (IFile)r1;
		expectedLines = new String[]{
				line0Start,
				aspectjBegin,
				aspects,
				aspectjEnd};
		
		compareFileContentsWithExpected(file, expectedLines);		
		
	}
	
	// one source folder with aspects and aop.xml file which
	// contains other contents
	public void testGenerateLTWConfigFile7() throws Exception {
		IProject project = createPredefinedProject("project.with.aop.xml.file");
		waitForJobsToComplete();
		IJavaProject jp = JavaCore.create(project);
		LTWUtils.generateLTWConfigFile(jp);
		waitForJobsToComplete();
		
		IResource r1 = jp.getProject().findMember("src/" + LTWUtils.AOP_XML_LOCATION);		
		assertNotNull("aop.xml should exist in src directory because there are aspects",r1);
		
		IFile file = (IFile)r1;
		String[] expectedLines = new String[]{
				line0Start,
				aspectjBegin,
				"\t<weaver options=\"-verbose -showWeaveInfo\"/>",
				aspectsBegin,
				"\t\t<aspect name=\"bean.BoundPoint\"/>",
				aspectsEnd,
				"<!-- this is a comment -->",
				aspectjEnd};
		
		file.refreshLocal(1, null);
		compareFileContentsWithExpected(file, expectedLines);
	}
	
	
	private void printFileContents(IFile file) throws IOException, CoreException {
		BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
		String line = br.readLine();
		System.out.println("----------");
		while (line != null) {
			System.out.println("line: " + line);
			line = br.readLine();
		}
		br.close();
	}
	
	private void compareFileContentsWithExpected(IFile file, String[] expectedLines) throws CoreException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(file
				.getContents()));
		String line = br.readLine();
		int counter = 0;
		while (line != null) {
			if (counter == 0) {
				if (!line.startsWith(expectedLines[0])) {
				 fail("expected line 1 to start with " + expectedLines[counter] 
						+ (counter+1) + ", found " + line);
				}
			} else if ((expectedLines.length <= counter) 
					|| !line.equals(expectedLines[counter]) ) {
				br.close();
				fail("expected " + expectedLines[counter] + " on line " 
						+ (counter+1) + ", found " + line);
			}
			counter++;
			line = br.readLine();
		}
		br.close();
	}
}
