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
package org.eclipse.ajdt.core.ras;

import org.aspectj.lang.*;

import org.eclipse.ajdt.ras.FFDC;
import org.eclipse.ajdt.core.AspectJPlugin;

public aspect CoreFFDC extends FFDC {

	public pointcut ffdcScope () :
		within(org.eclipse.ajdt..*)
		&& !within(CoreFFDC);

    declare warning : call(void AspectJPlugin.logException(..)) && !within(FFDC+) :
    	"Only FFDC aspect should call logException()"; //$NON-NLS-1$

    declare warning : call(void Throwable.printStackTrace(..)) :
    	"Don't dump stack trace"; //$NON-NLS-1$
    
    protected void processStaticFFDC (Throwable th, JoinPoint.StaticPart tjp) {
    	AspectJPlugin.logException(th);
    }

	protected void processNonStaticFFDC (Throwable th, Object obj, JoinPoint.StaticPart tjp) {
    	AspectJPlugin.logException(th);
    }
	
}
