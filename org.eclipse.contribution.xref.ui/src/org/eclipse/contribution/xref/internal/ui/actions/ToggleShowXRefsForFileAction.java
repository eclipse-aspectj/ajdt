/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.internal.ui.actions;

import org.eclipse.contribution.xref.internal.ui.text.XRefMessages;
import org.eclipse.contribution.xref.internal.ui.utils.XReferenceImages;
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
		super(XRefMessages.ToggleShowXRefsForFileAction_label);
		setDescription(XRefMessages.ToggleShowXRefsForFileAction_description);
		setToolTipText(XRefMessages.ToggleShowXRefsForFileAction_tooltip);
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
