/*******************************************************************************
 * Copyright (c) 2008 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Eisenberg  (SpringSource)
 *******************************************************************************/
/*
 * Copied from org.eclipse.mylyn.internal.java.ui.actions.ToggleActiveFoldingAction
 */
package org.eclipse.ajdt.mylyn.ui.actions;

import org.eclipse.ajdt.mylyn.ui.AspectJStructureBridgePlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author Mik Kersten
 * @author andrew eisenberg
 */
public class AJToggleActiveFoldingAction extends Action implements IWorkbenchWindowActionDelegate, IActionDelegate2 {

	private static AJToggleActiveFoldingAction INSTANCE;

	private IAction parentAction = null;

	public AJToggleActiveFoldingAction() {
		super();
		INSTANCE = this;
		setText("Active folding");
		setImageDescriptor(TasksUiImages.CONTEXT_FOCUS);
	}

	public static void toggleFolding(boolean on) {
		if (INSTANCE.parentAction != null) {
			INSTANCE.valueChanged(INSTANCE.parentAction, on);
		}
	}

	public void run(IAction action) {
		valueChanged(action, action.isChecked());
	}

	private void valueChanged(IAction action, final boolean on) {
		try {
			if (on) {
				org.eclipse.jdt.internal.ui.JavaPlugin.
					getDefault().getPreferenceStore().setValue(PreferenceConstants.EDITOR_FOLDING_ENABLED, true);
			}
			action.setChecked(on);
			org.eclipse.mylyn.internal.java.ui.JavaUiBridgePlugin
				.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.java.ui.JavaUiBridgePlugin
						.AUTO_FOLDING_ENABLED, on);
		} catch (Throwable t) {
			StatusHandler.fail(new Status(IStatus.ERROR, AspectJStructureBridgePlugin.PLUGIN_ID, "Could not enable editor management", t));
		}
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// don't care when the active editor changes
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// don't care when the selection changes
	}

	public void init(IAction action) {
		this.parentAction = action;
		valueChanged(action, org.eclipse.mylyn.internal.java.ui.JavaUiBridgePlugin
				.getDefault().getPreferenceStore().getBoolean(
						org.eclipse.mylyn.internal.java.ui.JavaUiBridgePlugin
						.AUTO_FOLDING_ENABLED));
	}

	public void dispose() {
		// don't need to do anything

	}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public void init(IWorkbenchWindow window) {
	}
}
