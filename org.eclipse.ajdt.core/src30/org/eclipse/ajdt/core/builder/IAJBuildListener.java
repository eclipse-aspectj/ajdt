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

import org.eclipse.core.resources.IProject;

/**
 * A listener for receiving notifications relating to the building of AspectJ projects
 */
public interface IAJBuildListener {

	/**
	 * The given project is about to be built
	 * @param project
	 */
	public void preAJBuild(int kind, IProject project, IProject[] requiredProjects);
	
	/**
	 * The given project has just been built
	 * @param project
	 */
	public void postAJBuild(IProject project, boolean buildCancelled, boolean noSourceChanges);
	
}
