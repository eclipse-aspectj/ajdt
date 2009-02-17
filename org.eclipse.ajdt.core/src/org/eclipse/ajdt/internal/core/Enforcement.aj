/*******************************************************************************
 * Copyright (c) 2004, 2006, 2009 SpringSource, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *     Andrew Eisenberg - more enforcements
 *******************************************************************************/
package org.eclipse.ajdt.internal.core;

import org.eclipse.ajdt.core.AJLog;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IRelationshipMap;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJModelChecker;

/**
 * Check some coding standards and conventions
 */
public aspect Enforcement {

	declare warning : (get(* System.out) || get(* System.err))
		&& !withincode(void AJLog.log(..))
		: "There should be no printlns"; //$NON-NLS-1$
	
	declare warning : call(* Exception.printStackTrace(..)) : 
	    "There should be no calls to printStackTrace"; //$NON-NLS-1$

    declare warning : call(* IHierarchy+.*(..)) && ! within(AJProjectModelFacade): 
        "All calls to IHierarchy should go through AJProjectModelFacade"; //$NON-NLS-1$

    declare warning : call(* IRelationshipMap+.*(..)) && 
    !( within(AJProjectModelFacade) || within(AJModelChecker)) : 
        "All calls to IRelationshipMap should go through AJProjectModelFacade"; //$NON-NLS-1$

}
