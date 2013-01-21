/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;
import org.eclipse.jdt.internal.ui.javaeditor.JavaMarkerAnnotation;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Andrew Eisenberg
 * @created Jan 2, 2009
 *
 */
public abstract class AbstractQuickFixTest extends UITestCase {

    protected ITextEditor quickFixSetup(IFile sourceFile) throws Exception {
        return quickFixSetup(sourceFile, true);
    }
    protected ITextEditor quickFixSetup(IFile sourceFile, boolean shouldFindError) throws Exception {   
        ITextEditor editorPart = (ITextEditor) openFileInAspectJEditor(
                sourceFile, false);

        //wait for annotation model to be created
        waitForJobsToComplete();

        IMarker[] markers = getMarkers(sourceFile);

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
        assertTrue("Didn't find a warning marker", foundWarning); //$NON-NLS-1$
        assertEquals("Didn't find an error marker", shouldFindError, foundError); //$NON-NLS-1$
        
        return editorPart;
    }

    
    protected IMarker[] getMarkers(IResource resource)
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

    protected IJavaCompletionProposal[] getQuickFixes(IFile sourceFile) throws Exception {
        QuickFixProcessor qfp = new QuickFixProcessor();
        return getQuickFixes(sourceFile, qfp, "File");
    }
    
    
    protected IJavaCompletionProposal[] getQuickFixes(IFile sourceFile, IQuickFixProcessor processor, String toLookFor) throws Exception {
        ICompilationUnit unit = (ICompilationUnit) AspectJCore.create(sourceFile);
        int location = new String(((CompilationUnit) unit).getContents()).indexOf(toLookFor) + 1;
        
        AbstractMarkerAnnotationModel model = getAnnotationModel(sourceFile);
        List probLocs = new ArrayList();
        for (Iterator annotationIter = model.getAnnotationIterator(); annotationIter.hasNext(); ) {
            Object obj = (Object) annotationIter.next();
            if (obj instanceof JavaMarkerAnnotation) {
                JavaMarkerAnnotation ja = (JavaMarkerAnnotation) obj;
                if (isMarkerAtLocation(location, ja)) {
                    probLocs.add(getProblemLocation(ja, model));
                }
            }
        }
        
        AssistContext context = new AssistContext(unit, location, 0);
        context.setASTRoot(ASTResolving.createQuickFixAST(unit, null));
        return processor.getCorrections(context, 
                (IProblemLocation[]) probLocs.toArray(new IProblemLocation[probLocs.size()]));
    }
    private boolean isMarkerAtLocation(int location, JavaMarkerAnnotation ja)
            throws CoreException {
        return location >= ((Integer) ja.getMarker().getAttribute(IMarker.CHAR_START)).intValue() && 
                location <= ((Integer) ja.getMarker().getAttribute(IMarker.CHAR_END)).intValue();
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

}
