/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *     Helen Hawkins - updated for new ajde interface (bug 148190)
 *******************************************************************************/
package org.eclipse.ajdt.core.builder;

import org.aspectj.ajde.core.IBuildProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Define additional operations required by AJDT
 */
public interface IAJCompilerMonitor extends IBuildProgressMonitor {

	/**
	 * Prepare the compiler monitor with the given 
	 * org.eclipse.core.runtime.IProgressMonitor
	 * 
	 * @param eclipseMonitor
	 */
	public void prepare(IProgressMonitor eclipseMonitor);
	
	/**
	 * Return whether the previously run build was cancelled
	 * 
	 * @return true if the last build was cancelled and false otherwise
	 */
	public boolean buildWasCancelled();

}
