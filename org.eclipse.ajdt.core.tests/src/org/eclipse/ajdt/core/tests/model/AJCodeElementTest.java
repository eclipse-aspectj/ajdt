/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.JavaElement;

import static org.junit.Assert.assertNotEquals;

/**
 * @author hawkinsh
 *
 */
public class AJCodeElementTest extends AJDTCoreTestCase {

	IProject project;
	AJCodeElement[] ajCodeElements;
	AJProjectModelFacade model;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = createPredefinedProject("AJProject83082"); //$NON-NLS-1$

		model = AJProjectModelFactory.getInstance().getModelForProject(project);

		IFolder src = project.getFolder("src"); //$NON-NLS-1$
		IFolder wpstest = src.getFolder("wpstest"); //$NON-NLS-1$
		IFolder aspectjPackage = wpstest.getFolder("aspectj"); //$NON-NLS-1$
		IFile main = aspectjPackage.getFile("Main.java"); //$NON-NLS-1$

		AsmManager asm = AspectJPlugin.getDefault().getCompilerFactory().getCompilerForProject(project.getProject()).getModel();
		Map<? extends Integer, ? extends List<IProgramElement>> annotationsMap = asm.getInlineAnnotations(main.getRawLocation().toOSString(),true, true);
		ajCodeElements = createAJCodeElements(annotationsMap);

	}

	public void testHashCode() {
		// through the normal running of a program, the hashcode must always return the same answer
		int hash1 = ajCodeElements[0].hashCode();
		int hash2 = ajCodeElements[0].hashCode();
    assertEquals("through the normal running of a program, the hashcodes must always return the same int", hash1, hash2);

		// if A and B are objects such that A.equals(B) then A.hashCode() == B.hashCode()
		JavaElement parent = ajCodeElements[0].getParent();
		String name = ajCodeElements[0].getName();
		AJCodeElement newAJCE = new AJCodeElement(parent, name);
    assertEquals("these should be equal according to equals method", ajCodeElements[0], newAJCE); //$NON-NLS-1$
    assertEquals("if A and B are objects such that A.equals(B) then A.hashCode() == B.hashCode()", newAJCE.hashCode(), ajCodeElements[0].hashCode());

		// if A and B are objects such that !(A.equals(B)) then less confusing if A.hashCode() != B.hashCode()
    assertNotEquals("these should not be equal according to the equals method", ajCodeElements[0], ajCodeElements[1]); //$NON-NLS-1$
		assertTrue("if A and B are objects such that !(A.equals(B)) then less confusing if A.hashCode() != B.hashCode()", //$NON-NLS-1$
				ajCodeElements[0].hashCode() != ajCodeElements[1].hashCode());

	}

	/*
	 * Class under test for boolean equals(Object)
	 */
	public void testEqualsObject() {
		JavaElement parent = ajCodeElements[0].getParent();
		String name = ajCodeElements[0].getName();
		AJCodeElement a1 = new AJCodeElement(parent, name);
		AJCodeElement a2 = new AJCodeElement(parent, name);
		AJCodeElement a3 = ajCodeElements[0];

		// equals must be reflexive: x.equals(x) == true (always)
    assertEquals("x.equals(x) == true (always)", a1, a1); //$NON-NLS-1$

		// equals must be symmetric: x.equals(y) == true <==> y.equals(x) == true
		// (way to test this is if x.equals(y) == false ==> y.equals(x) == false)
    assertFalse("equals must be symmetric", a3.equals(ajCodeElements[1]) && ajCodeElements[1].equals(a3)); //$NON-NLS-1$

		// equals must be transitive: for all x,y,z, if x.equals(y) == true and y.equals(z) == true
		// ==> x.equals(z) == true
		assertTrue("equals must be transitive",a1.equals(a2) && a2.equals(a3) && a1.equals(a3) ); //$NON-NLS-1$

		// equals must be consistent: for any x,y, multiple invocations of x.equals(y) must
		// always return the same result
		boolean first = a1.equals(a2);
		boolean second = a1.equals(a2);
		assertTrue("equals must be consistent", first && second); //$NON-NLS-1$
		boolean b1 = a1.equals(ajCodeElements[1]);
		boolean b2 = a1.equals(ajCodeElements[1]);
		assertFalse("equals must be consistent", b1 && b2); //$NON-NLS-1$

		// for any non-null reference value x, x.equals(null) must return false
		assertNotNull("shouldn't be null",a1); //$NON-NLS-1$
    assertNotEquals("for any non-null reference value x, x.equals(null) must return false", null, a1); //$NON-NLS-1$
	}

	/**
	 * @param model
	 * @return
	 */
	private AJCodeElement[] createAJCodeElements(Map<?, ? extends List<? extends IProgramElement>> annotationsMap) {
		AJCodeElement[] arrayOfajce = new AJCodeElement[2];
		Set<?> keys = annotationsMap.keySet();
    for (Object key : keys) {
      List<? extends IProgramElement> annotations = annotationsMap.get(key);
      for (IProgramElement node : annotations) {
        if (node.getHandleIdentifier()
          .equals("=AJProject83082/src<wpstest.aspectj{Main.java[Main~main~\\[QString;?method-call(void java.io.PrintStream.println(java.lang.String))")  //$NON-NLS-1$
        )
        {

          IJavaElement ije = model.programElementToJavaElement(node);
          if (ije instanceof AJCodeElement) {
            arrayOfajce[0] = (AJCodeElement) ije;
          }
        }
        else if (node.getHandleIdentifier()
          .equals("=AJProject83082/src<wpstest.aspectj{Main.java[Main~main~\\[QString;?method-call(void java.io.PrintStream.println(java.lang.String))!2")  //$NON-NLS-1$
        )
        {

          IJavaElement ije = model.programElementToJavaElement(node);
          if (ije instanceof AJCodeElement) {
            arrayOfajce[1] = (AJCodeElement) ije;
          }
        }
      }
    }
		return arrayOfajce;
	}



}
