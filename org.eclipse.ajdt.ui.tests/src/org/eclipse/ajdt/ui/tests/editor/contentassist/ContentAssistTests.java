/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.editor.contentassist;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.internal.ui.editor.contentassist.AJCompletionProcessor;
import org.eclipse.ajdt.ui.tests.AllUITests;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.ui.text.java.ContentAssistProcessor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProcessor;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocCompletionProcessor;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests for completion proposals.
 * 
 * @author Luzius Meisser
 */
public class ContentAssistTests extends UITestCase {

	IProject fProject;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		AllUITests.setupAJDTPlugin();
		fProject = createPredefinedProject("CodeCompletionTestArea"); //$NON-NLS-1$
	}
	
	public void testContentAssistA() throws JavaModelException{
		IFile file = (IFile)fProject.findMember("src/p2/Aspect.aj"); //$NON-NLS-1$
		ICompletionProposal[] props = getCompletionProposals(file, "/*completion test pos A*/"); //$NON-NLS-1$
		if (contains(props, "bar")) //$NON-NLS-1$
			fail("The intertype declaration Foo.bar should not be visible here."); //$NON-NLS-1$
		if (!contains(props, "x37")) //$NON-NLS-1$
			fail("Field x37 missing in completion proposals."); //$NON-NLS-1$
		if (!contains(props, "limited AspectJ")) //$NON-NLS-1$
			fail("Limited AspectJ support note missing"); //$NON-NLS-1$
	}
	
	public void testContentAssistB() throws JavaModelException{
		IFile file = (IFile)fProject.findMember("src/p2/Aspect.aj"); //$NON-NLS-1$
		ICompletionProposal[] props = getCompletionProposals(file, "/*completion test pos B*/"); //$NON-NLS-1$
		if (!contains(props, "localInt")) //$NON-NLS-1$
			fail("local variable not visible."); //$NON-NLS-1$
		if (!contains(props, "desiredAssertionStatus")) //$NON-NLS-1$
			fail("Not all methods from java.lang.Class available."); //$NON-NLS-1$
		if (contains(props, "x37")) //$NON-NLS-1$
			fail("Field x37 should not be visible."); //$NON-NLS-1$
	}
	
	public void testContentAssistC() throws JavaModelException{
		IFile file = (IFile)fProject.findMember("src/p2/TestClass.java"); //$NON-NLS-1$
		ICompletionProposal[] props = getCompletionProposals(file, "/*completion test pos C*/"); //$NON-NLS-1$
		if (contains(props, "ajc$")) //$NON-NLS-1$
			fail("AspectJ artefact members have not been filtered."); //$NON-NLS-1$
		if (!contains(props, "aspectOf")) //$NON-NLS-1$
			fail("Not all members of local variable asp are visible."); //$NON-NLS-1$
		if (!contains(props, "hasAspect")) //$NON-NLS-1$
		    fail("Not all members of local variable asp are visible."); //$NON-NLS-1$
		if (!contains(props, "limited AspectJ")) //$NON-NLS-1$
			fail("Limited AspectJ support note missing"); //$NON-NLS-1$
	}
	
	public void testContentAssistD() throws JavaModelException{
		IFile file = (IFile)fProject.findMember("src/p2/TestClass.java"); //$NON-NLS-1$
		ICompletionProposal[] props = getCompletionProposals(file, "/*completion test pos D*/"); //$NON-NLS-1$
		if (contains(props, "decw")) //$NON-NLS-1$
			fail("AspectJ code template exists in a java file, but it should not."); //$NON-NLS-1$
	}
	
    public void testContentAssistE() throws JavaModelException{
        IFile file = (IFile)fProject.findMember("src/p2/Aspect.aj"); //$NON-NLS-1$
        ICompletionProposal[] props = getCompletionProposals(file, "/*completion test pos E*/"); //$NON-NLS-1$
        if (!contains(props, "thisJoinPoint")) //$NON-NLS-1$
            fail("thisJoinPoint not available."); //$NON-NLS-1$
        if (!contains(props, "meth")) //$NON-NLS-1$
            fail("Member meth missing."); //$NON-NLS-1$
        if (contains(props, "interMethod")) //$NON-NLS-1$
            fail("interMethod should not be visible here."); //$NON-NLS-1$
    }
    public void testContentAssistF() throws JavaModelException{
        IFile file = (IFile)fProject.findMember("src/p2/Aspect.aj"); //$NON-NLS-1$
        ICompletionProposal[] props = getCompletionProposals(file, "/*completion test pos F*/"); //$NON-NLS-1$
        if (!contains(props, "decw")) //$NON-NLS-1$
            fail("AspectJ code template missing."); //$NON-NLS-1$
    }
	
	private ICompletionProposal[] getCompletionProposals(IFile file, String marker) throws JavaModelException{
		AspectJEditor editor = (AspectJEditor)openFileInAspectJEditor(file, false);
		AJCompletionProcessor proc = new AJCompletionProcessor(editor, getContentAssistant(editor), IDocument.DEFAULT_CONTENT_TYPE);
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
	
	private ContentAssistant getContentAssistant(ITextEditor editor) {
		ContentAssistant assistant= new ContentAssistant();
		IContentAssistProcessor javaProcessor= new JavaCompletionProcessor(editor, assistant, IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(javaProcessor, IDocument.DEFAULT_CONTENT_TYPE);
	
		ContentAssistProcessor singleLineProcessor= new JavaCompletionProcessor(editor, assistant, IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);
		assistant.setContentAssistProcessor(singleLineProcessor, IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);
	
		ContentAssistProcessor stringProcessor= new JavaCompletionProcessor(editor, assistant, IJavaPartitions.JAVA_STRING);
		assistant.setContentAssistProcessor(stringProcessor, IJavaPartitions.JAVA_STRING);
		
		ContentAssistProcessor multiLineProcessor= new JavaCompletionProcessor(editor, assistant, IJavaPartitions.JAVA_MULTI_LINE_COMMENT);
		assistant.setContentAssistProcessor(multiLineProcessor, IJavaPartitions.JAVA_MULTI_LINE_COMMENT);
	
		ContentAssistProcessor javadocProcessor= new JavadocCompletionProcessor(editor, assistant);
		assistant.setContentAssistProcessor(javadocProcessor, IJavaPartitions.JAVA_DOC);
	
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		return assistant;
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
