/*******************************************************************************
 * Copyright (c) 2005 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core;

/**
 * Minimal logging interface
 */
public interface IAJLogger {
	
	public void log(String msg);

	public void log(int category, String msg);
	
}
