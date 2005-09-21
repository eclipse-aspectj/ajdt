/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests.visual;

import java.util.Iterator;

import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests eager parsing support in the AspectJ editor
 */
public class EagerParsingTest extends VisualTestCase {

	IProject project;
	
	protected void setUp() throws Exception {	
		final IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_EVALUTE_TEMPORARY_PROBLEMS, true);		
		super.setUp();
		project = createPredefinedProject("Simple AJ Project"); //$NON-NLS-1$
	}
	
	public void testIntroduceError(){
		IResource res = project.findMember("src/p2/Aspect.aj"); //$NON-NLS-1$
		if (res == null)
			fail("Required file not found."); //$NON-NLS-1$
		eagerParserTest((IFile)res);
	}
	
	public void testIntroduceErrorInDefaultPackage(){
		IResource res = project.findMember("src/AspectInDefaultPackage.aj"); //$NON-NLS-1$
		if (res == null)
			fail("Required file not found."); //$NON-NLS-1$
		eagerParserTest((IFile)res);
	}

	public void testIntroduceErrorInJava(){
		IResource res = project.findMember("src/ClassInDefaultPackage.java"); //$NON-NLS-1$
		if (res == null)
			fail("Required file not found."); //$NON-NLS-1$
		eagerParserTest((IFile)res);
	}
	
	public void eagerParserTest(IFile sourcefile){
		ITextEditor editorPart = (ITextEditor)openFileInAspectJEditor(sourcefile, false);
		
		try {
		
			//wait for annotation model to be created
			waitForJobsToComplete();
			
			createError(true, editorPart);		
			waitForJobsToComplete();
			
			createError(false,  editorPart);
			waitForJobsToComplete();			

		} finally {
			editorPart.close(false);
		}
	}
	
	private void createError(final boolean addsError, final ITextEditor editor){
		final int numAnnotations = getNumErrorAnnotations(editor);
		if(addsError) {
			editor.setFocus();		
			gotoLine(15);
			moveCursorRight(8);
			postKey(' ');
		} else {
			postKey(SWT.BS);
		}
		waitForJobsToComplete();		
		new DisplayHelper() {

			protected boolean condition() {
				int newNumAnnotations = getNumErrorAnnotations(editor);
				boolean ret = numAnnotations != newNumAnnotations;
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
			
		int newNumAnnotations = getNumErrorAnnotations(editor);
		if (numAnnotations == newNumAnnotations)
			fail(addsError?"Error did not appear.":"Error did not disappear."); //$NON-NLS-1$ //$NON-NLS-2$
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
	
	
	/**
	 * Get the number of error annotations in an editor
	 */
	private int getNumErrorAnnotations(ITextEditor editor) {
		
		IAnnotationModel model = getAnnotationModel(editor);
		int num = 0;
		for (Iterator iter = model.getAnnotationIterator(); iter.hasNext();) {
			Annotation a = (Annotation)iter.next();
			String message = a.getText();
			if(message.startsWith("Syntax error")) { //$NON-NLS-1$
				num++;
			}
			
		}
		return num;		
	}

}
