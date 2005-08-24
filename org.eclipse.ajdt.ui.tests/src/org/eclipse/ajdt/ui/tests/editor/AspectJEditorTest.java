/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Luzius Meisser  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests.editor;

import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorPart;

/**
 * Tests for AspectJEditor.
 * TODO: add more :)
 * 
 * @author Luzius Meisser
 */
public class AspectJEditorTest extends UITestCase {

	IProject project;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = createPredefinedProject("Simple AJ Project");
	}
	
	private void openFileTest(IFile file, boolean shouldBeOpenedInAspectJEditor){
		IEditorPart editor = openFileInDefaultEditor(file, false);
		if (editor == null)
			fail("Editor for file " + file.getName() + " could not be opened.");
		if (shouldBeOpenedInAspectJEditor && !(editor instanceof AspectJEditor))
			fail("File " + file.getName() + " was opened in editor " + editor.getClass() + " instead of AspectJEditor.");
	}
	
	public void testOpenJavaFile(){
		IResource res = project.findMember("src/p1/Main.java");
		openFileTest((IFile)res, false);
	}
	
	public void testOpenAJFile(){
		IResource res = project.findMember("src/p2/Aspect.aj");
		openFileTest((IFile)res, true);
	}


}
