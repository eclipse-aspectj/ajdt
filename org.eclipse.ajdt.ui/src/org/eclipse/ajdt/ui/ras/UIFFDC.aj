/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     	IBM Corporation - initial API and implementation
 * 		Matthew Webster - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.ras;

import org.aspectj.lang.JoinPoint;

//import org.eclipse.ajdt.ras.FFDC;
import org.eclipse.ajdt.ui.AspectJUIPlugin;

public aspect UIFFDC extends FFDC {

	public pointcut ffdcScope () :
		within(org.eclipse.ajdt..*)
		&& !within(UIFFDC);

    declare warning : call(void AspectJUIPlugin.logException(..)) && !within(FFDC+) :
    	"Only FFDC aspect should call logException()";

    declare warning : call(* AspectJUIPlugin.getLog(..)) && !within(FFDC+) && !within(AspectJUIPlugin) :
    	"Only FFDC aspect or AspectJUIPlugin should call getLog()";

    declare warning : call(void Throwable.printStackTrace(..)) :
    	"Don't dump stack trace";
    
    protected void processStaticFFDC (Throwable th, JoinPoint.StaticPart tjp) {
    	AspectJUIPlugin.logException(th);
    }

	protected void processNonStaticFFDC (Throwable th, Object obj, JoinPoint.StaticPart tjp) {
    	AspectJUIPlugin.logException(th);
    }

}
