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
import org.eclipse.contribution.xref.internal.ui.views.XReferenceView;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.action.Action;


/**
 * This action toggles whether the relationships view follows the
 * active workbench selection.
 */
public class ToggleLinkingAction extends Action {
	
	XReferenceView xrefView;
	
	/**
	 * Constructs a new action.
	 */
	public ToggleLinkingAction(XReferenceView xrefView) {
		super(XReferenceUIPlugin.getResourceString("ToggleLinkingAction.label")); //$NON-NLS-1$
		setDescription(XReferenceUIPlugin.getResourceString("ToggleLinkingAction.description")); //$NON-NLS-1$
		setToolTipText(XReferenceUIPlugin.getResourceString("ToggleLinkingAction.tooltip")); //$NON-NLS-1$
		// TODO break dependency on internal api
		JavaPluginImages.setLocalImageDescriptors(this, "synced.gif"); //$NON-NLS-1$		
//		WorkbenchHelp.setHelp(this, IJavaHelpContextIds.LINK_EDITOR_ACTION);

		setChecked(xrefView.isLinkingEnabled());
		this.xrefView = xrefView;
	}

	/**
	 * Runs the action.
	 */
	public void run() {
		xrefView.setLinkingEnabled(isChecked());
	}

}
