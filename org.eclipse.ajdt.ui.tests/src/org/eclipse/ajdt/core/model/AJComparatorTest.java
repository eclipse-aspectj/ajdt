/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.core.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.test.utils.BlockingProgressMonitor;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author hawkinsh
 *
 */
public class AJComparatorTest extends TestCase {

	IProject project;
	IFolder aspectjPackage;
	AJModel model;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = Utils.getPredefinedProject("AJProject83082", true);
		model = AJModel.getInstance();
		model.createMap(project);

		IFolder src = project.getFolder("src");
		IFolder com = src.getFolder("com");
		IFolder ibm = com.getFolder("ibm");
		IFolder wpstest = ibm.getFolder("wpstest");
		aspectjPackage = wpstest.getFolder("aspectj");
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		model.clearMap(project);
		Utils.deleteProject(project);
	}

	public void testCompareTwoAJCodeElements() throws CoreException {

		// get the class file and create the map for the file (the underlying one)		
		IFile main = aspectjPackage.getFile("Main.java");

		Map annotationsMap = AsmManager.getDefault().getInlineAnnotations(main.getRawLocation().toOSString(),true, true);
		assertNotNull("annotation map should not be null for Main.java",annotationsMap);
		// for the two IProgramElements which correspond to the two calls
		// in main - create the IJavaElements (or AJCodeElements)
		AJCodeElement ajce1 = null;
		AJCodeElement ajce2 = null;
		Set keys = annotationsMap.keySet();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			Object key = it.next();
			List annotations = (List) annotationsMap.get(key);
			for (Iterator it2 = annotations.iterator(); it2.hasNext();) {
				IProgramElement node = (IProgramElement) it2.next();
				ISourceLocation sl = node.getSourceLocation();
				if (node.toLinkLabelString()
						.equals("Main: method-call(void java.io.PrintStream.println(java.lang.String))") 
					&& (sl.getLine() == 18) ){
					
					IJavaElement ije = model.getCorrespondingJavaElement(node);
					if (ije instanceof AJCodeElement) {
						ajce1 = (AJCodeElement) ije;
					}					
				} else if (node.toLinkLabelString()
						.equals("Main: method-call(void java.io.PrintStream.println(java.lang.String))") 
					&& (sl.getLine() == 19) ){
					
					IJavaElement ije = model.getCorrespondingJavaElement(node);
					if (ije instanceof AJCodeElement) {
						ajce2 = (AJCodeElement) ije;
					}					
				}
			}
		}		
		assertNotNull("AJCodeElement shouldn't be null",ajce1);
		assertNotNull("AJCodeElement shouldn't be null",ajce2);
		
		// check that when call compare on them, that the one with
		// the lowest line number is first in the list
		AJComparator comp = new AJComparator();
		assertTrue("ajce1 should be less than ajce2",comp.compare(ajce1,ajce2) < 0);
		assertTrue("ajce2 should be greater than ajce1",comp.compare(ajce2,ajce1) > 0);
		assertTrue("ajce1 should be equal to ajce1",comp.compare(ajce1,ajce1) == 0);
	}

	public void testCompareTwoIJavaElements() throws CoreException {

		// get the aspect and create the map for the file (the underlying one)
		IFile aspect = aspectjPackage.getFile("A.aj");
		
		// for the two IProgramElements which correspond to the two calls
		// in main - create the IJavaElements

		Map annotationsMap = AsmManager.getDefault().getInlineAnnotations(aspect.getRawLocation().toOSString(),true, true);
		assertNotNull("annotation map should not be null for Main.java",annotationsMap);
		// for the two IProgramElements which correspond to the two pieces
		// of advice (before and after) in A.aj - create the IJavaElements 
		IJavaElement ije1 = null;
		IJavaElement ije2 = null;
		Set keys = annotationsMap.keySet();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			Object key = it.next();
			List annotations = (List) annotationsMap.get(key);
			for (Iterator it2 = annotations.iterator(); it2.hasNext();) {
				IProgramElement node = (IProgramElement) it2.next();
				ISourceLocation sl = node.getSourceLocation();
				if (node.toLinkLabelString()
						.equals("A.after(String): tracedPrint..") 
					&& (sl.getLine() == 25) ){
					
					IJavaElement ije = model.getCorrespondingJavaElement(node);
					if (!(ije instanceof AJCodeElement)) {
						ije1 = ije;
					}					
				} else if (node.toLinkLabelString()
						.equals("A.before(String): tracedPrint..") 
					&& (sl.getLine() == 21) ){
					
					IJavaElement ije = model.getCorrespondingJavaElement(node);
					if (!(ije instanceof AJCodeElement)) {
						ije2 = ije;
					}					
				}
			}
		}		
		assertNotNull("IJavaElement shouldn't be null",ije1);
		assertNotNull("IJavaElement shouldn't be null",ije2);
		
		// check that when call compare on them, that the one that comes first
		// alphabetically is the first in the list
		AJComparator comp = new AJComparator();
		assertTrue("ije1 should be less than ije2",comp.compare(ije1,ije2) < 0);
		assertTrue("ije2 should be greater than ije1",comp.compare(ije2,ije1) > 0);
		assertTrue("ije1 should be equal to ije1",comp.compare(ije1,ije1) == 0);
		
	}

	public void testCompareAJCodeElementAndIJavaElement() {
		AJCodeElement ajce = null;
		IJavaElement ije = null;

		// get the aspect and create the map for the file (the underlying one)
		IFile main = aspectjPackage.getFile("Main.java");
				
		Map annotationsMap = AsmManager.getDefault().getInlineAnnotations(main.getRawLocation().toOSString(),true, true);
		assertNotNull("annotation map should not be null for Main.java",annotationsMap);
		Set keys = annotationsMap.keySet();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			Object key = it.next();
			List annotations = (List) annotationsMap.get(key);
			for (Iterator it2 = annotations.iterator(); it2.hasNext();) {
				IProgramElement node = (IProgramElement) it2.next();
				ISourceLocation sl = node.getSourceLocation();
				if (node.toLinkLabelString()
						.equals("Main: method-call(void java.io.PrintStream.println(java.lang.String))") 
					&& (sl.getLine() == 18) ){
					
					IJavaElement je = model.getCorrespondingJavaElement(node);
					if (je instanceof AJCodeElement) {
						ajce = (AJCodeElement) je;
						break;
					}					
				}
			}
		}	
		
		IFile aspect = aspectjPackage.getFile("A.aj");

		Map annotationsMap2 = AsmManager.getDefault().getInlineAnnotations(aspect.getRawLocation().toOSString(),true, true);
		assertNotNull("annotation map should not be null for Main.java",annotationsMap2);

		Set keys2 = annotationsMap2.keySet();
		for (Iterator it = keys2.iterator(); it.hasNext();) {
			Object key = it.next();
			List annotations = (List) annotationsMap2.get(key);
			for (Iterator it2 = annotations.iterator(); it2.hasNext();) {
				IProgramElement node = (IProgramElement) it2.next();
				ISourceLocation sl = node.getSourceLocation();
				if (node.toLinkLabelString()
						.equals("A.after(String): tracedPrint..") 
					&& (sl.getLine() == 25) ){
					
					IJavaElement je = model.getCorrespondingJavaElement(node);
					if (!(je instanceof AJCodeElement)) {
						ije = je;
						break;
					}					
				}
			}
		}		
		
		assertNotNull("AJCodeElement shouldn't be null",ajce);
		assertNotNull("IJavaElement shouldn't be null",ije);
		
		// check that when call compare on them, that the one that comes first
		// alphabetically is the first in the list
		AJComparator comp = new AJComparator();
		assertTrue("ije should be less than ajce",comp.compare(ije,ajce) < 0);
		assertTrue("ajce should be greater than ije",comp.compare(ajce,ije) > 0);
		
	}
	
	public void testCompareTwoStrings() {
		String s1 = "hello";
		String s2 = "goodbye";
		AJComparator comp = new AJComparator();
		assertTrue("comparing things which aren't AJCodeElements or IJavaElements should return 0",comp.compare(s1,s2) == 0);
	}

}
