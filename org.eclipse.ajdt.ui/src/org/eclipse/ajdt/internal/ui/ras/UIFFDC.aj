/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     	IBM Corporation - initial API and implementation
 * 		Matthew Webster - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.ras;

import org.eclipse.ajdt.core.ras.PluginFFDC;
import org.eclipse.ajdt.internal.ui.dialogs.OpenTypeSelectionDialog2;
import org.eclipse.ajdt.internal.ui.editor.AspectJBreakpointRulerAction;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;

/**
 * FFDC policy for org.eclipse.ajdt.ui plug-in
 */
public aspect UIFFDC extends PluginFFDC {

	public pointcut ffdcScope () :
		within(org.eclipse.ajdt..*)
		// Exclude lazy start package to avoid early plugin activation
		&& !within(org.eclipse.ajdt.internal.ui.lazystart..*)
		// Exclude programming by exception cases
		&& !within(OpenTypeSelectionDialog2)
		&& !(within(AspectJBreakpointRulerAction) && handler(BadLocationException));
		
    protected String getPluginId () {
    	return AspectJUIPlugin.PLUGIN_ID;
    }

    protected void log (IStatus status) {
    	AspectJUIPlugin.getDefault().getLog().log(status);
    }
	
    /* XXX Move to FFDC/PluginFFDC when 78615 fixed */
    declare warning : call(void Throwable.printStackTrace(..)) :
    	"Don't dump stack trace"; //$NON-NLS-1$
}
