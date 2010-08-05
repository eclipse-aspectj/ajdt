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
 * Further testing of ITD hyperlinks.
 * Ensure that hyperlinking works when target aspect is in separate project
 * ITDHyperlink detector is not used any more, codeSelect (via ITDAwareness is used instead).
 * but this is nonetheless a good test of ITDAwareness.
 * @author Andrew Eisenberg
 */
public class ITDHyperlinkTest2 extends UITestCase {

	IProject base;
	IProject depending;
	IFile baseFile;
	
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		base = createPredefinedProject("Bug273334base"); //$NON-NLS-1$
		depending = createPredefinedProject("Bug273334depending"); //$NON-NLS-1$
		baseFile = base.getFile("src/q/UsesITDs1.java");
		waitForJobsToComplete();
	}
	
    /**
     * Test that ITD hyperlinks work when the aspect is in
     * other project and ITDs are declared in this project
     */
    public void testITDTargetFileHyperlink() throws Exception {
        JavaEditor editor = (JavaEditor) openFileInDefaultEditor(baseFile, true);
        ITDHyperlinkDetector detector = new ITDHyperlinkDetector();
        detector.setContext(editor);
        validateHyperlink(detector.detectHyperlinks(editor.getViewer(), findRegion(editor, "x", 1), false), "InterfaceForITD.x");
        validateHyperlink(detector.detectHyperlinks(editor.getViewer(), findRegion(editor, "nothing", 1), false), "InterfaceForITD.nothing");
    }
    /**
     * Test that ITD hyperlinks work when the aspect is in
     * other project and ITDs are declared in other project
     */
    public void testITDTargetFileHyperlinkOtherProject() throws Exception {
        JavaEditor editor = (JavaEditor) openFileInDefaultEditor(baseFile, true);
        ITDHyperlinkDetector detector = new ITDHyperlinkDetector();
        detector.setContext(editor);
        validateHyperlink(detector.detectHyperlinks(editor.getViewer(), findRegion(editor, "x", 2), false), "InterfaceForITD.x");
        validateHyperlink(detector.detectHyperlinks(editor.getViewer(), findRegion(editor, "nothing", 2), false), "InterfaceForITD.nothing");
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
