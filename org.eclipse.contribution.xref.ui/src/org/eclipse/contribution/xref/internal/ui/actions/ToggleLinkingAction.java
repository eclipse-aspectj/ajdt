/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
import org.eclipse.contribution.xref.ui.views.XReferenceView;
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
		super(XRefMessages.ToggleLinkingAction_label);
		setDescription(XRefMessages.ToggleLinkingAction_description);
		setToolTipText(XRefMessages.ToggleLinkingAction_tooltip);
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
