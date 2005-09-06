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
package org.eclipse.contribution.xref.internal.ui.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.contribution.xref.core.IDeferredXReference;
import org.eclipse.contribution.xref.core.IXReferenceAdapter;
import org.eclipse.contribution.xref.internal.ui.providers.TreeObject;
import org.eclipse.contribution.xref.internal.ui.providers.TreeParent;
import org.eclipse.contribution.xref.internal.ui.providers.XReferenceContentProvider;
import org.eclipse.contribution.xref.ui.XReferenceUIPlugin;
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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceType;
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
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
	 * in the JavaEditor class which is used to populate the outline view
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

		IJavaElement element= getElementAt(editor, caret, false);
		
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
					IJavaElement elementAt = unit.getElementAt(offset);
					if (elementAt != null) {
						return elementAt;
					} 
					// this is if the selection in the editor
					// is outside the {} of the class or aspect
					IJavaElement[] children = unit.getChildren();
					for (int i = 0; i < children.length; i++) {
						if (children[i] instanceof SourceType) {
							return children[i];
						}
					}
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
		}
	}

	// revealInEditor(IResource) might not work for inplace view
	public static void revealInEditor(IResource r) {
		IMarker m;
		try {
			m = r.createMarker(IMarker.MARKER);
			IDE.openEditor(getActiveWorkbenchWindow().getActivePage(), m, true);
		} catch (CoreException e) {
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
					"Something nasty here, " + xr + " could not be evaluated: " + invEx); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return XReferenceUIPlugin
		.getDefault()
		.getWorkbench()
		.getActiveWorkbenchWindow();
	}

	/**
	 * Returns an ArrayList of the IXReferenceAdapters either for the current
	 * selection, or for the file (ICompilationUnit) containing the current selection
	 */
	public static List getXRefAdapterForSelection(IWorkbenchPart part, ISelection selection, boolean showParentCrosscutting) {
		List xrefAdapterList = new ArrayList();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object first = structuredSelection.getFirstElement();
			if (first instanceof IJavaElement) {
				return getXRefAdapterList((IJavaElement)first,showParentCrosscutting);
			} 
		} else if (part instanceof IEditorPart && selection instanceof ITextSelection) {
 		    if (part instanceof JavaEditor) {
			    JavaEditor je = (JavaEditor)part;
			    ISourceReference sourceRef = XRefUIUtils.computeHighlightRangeSourceReference(je);
			    IJavaElement javaElement = (IJavaElement)sourceRef;
			    // if we want to show the parent crosscutting then need to show the xrefs for 
			    // all top level SourceTypes declared in the containing compilation unit
			    return getXRefAdapterList(javaElement,showParentCrosscutting);
            }
		}
		return xrefAdapterList;
	}
	
	private static List getXRefAdapterList(IJavaElement javaElement, boolean showParentCrosscutting) {
		List xrefAdapterList = new ArrayList();
	    if (javaElement != null && showParentCrosscutting) {
	    	ICompilationUnit parent = (ICompilationUnit)javaElement.getAncestor(IJavaElement.COMPILATION_UNIT);
	    	if (parent != null) {
		    	try {
					IType[] types = parent.getAllTypes();
					for (int i = 0; i < types.length; i++) {
						if ((types[i] instanceof SourceType)
								&& (types[i].getParent() instanceof ICompilationUnit)) {
							IAdaptable a = ((SourceType)types[i]);
							if (a != null) {
								xrefAdapterList.add(a.getAdapter(IXReferenceAdapter.class));
							}
						}
					}
				} catch (JavaModelException e) {
				}				
			}
		} else {
			IAdaptable a = javaElement;
			if (a != null) {
				xrefAdapterList.add(a.getAdapter(IXReferenceAdapter.class));
			}
		}	
	    return xrefAdapterList;
	}

	/**
	 * Returns the current selection in the workbench
	 */
	public static ISelection getCurrentSelection() {
		IWorkbenchWindow window= JavaPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			return window.getSelectionService().getSelection();
		}
		return null;
	}

	public static TreeObject getTreeObjectForSelection(TreeViewer viewer, ISelection selection, IWorkbenchPart part) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object first = structuredSelection.getFirstElement();
			if (first instanceof IJavaElement) {
				return getTreeObjectForJavaElement(viewer.getTree().getItems(),(IJavaElement)first);
			} else if (first instanceof TreeObject) {
				Object data = ((TreeObject)first).getData();
				if (data instanceof IJavaElement) {
					return getTreeObjectForJavaElement(viewer.getTree().getItems(),(IJavaElement)data);
				}
			}
		} else if (part instanceof IEditorPart && selection instanceof ITextSelection) {
 		    if (part instanceof JavaEditor) {
			    JavaEditor je = (JavaEditor)part;
			    ISourceReference sourceRef = XRefUIUtils.computeHighlightRangeSourceReference(je);
			    IJavaElement javaElement = (IJavaElement)sourceRef;
			    return getTreeObjectForJavaElement(viewer.getTree().getItems(),javaElement);
            }
		}
		return null;
	}
	
	public static TreeObject getTreeObjectForJavaElement(TreeItem[] items, IJavaElement javaElement) {
		for (int i = 0; i < items.length; i++) {
			Object o = items[i].getData();
			TreeParent treeParent = null;
			TreeObject treeObject = null;
			if (o instanceof TreeParent) {
				treeParent = (TreeParent) o;
			} else if (o instanceof TreeObject) {
				treeObject = (TreeObject) o;
			}
			TreeObject element = null;
			if (treeParent == null) {
				element = treeObject;
			} else {
				element = treeParent;
			}
			
			if (element != null && element.getData() != null) {
				if (element.getData().equals(javaElement)) {
					return element;
				}
			}
			element = getTreeObjectForJavaElement(items[i].getItems(),javaElement);
			if (element != null)
				return element;
		}
		return null;
	}
	
	public static void setSelection(IWorkbenchPart part, ISelection selection, TreeViewer viewer) {
		TreeObject o = XRefUIUtils.getTreeObjectForSelection(viewer,selection, part);
		if (o != null) {
			viewer.setSelection(new StructuredSelection(o),true);
			viewer.reveal(o);
		} else if (selection != null) {
			viewer.setSelection(selection,true);
			viewer.reveal(selection);
		}		
	}
	
}
