/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests.editor;

import java.lang.reflect.Field;

import org.eclipse.ajdt.internal.ui.text.ITDHyperlinkDetector;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlink;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

/**
 * ITDHyperlink detector is not used any more, codeSelect (via ITDAwareness is used instead).
 * but this is nonetheless a good test of ITDAwareness.
 * @author Andrew Eisenberg
 */
public class ITDHyperlinkTest extends UITestCase {

	IProject project;
	IFile targetFile;
	IFile otherFile;
	IFile aspectFile;
	
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = createPredefinedProject("Bug273334"); //$NON-NLS-1$
		targetFile = project.getFile("src/a/HasAnITD.java");
		otherFile = project.getFile("src/a/AThird.java");
		aspectFile = project.getFile("src/a/DeclaresITDs.aj");
	}
	
    /**
     * Test that ITD hyperlinks work when inside the CU that
     * is a target of the ITD
     */
    public void testITDTargetFileHyperlink() throws Exception {
        JavaEditor editor = (JavaEditor) openFileInDefaultEditor(targetFile, true);
        ITDHyperlinkDetector detector = new ITDHyperlinkDetector();
        detector.setContext(editor);
        validateHyperlink(detector.detectHyperlinks(editor.getViewer(), findRegion(editor, "field", 1), false), "HasAnITD.field");
        validateHyperlink(detector.detectHyperlinks(editor.getViewer(), findRegion(editor, "field", 2), false), "HasAnITD.field");
        validateHyperlink(detector.detectHyperlinks(editor.getViewer(), findRegion(editor, "method", 1), false), "HasAnITD.method");
        validateHyperlink(detector.detectHyperlinks(editor.getViewer(), findRegion(editor, "method", 2), false), "HasAnITD.method");
        
    }
    /**
     * Test that ITD hyperlinks work when in another CU
     */
    public void testOtherFileHyperlink() throws Exception {
        JavaEditor editor = (JavaEditor) openFileInDefaultEditor(otherFile, true);
        ITDHyperlinkDetector detector = new ITDHyperlinkDetector();
        detector.setContext(editor);
        validateHyperlink(detector.detectHyperlinks(editor.getViewer(), findRegion(editor, "field", 1), false), "HasAnITD.field");
        validateHyperlink(detector.detectHyperlinks(editor.getViewer(), findRegion(editor, "method", 1), false), "HasAnITD.method");
        
    }
    /**
     * Test that ITD hyperlinks work when in an aspect
     */
    public void testAspectFileHyperlink() throws Exception {
        JavaEditor editor = (JavaEditor) openFileInDefaultEditor(aspectFile, true);
        ITDHyperlinkDetector detector = new ITDHyperlinkDetector();
        detector.setContext(editor);
        validateHyperlink(detector.detectHyperlinks(editor.getViewer(), findRegion(editor, "field", 1), false), "HasAnITD.field");
        validateHyperlink(detector.detectHyperlinks(editor.getViewer(), findRegion(editor, "method", 1), false), "HasAnITD.method");
        
    }
	
	
	private void validateHyperlink(IHyperlink[] links, String name) throws Exception {
	    assertEquals("Should have found exactly one hyperlink", 1, links.length);
	    JavaElementHyperlink link = (JavaElementHyperlink) links[0];
	    IJavaElement elt = getElement(link);
	    assertTrue("Java element " + elt.getHandleIdentifier() + " should exist", elt.exists());
	    assertEquals(name, elt.getElementName());
    }

    private IRegion findRegion(JavaEditor editor, String string, int occurrence) {
	    IDocument document = editor.getDocumentProvider().getDocument(
                editor.getEditorInput());
	    int start = 0;
	    while (occurrence-- > 0) {
	        start = document.get().indexOf(string, start);
	    }
	    return new Region(start, string.length());
	}
    
    IJavaElement getElement(JavaElementHyperlink link) throws Exception {
        Field elementField = JavaElementHyperlink.class.getDeclaredField("fElement");
        elementField.setAccessible(true);
        return (IJavaElement) elementField.get(link);
    }
}
