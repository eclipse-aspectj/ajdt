/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - initial version
 *     Helen Hawkins   - updated for new ajde interface (bug 148190)
 *******************************************************************************/
package org.eclipse.ajdt.internal.utils;

/**
 * Check some coding standards and conventions
 */ 
public aspect Enforcement {

	declare warning : (get(* System.out) || get(* System.err)) : "There should be no printlns"; //$NON-NLS-1$
	
	declare warning : call(* Exception.printStackTrace(..)) : 
	    "There should be no calls to printStackTrace"; //$NON-NLS-1$
	
	declare warning : call(void org.eclipse.ajdt.internal.ui.tracing.EventTrace.*(..)) 
		&& !call(void *.startup(..))
		&& !within(org.eclipse.ajdt.internal.ui.tracing.AJDTEventTraceConsolePage)
		&& !within(org.eclipse.ajdt.internal.ui.tracing.EventTrace) 
		&& !within(org.eclipse.ajdt.internal.ui.tracing.EventTraceView)
		&& !within(org.eclipse.ajdt.internal.ui.tracing.EventTraceLogger) :
		"There should be no calls to AJDTEventTrace methods, use AJLog.log instead"; //$NON-NLS-1$
}
