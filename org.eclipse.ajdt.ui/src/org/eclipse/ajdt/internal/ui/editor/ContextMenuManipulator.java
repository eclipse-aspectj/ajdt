/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
//import org.eclipse.jface.action.IContributionItem;

/**
 * Looks for the jdt breakpoint context menu action and replaces it by our version
 */
public class ContextMenuManipulator implements IMenuListener {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	public void menuAboutToShow(IMenuManager manager) {		
//		IContributionItem jdtBreakpointManager = manager.find("org.eclipse.jdt.debug.ui.actions.ManageBreakpointRulerActionDelegate");
//		IContributionItem ajdtBreakpointManager = manager.find("org.eclipse.ajdt.internal.ui.editor.AspectJBreakpointRulerActionDelegate");
//		if ((jdtBreakpointManager != null) && (ajdtBreakpointManager != null)){
//			manager.remove(ajdtBreakpointManager);
//			manager.insertAfter(jdtBreakpointManager.getId(), ajdtBreakpointManager);
//			manager.remove(jdtBreakpointManager);
//			jdtBreakpointManager.setVisible(false);
//		}
	}

}
