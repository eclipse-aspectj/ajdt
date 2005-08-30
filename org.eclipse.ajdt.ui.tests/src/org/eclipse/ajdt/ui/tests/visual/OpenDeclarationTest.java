/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.visual;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.ui.tests.AspectJTestPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

public class OpenDeclarationTest extends VisualTestCase {
	public void testOpenDeclaration() throws Exception {
		IProject project = createPredefinedProject("OpenDeclaration");
		assertNotNull("The OpenDeclaration project should have been created",
				project);
		IFile getinfo = (IFile) project.findMember("src/tjp/GetInfo.aj");
		assertNotNull(
				"The OpenDeclaration project should contain a file called 'GetInfo.aj'",
				getinfo);
		// make sure ajdt knows about the supertype - we probably shouldn't need
		// this
		AJCompilationUnitManager.INSTANCE.initCompilationUnits(AspectJPlugin
				.getWorkspace());
		
		// workaround for Eclipse 3.0 limitation: supertype can only be resolved
		// if it is open in the editor
		openFileInDefaultEditor((IFile) project
				.findMember("src/foo/AbstractGetInfo.aj"), true);
		
		openFileInDefaultEditor(getinfo, true);

		// F3 over the demoExecs pointcut used in around advice
		// perform the test at each valid cursor position
		for (int i = 20; i <= 20; i++) {
			gotoLine(28);
			moveCursorRight(i);
			postKey(SWT.F3);
			waitForJobsToComplete();
			checkSelection(25, "demoExecs");
		}
		// check that F3 either side of the pointcut doesnt work
		gotoLine(28);
		moveCursorRight(19);
		postKey(SWT.F3);
		checkEmptySelection();
		gotoLine(28);
		moveCursorRight(21);
		postKey(SWT.F3);
		checkEmptySelection();

		// F3 over the goCut pointcut used in the demoExecs pointcut
		gotoLine(26);
		moveCursorRight(43);
		postKey(SWT.F3);
		waitForJobsToComplete();
		checkSelection(23, "goCut");

		// F3 over the executeGo pointcut inherited from the supertype
		gotoLine(24);
		moveCursorRight(46);
		postKey(SWT.F3);
		waitForJobsToComplete();
		checkSelection(3, "executeGo");

		// close the editor opened for the supertype, to return to GetInfo.aj
		IWorkbenchPage page = getWindow().getActivePage();
		IEditorPart activeEditor = page.getActiveEditor();
		page.closeEditor(activeEditor, false);

		// F3 over the demoExecs2 pointcut defined in the Demo type
		gotoLine(40);
		moveCursorRight(24);
		postKey(SWT.F3);
		waitForJobsToComplete();
		checkSelection(18, "demoExecs2");
	}

	private IWorkbenchWindow getWindow() {
		IWorkbenchWindow window = AspectJTestPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null) {
			fail("Couldn't get active workbench window");
		}
		return window;
	}

	private TextSelection getCurrentTextSelection() {
		ISelection sel = getWindow().getSelectionService().getSelection();
		if (!(sel instanceof TextSelection)) {
			fail("Expected ISelection to be a TextSelection");
		}
		return (TextSelection) sel;
	}

	private void checkEmptySelection() {
		TextSelection ts = getCurrentTextSelection();
		assertTrue("Current text selection should be empty", ts.getText()
				.length() == 0);
	}

	private void checkSelection(int expectedLine, String expectedText) {
		TextSelection ts = getCurrentTextSelection();
		assertTrue("Pressing F3 should have selected the pointcut on line "
				+ expectedLine + ". got start line: " + ts.getStartLine(), ts
				.getStartLine() == expectedLine);
		assertTrue("Pressing F3 should have selected the pointcut on line "
				+ expectedLine + ". got end line: " + ts.getEndLine(), ts
				.getEndLine() == expectedLine);
		assertEquals("Pressing F3 should have selected the " + expectedText
				+ " pointcut." + ts.getText(), expectedText, ts.getText());
	}
}
