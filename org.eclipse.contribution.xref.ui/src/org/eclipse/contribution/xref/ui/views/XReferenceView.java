/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.ui.views;

import org.eclipse.contribution.xref.core.IXReferenceAdapter;
import org.eclipse.contribution.xref.internal.ui.XReferenceUIPlugin;
import org.eclipse.contribution.xref.internal.ui.actions.CollapseAllAction;
import org.eclipse.contribution.xref.internal.ui.actions.DoubleClickAction;
import org.eclipse.contribution.xref.internal.ui.actions.NavigationHistoryActionGroup;
import org.eclipse.contribution.xref.internal.ui.actions.ToggleLinkingAction;
import org.eclipse.contribution.xref.internal.ui.providers.XReferenceContentProvider;
import org.eclipse.contribution.xref.internal.ui.providers.XReferenceLabelProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;

/**
 * This class represents the Cross Reference View
 */
public class XReferenceView extends ViewPart implements ISelectionListener {

	public static final String ID =
		"org.eclipse.contribution.xref.ui.views.XReferenceView";
	private static final String LINK_ID = ID + ".link";
	private static final String SELECTION_ID = ID + ".selection";

	private Action doubleClickAction;
	private Action collapseAllAction;
	private Action toggleLinkingAction;
	private boolean linkingEnabled = true; // following selection?
	private IXReferenceAdapter lastSelection;
	private NavigationHistoryActionGroup navigationHistoryGroup;
	private TreeViewer viewer;
	private XReferenceContentProvider contentProvider;

	public XReferenceView() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		viewer =
			new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		navigationHistoryGroup = new NavigationHistoryActionGroup(viewer);
		contentProvider = new XReferenceContentProvider();
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new XReferenceLabelProvider());
		//		viewer.setSorter(new NameSorter());
		viewer.setAutoExpandLevel(3);

		restorePersistedSettings();
		makeActions();
		hookDoubleClickAction();
		contributeToActionBars();
		
		IWorkbenchWindow window = getActiveWorkbenchWindow();

		if (window != null) {
			window.getSelectionService().addSelectionListener(this);
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
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
			    a = (IAdaptable)(IJavaElement)computeHighlightRangeSourceReference(je);                
            }
		}
		if (a != null) {
			xra = (IXReferenceAdapter) a.getAdapter(IXReferenceAdapter.class);
		}
		if (xra != null) {
			lastSelection = xra;
			if (linkingEnabled) {
				viewer.setInput(xra);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		IWorkbenchWindow window = getActiveWorkbenchWindow();

		if (window != null) {
			window.getSelectionService().removeSelectionListener(this);
		}
		persistSettings();
	}

	/*
	 * helper method to return the currently active workbench window 
	 * (if there is one)
	 */
	private IWorkbenchWindow getActiveWorkbenchWindow() {
		return XReferenceUIPlugin
			.getDefault()
			.getWorkbench()
			.getActiveWorkbenchWindow();
	}

	public boolean isLinkingEnabled() {
		return linkingEnabled;
	}

	public void setLinkingEnabled(boolean isOn) {
		linkingEnabled = isOn;
		if (linkingEnabled && lastSelection != null) {
			viewer.setInput(lastSelection);
		}
	}

	public NavigationHistoryActionGroup getNavigationHistoryActionGroup() {
		return navigationHistoryGroup;
	}
	
	public void collapseAll() {
		viewer.collapseAll();
	}
	
	private void persistSettings() {
		IPreferenceStore pstore =
			XReferenceUIPlugin.getDefault().getPreferenceStore();
		if (!pstore.contains(LINK_ID)) {
			pstore.setDefault(LINK_ID, true);
		}
		pstore.setValue(LINK_ID, linkingEnabled);
		IXReferenceAdapter xra = (IXReferenceAdapter) viewer.getInput();
		if (xra != null) {
			Object input = xra.getReferenceSource();
			String handle = null;
			if (input instanceof IResource) {
				handle = "R" + ((IResource) input).getFullPath().toString();
			} else if (input instanceof IJavaElement) {
				handle = "J" + ((IJavaElement) input).getHandleIdentifier();
			}
			if (handle != null) {
				pstore.setValue(SELECTION_ID, handle);
			}
		}
		XReferenceUIPlugin.getDefault().savePluginPreferences();
	}

	private void restorePersistedSettings() {
		IPreferenceStore pstore =
			XReferenceUIPlugin.getDefault().getPreferenceStore();
		if (pstore.contains(LINK_ID)) {
			linkingEnabled = pstore.getBoolean(LINK_ID);
		}
		if (pstore.contains(SELECTION_ID)) {
			String sel = pstore.getString(SELECTION_ID);
			String handle = sel.substring(1);
			IXReferenceAdapter xra = null;
			if (sel.startsWith("R")) {
				// its an IResource, handle is a path
				IPath p = new Path(handle);
				IResource r =
					XReferenceUIPlugin.getWorkspace().getRoot().findMember(
						handle);
				if (r != null) {
					xra =
						(IXReferenceAdapter) r.getAdapter(
							IXReferenceAdapter.class);
				}
			} else if (sel.startsWith("J")) {
				// its an IJavaElement
				IJavaElement j = JavaCore.create(handle);
				if (j != null) {
					xra =
						(IXReferenceAdapter) j.getAdapter(
							IXReferenceAdapter.class);
				}
			} else {
				// what the hell is it then?? - ignore
			}
			if (xra != null) {
				viewer.setInput(xra);
			}
		}
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(toggleLinkingAction);
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		navigationHistoryGroup.addNavigationActions(manager);
		manager.add(new Separator());
		manager.add(collapseAllAction);
		manager.add(toggleLinkingAction);
	}
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void makeActions() {
		toggleLinkingAction = new ToggleLinkingAction(this);
		collapseAllAction = new CollapseAllAction(this);
		doubleClickAction = new DoubleClickAction(getViewSite().getShell(),viewer,this);
	}
	
	/**
	 * Computes and returns the source reference.
	 * 
	 * This is taken from the computeHighlightRangeSourceReference() method
	 * in the JavaEditor class which is used to populate the outline view.
	 * 
	 * @return the computed source reference
	 */
	private ISourceReference computeHighlightRangeSourceReference(JavaEditor editor) {
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
	private IJavaElement getElementAt(JavaEditor editor, int offset, boolean reconcile) {
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
}
