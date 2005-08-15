/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.testutils.ReaderInputStream;
import org.eclipse.ajdt.core.tests.testutils.TestLogger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Bug 99133 - if A depends on B, and you make a change to
 * B such that A should notice, A was not being built
 * 
 * The tests in this testcase are different scenarios around this.
 */
public class Bug99133Test extends AJDTCoreTestCase {
	
	IProject pA,pB;
	TestLogger testLog;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		pB = createPredefinedProject("bug99133b");
		waitForAutoBuild();
		waitForAutoBuild();
		pA = createPredefinedProject("bug99133a");
		waitForAutoBuild();
		waitForAutoBuild();
		checkForJDTBug84214(pA,pB);
		waitForAutoBuild();
		waitForAutoBuild();
		// test for bug 107027 - there were classpath problems if this
		// test is run after others because we didn't flush the classpath
		// in the core plugin
		assertFalse("log should contain no errors",testLog.containsMessage("error"));
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		AspectJPlugin.getDefault().setAJLogger(null);
		testLog = null;
		deleteProject(pA);
		deleteProject(pB);
	}
	
	/**
	 * A depends on B and in particular calls a method in B
	 * and that method body changes. Ultimately, no build of project A should 
	 * occurr. However, AJDT can't tell whether to build or not and so we 
	 * pass it down to the compiler. The compiler then uses A's incremental
	 * state to say that nothing has structurely changed and 
	 * returns quickly. 
	 * (both A and B are AJ projects)
	 */
	public void testBug99133a() throws Exception {
		// change the contents of the method m1() in 
		// bug99133b\src\p\C1.java to include a sysout call
		IFile c1 = getFile(pB,"p","C1.java");
		BufferedReader br1 = new BufferedReader(new InputStreamReader(c1.getContents()));

		StringBuffer sb1 = new StringBuffer();
		int lineNumber1 = 1;
		String line1 = br1.readLine();
		while (line1 != null) {
			if (lineNumber1 == 5) {
				sb1.append("System.out.println(\"Hello\");");
			} else {
				sb1.append(line1);
			}
			sb1.append(System.getProperty("line.separator"));
			lineNumber1++;
			line1 = br1.readLine();
		}
		br1.close();
		StringReader reader1 = new StringReader(sb1.toString());
		c1.setContents(new ReaderInputStream(reader1), true, true, null);
		waitForAutoBuild();
		waitForAutoBuild();
		
		// At the moment, can at least check that the build of the
		// dependent project is an incremental build.
		List buildLogB = testLog.getPreviousBuildEntry(2);
		boolean incB = listContainsString(buildLogB,
				"AspectJ reports build successful, build was: INCREMENTAL");
		boolean fullB = listContainsString(buildLogB,
				"AspectJ reports build successful, build was: FULL");
		// if not an incremental build of project bug99133b build then fail
		// printing out whether did a full build instead
		if (!incB) {
			fail("Changing method contents of a method in project bug99133b " 
				+ "should cause an incremental build of project bug99133b "
				+ ": (did a full build instead:" + fullB+")");
		}
		
		List buildLogA = testLog.getPreviousBuildEntry(1);
		boolean incA = listContainsString(buildLogA,
				"AspectJ reports build successful, build was: INCREMENTAL");
		boolean fullA = listContainsString(buildLogA,
				"AspectJ reports build successful, build was: FULL");
		// if not an incremental build of project bug99133a build then fail
		// printing out whether did a full build instead
		if (!incA) {
			fail("Changing method contents of method in project bug99133b "
					+ "should cause an incremental build of dependent project "
					+ "bug99133a : (did a full build instead:" + fullA + ")");
		}
	}

	/**
	 * A depends on B and in particular calls a method in B
	 * and the signature of that method changes. Then an
	 * incremental build of project B and a full build of project A 
	 * should occurr (both A and B are AJ projects). 
	 */
	public void testBug99133b() throws Exception {
		//change the return type of the method m1() in bug99133b\src\p\C1.java
		IFile c1 = getFile(pB,"p","C1.java");
		BufferedReader br1 = new BufferedReader(new InputStreamReader(c1.getContents()));

		StringBuffer sb1 = new StringBuffer();
		int lineNumber1 = 1;
		String line1 = br1.readLine();
		while (line1 != null) {
			if (lineNumber1 == 4) {
				sb1.append("public String m1() {");
			} else if (lineNumber1 == 5) {
				sb1.append("return \"Hello\";");
			} else {
				sb1.append(line1);
			}
			sb1.append(System.getProperty("line.separator"));
			lineNumber1++;
			line1 = br1.readLine();
		}
		br1.close();
		StringReader reader1 = new StringReader(sb1.toString());
		c1.setContents(new ReaderInputStream(reader1), true, true, null);
		waitForAutoBuild();
		waitForAutoBuild();
		waitForAutoBuild();
		waitForAutoBuild();
	
		List buildLogB = testLog.getPreviousBuildEntry(2);
		boolean incB = listContainsString(buildLogB,
				"AspectJ reports build successful, build was: INCREMENTAL");
		boolean fullB = listContainsString(buildLogB,
				"AspectJ reports build successful, build was: FULL");
		// if not an incremental build of project bug99133b build then fail
		// printing out whether did a full build instead
		if (!incB) {
			fail("Changing the method signature of method in project bug99133b " 
				+ "should cause an incremental build of project bug99133b "
				+ ": (did a full build instead:" + fullB+")");
		}
		
		List buildLogA = testLog.getPreviousBuildEntry(1);
		boolean incA = listContainsString(buildLogA,
				"AspectJ reports build successful, build was: INCREMENTAL");
		boolean fullA = listContainsString(buildLogA,
				"AspectJ reports build successful, build was: FULL");
		// if not a full build of project bug99133a build then fail
		// printing out whether did an incremental build instead
		if (!fullA) {
			fail("Changing the method signature of a method in project bug99133b "
					+ "should cause an full build of dependent project bug99133a "
					+ ": (did an incremental build instead:" + incA + ")");
		}
	}

	/**
	 * A depends on B and in particular calls a method in B.
	 * The method body of a method not referenced in A changes. 
	 */
	public void testBug99133c() throws Exception {
		// change the contents of the method m1() in 
		// bug99133b\src\p\C1.java to include a sysout call
		IFile c1 = getFile(pB,"p","C1.java");
		BufferedReader br1 = new BufferedReader(new InputStreamReader(c1.getContents()));

		StringBuffer sb1 = new StringBuffer();
		int lineNumber1 = 1;
		String line1 = br1.readLine();
		while (line1 != null) {
			if (lineNumber1 == 9) {
				sb1.append("System.out.println(\"Hello\");");
			} else {
				sb1.append(line1);
			}
			sb1.append(System.getProperty("line.separator"));
			lineNumber1++;
			line1 = br1.readLine();
		}
		br1.close();
		StringReader reader1 = new StringReader(sb1.toString());
		c1.setContents(new ReaderInputStream(reader1), true, true, null);
		waitForAutoBuild();
		waitForAutoBuild();

		// At the moment, can at least check that the build is an
		// incremental build.
		List buildLogB = testLog.getPreviousBuildEntry(2);
		boolean incB = listContainsString(buildLogB,
				"AspectJ reports build successful, build was: INCREMENTAL");
		boolean fullB = listContainsString(buildLogB,
				"AspectJ reports build successful, build was: FULL");
		// if not an incremental build of project bug99133b build then fail
		// printing out whether did a full build instead
		if (!incB) {
			fail("Changing the method contents of a method in project bug99133b " 
				+ "should cause an incremental build of project bug99133b "
				+ ": (did a full build instead:" + fullB+")");
		}
		
		List buildLogA = testLog.getPreviousBuildEntry(1);
		boolean incA = listContainsString(buildLogA,
				"AspectJ reports build successful, build was: INCREMENTAL");
		boolean fullA = listContainsString(buildLogA,
				"AspectJ reports build successful, build was: FULL");
		// if not an incremental build of project bug99133a build then fail
		// printing out whether did a full build instead
		if (!incA) {
			fail("Changing the method contents of an unreferenced method " 
					+ "in project bug99133b should cause an incremental build " 
					+ "of dependent project bug99133a "
					+ ": (did a full build instead:" + fullA + ")");
		}

	}

	/**
	 * A depends on B and in particular calls a method in B.
	 * The signature of an unreferenced method in B changes. Then an
	 * incremental build of project B and a full build of project A
	 * should occur. (both A and B are AJ projects)
	 */
	public void testBug99133d() throws Exception {

		//change the return type of the method m1() in bug99133b\src\p\C1.java
		IFile c1 = getFile(pB,"p","C1.java");
		BufferedReader br1 = new BufferedReader(new InputStreamReader(c1.getContents()));

		StringBuffer sb1 = new StringBuffer();
		int lineNumber1 = 1;
		String line1 = br1.readLine();
		while (line1 != null) {
			if (lineNumber1 == 8) {
				sb1.append("public String m2() {");
			} else if (lineNumber1 == 9) {
				sb1.append("return \"Hello\";");
			} else {
				sb1.append(line1);
			}
			sb1.append(System.getProperty("line.separator"));
			lineNumber1++;
			line1 = br1.readLine();
		}
		br1.close();
		StringReader reader1 = new StringReader(sb1.toString());
		c1.setContents(new ReaderInputStream(reader1), true, true, null);
		waitForAutoBuild();
		waitForAutoBuild();


		List buildLogB = testLog.getPreviousBuildEntry(2);
		boolean incB = listContainsString(buildLogB,
				"AspectJ reports build successful, build was: INCREMENTAL");
		boolean fullB = listContainsString(buildLogB,
				"AspectJ reports build successful, build was: FULL");
		// if not an incremental build of project bug99133b build then fail
		// printing out whether did a full build instead
		if (!incB) {
			fail("Changing the signature of a method in project bug99133b " 
				+ "should cause an incremental build of project bug99133b "
				+ ": (did a full build instead:" + fullB+")");
		}
		
		List buildLogA = testLog.getPreviousBuildEntry(1);
		boolean incA = listContainsString(buildLogA,
				"AspectJ reports build successful, build was: INCREMENTAL");
		boolean fullA = listContainsString(buildLogA,
				"AspectJ reports build successful, build was: FULL");
		// if not a full build of project bug99133a build then fail
		// printing out whether did an incremental build instead
		if (!fullA) {
			fail("Changing the signature of an unreferenced method in" 
					+ " project bug99133b should cause a full build of "
					+ " dependent project bug99133a "
					+ ": (did an incremental build instead:" + incA + ")");
		}
	}

	/**
	 * A depends on B and in particular calls a method in B.
	 * A new method is added to a class in B. 
	 * Then an incremental build of project B and a full build
	 * of project A should occurr. (both A and B are AJ projects)
	 */
	public void testBug99133e() throws Exception {

		//change the return type of the method m1() in bug99133b\src\p\C1.java
		IFile c1 = getFile(pB,"p","C1.java");
		BufferedReader br1 = new BufferedReader(new InputStreamReader(c1.getContents()));

		StringBuffer sb1 = new StringBuffer();
		int lineNumber1 = 1;
		String line1 = br1.readLine();
		while (line1 != null) {
			if (lineNumber1 == 10) {
				sb1.append("}");
				sb1.append(System.getProperty("line.separator"));
				sb1.append("public void m3() {}");
			} else {
				sb1.append(line1);
			}
			sb1.append(System.getProperty("line.separator"));
			lineNumber1++;
			line1 = br1.readLine();
		}
		br1.close();
		StringReader reader1 = new StringReader(sb1.toString());
		c1.setContents(new ReaderInputStream(reader1), true, true, null);
		waitForAutoBuild();
		waitForAutoBuild();

		List buildLogB = testLog.getPreviousBuildEntry(2);
		boolean incB = listContainsString(buildLogB,
				"AspectJ reports build successful, build was: INCREMENTAL");
		boolean fullB = listContainsString(buildLogB,
				"AspectJ reports build successful, build was: FULL");
		// if not an incremental build of project bug99133b build then fail
		// printing out whether did a full build instead
		if (!incB) {
			fail("Adding a method in project bug99133b " 
				+ "should cause an incremental build of project bug99133b "
				+ ": (did a full build instead:" + fullB+")");
		}
		
		List buildLogA = testLog.getPreviousBuildEntry(1);
		boolean incA = listContainsString(buildLogA,
				"AspectJ reports build successful, build was: INCREMENTAL");
		boolean fullA = listContainsString(buildLogA,
				"AspectJ reports build successful, build was: FULL");
		// if not a full build of project bug99133a build then fail
		// printing out whether did an incremental build instead
		if (!fullA) {
			fail("Adding a method in project bug99133b "
					+ "should cause an full build of dependent project bug99133b "
					+ ": (did an incremental build instead:" + incA + ")");
		}

	}
	
	
	private void addProjectDependency(IProject project, IProject projectDependedOn) throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] originalCP = javaProject.getRawClasspath();
		IClasspathEntry newEntry = JavaCore.newProjectEntry(projectDependedOn.getFullPath());
		int originalCPLength = originalCP.length;
		IClasspathEntry[] newCP = new IClasspathEntry[originalCPLength + 1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCPLength);
		newCP[originalCPLength] = newEntry;
		javaProject.setRawClasspath(newCP, null);
		waitForAutoBuild();
	}
	
	private void removeProjectDependency(IProject project, IProject projectDependedOn) throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
		List newEntries = new ArrayList();

		for (int j = 0; j < cpEntry.length; j++) {
			IClasspathEntry entry = cpEntry[j];
			if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				if (!entry.getPath().equals(projectDependedOn.getFullPath())
						&& !entry.getPath().equals(
								projectDependedOn.getFullPath().makeAbsolute())) {
					newEntries.add(entry);
				}
			} else {
				newEntries.add(entry);
			}
		}
		IClasspathEntry[] newCP = (IClasspathEntry[]) newEntries
				.toArray(new IClasspathEntry[newEntries.size()]);
		javaProject.setRawClasspath(newCP, null);
	}
	
	private IFile getFile(IProject project, String packageName, String fileName) throws CoreException {
		IFolder src = project.getFolder("src");
		if (!src.exists()) {
			src.create(true, true, null);
		}
		IFolder pack = src.getFolder(packageName);
		if (!pack.exists()) {
			pack.create(true, true, null);
		}
		
		assertNotNull("src folder should not be null", src);
		assertNotNull("package pack should not be null", pack);

		IFile f = pack.getFile(fileName);
		assertNotNull(fileName + " should not be null", f);
		assertTrue(fileName + " should exist", f.exists());
		return f;
	}
	
	/**
	 * There is JDT bug 84214 - sometimes project dependencies don't
	 * get picked up properly. Therefore, to work around this, if
	 * remove, then re-add the project dependency.
	 * 
	 * @throws JavaModelException 
	 */
	private void checkForJDTBug84214(IProject projectWhichShouldHaveDependency, IProject projectDependedOn) throws JavaModelException {
		if (projectDependedOn.getReferencingProjects().length == 0) {
			removeProjectDependency(projectWhichShouldHaveDependency,projectDependedOn);
			waitForAutoBuild();
			addProjectDependency(projectWhichShouldHaveDependency,projectDependedOn);
			waitForAutoBuild();
			waitForAutoBuild();
		}
		assertEquals(" " + projectDependedOn  + " should have " 
				+ projectWhichShouldHaveDependency 
				+ " as it's list of referencing projects - if not, see JDT bug 84214",
				1, projectDependedOn.getReferencingProjects().length);
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
	
	private boolean listContainsString(List l, String msg) {
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            String logEntry = (String) iter.next();
            if (logEntry.indexOf(msg) != -1) {
                return true;
            }
        }
        return false;
	}
	
	private void printListOfStrings(List l) {
		System.out.println("");
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			System.out.println("LIST: " + element);
		}
		System.out.println("");
	}
	
}
