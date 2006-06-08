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
package org.eclipse.contribution.xref.internal.core.policy;


/**
 * An aspect to insure internal Eclipse API's are not used
 */
public aspect DontUseEclipseInternalAPIs {

	pointcut eclipseInternalAPICall() :
		call(* org.eclipse..internal..*.*(..)) ||
		call(org.eclipse..internal..*.new(..));
	
	pointcut xRefCoreCall() :
		call(* org.eclipse.contribution.xref..*.*(..)) ||
		call(org.eclipse.contribution.xref..*.new(..));
	
	declare warning : eclipseInternalAPICall() && !xRefCoreCall() :
		"Avoid use of internal Eclipse APIs"; //$NON-NLS-1$
	
}
