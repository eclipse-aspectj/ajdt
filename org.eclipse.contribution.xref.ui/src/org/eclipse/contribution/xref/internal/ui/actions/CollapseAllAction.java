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
 * Collapse all nodes.
 */
public class CollapseAllAction extends Action {
	
	private XReferenceView xrefView;
	
	public CollapseAllAction(XReferenceView v) {
		super(XRefMessages.CollapseAllAction_label);
		setDescription(XRefMessages.CollapseAllAction_description);
		setToolTipText(XRefMessages.CollapseAllAction_tooltip);
		// TODO break dependence on internal api
		JavaPluginImages.setLocalImageDescriptors(this, "collapseall.gif"); //$NON-NLS-1$
		
		xrefView = v;
		//WorkbenchHelp.setHelp(this, IJavaHelpContextIds.COLLAPSE_ALL_ACTION);
	}
 
	public void run() { 
		xrefView.collapseAll();
	}
}
