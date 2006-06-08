/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.internal.ajdt.utils;

import org.eclipse.ajdt.internal.ui.ajde.ErrorHandler;

/**
 *
 */
public aspect Enforcement {

	declare warning : (get(* System.out) || get(* System.err)) : "There should be no printlns"; //$NON-NLS-1$
	
	declare warning : call(* Exception.printStackTrace(..)) : 
	    "There should be no calls to printStackTrace"; //$NON-NLS-1$
	
	declare warning : call(void org.eclipse.ajdt.internal.utils.AJDTEventTrace.*(..)) 
		&& !call(void *.startup(..))
		&& !within(org.eclipse.ajdt.internal.utils.AJDTEventTrace) 
		&& !within(org.eclipse.ajdt.internal.ui.AJDTEventTraceView)
		&& !within(org.eclipse.ajdt.internal.ui.EventTraceLogger) :
		"There should be no calls to AJDTEventTrace methods, use AJLog.log instead"; //$NON-NLS-1$

		declare error : call(void ErrorHandler.handleError(..)) :
		"Calls to handleError should not come from within AJDT, use ErrorHandler.handleAJDTError instead"; //$NON-NLS-1$

}
