/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajde.Ajde;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.buildconfigurator.BuildConfiguration;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ActionDelegateHelper;
import org.eclipse.jdt.internal.debug.ui.actions.ActionMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

public class AspectJBreakpointRulerAction extends Action {

	private IVerticalRulerInfo fRuler;

	private ITextEditor fTextEditor;

	private IEditorStatusLine fStatusLine;

	//private ToggleBreakpointAdapter fBreakpointAdapter;

	public AspectJBreakpointRulerAction(IVerticalRulerInfo ruler,
			ITextEditor editor, IEditorPart editorPart) {
		super(ActionMessages.getString("ManageBreakpointRulerAction.label")); //$NON-NLS-1$
		fRuler = ruler;
		fTextEditor = editor;
		fStatusLine = (IEditorStatusLine) editorPart
				.getAdapter(IEditorStatusLine.class);
	}

	/**
	 * Disposes this action
	 */
	public void dispose() {
		fTextEditor = null;
		fRuler = null;
	}

	/**
	 * Returns this action's vertical ruler info.
	 * 
	 * @return this action's vertical ruler
	 */
	protected IVerticalRulerInfo getVerticalRulerInfo() {
		return fRuler;
	}

	/**
	 * Returns this action's editor.
	 * 
	 * @return this action's editor
	 */
	protected ITextEditor getTextEditor() {
		return fTextEditor;
	}

	/**
	 * Returns the <code>IDocument</code> of the editor's input.
	 * 
	 * @return the document of the editor's input
	 */
	protected IDocument getDocument() {
		IDocumentProvider provider = fTextEditor.getDocumentProvider();
		return provider.getDocument(fTextEditor.getEditorInput());
	}

	boolean containsAspect(IProgramElement elem) {
		if (elem.getKind().equals(IProgramElement.Kind.ASPECT))
			return true;
		List children = elem.getChildren();
		if ((children != null) && (children.size() > 0)) {
			Iterator iter = children.iterator();
			while (iter.hasNext()) {
				if (containsAspect((IProgramElement) iter.next()))
					return true;
			}
		}
		return false;
	}

	boolean isInAspect(IProgramElement elem) {
		if (elem.getKind().equals(IProgramElement.Kind.ASPECT))
			return true;
		IProgramElement parent = elem.getParent();
		if (parent != null)
			return isInAspect(parent);
		return false;
	}

	boolean isBreakpointable(IProgramElement elem) {
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

	public String getQualifiedMainClassName(IProgramElement elem) {
		String packName = elem.getPackageName();
		IProgramElement parent = elem.getParent();
		
		while ((parent.getKind() != IProgramElement.Kind.PACKAGE) &&  (parent.getKind() != IProgramElement.Kind.FILE_LST)){
			elem = parent;
			parent = parent.getParent();
		}
		String className = elem.getName();
		className = className.substring(0, className.lastIndexOf('.'));
		if ("".equals(packName)) {
			//default package
			return className;
		} else {
			return packName + "." + className;
		}
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
public void run() {
		try {
			List list = getMarkers();
			if (list.isEmpty()) {
				int lineNumber = getVerticalRulerInfo()
						.getLineOfLastMouseButtonActivity() + 1;
				if (lineNumber >= getDocument().getNumberOfLines()) {
					return;
				}

				IResource res = getResource();
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
								report(AspectJUIPlugin.getResourceString("breakpoint.fileExcluded"));
								return;
							}
						}
					}
					
					IJavaElement javaElem = JavaCore.create(res.getParent());
					if (javaElem instanceof IPackageFragment)
						qualifiedName = javaElem.getElementName() + '.';
					else
						qualifiedName = "";
					
					
					//report(AspectJPlugin.getResourceString("breakpoint.missingStructureModel"));
				} else {
					
//					if (!isInAspect(elem)){
//						if ((elem.getKind() != IProgramElement.Kind.FILE_ASPECTJ) && (elem.getKind() != IProgramElement.Kind.FILE_JAVA)){
//							(new ManageBreakpointRulerAction(fRuler, fTextEditor)).run();
//						}
//						//we are not on any reasonable position - ignore
//						//(not ignoring here leads to error messages if 
//						//the position is before the main aspect declaration
//						return;
//					}
					
					//TODO: improve breakpoint check, don't allow breakpoints
					// on empty or comment line at all
					//at the moment, we are just moving the breakpoint into the
					// next method
					while(!isBreakpointable(elem) && (lineNumber < getDocument().getNumberOfLines())){
						lineNumber++;
						elem = hierarchy.findElementForSourceLine(res.getLocation().toOSString(), lineNumber);
					}
					
					String signature = elem.getBytecodeName();
					if (signature != null){
						IProgramElement elemBefore = hierarchy.findElementForSourceLine(res.getLocation().toOSString(), lineNumber - 1);
						if ((!signature.equals(elemBefore.getBytecodeName())) && (lineNumber<getDocument().getNumberOfLines())){
							elemBefore = hierarchy.findElementForSourceLine(res.getLocation().toOSString(), lineNumber + 1);
							if (signature.equals(elemBefore.getBytecodeName()))
								lineNumber++;
						}
					}
					
					qualifiedName = elem.getPackageName();
					if (!"".equals(qualifiedName))
						qualifiedName += '.';
				}
				qualifiedName += res.getName().substring(0, res.getName().lastIndexOf('.'));
				
				AJDTEventTrace.generalEvent("creating breakpoint in " + qualifiedName);
				JDIDebugModel.createLineBreakpoint(getResource(),
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
	//copied from ToggleBreakpointAdapter
	protected IType getType(ITextSelection selection) {
		IMember member = ActionDelegateHelper.getDefault().getCurrentMember(
				selection);
		IType type = null;
		if (member instanceof IType) {
			type = (IType) member;
		} else if (member != null) {
			type = member.getDeclaringType();
		}
		// bug 52385: we don't want local and anonymous types from compilation
		// unit,
		// we are getting 'not-always-correct' names for them.
		try {
			while (type != null && !type.isBinary() && type.isLocal()) {
				type = type.getDeclaringType();
			}
		} catch (JavaModelException e) {
			JDIDebugUIPlugin.log(e);
		}
		return type;
	}

	protected IResource getResource() {
		IResource resource = null;
		IEditorInput editorInput = fTextEditor.getEditorInput();
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
	protected List getMarkers() {

		List breakpoints = new ArrayList();

		IResource resource = getResource();
		IDocument document = getDocument();
		AbstractMarkerAnnotationModel model = getAnnotationModel();

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
								&& breakpointManager.isRegistered(breakpoint)
								&& includesRulerLine(model
										.getMarkerPosition(markers[i]),
										document))
							breakpoints.add(markers[i]);
					}
				}
			} catch (CoreException x) {
				JDIDebugUIPlugin.log(x.getStatus());
			}
		}
		return breakpoints;
	}

	/**
	 * Returns the <code>AbstractMarkerAnnotationModel</code> of the editor's
	 * input.
	 * 
	 * @return the marker annotation model
	 */
	protected AbstractMarkerAnnotationModel getAnnotationModel() {
		IDocumentProvider provider = fTextEditor.getDocumentProvider();
		IAnnotationModel model = provider.getAnnotationModel(fTextEditor
				.getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel) {
			return (AbstractMarkerAnnotationModel) model;
		}
		return null;
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
	protected boolean includesRulerLine(Position position, IDocument document) {

		if (position != null) {
			try {
				int markerLine = document.getLineOfOffset(position.getOffset());
				int line = fRuler.getLineOfLastMouseButtonActivity();
				if (line == markerLine) {
					return true;
				}
			} catch (BadLocationException x) {
			}
		}

		return false;
	}

	protected void report(final String message) {
		JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fStatusLine != null) {
					fStatusLine.setMessage(true, message, null);
				}
				if (message != null
						&& JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
					Display.getCurrent().beep();
				}
			}
		});
	}
}