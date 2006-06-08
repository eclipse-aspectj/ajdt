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
package org.eclipse.contribution.xref.internal.core;


/**
 * @author hawkinsh
 *
 */
public aspect ProviderExceptionLoggingTest {

	public static boolean exceptionLoggedOnRegistration = false;
	
	pointcut registeringProviders() : execution (* XReferenceProviderManager.getRegisteredProviders()) ;
	pointcut logCall() : call (* XReferencePlugin.log(..)) ;
	
	after() returning : cflow(registeringProviders()) && logCall() {
		exceptionLoggedOnRegistration = true;
	}
	
}
