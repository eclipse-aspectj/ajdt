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
import org.eclipse.contribution.xref.internal.ui.actions.ToggleShowXRefsForFileAction;
import org.eclipse.contribution.xref.internal.ui.providers.XReferenceContentProvider;
import org.eclipse.contribution.xref.internal.ui.providers.XReferenceLabelProvider;
import org.eclipse.contribution.xref.ui.utils.XRefUIUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * This class represents the Cross Reference View
 */
public class XReferenceView extends ViewPart implements ISelectionListener {

	public static final String ID =
		"org.eclipse.contribution.xref.ui.views.XReferenceView"; //$NON-NLS-1$
	private static final String LINK_ID = ID + ".link"; //$NON-NLS-1$
	private static final String SELECTION_ID = ID + ".selection"; //$NON-NLS-1$
	private static final String XREFS_FOR_FILE_ID = ID + ".xrefsForFile"; //$NON-NLS-1$

	private Action doubleClickAction;
	private Action collapseAllAction;
	private Action toggleLinkingAction;
	private Action toggleShowXRefsForFileAction;
	private boolean linkingEnabled = true; // following selection?
	private boolean showXRefsForFileEnabled = false;
	private IXReferenceAdapter lastXRefAdapter;
	private ISelection lastSelection, lastLinkedSelection;
	private IWorkbenchPart lastWorkbenchPart, lastLinkedWorkbenchPart;
	private NavigationHistoryActionGroup navigationHistoryGroup;
	private TreeViewer viewer;
	private XReferenceContentProvider contentProvider;

	private boolean changeDrivenByBuild = false;
	
	public XReferenceView() {
		XReferenceUIPlugin.xrefView = this;
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
		viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		
		restorePersistedSettings();
		makeActions();
		hookDoubleClickAction();
		contributeToActionBars();
		
		IWorkbenchWindow window = XRefUIUtils.getActiveWorkbenchWindow();

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
		if (!(part instanceof AbstractTextEditor)) {
			// only want to respond to changes in selection
			// in editors
			return;
		}
		lastWorkbenchPart = part;
		lastSelection = selection;	
		
		if (linkingEnabled) {
			lastLinkedWorkbenchPart = part;
			lastLinkedSelection = selection;
		}

		IXReferenceAdapter xra = null;
		if (showXRefsForFileEnabled) {
			xra = XRefUIUtils.getXRefAdapterForSelection(part,selection,true);	
		} else {
			xra = XRefUIUtils.getXRefAdapterForSelection(part,selection,false);	
		}
		if (xra != null) {
			if (lastXRefAdapter != null 
					&& xra.getReferenceSource().equals(lastXRefAdapter.getReferenceSource())
					&& !changeDrivenByBuild) {
				return;
			}
			lastXRefAdapter = xra;
			if (linkingEnabled && !changeDrivenByBuild) {
				viewer.setInput(xra);
			} else if (changeDrivenByBuild){
				Object o = viewer.getInput();
				if (o instanceof IXReferenceAdapter) {
					IXReferenceAdapter xrefAdapter = (IXReferenceAdapter)o;
					viewer.setInput(xrefAdapter);					
				}
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
		IWorkbenchWindow window = XRefUIUtils.getActiveWorkbenchWindow();

		if (window != null) {
			window.getSelectionService().removeSelectionListener(this);
		}
		persistSettings();
		XReferenceUIPlugin.xrefView = null;
	}

	public boolean isLinkingEnabled() {
		return linkingEnabled;
	}

	public void setLinkingEnabled(boolean isOn) {
		linkingEnabled = isOn;
		if (linkingEnabled && lastXRefAdapter != null) {
			viewer.setInput(lastXRefAdapter);
		}
	}

	public boolean isShowXRefsForFileEnabled() {
		return showXRefsForFileEnabled;
	}

	public void setShowXRefsForFileEnabled(boolean isOn) {
		showXRefsForFileEnabled = isOn;

		IXReferenceAdapter xra = null;
		if (!linkingEnabled) {
			// if linking is not enabled then just want to show/hide the cross references
			// for the file of the contents of the xref view
			if (lastLinkedSelection != null && lastLinkedWorkbenchPart != null) {
				if (showXRefsForFileEnabled) {
					xra = XRefUIUtils.getXRefAdapterForSelection(lastLinkedWorkbenchPart,lastLinkedSelection,true);
				} else {
					xra = XRefUIUtils.getXRefAdapterForSelection(lastLinkedWorkbenchPart,lastLinkedSelection,false);
				}
			}			
		} else {
			// if linking is enabled, then want to show/hide the cross references
			// for the file which is open in the active editor
			if (lastSelection != null && lastWorkbenchPart != null) {
				if (showXRefsForFileEnabled) {
					xra = XRefUIUtils.getXRefAdapterForSelection(lastWorkbenchPart,lastSelection,true);
				} else {
					xra = XRefUIUtils.getXRefAdapterForSelection(lastWorkbenchPart,lastSelection,false);
				}
			}
		}
		if (xra != null) {
			viewer.setInput(xra);
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
		
		if (!pstore.contains(XREFS_FOR_FILE_ID)) {
			pstore.setDefault(XREFS_FOR_FILE_ID, false);
		}
		pstore.setValue(XREFS_FOR_FILE_ID, showXRefsForFileEnabled);
		// MPC: do we need this? doesn't work for some IJavaElements
//		IXReferenceAdapter xra = (IXReferenceAdapter) viewer.getInput();
//		if (xra != null) {
//			Object input = xra.getReferenceSource();
//			String handle = null;
//			if (input instanceof IResource) {
//				handle = "R" + ((IResource) input).getFullPath().toString(); //$NON-NLS-1$
//			} else if (input instanceof IJavaElement) {
//				handle = "J" + ((IJavaElement) input).getHandleIdentifier(); //$NON-NLS-1$
//			}
//			if (handle != null) {
//				pstore.setValue(SELECTION_ID, handle);
//			}
//		}
		XReferenceUIPlugin.getDefault().savePluginPreferences();
	}

	private void restorePersistedSettings() {
		IPreferenceStore pstore =
			XReferenceUIPlugin.getDefault().getPreferenceStore();
		if (pstore.contains(LINK_ID)) {
			linkingEnabled = pstore.getBoolean(LINK_ID);
		}
		
		if (pstore.contains(XREFS_FOR_FILE_ID)) {
			showXRefsForFileEnabled = pstore.getBoolean(XREFS_FOR_FILE_ID);
		}
//		if (pstore.contains(SELECTION_ID)) {
//			String sel = pstore.getString(SELECTION_ID);
//			String handle = sel.substring(1);
//			IXReferenceAdapter xra = null;
//			if (sel.startsWith("R")) { //$NON-NLS-1$
//				// its an IResource, handle is a path
//				IPath p = new Path(handle);
//				IResource r =
//					XReferenceUIPlugin.getWorkspace().getRoot().findMember(
//						handle);
//				if (r != null) {
//					xra =
//						(IXReferenceAdapter) r.getAdapter(
//							IXReferenceAdapter.class);
//				}
//			} else if (sel.startsWith("J")) { //$NON-NLS-1$
//				// its an IJavaElement
//				IJavaElement j = JavaCore.create(handle);
//				if (j != null) {
//					xra =
//						(IXReferenceAdapter) j.getAdapter(
//							IXReferenceAdapter.class);
//				}
//			} else {
//				// what the hell is it then?? - ignore
//			}
//			if (xra != null) {
//				viewer.setInput(xra);
//				lastSelection = xra;
//			}
//		}
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(toggleLinkingAction);
		manager.add(toggleShowXRefsForFileAction);
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		navigationHistoryGroup.addNavigationActions(manager);
		manager.add(new Separator());
		manager.add(collapseAllAction);
		manager.add(toggleLinkingAction);
		manager.add(toggleShowXRefsForFileAction);
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
		doubleClickAction = new DoubleClickAction(getViewSite().getShell(),viewer);
		toggleShowXRefsForFileAction = new ToggleShowXRefsForFileAction(this);
	}
	
	/**
	 * @param changeDrivenByBuild The changeDrivenByBuild to set.
	 */
	public void setChangeDrivenByBuild(boolean changeDrivenByBuild) {
		this.changeDrivenByBuild = changeDrivenByBuild;
	}
	
}
