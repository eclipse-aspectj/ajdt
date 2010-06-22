/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.refactoring;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.internal.core.refactoring.AspectRenameParticipant;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;

public class AspectRenameParticipantTest extends AJDTCoreTestCase {
    
    public void testDisabled() throws Exception {
        System.out.println("All tests in this class have been temporarily disabled");
    }

	public void _testTJPTypeRename() throws Exception {
		IProject project = createPredefinedProject("TJP Example"); //$NON-NLS-1$
		
		AJCompilationUnitManager.INSTANCE.initCompilationUnits(project.getWorkspace());
		
		
		AspectRenameParticipantTester participant = new AspectRenameParticipantTester(
				"Demo2"); //$NON-NLS-1$
		IFile file = project.getFile("src/tjp/Demo.java"); //$NON-NLS-1$
		assertTrue("File doesn't exist: " + file, file.exists()); //$NON-NLS-1$
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		assertNotNull("Couldn't obtain compilation unit for file " + file, //$NON-NLS-1$
				cu);
		IType demo = cu.getType("Demo"); //$NON-NLS-1$
		assertTrue("Compilation unit does not contain Demo type", demo //$NON-NLS-1$
				.exists());

		// set up participant
		participant.initialize(demo);

		// ask for changes
		Change allChanges = participant.createChange(new NullProgressMonitor());
		assertNotNull("Refactoring participant returned null change", //$NON-NLS-1$
				allChanges);
		assertTrue(
				"Expected refactoring participant to return a CompositeChange", //$NON-NLS-1$
				allChanges instanceof CompositeChange);
		CompositeChange composite = (CompositeChange) allChanges;
		Change[] children = composite.getChildren();
		assertEquals("Wrong number of children in CompositeChange", 1, //$NON-NLS-1$
				children.length);
		Change change = children[0];
		assertNotNull("name of change should not be null", change.getName()); //$NON-NLS-1$
		assertNotNull("getModifiedElement should not be null", change //$NON-NLS-1$
				.getModifiedElement());
		assertTrue(
				"Modified element should be GetInfo.aj", //$NON-NLS-1$
				change.getModifiedElement().toString().indexOf("GetInfo.aj") != -1); //$NON-NLS-1$

		// now apply the changes
		allChanges.perform(new NullProgressMonitor());

		// check the results
		IFile getInfo = project.getFile("src/tjp/GetInfo.aj"); //$NON-NLS-1$
		assertTrue("File doesn't exist: " + getInfo, getInfo.exists()); //$NON-NLS-1$

		String[] expected = new String[] {
				"pointcut goCut(): cflow(this(Demo2) && execution(void go()));", //$NON-NLS-1$
				"pointcut demoExecs(): within(Demo2) && execution(* *(..));", //$NON-NLS-1$
				"Object around(): demoExecs() && !execution(* go()) && goCut() {" }; //$NON-NLS-1$
		checkForExpected(getInfo, expected);
	}

	public void _testBeanTypeRename() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		AspectRenameParticipantTester participant = new AspectRenameParticipantTester(
				"Line"); //$NON-NLS-1$
		IFile file = project.getFile("src/bean/Point.java"); //$NON-NLS-1$
		assertTrue("File doesn't exist: " + file, file.exists()); //$NON-NLS-1$
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		assertNotNull("Couldn't obtain compilation unit for file " + file, //$NON-NLS-1$
				cu);
		IType point = cu.getType("Point"); //$NON-NLS-1$
		assertTrue("Compilation unit does not contain Point type", point //$NON-NLS-1$
				.exists());

		// set up participant
		participant.initialize(point);

		// ask for changes
		Change allChanges = participant.createChange(new NullProgressMonitor());
		assertNotNull("Refactoring participant returned null change", //$NON-NLS-1$
				allChanges);
		assertTrue(
				"Expected refactoring participant to return a CompositeChange", //$NON-NLS-1$
				allChanges instanceof CompositeChange);
		CompositeChange composite = (CompositeChange) allChanges;
		Change[] children = composite.getChildren();
		assertEquals("Wrong number of children in CompositeChange", 1, //$NON-NLS-1$
				children.length);
		Change change = children[0];
		assertNotNull("name of change should not be null", change.getName()); //$NON-NLS-1$
		assertNotNull("getModifiedElement should not be null", change //$NON-NLS-1$
				.getModifiedElement());
		assertTrue(
				"Modified element should be BoundPoint.aj", //$NON-NLS-1$
				change.getModifiedElement().toString().indexOf("BoundPoint.aj") != -1); //$NON-NLS-1$

		// now apply the changes
		allChanges.perform(new NullProgressMonitor());

		// check the results
		IFile getInfo = project.getFile("src/bean/BoundPoint.aj"); //$NON-NLS-1$
		assertTrue("File doesn't exist: " + getInfo, getInfo.exists()); //$NON-NLS-1$

		String[] expected = new String[] {
				"private PropertyChangeSupport Line.support = new PropertyChangeSupport(this);", //$NON-NLS-1$
				"public void Line.addPropertyChangeListener(PropertyChangeListener listener){", //$NON-NLS-1$
				"declare parents: Line implements Serializable;", //$NON-NLS-1$
				"declare parents: Demo implements Serializable;", //$NON-NLS-1$
				"pointcut setter(Line p): call(void Line.set*(*)) && target(p);", //$NON-NLS-1$
				"void around(Line p): setter(p) {", //$NON-NLS-1$
				"void firePropertyChange(Line p," }; //$NON-NLS-1$
		checkForExpected(getInfo, expected);
	}

	public void _testTypeRenameWithImports() throws Exception {
		IProject project = createPredefinedProject("RenameParticipation"); //$NON-NLS-1$
		AspectRenameParticipantTester participant = new AspectRenameParticipantTester(
				"Lemur"); //$NON-NLS-1$
		IFile file = project.getFile("src/p1/Test.java"); //$NON-NLS-1$
		assertTrue("File doesn't exist: " + file, file.exists()); //$NON-NLS-1$
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		assertNotNull("Couldn't obtain compilation unit for file " + file, //$NON-NLS-1$
				cu);
		IType test1 = cu.getType("Test"); //$NON-NLS-1$
		assertTrue("Compilation unit does not contain Test type", test1 //$NON-NLS-1$
				.exists());

		// set up participant
		participant.initialize(test1);

		// ask for changes
		Change allChanges = participant.createChange(new NullProgressMonitor());
		assertNotNull("Refactoring participant returned null change", //$NON-NLS-1$
				allChanges);
		assertTrue(
				"Expected refactoring participant to return a CompositeChange", //$NON-NLS-1$
				allChanges instanceof CompositeChange);
		CompositeChange composite = (CompositeChange) allChanges;
		Change[] children = composite.getChildren();
		assertEquals("Wrong number of children in CompositeChange", 1, //$NON-NLS-1$
				children.length);
		Change change = children[0];
		assertNotNull("name of change should not be null", change.getName()); //$NON-NLS-1$
		assertNotNull("getModifiedElement should not be null", change //$NON-NLS-1$
				.getModifiedElement());
		assertTrue(
				"Modified element should be MyAspect.aj", //$NON-NLS-1$
				change.getModifiedElement().toString().indexOf("MyAspect.aj") != -1); //$NON-NLS-1$

		// now apply the changes
		allChanges.perform(new NullProgressMonitor());

		// check the results
		IFile myaspect = project.getFile("src/test/MyAspect.aj"); //$NON-NLS-1$
		assertTrue("File doesn't exist: " + myaspect, myaspect.exists()); //$NON-NLS-1$

		String[] expected = new String[] { "before() : execution(void Lemur.foo(..))", //$NON-NLS-1$
		};
		checkForExpected(myaspect, expected);
	}

	public void _testTypeRenameWithImports2() throws Exception {
		IProject project = createPredefinedProject("RenameParticipation"); //$NON-NLS-1$
		AspectRenameParticipantTester participant = new AspectRenameParticipantTester(
				"Lemur"); //$NON-NLS-1$
		IFile file = project.getFile("src/p2/Test.java"); //$NON-NLS-1$
		assertTrue("File doesn't exist: " + file, file.exists()); //$NON-NLS-1$
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		assertNotNull("Couldn't obtain compilation unit for file " + file, //$NON-NLS-1$
				cu);
		IType test2 = cu.getType("Test"); //$NON-NLS-1$
		assertTrue("Compilation unit does not contain Test type", test2 //$NON-NLS-1$
				.exists());

		// set up participant
		participant.initialize(test2);

		// ask for changes
		Change allChanges = participant.createChange(new NullProgressMonitor());
		assertNull(
				"Refactoring participant should have returned null change as " //$NON-NLS-1$
						+ "aspect references p1.Test not p2.Test", //$NON-NLS-1$
				allChanges);
	}

	private void checkForExpected(IFile file, String[] expected)
			throws Exception {
		boolean[] got = new boolean[expected.length];
		InputStream is = file.getContents();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = br.readLine();
		while (line != null) {
			// System.out.println("line: " + line);
			boolean done = false;
			for (int i = 0; !done && (i < expected.length); i++) {
				if (line.indexOf(expected[i]) != -1) {
					// System.out.println("found i=" + i);
					got[i] = true;
					done = true;
				}
			}
			line = br.readLine();
		}
		br.close();
		is.close();
		StringBuffer missed = new StringBuffer();
		int notGot = 0;
		for (int i = 0; i < got.length; i++) {
			if (!got[i]) {
				notGot++;
				missed.append(expected[i]);
				missed.append('\n');
			}
		}
		if (notGot > 0) {
			fail("Didn't find " + notGot + " expected strings in file " + file //$NON-NLS-1$ //$NON-NLS-2$
					+ "\nMissed:\n" + missed.toString()); //$NON-NLS-1$
		}
	}
}

class AspectRenameParticipantTester extends AspectRenameParticipant {
	private String newName;

	public AspectRenameParticipantTester(String newName) {
		super();
		this.newName = newName;
	}

	protected boolean initialize(Object element) {
		return super.initialize(element);
	}

	public RenameArguments getArguments() {
		return new RenameArguments(newName, true);
	}
}