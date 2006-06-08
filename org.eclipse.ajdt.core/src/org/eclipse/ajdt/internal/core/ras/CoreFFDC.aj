/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     	IBM Corporation - initial API and implementation
 * 		Matthew Webster - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.core.ras;

import org.eclipse.ajdt.ras.PluginFFDC;
import org.eclipse.ajdt.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.builder.CoreErrorHandler;
import org.eclipse.core.runtime.IStatus;

/**
 * FFDC policy for org.eclipse.ajdt.core plug-in
 */
public aspect CoreFFDC extends PluginFFDC {

	protected pointcut ffdcScope () :
		within(org.eclipse.ajdt..*)
		// Exclude programming by exception cases
		&& !within(AspectsConvertingParser);
		
    protected String getPluginId () {
    	return AspectJPlugin.PLUGIN_ID;
    }

    protected void log (IStatus status) {
    	AspectJPlugin.getDefault().getLog().log(status);
    }
	
    /* XXX Move to FFDC/PluginFFDC when 78615 fixed */
    declare warning : call(void Throwable.printStackTrace(..)) 
    	&& !within(CoreErrorHandler):
    	"Don't dump stack trace"; //$NON-NLS-1$
}
