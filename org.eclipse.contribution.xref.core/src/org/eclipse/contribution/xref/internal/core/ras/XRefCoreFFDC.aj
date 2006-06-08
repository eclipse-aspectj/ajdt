/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.contribution.xref.internal.core.ras;

import org.eclipse.contribution.xref.core.XReferencePlugin;
import org.eclipse.contribution.xref.ras.PluginFFDC;
import org.eclipse.core.runtime.IStatus;

public aspect XRefCoreFFDC extends PluginFFDC  {
    
	protected pointcut ffdcScope () :
		within(org.eclipse.contribution.xref..*);
		
    protected String getPluginId () {
    	return XReferencePlugin.PLUGIN_ID;
    }

    protected void log (IStatus status) {
    	XReferencePlugin.getDefault().getLog().log(status);
    }
	
    /* XXX Move to FFDC/PluginFFDC when 78615 fixed */
    declare warning : call(void Throwable.printStackTrace(..)) :
    	"Don't dump stack trace"; //$NON-NLS-1$
}
