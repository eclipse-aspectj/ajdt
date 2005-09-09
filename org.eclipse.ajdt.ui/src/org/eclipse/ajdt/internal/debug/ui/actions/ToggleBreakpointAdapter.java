/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/
package org.eclipse.ajdt.internal.debug.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajde.Ajde;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ActionMessages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 */
public class ToggleBreakpointAdapter implements IToggleBreakpointsTarget {

	private IEditorStatusLine fStatusLine;
	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		if(selection instanceof ITextSelection && part instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor)part;
			ITextSelection textSelection = ((ITextSelection)selection);
			int lineNumber = textSelection.getStartLine() + 1;
			try {
				List list = getMarkers(editor, lineNumber);
				if (list.isEmpty()) {
					
					IResource res = getResource(editor);
					IHierarchy hierarchy = Ajde.getDefault().getStructureModelManager().getHierarchy();
					IProgramElement elem = hierarchy.findElementForSourceLine(res.getLocation().toOSString(), lineNumber);
					
					String qualifiedName;
					if (elem.getParent() == null) {
						//structure model missing
						ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator().getProjectBuildConfigurator(res.getProject());
						if (pbc != null){
							BuildConfiguration bc = pbc.getActiveBuildConfiguration();
							if (bc != null){
								if (!bc.isIncluded(res)){
									return;
								}
							}
						}
						
						IJavaElement javaElem = JavaCore.create(res.getParent());
						if (javaElem instanceof IPackageFragment)
							qualifiedName = javaElem.getElementName() + '.';
						else
							qualifiedName = ""; //$NON-NLS-1$
						
						
					} else {

						
						//TODO: improve breakpoint check, don't allow breakpoints
						// on empty or comment line at all
						//at the moment, we are just moving the breakpoint into the
						// next method
						while(!isBreakpointable(elem) && (lineNumber < getDocument(editor).getNumberOfLines())){
							lineNumber++;
							elem = hierarchy.findElementForSourceLine(res.getLocation().toOSString(), lineNumber);
						}
						
						String signature = elem.getBytecodeName();
						if (signature != null){
							IProgramElement elemBefore = hierarchy.findElementForSourceLine(res.getLocation().toOSString(), lineNumber - 1);
							if ((!signature.equals(elemBefore.getBytecodeName())) && (lineNumber<getDocument(editor).getNumberOfLines())){
								elemBefore = hierarchy.findElementForSourceLine(res.getLocation().toOSString(), lineNumber + 1);
								if (signature.equals(elemBefore.getBytecodeName()))
									lineNumber++;
							}
						}
						
						qualifiedName = elem.getPackageName();
						if (!"".equals(qualifiedName)) //$NON-NLS-1$
							qualifiedName += '.';
					}
					qualifiedName += res.getName().substring(0, res.getName().lastIndexOf('.'));
					
					AJLog.log("creating breakpoint in " + qualifiedName); //$NON-NLS-1$
					JDIDebugModel.createLineBreakpoint(getResource(editor),
							qualifiedName, lineNumber, -1, -1, 0, true,
							new HashMap(10));
						//report(AspectJPlugin
						//		.getResourceString("breakpoint.validityNote"));
				} else {
					// remove existing breakpoints of any type
					IBreakpointManager manager = DebugPlugin.getDefault()
							.getBreakpointManager();
					Iterator iterator = list.iterator();
					while (iterator.hasNext()) {
						IMarker marker = (IMarker) iterator.next();
						IBreakpoint breakpoint = manager.getBreakpoint(marker);
						if (breakpoint != null) {
							breakpoint.delete();
						}
					}
				}
			} catch (CoreException e) {
				JDIDebugUIPlugin
						.errorDialog(
								ActionMessages
										.getString("ManageBreakpointRulerAction.error.adding.message1"), e); //$NON-NLS-1$
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		return selection instanceof ITextSelection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}

	private IResource getResource(ITextEditor editor) {
		IResource resource = null;
		IEditorInput editorInput = editor.getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			resource = ((IFileEditorInput) editorInput).getFile();
		}
		return resource;
	}

	/**
	 * Returns a list of markers that exist at the current ruler location.
	 * 
	 * @return a list of markers that exist at the current ruler location
	 */
	private List getMarkers(ITextEditor editor, int lineNumber) {

		List breakpoints = new ArrayList();

		AbstractMarkerAnnotationModel model = getAnnotationModel(editor);
		IResource resource = getResource(editor);
		IDocument document = getDocument(editor);
		 

		if (model != null) {
			try {

				IMarker[] markers = null;
				if (resource instanceof IFile)
					markers = resource.findMarkers(
							IBreakpoint.BREAKPOINT_MARKER, true,
							IResource.DEPTH_INFINITE);
				else {
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
							.getRoot();
					markers = root.findMarkers(IBreakpoint.BREAKPOINT_MARKER,
							true, IResource.DEPTH_INFINITE);
				}

				if (markers != null) {
					IBreakpointManager breakpointManager = DebugPlugin
							.getDefault().getBreakpointManager();
					for (int i = 0; i < markers.length; i++) {
						IBreakpoint breakpoint = breakpointManager
								.getBreakpoint(markers[i]);
						if (breakpoint != null
								&& breakpointManager.isRegistered(breakpoint) && includesRulerLine(model
										.getMarkerPosition(markers[i]),
										document, lineNumber)) {
							breakpoints.add(markers[i]);
						}
					}
				}
			} catch (CoreException x) {
				JDIDebugUIPlugin.log(x.getStatus());
			}
		}
		return breakpoints;
	}
	
	
	private IDocument getDocument(ITextEditor editor) {
		IDocumentProvider provider = editor.getDocumentProvider();
		return provider.getDocument(editor.getEditorInput());
	}

	
	/**
	 * Returns the <code>AbstractMarkerAnnotationModel</code> of the editor's
	 * input.
	 * 
	 * @return the marker annotation model
	 */
	private AbstractMarkerAnnotationModel getAnnotationModel(ITextEditor editor) {
		IDocumentProvider provider = editor.getDocumentProvider();
		IAnnotationModel model = provider.getAnnotationModel(editor
				.getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel) {
			return (AbstractMarkerAnnotationModel) model;
		}
		return null;
	}

	private boolean isBreakpointable(IProgramElement elem) {
		return ((elem.getKind().equals(IProgramElement.Kind.ADVICE))
				|| (elem.getKind().equals(IProgramElement.Kind.METHOD))
				|| (elem.getKind().equals(IProgramElement.Kind.CODE))
				|| (elem.getKind().equals(IProgramElement.Kind.CONSTRUCTOR))
				|| (elem.getKind().equals(IProgramElement.Kind.INITIALIZER))
				|| (elem.getKind()
						.equals(IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR))
				|| (elem.getKind()
						.equals(IProgramElement.Kind.INTER_TYPE_METHOD))
				|| (elem.getKind()
						.equals(IProgramElement.Kind.INTER_TYPE_FIELD)) || (elem
				.getKind().equals(IProgramElement.Kind.FIELD)));
	}

	/**
	 * Checks whether a position includes the ruler's line of activity.
	 * 
	 * @param position
	 *            the position to be checked
	 * @param document
	 *            the document the position refers to
	 * @return <code>true</code> if the line is included by the given position
	 */
	private boolean includesRulerLine(Position position, IDocument document, int lineNumber) {

		if (position != null) {
			try {
				int markerLine = document.getLineOfOffset(position.getOffset());
				if (lineNumber == markerLine + 1) {
					return true;
				}
			} catch (BadLocationException x) {
			}
		}

		return false;
	}

	
}
