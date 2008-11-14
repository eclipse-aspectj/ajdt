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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.internal.ui.editor.quickfix.QuickFixProcessor;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;
import org.eclipse.jdt.internal.ui.javaeditor.JavaMarkerAnnotation;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
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
    IFile javaFile;
    IFile javaWithAspectFile;
    IFile aspectFile;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = createPredefinedProject("QuickFix"); //$NON-NLS-1$
		
        javaFile = (IFile) project.findMember("src/test/TestJava.java");
        javaWithAspectFile = (IFile) project.findMember("src/test/TestAspect.java");
        aspectFile = (IFile) project.findMember("src/test/TestAspect2.aj");
	}

    public void testJavaQuickFixSetUp() throws Exception {
        quickFixSetup(javaFile);
    }
    
    public void testJavaWithAspectQuickFixSetUp() throws Exception {
        quickFixSetup(javaWithAspectFile);
    }
    
    public void testAspectQuickFixSetUp() throws Exception {
        quickFixSetup(aspectFile);
    }
    
    // just test to see that we have some completions coming back
    public void testJavaHasQuickFixes() throws Exception {
        IJavaCompletionProposal[] proposals = getQuickFixes(javaFile);
        assertTrue("Should have some completions, but doesn't", proposals.length > 0);
    }
    
    // just test to see that we have some completions coming back
    public void testJavaWithAspectHasQuickFixes() throws Exception {
        IJavaCompletionProposal[] proposals = getQuickFixes(javaWithAspectFile);
        assertTrue("Should not have any completions because we don't support completions for Aspects in Java files", proposals.length == 0);
    }
    
    // just test to see that we have some completions coming back
    public void testAspectHasQuickFixes() throws Exception {
        IJavaCompletionProposal[] proposals = getQuickFixes(aspectFile);
        assertTrue("Should have some completions, but doesn't", proposals.length > 0);
    }
    
	private void quickFixSetup(IFile sourceFile) throws Exception {	
		ITextEditor editorPart = (ITextEditor) openFileInAspectJEditor(
		        sourceFile, false);

		//wait for annotation model to be created
		waitForJobsToComplete();

		IMarker[] markers = getMarkers(sourceFile, editorPart);

		assertTrue("Should have found some Java model markers", markers.length > 0); //$NON-NLS-1$

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
	
	
	private AbstractMarkerAnnotationModel getAnnotationModel(
			IFile sourceFile) {
	    ITextEditor editor = (ITextEditor) openFileInAspectJEditor(
                sourceFile, false);
		IDocumentProvider provider = editor.getDocumentProvider();
		IAnnotationModel model = provider.getAnnotationModel(editor
				.getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel) {
			return (AbstractMarkerAnnotationModel) model;
		}
		return null;
	}
	
	public IJavaCompletionProposal[] getQuickFixes(IFile sourceFile) throws Exception {
	    ICompilationUnit unit = (ICompilationUnit) AspectJCore.create(sourceFile);
	    String toLookFor = "File";
        int location = new String(((CompilationUnit) unit).getContents()).indexOf(toLookFor);
	    
	    AbstractMarkerAnnotationModel model = getAnnotationModel(sourceFile);
	    List probLocs = new ArrayList();
	    for (Iterator annotationIter = model.getAnnotationIterator(); annotationIter.hasNext(); ) {
	        Object obj = (Object) annotationIter.next();
	        if (obj instanceof JavaMarkerAnnotation) {
	            JavaMarkerAnnotation ja = (JavaMarkerAnnotation) obj;
                probLocs.add(getProblemLocation(ja, model));
            }
	    }
	    
	    IInvocationContext context = new AssistContext(unit, location+1, 0);
	    QuickFixProcessor qfp = new QuickFixProcessor();
	    return qfp.getAssists(context, 
	            (IProblemLocation[]) probLocs.toArray(new IProblemLocation[probLocs.size()]));
	}
	
	private ProblemLocation getProblemLocation(IJavaAnnotation javaAnnotation, IAnnotationModel model) {
        int problemId= javaAnnotation.getId();
        if (problemId != -1) {
            Position pos= model.getPosition((Annotation) javaAnnotation);
            if (pos != null) {
                return new ProblemLocation(pos.getOffset(), pos.getLength(), javaAnnotation); // java problems all handled by the quick assist processors
            }
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