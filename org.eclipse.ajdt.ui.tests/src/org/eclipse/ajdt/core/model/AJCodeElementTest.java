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
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * @author hawkinsh
 *
 */
public class AJCodeElementTest extends TestCase {
	
	IProject project;
	AJCodeElement[] ajCodeElements;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = Utils.getPredefinedProject("AJProject83082");
		AJModel model = AJModel.getInstance();
		model.createMap(project);

		IFolder src = project.getFolder("src");
		IFolder com = src.getFolder("com");
		IFolder ibm = com.getFolder("ibm");
		IFolder wpstest = ibm.getFolder("wpstest");
		IFolder aspectjPackage = wpstest.getFolder("aspectj");
		IFile main = aspectjPackage.getFile("Main.java");
		Map annotationsMap = AsmManager.getDefault().getInlineAnnotations(main.getRawLocation().toOSString(),true, true);
		ajCodeElements = createAJCodeElements(model,annotationsMap);
		
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		Utils.deleteProject(project);
	}

	public void testHashCode() {
		// through the normal running of a program, the hashcode must always return the same answer
		int hash1 = ajCodeElements[0].hashCode();
		int hash2 = ajCodeElements[0].hashCode();
		assertTrue("through the normal running of a program, the hashcodes must always return the same int",hash1 == hash2);

		// if A and B are objects such that A.equals(B) then A.hashCode() == B.hashCode()
		IJavaElement parent = ajCodeElements[0].getParent();
		String name = ajCodeElements[0].getName();
		int line = ajCodeElements[0].getLine();
		AJCodeElement newAJCE = new AJCodeElement((JavaElement) parent,line,name);
		assertTrue("these should be equal according to equals method", ajCodeElements[0].equals(newAJCE));
		assertTrue("if A and B are objects such that A.equals(B) then A.hashCode() == B.hashCode()",
				newAJCE.hashCode() == ajCodeElements[0].hashCode());
		
		// if A and B are objects such that !(A.equals(B)) then less confusing if A.hashCode() != B.hashCode()
		assertFalse("these should not be equal according to the equals method",ajCodeElements[0].equals(ajCodeElements[1]));
		assertTrue("if A and B are objects such that !(A.equals(B)) then less confusing if A.hashCode() != B.hashCode()",
				ajCodeElements[0].hashCode() != ajCodeElements[1].hashCode());
		
	}

	/*
	 * Class under test for boolean equals(Object)
	 */
	public void testEqualsObject() {
		IJavaElement parent = ajCodeElements[0].getParent();
		String name = ajCodeElements[0].getName();
		int line = ajCodeElements[0].getLine();
		AJCodeElement a1 = new AJCodeElement((JavaElement) parent,line,name);
		AJCodeElement a2 = new AJCodeElement((JavaElement) parent,line,name);
		AJCodeElement a3 = ajCodeElements[0];
		
		// equals must be reflexive: x.equals(x) == true (always)
		assertTrue("x.equals(x) == true (always)", a1.equals(a1));
		
		// equals must be symmetric: x.equals(y) == true <==> y.equals(x) == true
		// (way to test this is if x.equals(y) == false ==> y.equals(x) == false)
		assertTrue("equals must be symmetric",!(a3.equals(ajCodeElements[1]) && ajCodeElements[1].equals(a3)));
				
		// equals must be transitive: for all x,y,z, if x.equals(y) == true and y.equals(z) == true 
		// ==> x.equals(z) == true
		assertTrue("equals must be transitive",a1.equals(a2) && a2.equals(a3) && a1.equals(a3) );

		// equals must be consistent: for any x,y, multiple invocations of x.equals(y) must
		// always return the same result
		boolean first = a1.equals(a2);
		boolean second = a1.equals(a2);
		assertTrue("equals must be consistent", first && second);
		boolean b1 = a1.equals(ajCodeElements[1]);
		boolean b2 = a1.equals(ajCodeElements[1]);
		assertFalse("equals must be consistent", b1 && b2);
		
		// for any non-null reference value x, x.equals(null) must return false
		assertNotNull("shouldn't be null",a1);
		assertFalse("for any non-null reference value x, x.equals(null) must return false",a1.equals(null));
	}

	/**
	 * @param model
	 * @return
	 */
	private AJCodeElement[] createAJCodeElements(AJModel model, Map annotationsMap) {
		AJCodeElement[] arrayOfajce = new AJCodeElement[2];
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
						arrayOfajce[0] = (AJCodeElement) ije;
					}					
				} else if (node.toLinkLabelString()
						.equals("Main: method-call(void java.io.PrintStream.println(java.lang.String))") 
					&& (sl.getLine() == 19) ){
					
					IJavaElement ije = model.getCorrespondingJavaElement(node);
					if (ije instanceof AJCodeElement) {
						arrayOfajce[1] = (AJCodeElement) ije;
					}					
				}
			}
		}				
		return arrayOfajce;
	}


	
}
