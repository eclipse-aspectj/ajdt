/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.internal.builder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

/**
 * Tests for mapping between IProgramElement and IJavaElements
 * 
 * @author Matt Chapman
 */
public class AJModelTest extends TestCase {

	private IProject project;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = Utils.getPredefinedProject("MarkersTest", true);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Utils.waitForJobsToComplete(project);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testProgramElementToJavaElementDemo() {
		String filename = "src/tjp/Demo.java";
		String[][] results = {
				{ "Demo", "Demo" },
				{ "main(String[])", "main" },
				{ "go()", "go" },
				{ "field-set(int tjp.Demo.x)", "field-set(int tjp.Demo.x)" },
				{ "foo(int, Object)", "foo" },
				{ "exception-handler(void tjp.Demo.<catch>(tjp.DemoException))", "exception-handler(void tjp.Demo.<catch>(tjp.DemoException))" },
				{ "bar(Integer)", "bar" }
		};
		mappingTestForFile(filename, results);
		
	}
	
	public void testProgramElementToJavaElementGetInfo() {
		String filename = "src/tjp/GetInfo.aj";
		String[][] results = {
				{ "GetInfo", "GetInfo" },
				{ "declare warning: \"field set\"", "declare warning: \"field set\"" },
				{ "declare parents: implements Serializable", "declare parents" },
			    { "declare soft: tjp.DemoException", "declare soft" },
				{ "Demo.itd(int)", "Demo.itd" },
				{ "Demo.f", "Demo.f" },
				{ "before(): <anonymous pointcut>", "before" },
				{ "goCut()", "goCut" },
				{ "fieldSet()", "fieldSet" },
				{ "demoExecs()", "demoExecs" },
				{ "before(): demoExecs..", "before" },
				{ "before(): <anonymous pointcut>..", "before" },
				{ "after(): fieldSet..", "after" },
				{ "around(): demoExecs()..", "around" },
				{ "after(): <anonymous pointcut>", "after" },
				{ "printParameters(JoinPoint)", "printParameters" }
		};
		mappingTestForFile(filename, results);
	}
	
	private void mappingTestForFile(String filename, String[][] results) {
		IFile file = (IFile)project.findMember(filename);
		if (file == null)
			fail("Required file not found: " + filename);
		
		String path = file.getRawLocation().toOSString();
		Map annotationsMap = AsmManager.getDefault().getInlineAnnotations(path,
				true, true);
		
		assertNotNull("Didn't get annotations map for file: "+path,annotationsMap);

		ICompilationUnit unit = AJCompilationUnitManager.INSTANCE
			.getAJCompilationUnit(file);
		if (unit == null) {
			unit = JavaCore.createCompilationUnitFrom(file);
		}

		assertNotNull("Didn't get a compilation unit from file: "+path,unit);
		
		List toFind = new ArrayList();
		List toMatch = new ArrayList();
		for (int i = 0; i < results.length; i++) {
			toFind.add(results[i][0].intern());
			toMatch.add(results[i][1].intern());
		}
		
		Set keys = annotationsMap.keySet();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			Object key = it.next();
			List annotations = (List) annotationsMap.get(key);
			for (Iterator it2 = annotations.iterator(); it2.hasNext();) {
				IProgramElement node = (IProgramElement) it2.next();
				String peName = node.toLabelString().intern();;
				IJavaElement je = AJModel.getInstance().getCorrespondingJavaElement(node);
				if (je==null) {
					System.out.println("je is null");
					continue;
				}
				String jaName = je.getElementName().intern();
				//System.out.println("node="+peName);
				//System.out.println("je="+jaName);
				int index = toFind.indexOf(peName);
				if (index == -1) {
					fail("Unexpected additional IProgramElement name found: "+peName);
				} else {
					String expected = (String)toMatch.get(index);
					if (expected.equals(jaName)) {
						toFind.remove(index);
						toMatch.remove(index);
					} else {
						fail("Incorrect corresponding Java element. Found: "+jaName+" Expected: "+expected);
					}
				}
			}
		}
		
		// check that we found everything we were looking for
		if (toFind.size() > 0) {
			String missing = "";
			for (int j = 0; j < toFind.size(); j++) {
				missing += System.getProperty("line.separator");
				missing += (String)toFind.get(j);					
			}
			fail("Did not find all expected IProgramElement names. Missing: " + missing);
		}
	}
}