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
package org.eclipse.contribution.xref.internal.ui.actions;

import org.eclipse.contribution.xref.internal.ui.XReferenceUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Provides forward and back navigation icons (actions) on top of a basic
 * history management component.
 */
public class NavigationHistoryActionGroup extends NavigationHistoryManager {

	private Viewer viewer;
	private Action backAction;
	private Action forwardAction;

	/**
	 * Create a new navigation history action group
	 * 
	 * @param viewer
	 *            the viewer whose input will be set (setInput()) in response
	 *            to navigation requests.
	 */
	public NavigationHistoryActionGroup(Viewer viewer) {
		this.viewer = viewer;
	}

	/**
	 * Adds actions for "back", "forward" to a menu manager.
	 * 
	 * @param manager
	 *            is the target manager to update
	 */
	public void addNavigationActions(IMenuManager manager) {
		createActions();
		manager.add(backAction);
		manager.add(forwardAction);
		updateNavigationButtons();
	}

	/**
	 * Adds actions for "back", "forward" to a tool bar manager.
	 * 
	 * @param manager
	 *            is the target manager to update
	 */
	public void addNavigationActions(IToolBarManager toolBar) {
		createActions();
		toolBar.add(backAction);
		toolBar.add(forwardAction);
		updateNavigationButtons();
	}

	/**
	 * Create the actions for navigation.
	 */
	private void createActions() {
		// Only do this once.
		if (backAction != null)
			return;

		// Back.
		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		backAction = 
			new Action(XReferenceUIPlugin.getResourceString("GoBack.text")){//$NON-NLS-1$
				public void run() {
					goBack(viewer.getInput());
				}
			};
		backAction.setToolTipText(XReferenceUIPlugin.getResourceString("GoBack.toolTip")); //$NON-NLS-1$
		backAction.setImageDescriptor(
			images.getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
		backAction.setDisabledImageDescriptor(
			images.getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));

		// Forward.
		forwardAction = 
			new Action(XReferenceUIPlugin.getResourceString("GoForward.text")){//$NON-NLS-1$
				public void run() {
					goForward(viewer.getInput());
				}
			};
		forwardAction.setToolTipText(XReferenceUIPlugin.getResourceString("GoForward.toolTip")); //$NON-NLS-1$
		forwardAction.setImageDescriptor(
			images.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
		forwardAction.setDisabledImageDescriptor(
			images.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD_DISABLED));
	}

	/**
	 * Updates the enabled state for each navigation button.
	 */
	private void updateNavigationButtons() {
		if (backAction != null) {
			backAction.setEnabled(hasBack());
			forwardAction.setEnabled(hasForward());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.aosd.relations.internal.views.NavigationHistoryManager#goBack(java.lang.Object)
	 */
	public Object goBack(Object o) {
		Object back = super.goBack(o);
		viewer.setInput(back);
		updateNavigationButtons();
		return back;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.aosd.relations.internal.views.NavigationHistoryManager#goForward(java.lang.Object)
	 */
	public Object goForward(Object o) {
		Object fwd = super.goForward(o);
		viewer.setInput(fwd);
		updateNavigationButtons();
		return fwd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.aosd.relations.internal.views.NavigationHistoryManager#nowLeaving(java.lang.Object)
	 */
	public void nowLeaving(Object o) {
		super.nowLeaving(o);
		updateNavigationButtons();
	}

}
