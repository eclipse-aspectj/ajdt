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

import java.util.Iterator;
import java.util.List;

import org.eclipse.contribution.xref.core.IXReferenceAdapter;
import org.eclipse.contribution.xref.core.XReferenceProviderManager;
import org.eclipse.contribution.xref.internal.ui.actions.CollapseAllAction;
import org.eclipse.contribution.xref.internal.ui.actions.DoubleClickAction;
import org.eclipse.contribution.xref.internal.ui.actions.ToggleLinkingAction;
import org.eclipse.contribution.xref.internal.ui.actions.ToggleShowXRefsForFileAction;
import org.eclipse.contribution.xref.internal.ui.actions.XReferenceCustomFilterAction;
import org.eclipse.contribution.xref.internal.ui.help.IXRefHelpContextIds;
import org.eclipse.contribution.xref.internal.ui.help.XRefUIHelp;
import org.eclipse.contribution.xref.internal.ui.providers.XReferenceContentProvider;
import org.eclipse.contribution.xref.internal.ui.providers.XReferenceLabelProvider;
import org.eclipse.contribution.xref.internal.ui.utils.XRefUIUtils;
import org.eclipse.contribution.xref.ui.XReferenceUIPlugin;
import org.eclipse.help.IContextProvider;
import org.eclipse.jdt.internal.ui.JavaPlugin;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutline;

/**
 * This class represents the Cross Reference View
 */
public class XReferenceView extends ViewPart implements ISelectionListener,
		IPartListener {

	public static final String ID = "org.eclipse.contribution.xref.ui.views.XReferenceView"; //$NON-NLS-1$

	private static final String LINK_ID = ID + ".link"; //$NON-NLS-1$

	private static final String XREFS_FOR_FILE_ID = ID + ".xrefsForFile"; //$NON-NLS-1$

	private Action doubleClickAction;

	private Action collapseAllAction;

	private Action toggleLinkingAction;

	private Action toggleShowXRefsForFileAction;

	private Action xRefCustomFilterAction;

	private boolean linkingEnabled = true; // following selection?

	private boolean showXRefsForFileEnabled = false;

	private List /* IXReferenceAdapter */lastXRefAdapterList;

	private ISelection lastSelection, lastLinkedSelection;

	private IWorkbenchPart lastWorkbenchPart, lastLinkedWorkbenchPart;

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
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
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
			window.getSelectionService().addPostSelectionListener(this);
		}
		getSite().setSelectionProvider(viewer);
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
		XReferenceProviderManager.getManager().setIsInplace(false);
		IWorkbenchWindow activeWindow = JavaPlugin.getActiveWorkbenchWindow();
		if (activeWindow != null) {
			IWorkbenchPage activePage = activeWindow.getActivePage();
			if (activePage != null) {
				IEditorReference[] openEditors = activePage
						.getEditorReferences();
				if (openEditors.length != 0) {
					// add a part listener to each open editor
					for (int i = 0; i < openEditors.length; i++) {
						openEditors[i].getPage().addPartListener(this);
					}
				}
			}
		}

		if (!(part instanceof AbstractTextEditor)
				&& !(part instanceof ContentOutline)) {
			// only want to respond to changes in selection
			// in editors and outline view
			return;
		}
		lastWorkbenchPart = part;
		lastSelection = selection;

		if (linkingEnabled) {
			lastLinkedWorkbenchPart = part;
			lastLinkedSelection = selection;
		}

		List xraList = null;
		if (showXRefsForFileEnabled) {
			xraList = XRefUIUtils.getXRefAdapterForSelection(part, selection,
					true);
		} else {
			xraList = XRefUIUtils.getXRefAdapterForSelection(part, selection,
					false);
		}

		// bug 92895 - add extra check for empty list (which means the
		// compilation unit doesn't exist anymore).
		if (xraList != null && !xraList.isEmpty()) {
			// if we've selected the same element then don't want the xref view
			// to flicker, therefore we return without updating the view.
			if (lastXRefAdapterList != null && !changeDrivenByBuild) {
				boolean sameXRefAdapter = true;
				for (Iterator iter = xraList.iterator(); iter.hasNext();) {
					Object o = iter.next();
					boolean foundMatch = false;
					if (o instanceof IXReferenceAdapter) {
						IXReferenceAdapter currentXra = (IXReferenceAdapter) o;

						for (Iterator i2 = lastXRefAdapterList.iterator(); i2
								.hasNext();) {
							Object o2 = i2.next();
							if (o2 instanceof IXReferenceAdapter) {
								IXReferenceAdapter lastXra = (IXReferenceAdapter) o2;
								if (currentXra.getReferenceSource().equals(
										lastXra.getReferenceSource())) {
									foundMatch = true;
								}
							}
						}
					}
					if (!foundMatch) {
						sameXRefAdapter = false;
					}
				}
				if (sameXRefAdapter) {
					XRefUIUtils.setSelection(part, selection, viewer);
					return;
				}
			}

			if ((linkingEnabled && !changeDrivenByBuild)
					|| lastXRefAdapterList == null) {
				viewer.setInput(xraList);
			} else if (changeDrivenByBuild) {
				Object o = viewer.getInput();
				if (o instanceof IXReferenceAdapter || o instanceof List) {
					viewer.setInput(o);
				}
			}
			lastXRefAdapterList = xraList;
			XRefUIUtils.setSelection(part, selection, viewer);
		} else {
			// bug 92895 - want to clear the view (and all settings) if
			// have a compilation error in current selection
			clearView();
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
			window.getSelectionService().removePostSelectionListener(this);
		}
		IWorkbenchWindow activeWindow = JavaPlugin.getActiveWorkbenchWindow();
		if (activeWindow != null) {
			IWorkbenchPage activePage = activeWindow.getActivePage();
			if (activePage != null) {
				IEditorReference[] openEditors = activePage
						.getEditorReferences();
				if (openEditors.length != 0) {
					// remove the part listener for each open editor
					for (int i = 0; i < openEditors.length; i++) {
						openEditors[i].getPage().removePartListener(this);
					}
				}
			}
		}
		persistSettings();
		XReferenceUIPlugin.xrefView = null;
	}

	public boolean isLinkingEnabled() {
		return linkingEnabled;
	}

	public void setLinkingEnabled(boolean isOn) {
		linkingEnabled = isOn;
		if (linkingEnabled && lastXRefAdapterList != null) {
			viewer.setInput(lastXRefAdapterList);
		}
	}

	public boolean isShowXRefsForFileEnabled() {
		return showXRefsForFileEnabled;
	}

	public void setShowXRefsForFileEnabled(boolean isOn) {
		showXRefsForFileEnabled = isOn;

		List xraList = null;
		IWorkbenchPart part = null;
		if (!linkingEnabled) {
			// if linking is not enabled then just want to show/hide the cross
			// references
			// for the file of the contents of the xref view
			if (lastLinkedSelection != null && lastLinkedWorkbenchPart != null) {
				part = lastLinkedWorkbenchPart;
				if (showXRefsForFileEnabled) {
					xraList = XRefUIUtils.getXRefAdapterForSelection(
							lastLinkedWorkbenchPart, lastLinkedSelection, true);
				} else {
					xraList = XRefUIUtils
							.getXRefAdapterForSelection(
									lastLinkedWorkbenchPart,
									lastLinkedSelection, false);
				}
			}
		} else {
			// if linking is enabled, then want to show/hide the cross
			// references
			// for the file which is open in the active editor
			if (lastSelection != null && lastWorkbenchPart != null) {
				part = lastWorkbenchPart;
				if (showXRefsForFileEnabled) {
					xraList = XRefUIUtils.getXRefAdapterForSelection(
							lastWorkbenchPart, lastSelection, true);
				} else {
					xraList = XRefUIUtils.getXRefAdapterForSelection(
							lastWorkbenchPart, lastSelection, false);
				}
			}
		}
		if (xraList != null) {
			ISelection sel = viewer.getSelection();
			viewer.setInput(xraList);
			XRefUIUtils.setSelection(part, sel, viewer);
		}
	}

	public void collapseAll() {
		viewer.collapseAll();
	}

	public ISelection getLastSelection() {
		return lastSelection;
	}
	
	public IWorkbenchPart getLastSelectedWorkbenchPart() {
		return lastWorkbenchPart;
	}

	private void persistSettings() {
		IPreferenceStore pstore = XReferenceUIPlugin.getDefault()
				.getPreferenceStore();
		if (!pstore.contains(LINK_ID)) {
			pstore.setDefault(LINK_ID, true);
		}
		pstore.setValue(LINK_ID, linkingEnabled);

		if (!pstore.contains(XREFS_FOR_FILE_ID)) {
			pstore.setDefault(XREFS_FOR_FILE_ID, false);
		}
		pstore.setValue(XREFS_FOR_FILE_ID, showXRefsForFileEnabled);
		XReferenceUIPlugin.getDefault().savePluginPreferences();
	}

	private void restorePersistedSettings() {
		IPreferenceStore pstore = XReferenceUIPlugin.getDefault()
				.getPreferenceStore();
		if (pstore.contains(LINK_ID)) {
			linkingEnabled = pstore.getBoolean(LINK_ID);
		}

		if (pstore.contains(XREFS_FOR_FILE_ID)) {
			showXRefsForFileEnabled = pstore.getBoolean(XREFS_FOR_FILE_ID);
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
		manager.add(toggleShowXRefsForFileAction);
		manager.add(new Separator("filters")); //$NON-NLS-1$
		manager.add(xRefCustomFilterAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
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
		doubleClickAction = new DoubleClickAction(getViewSite().getShell(),
				viewer);
		toggleShowXRefsForFileAction = new ToggleShowXRefsForFileAction(this);
		xRefCustomFilterAction = new XReferenceCustomFilterAction(getSite()
				.getShell());
	}

	/**
	 * @param changeDrivenByBuild
	 *            The changeDrivenByBuild to set.
	 */
	public void setChangeDrivenByBuild(boolean changeDrivenByBuild) {
		this.changeDrivenByBuild = changeDrivenByBuild;
	}

	// ----------------- IPartLisenter implementation ----------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IWorkbenchWindow activeWindow = JavaPlugin
					.getActiveWorkbenchWindow();
			if (activeWindow != null) {
				IWorkbenchPage activePage = activeWindow.getActivePage();
				if (activePage != null) {
					IEditorReference[] openEditors = activePage
							.getEditorReferences();
					if (openEditors.length == 0) {
						// if there are no editors open, then want to clear the
						// contents of the xref view and all the records
						clearView();
					}
				}
			}
		}

	}

	private void clearView() {
		if (viewer != null && viewer.getContentProvider() != null) {
			viewer.setInput(null);
		}
		lastXRefAdapterList = null;
		lastLinkedSelection = null;
		lastLinkedWorkbenchPart = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
	}
	
	
	public Object getAdapter(Class key) {
		if (key.equals(IContextProvider.class)) {
			return XRefUIHelp.getHelpContextProvider(this, IXRefHelpContextIds.XREF_VIEW);
		}
		return super.getAdapter(key);
	}


	// ----------------- This is for testing ----------------------

	/**
	 * Returns the tree viewer for the xref view - this method is for testing
	 * purposes and not part of the published API.
	 */
	public TreeViewer getTreeViewer() {
		return viewer;
	}

	/**
	 * Returns the action for the xref view - this method is for testing
	 * purposes and not part of the published API.
	 */
	public Action getCustomFilterAction() {
		return xRefCustomFilterAction;
	}
	
	/**
	 * Returns the action for the xref view - this method is for testing
	 * purposes and not part of the published API.
	 */
	public Action getToggleShowXRefsForEntireFileAction() {
		return toggleShowXRefsForFileAction;
	}
	
	/**
	 * Returns the action for the xref view - this method is for testing
	 * purposes and not part of the published API.
	 */
	public Action getToggleLinkWithEditorAction() {
		return toggleLinkingAction;
	}

}
