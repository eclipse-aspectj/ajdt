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
package org.eclipse.ajdt.core.builder;

import java.util.List;

import org.aspectj.ajde.BuildProgressMonitor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Define additional operations required by AJDT
 */
public interface AJCompilerMonitor extends BuildProgressMonitor {

	public void prepare(IProject project, List buildList,
			IProgressMonitor eclipseMonitor);

	public boolean finished();

}
