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

import org.aspectj.lang.*;

public abstract aspect FFDC {
	
    public abstract pointcut ffdcScope ();
    
    final pointcut staticContext () : !this(Object);
    final pointcut nonStaticContext (Object obj) : this(obj);
    final pointcut caughtThrowable (Throwable th) : handler(Throwable+) && args(th);

    // Advice for catch blocks in static contexts
    before (Throwable th) : caughtThrowable(th) && ffdcScope() && staticContext() {
       processStaticFFDC(th,thisJoinPointStaticPart);
    }

    // Advice for catch blocks in non-static contexts
    before (Throwable th, Object obj) : caughtThrowable(th) && ffdcScope() && nonStaticContext(obj) {
       processNonStaticFFDC(th,obj,thisJoinPointStaticPart );
    }
    
    protected abstract void processStaticFFDC (Throwable th, JoinPoint.StaticPart tjp);

    protected abstract void processNonStaticFFDC (Throwable th, Object obj, JoinPoint.StaticPart tjp);

}
