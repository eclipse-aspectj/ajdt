/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.core;

/**
 * Minimal logging - if a logger hasn't been set, dump to sdout
 */
public class AJLog {

	private static AJLogger logger;
	
	public static void log(String msg) {
		if (logger != null) {
			logger.log(msg);
		} else {
			System.out.println(msg);
		}
	}
	
	public static void setLogger(AJLogger l) {
		logger = l;
	}
}
