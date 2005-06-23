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

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests eager parsing support in the AspectJ editor
 */
public class EagerParsingTest extends TestCase {

	IProject project;
	
	protected void setUp() throws Exception {	
		final IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_EVALUTE_TEMPORARY_PROBLEMS, true);		
		super.setUp();
		project = Utils.createPredefinedProject("Simple AJ Project");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		Utils.deleteProject(project);
	}
	
	public void testIntroduceError(){
		IResource res = project.findMember("src/p2/Aspect.aj");
		if (res == null)
			fail("Required file not found.");
		eagerParserTest((IFile)res);
	}
	
	public void testIntroduceErrorInDefaultPackage(){
		IResource res = project.findMember("src/AspectInDefaultPackage.aj");
		if (res == null)
			fail("Required file not found.");
		eagerParserTest((IFile)res);
	}

	public void testIntroduceErrorInJava(){
		IResource res = project.findMember("src/ClassInDefaultPackage.java");
		if (res == null)
			fail("Required file not found.");
		eagerParserTest((IFile)res);
	}
	
	public void eagerParserTest(IFile sourcefile){
		ITextEditor editorPart = (ITextEditor)Utils.openFileInAspectJEditor(sourcefile, false);
		
		try {
		
			//wait for annotation model to be created
			Utils.waitForJobsToComplete();
			
			createError(true, sourcefile, editorPart);		
			Utils.waitForJobsToComplete();
			
			createError(false, sourcefile, editorPart);
			Utils.waitForJobsToComplete();			

		} finally {
			editorPart.close(false);
		}
	}
	
	private void createError(final boolean addsError, final IFile file, final ITextEditor editor){
		final int numAnnotations = getNumErrorAnnotations(editor);
		if(addsError) {
			editor.setFocus();		
			VisualTestUtils.gotoLine(15);
			VisualTestUtils.moveCursorRight(8);
			addSpace();
		} else {
			removeSpace();
		}
		Utils.waitForJobsToComplete();		
		new DisplayHelper() {

			protected boolean condition() {
				int newNumAnnotations = getNumErrorAnnotations(editor);
				boolean ret = numAnnotations != newNumAnnotations;
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
			
		int newNumAnnotations = getNumErrorAnnotations(editor);
		if (numAnnotations == newNumAnnotations)
			fail(addsError?"Error did not appear.":"Error did not disappear.");
	}
	
	private void removeSpace() {
		Display display = Display.getCurrent(); 
		
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.character = SWT.BS;
		display.post(event);
		

		event = new Event();
		event.type = SWT.KeyUp;
		event.character = SWT.BS;
		display.post(event);	
	}

	private void addSpace() {
		Display display = Display.getCurrent(); 
		
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.character = ' ';
		display.post(event);
		

		event = new Event();
		event.type = SWT.KeyUp;
		event.character = ' ';
		display.post(event);
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
			if(message.startsWith("Syntax error")) {
				num++;
			}
			
		}
		return num;		
	}

}
