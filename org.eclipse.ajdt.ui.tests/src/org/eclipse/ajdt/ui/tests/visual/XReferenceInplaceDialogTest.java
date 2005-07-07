/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.visual;

import java.util.Iterator;

import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class XReferenceInplaceDialogTest extends VisualTestCase {

	private IProject project;
	
	protected void setUp() throws Exception {	
		super.setUp();
		project = Utils.createPredefinedProject("bug102865");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		Utils.deleteProject(project);
	}
	
	public void testEscape() {
		IResource res = project.findMember("src/pack/A.aj");
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found.");
		} 
		IFile ajFile = (IFile)res;

		// open Aspect.aj and select the pointcut
		final ITextEditor editorPart = (ITextEditor)Utils.openFileInAspectJEditor(ajFile, false);
		editorPart.setFocus();
		gotoLine(8);
		moveCursorRight(8);
		Utils.waitForJobsToComplete();
		
		// open inplace outline view
		postKeyDown(SWT.CTRL);
		postKeyDown(SWT.ALT);
		postCharacterKey('x');
		postKeyUp(SWT.ALT);
		postKeyUp(SWT.CTRL);
		
		// wait a few secs
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// press esc
		postKey(SWT.ESC);
		
		// introduce compilation errror
		postCharacterKey('p');

		// save file by using "Ctrl+S"
		postKeyDown(SWT.CTRL);
		postCharacterKey('s');
		postKeyUp(SWT.CTRL);

		// check that there is an error (if esc not implemented
		// then there will be no error because still in xref view)
		final int numAnnotations = getNumErrorAnnotations(editorPart);
		new DisplayHelper() {

			protected boolean condition() {
				int newNumAnnotations = getNumErrorAnnotations(editorPart);
				boolean ret = numAnnotations != newNumAnnotations;
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
			
		int newNumAnnotations = getNumErrorAnnotations(editorPart);

		assertTrue("should have more syntax errors now", newNumAnnotations > numAnnotations);
	}
	
	/**
	 * Get the number of error annotations in an editor
	 */
	private int getNumErrorAnnotations(ITextEditor editor) {
		
		IAnnotationModel model = getAnnotationModel(editor);
		int num = 0;
		for (Iterator iter = model.getAnnotationIterator(); iter.hasNext();) {
			Annotation a = (Annotation)iter.next();
			String message = a.getText();
			if(message != null && message.startsWith("Syntax error")) {
				num++;
			}
			
		}
		return num;		
	}
	
	/**
	 * Get the annotation model for an editor
	 * @param editor
	 * @return
	 */
	private IAnnotationModel getAnnotationModel(ITextEditor editor) {
		if (editor instanceof AspectJEditor) {
			IAnnotationModel annotationModel = ((AspectJEditor)editor).getViewer().getAnnotationModel();
			return annotationModel;
		}
		IDocumentProvider provider = editor.getDocumentProvider();
		IAnnotationModel model = provider.getAnnotationModel(editor
				.getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel) {
			return (AbstractMarkerAnnotationModel) model;
		}
		return null;
	}
	
}
