/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.ui.utils;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.contribution.xref.core.IDeferredXReference;
import org.eclipse.contribution.xref.core.IXReferenceAdapter;
import org.eclipse.contribution.xref.internal.ui.XReferenceUIPlugin;
import org.eclipse.contribution.xref.internal.ui.providers.XReferenceContentProvider;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;

/**
 * A utility class which contains common methods which are required
 * for both the Cross Reference View and the Cross Reference Inplace View.
 */
public class XRefUIUtils {

	/**
	 * Computes and returns the source reference.
	 * 
	 * This is taken from the computeHighlightRangeSourceReference() method
	 * in the JavaEditor class which is used to populate the outline view.
	 * 
	 * @return the computed source reference
	 */
	public static ISourceReference computeHighlightRangeSourceReference(JavaEditor editor) {
		ISourceViewer sourceViewer = editor.getViewer();
		if (sourceViewer == null)
			return null;
			
		StyledText styledText= sourceViewer.getTextWidget();
		if (styledText == null)
			return null;
		
		int caret= 0;
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5)sourceViewer;
			caret= extension.widgetOffset2ModelOffset(styledText.getCaretOffset());
		} else {
			int offset= sourceViewer.getVisibleRegion().getOffset();
			caret= offset + styledText.getCaretOffset();
		}

		IJavaElement element= getElementAt(editor, caret, true);
		
		if ( !(element instanceof ISourceReference))
			return null;
		
		if (element.getElementType() == IJavaElement.IMPORT_DECLARATION) {
			
			IImportDeclaration declaration= (IImportDeclaration) element;
			IImportContainer container= (IImportContainer) declaration.getParent();
			ISourceRange srcRange= null;
			
			try {
				srcRange= container.getSourceRange();
			} catch (JavaModelException e) {
			}
			
			if (srcRange != null && srcRange.getOffset() == caret)
				return container;
		}
		
		return (ISourceReference) element;
	}
	
	/**
	 * Returns the most narrow java element including the given offset.
	 * 
	 * This is taken from the getElementAt(int offset, boolean reconcile) method
	 * in the CompilationUnitEditor class.
	 */
	private static IJavaElement getElementAt(JavaEditor editor, int offset, boolean reconcile) {
		IWorkingCopyManager manager= JavaPlugin.getDefault().getWorkingCopyManager();
		ICompilationUnit unit= manager.getWorkingCopy(editor.getEditorInput());
		
		if (unit != null) {
			try {
				if (reconcile) {
					synchronized (unit) {
						unit.reconcile(ICompilationUnit.NO_AST, false, null, null);
					}
					return unit.getElementAt(offset);
				} else if (unit.isConsistent())
					return unit.getElementAt(offset);
					
			} catch (JavaModelException x) {
				if (!x.isDoesNotExist())
				JavaPlugin.log(x.getStatus());
				// nothing found, be tolerant and go on
			}
		}
		
		return null;
	}

	public static void revealInEditor(IJavaElement j) {
		try {
			IEditorPart p = JavaUI.openInEditor(j);
			JavaUI.revealInEditor(p, j);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// revealInEditor(IResource) might not work for inplace view
	public static void revealInEditor(IResource r) {
		IMarker m;
		try {
			m = r.createMarker(IMarker.MARKER);
			IDE.openEditor(getActiveWorkbenchWindow().getActivePage(), m, true);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	// evaluateXReferences might not work for inplace view 
	public static void evaluateXReferences(IDeferredXReference xr, TreeViewer viewer, Shell shell) {
		try {
			new ProgressMonitorDialog(shell).run(true, true, xr);
			if (!(viewer.getContentProvider() instanceof XReferenceContentProvider)) {
				return;
			}
			((XReferenceContentProvider)viewer.getContentProvider()).refresh();
			viewer.refresh();
			viewer.expandToLevel(3);
		} catch (InterruptedException intEx) {
			// user cancelled - this is ok...
		} catch (InvocationTargetException invEx) {
			System.err.println(
					"Something nasty here, " + xr + " could not be evaluated: " + invEx); //$NON-NLS-1$ //$NON-NLS-1$
		}
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return XReferenceUIPlugin
		.getDefault()
		.getWorkbench()
		.getActiveWorkbenchWindow();
	}

	public static IXReferenceAdapter getXRefAdapterForSelection(IWorkbenchPart part, ISelection selection) {
		IAdaptable a = null;
		IXReferenceAdapter xra = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection =
				(IStructuredSelection) selection;
			Object first = structuredSelection.getFirstElement();
			if (first instanceof IAdaptable) {
				a = (IAdaptable) first;
			}
		} else if (part instanceof IEditorPart && selection instanceof ITextSelection) {
		    if (part instanceof JavaEditor) {
			    JavaEditor je = (JavaEditor)part;
			    ISourceReference sourceRef = XRefUIUtils.computeHighlightRangeSourceReference(je);
			    a = (IAdaptable)(IJavaElement)sourceRef;                
            }
		}
		if (a != null) {
			xra = (IXReferenceAdapter) a.getAdapter(IXReferenceAdapter.class);
		}
		return xra;
	}
	
	public static ISelection getCurrentSelection() {
		IWorkbenchWindow window= JavaPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			return window.getSelectionService().getSelection();
		}
		return null;
	}
}
