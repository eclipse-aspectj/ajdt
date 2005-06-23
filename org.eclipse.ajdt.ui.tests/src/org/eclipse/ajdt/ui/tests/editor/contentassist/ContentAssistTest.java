/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.editor.contentassist;

import junit.framework.TestCase;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.internal.ui.AJDTConfigSettings;
import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.internal.ui.editor.contentassist.AJCompletionProcessor;
import org.eclipse.ajdt.ui.tests.AllUITests;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Tests for completion proposals.
 * 
 * @author Luzius Meisser
 */
public class ContentAssistTest extends TestCase {

	IProject fProject;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		AllUITests.setupAJDTPlugin();
		AJDTConfigSettings.setDefaultEditorForJavaFiles(true);
		fProject = Utils.createPredefinedProject("CodeCompletionTestArea");
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		Utils.deleteProject(fProject);
	}
	
	public void testContentAssistA() throws JavaModelException{
		IFile file = (IFile)fProject.findMember("src/p2/Aspect.aj");
		ICompletionProposal[] props = getCompletionProposals(file, "/*completion test pos A*/");
		if (contains(props, "bar"))
			fail("The intertype declaration Foo.bar should not be visible here.");
		if (!contains(props, "x"))
			fail("Field x missing in completion proposals.");
		if (!contains(props, "limited AspectJ"))
			fail("Limited AspectJ support note missing");
	}
	
	public void testContentAssistB() throws JavaModelException{
		IFile file = (IFile)fProject.findMember("src/p2/Aspect.aj");
		ICompletionProposal[] props = getCompletionProposals(file, "/*completion test pos B*/");
		if (!contains(props, "localInt"))
			fail("local variable not visible.");
		if (!contains(props, "desiredAssertionStatus"))
			fail("Not all methods from java.lang.Class available.");
		if (contains(props, "x"))
			fail("Field x should not be visible.");
	}
	
	public void testContentAssistC() throws JavaModelException{
		IFile file = (IFile)fProject.findMember("src/p2/TestClass.java");
		ICompletionProposal[] props = getCompletionProposals(file, "/*completion test pos C*/");
		if (contains(props, "ajc$"))
			fail("AspectJ artefact members have not been filtered.");
		if (!contains(props, "aspectOf"))
			fail("Not all members of local variable asp are visible.");
		if (!contains(props, "limited AspectJ"))
			fail("Limited AspectJ support note missing");
	}
	
	public void testContentAssistD() throws JavaModelException{
		IFile file = (IFile)fProject.findMember("src/p2/TestClass.java");
		ICompletionProposal[] props = getCompletionProposals(file, "/*completion test pos D*/");
		if (!contains(props, "decw"))
			fail("AspectJ code template missing.");
	}
	
	public void testContentAssistE() throws JavaModelException{
		IFile file = (IFile)fProject.findMember("src/p2/Aspect.aj");
		ICompletionProposal[] props = getCompletionProposals(file, "/*completion test pos E*/");
		if (!contains(props, "thisJoinPoint"))
			fail("thisJoinPoint not available.");
		if (!contains(props, "meth"))
			fail("Member meth missing.");
		if (contains(props, "interMethod"))
			fail("interMethod should not be visible here.");
	}
	
	private ICompletionProposal[] getCompletionProposals(IFile file, String marker) throws JavaModelException{
		AspectJEditor editor = (AspectJEditor)Utils.openFileInDefaultEditor(file, false);
		AJCompletionProcessor proc = new AJCompletionProcessor(editor);
		AJCompilationUnit unit = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file);
		String content;
		if (unit != null){
			unit.reconcile(ICompilationUnit.NO_AST, false, null, null);
			unit.requestOriginalContentMode();
			content = unit.getSource();
			unit.discardOriginalContentMode();
		} else {
			CompilationUnit javaunit = (CompilationUnit)JavaCore.create(file);
			content = javaunit.getSource();
		}
		int offset = content.indexOf(marker);
		return proc.computeCompletionProposals(editor.getViewer(), offset);
	}
	
	private boolean contains(ICompletionProposal[] props, String what){
		for (int i = 0; i < props.length; i++) {
			ICompletionProposal proposal = props[i];
			if (proposal.getDisplayString().indexOf(what) != -1)
				return true;
		}
		return false;
	}

}
