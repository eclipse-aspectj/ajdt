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
 *      Helen Hawkins   - updated for new ajde interface (bug 148190) 
 *******************************************************************************/
package org.eclipse.ajdt.internal.core.ras;

import org.aspectj.org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.ras.PluginFFDC;
import org.eclipse.ajdt.internal.core.ajde.CoreBuildMessageHandler;
import org.eclipse.core.runtime.IStatus;

/**
 * FFDC policy for org.eclipse.ajdt.core plug-in
 */
public aspect CoreFFDC extends PluginFFDC {

	protected pointcut ffdcScope () :
		within(org.eclipse.ajdt..*)
		// Exclude programming by exception cases
		&& !within(NoFFDC+)
		// see pr225785
		&& !withincode(* org.eclipse.ajdt.core.parserbridge.AJSourceElementParser.parseTypeMemberDeclarations(ISourceType,ICompilationUnit,int,int,boolean));
		
    protected String getPluginId () {
    	return AspectJPlugin.PLUGIN_ID;
    }

    protected void log (IStatus status) {
    	AspectJPlugin.getDefault().getLog().log(status);
    }
	
    /* XXX Move to FFDC/PluginFFDC when 78615 fixed */
    declare warning : call(void Throwable.printStackTrace(..)) 
    	&& !within(CoreBuildMessageHandler):
    	"Don't dump stack trace"; //$NON-NLS-1$
}
