/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.contribution.xref.ras;

import org.aspectj.lang.JoinPoint;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * FFDC aspect - a duplicate of org.eclipse.ajdt.ras.FFDC until longer
 * term solution is in place
 */
public abstract aspect FFDC {

	/** 
	 * Scope of FFDC policy e.g. packages, classes, methods is declared by
	 * sub-aspect.
	 */ 
    protected abstract pointcut ffdcScope ();
    
    private pointcut staticContext () : !this(Object);
    private pointcut nonStaticContext (Object obj) : this(obj);
    private pointcut caughtThrowable (Throwable th) : handler(Throwable+) && args(th);

    /** 
     * Exclude FFDC aspects from exception reporting to avoid unwanted
     * recursion. Also exclude exceptions for cancelled operations
     */
    private pointcut excluded () : within(FFDC+) || handler(OperationCanceledException) || within(NoFFDC+);

    /** 
     * Advice for catch blocks in static context
     */
    before (Throwable th) : caughtThrowable(th) && ffdcScope() && !excluded() && staticContext() {
       processStaticFFDC(th,thisJoinPointStaticPart,thisEnclosingJoinPointStaticPart);
    }

    /** 
     * Advice for catch blocks in non-static context. Extract the object
     * that caught the exception
     */
    before (Throwable th, Object obj) : caughtThrowable(th) && ffdcScope() && !excluded() && nonStaticContext(obj) {
       processNonStaticFFDC(th,obj,thisJoinPointStaticPart,thisEnclosingJoinPointStaticPart);
    }

    /** 
     * Template method for consumption of raw FFDC in a static context
     */ 
    protected void processStaticFFDC (Throwable th, JoinPoint.StaticPart tjp, JoinPoint.StaticPart ejp) {
    	processStaticFFDC(th,getSourceId(tjp,ejp));
    }

    /** 
     * Template method for consumption of raw FFDC in a non-static context
     */ 
    protected void processNonStaticFFDC (Throwable th, Object obj, JoinPoint.StaticPart tjp, JoinPoint.StaticPart ejp) {
    	processNonStaticFFDC(th,obj,getSourceId(tjp,ejp));
    }

    /** Generate source id describing where an exception is caught
     * 
	 * @return a String containing fully qualified class name, method name,
	 * source file name and line number
     */ 
    protected String getSourceId (JoinPoint.StaticPart tjp, JoinPoint.StaticPart ejp) {
    	StringBuffer sourceId = new StringBuffer();
		String typeName = ejp.getSignature().getDeclaringTypeName();
		String name = ejp.getSignature().getName();
		String sourceLocation = tjp.getSourceLocation().toString();
    	sourceId.append(typeName).append(".").append(name); //$NON-NLS-1$
    	sourceId.append("(").append(sourceLocation).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
    	return sourceId.toString();
    }

    /** Template method for consumption of static FFDC
     * 
     */ 
    protected abstract void processStaticFFDC (Throwable th, String sourceId);
    
    /** Template method for consumption of non-static FFDC
     * 
     */
    protected abstract void processNonStaticFFDC (Throwable th, Object obj, String sourcId);
}
