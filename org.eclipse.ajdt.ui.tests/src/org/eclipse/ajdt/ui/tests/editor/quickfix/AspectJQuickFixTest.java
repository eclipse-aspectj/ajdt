/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests.editor.quickfix;

import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests for light bulb quick fix markers
 * 
 * @author Matt Chapman
 */
public class AspectJQuickFixTest extends UITestCase {

	IProject project;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = createPredefinedProject("QuickFix"); //$NON-NLS-1$
	}

	public void testJavaQuickFix() throws Exception {
		String filename = "src/test/TestJava.java"; //$NON-NLS-1$
		IResource res = project.findMember(filename);
		if (res == null)
			fail("Required file not found: " + filename); //$NON-NLS-1$
		QuickFixTest((IFile) res);
	}

	public void QuickFixTest(IFile sourcefile) throws Exception {

		ITextEditor editorPart = (ITextEditor) openFileInDefaultEditor(
				sourcefile, false);

		//wait for annotation model to be created
		waitForJobsToComplete();

		IMarker[] markers = getMarkers(sourcefile, editorPart);

		assertTrue("Didn't find any Java model markers", markers.length > 0); //$NON-NLS-1$

		boolean foundWarning = false;
		boolean foundError = false;
		for (int i = 0; i < markers.length; i++) {
			IMarker m = markers[i];
			//String msg = (String)m.getAttribute(IMarker.MESSAGE);
			Integer sev = (Integer) m.getAttribute(IMarker.SEVERITY);
			if (!foundError && (sev.intValue() == IMarker.SEVERITY_ERROR)) {
				foundError = true;
				Integer pid = (Integer) m.getAttribute(IJavaModelMarker.ID);
				assertNotNull("Problem id attribute must be set", pid); //$NON-NLS-1$
				Integer start = (Integer) m.getAttribute(IMarker.CHAR_START);
				assertNotNull("Character start attribute must be set", start); //$NON-NLS-1$
				Integer end = (Integer) m.getAttribute(IMarker.CHAR_END);
				assertNotNull("Character end attribute must be set", end); //$NON-NLS-1$
			}
			if (!foundWarning && (sev.intValue() == IMarker.SEVERITY_WARNING)) {
				foundWarning = true;
				Integer pid = (Integer) m.getAttribute(IJavaModelMarker.ID);
				assertNotNull("Problem id attribute must be set", pid); //$NON-NLS-1$
				Integer start = (Integer) m.getAttribute(IMarker.CHAR_START);
				assertNotNull("Character start attribute must be set", start); //$NON-NLS-1$
				Integer end = (Integer) m.getAttribute(IMarker.CHAR_END);
				assertNotNull("Character end attribute must be set", end); //$NON-NLS-1$
			}
		}
		assertEquals("Didn't find a warning marker", foundWarning, true); //$NON-NLS-1$
		assertEquals("Didn't find an error marker", foundError, true); //$NON-NLS-1$
	}

	protected AbstractMarkerAnnotationModel getAnnotationModel(
			ITextEditor editor) {
		IDocumentProvider provider = editor.getDocumentProvider();
		IAnnotationModel model = provider.getAnnotationModel(editor
				.getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel) {
			return (AbstractMarkerAnnotationModel) model;
		}
		return null;
	}

	protected IMarker[] getMarkers(IResource resource, ITextEditor editor)
			throws Exception {
		if (resource instanceof IFile)
			return resource.findMarkers(
					IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true,
					IResource.DEPTH_INFINITE);
		else {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			return root.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
					true, IResource.DEPTH_INFINITE);
		}
	}

}