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
package org.eclipse.contribution.xref.internal.ui.actions;

import org.eclipse.contribution.xref.internal.ui.utils.XReferenceImages;
import org.eclipse.contribution.xref.ui.XReferenceUIPlugin;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.jface.action.Action;

/**
 * This action toggles whether the cross reference view displays 
 * the cross references for the current selection in the active 
 * editor, or for the file which is shown in the active editor.
 */
public class ToggleShowXRefsForFileAction extends Action {

	private XReferenceView xrefView;
	
	/**
	 * Constructs a new action.
	 */
	public ToggleShowXRefsForFileAction(XReferenceView xrefView) {
		super(XReferenceUIPlugin.getResourceString("ToggleShowXRefsForFileAction.label")); //$NON-NLS-1$
		setDescription(XReferenceUIPlugin.getResourceString("ToggleShowXRefsForFileAction.description")); //$NON-NLS-1$
		setToolTipText(XReferenceUIPlugin.getResourceString("ToggleShowXRefsForFileAction.tooltip")); //$NON-NLS-1$
		setImageDescriptor(XReferenceImages.XREFS_FOR_ENTIRE_FILE);
		setChecked(xrefView.isShowXRefsForFileEnabled());
		this.xrefView = xrefView;
	}

	/**
	 * Runs the action.
	 */
	public void run() {
		xrefView.setShowXRefsForFileEnabled(isChecked());
	}
}
