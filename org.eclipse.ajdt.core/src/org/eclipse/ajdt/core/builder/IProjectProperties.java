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
 *******************************************************************************/
package org.eclipse.ajdt.core.builder;

import org.aspectj.ajde.ProjectPropertiesAdapter;
import org.eclipse.core.resources.IProject;

/**
 * AJDT extension of ajde project properties
 */
public interface IProjectProperties extends ProjectPropertiesAdapter {

	/**
	 * Query whether the previously returned List of project source files is still
	 * current. If it hasn't then getProjectSourceFiles() doesn't need to be called
	 * again - a good optimization as the call is somewhat expensive.
	 * @param project
	 * @return true if the file list hasn't changed
	 */
	public boolean isProjectSourceFileListKnown(IProject project);
	
	/**
	 * Change the dirty state of the project source file list
	 * @param project
	 * @param known
	 */
	public void setProjectSourceFileListKnown(IProject project, boolean known);
}
