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


import org.eclipse.contribution.xref.ui.XReferenceUIPlugin;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.action.Action;

/**
 * Collapse all nodes.
 */
public class CollapseAllAction extends Action {
	
	private XReferenceView xrefView;
	
	public CollapseAllAction(XReferenceView v) {
		super(XReferenceUIPlugin.getResourceString("CollapseAllAction.label")); //$NON-NLS-1$
		setDescription(XReferenceUIPlugin.getResourceString("CollapseAllAction.description")); //$NON-NLS-1$
		setToolTipText(XReferenceUIPlugin.getResourceString("CollapseAllAction.tooltip")); //$NON-NLS-1$
		// TODO break dependence on internal api
		JavaPluginImages.setLocalImageDescriptors(this, "collapseall.gif"); //$NON-NLS-1$
		
		xrefView = v;
		//WorkbenchHelp.setHelp(this, IJavaHelpContextIds.COLLAPSE_ALL_ACTION);
	}
 
	public void run() { 
		xrefView.collapseAll();
	}
}
