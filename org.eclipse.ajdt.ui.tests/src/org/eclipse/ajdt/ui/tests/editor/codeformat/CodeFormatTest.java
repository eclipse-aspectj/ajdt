/*******************************************************************************
 * Copyright (c) 2004 Linton Ye
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial version (bug 79313)
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.editor.codeformat;

import java.util.ResourceBundle;

import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextOperationAction;

/**
 * @author Linton Ye
 */
public class CodeFormatTest extends UITestCase {
	IProject project;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = createPredefinedProject("CodeFormat");
		
	}
	
	public void testCodeFormat() {
		String filename = "src/bean/BoundPoint.aj";
		IFile sourceFile = (IFile)project.findMember(filename);
		if(sourceFile==null) fail("Cannot open file:"+filename);

		ITextEditor editorPart = (ITextEditor)openFileInDefaultEditor(
				(IFile)sourceFile, true);
		waitForJobsToComplete();
		
		IDocument document = editorPart.getDocumentProvider().getDocument(
				editorPart.getEditorInput());
		formatEditor(editorPart);
		waitForJobsToComplete();
		
		// save the buffer		
		editorPart.doSave(null);
		waitForJobsToComplete();

		verifyDoc1(document);
		verifyDoc2(document);
		verifyDoc3(document);
		verifyDoc4(document);
		verifyDoc5(document);//bug78023

		
		try {
			sourceFile.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		} catch (CoreException e) {
			fail("building failed.");
		}
		waitForJobsToComplete();
	}


	/**
	 * @param document
	 * @param lines
	 * @param lineNos
	 */
	private void verifyLines(IDocument document, String[] lines, int startFrom) {
		for(int l=0; l<lines.length; l++) {
			int offset;
			try {
				int lineNo = startFrom+l;
				offset = document.getLineOffset(lineNo);
				int length = document.getLineLength(lineNo);
				String line = document.get(offset, length);
				String lineDe = document.getLineDelimiter(lineNo);
				int lineDeLen = lineDe==null ? 0 : lineDe.length();
				// remove line delimiter
				line = line.substring(0, line.length()-lineDeLen);
				assertTrue(line+" correct:"+lines[l],line.equals(lines[l]));
			} catch (BadLocationException e) {
				failOnException("Exception occurs.", e);
			}
		}
	}

	/**
	 * @param string
	 * @param e
	 */
	private void failOnException(String string, BadLocationException e) {
		fail(string+" exception:"+e.toString()+" message:"+e.getMessage());
	}

	/**
	3. The setter pointcut should look like this:

		pointcut setter(Point p): call(void Point.set*(*))
	&& target(p);
	 
		 * @param document
		 */
	private void verifyDoc1(IDocument document) {
		String[] lines = {
				"\tpointcut setter(Point p): call(void Point.set*(*))",
				"&& target(p);"
		};
		//			int[] lineNos = {65, 66};
		verifyLines(document, lines, 65);
	}

	/**
4. The comment before the around advice should look like this:

	/**
	 * Advice to get the property change event fired when the setters are
	 * called. It's around advice because you need the old value of the
	 * property.
	 *

	 * @param document
	 */
	private void verifyDoc2(IDocument document) {
		String[] lines = {
				"\t/**",
				"\t * Advice to get the property change event fired when the setters are",
				"\t * called. It's around advice because you need the old value of the",
				"\t * property.",
				"\t */"
		};
//		int[] lineNos = {68, 69, 70, 71, 72};
		verifyLines(document, lines, 68);
	}

	/**
5. The around advice itself should look like this:

	void around(Point p): setter(p) {
		String propertyName = thisJoinPointStaticPart.getSignature().getName()
				.substring("set".length());
		int oldX = p.getX();
		int oldY = p.getY();
		proceed(p);
		if (propertyName.equals("X")) {
			firePropertyChange(p, propertyName, oldX, p.getX());
		} else {
			firePropertyChange(p, propertyName, oldY, p.getY());
		}
	}

	 * @param document
	 */
	private void verifyDoc3(IDocument document) {
		String[] lines = {
				"\tvoid around(Point p): setter(p) {",
				"\t\tString propertyName = thisJoinPointStaticPart.getSignature().getName()",
				"\t\t\t\t.substring(\"set\".length());",
				"\t\tint oldX = p.getX();",
				"\t\tint oldY = p.getY();",
				"\t\tproceed(p);",
				"\t\tif (propertyName.equals(\"X\")) {",
				"\t\t\tfirePropertyChange(p, propertyName, oldX, p.getX());",
				"\t\t} else {",
				"\t\t\tfirePropertyChange(p, propertyName, oldY, p.getY());",
				"\t\t}",
				"\t}"
		};
		verifyLines(document, lines, 73);
	}

	/**
6. The firePropertyChange method should look like this:

	void firePropertyChange(Point p, String property, double oldval,
			double newval) {
		p.support.firePropertyChange(property, new Double(oldval), new Double(
				newval));
	}

	 * @param document
	 */
	private void verifyDoc4(IDocument document) {
		String[] lines = {
				"\tvoid firePropertyChange(Point p, String property, double oldval,",
				"\t\t\tdouble newval) {",
				"\t\tp.support.firePropertyChange(property, new Double(oldval), new Double(",
				"\t\t\t\tnewval));",
				"\t}"
		};
		verifyLines(document, lines, 89);
	}

	/**
7. Check that the aspect is still "privileged" (bug 78023)
	 * @param document
	 */
	private void verifyDoc5(IDocument document) {
		String[] lines = {
				"privileged aspect BoundPoint {"
		};
		verifyLines(document, lines, 23);
		
	}

	/**
	 * @param doc
	 */
	private void formatEditor(ITextEditor editor) {
		IAction act= new TextOperationAction(
				ResourceBundle.getBundle(UIMessages.class.getName()),
				"Format.", editor, ISourceViewer.FORMAT); //$NON-NLS-1$
		act.run();
	}
}
