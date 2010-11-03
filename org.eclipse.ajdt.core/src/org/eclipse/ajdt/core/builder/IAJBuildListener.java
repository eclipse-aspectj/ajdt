/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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

import java.util.List;
import java.util.Map;

import org.eclipse.ajdt.core.lazystart.IAdviceChangedListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.compiler.CategorizedProblem;

/**
 * A listener for receiving notifications relating to the building of AspectJ
 * projects
 */
public interface IAJBuildListener {

	/**
	 * The given project is about to be built
	 * 
	 * @param kind
	 * @param project
	 * @param requiredProjects
	 */
	public void preAJBuild(int kind, IProject project, IProject[] requiredProjects);

	/**
	 * The given project has just been built
	 * @param project
	 * @param noSourceChanges
	 * @param newProblems TODO
	 */
	public void postAJBuild(int kind, IProject project, boolean noSourceChanges, Map<IFile, List<CategorizedProblem>> newProblems);

	/**
	 * Add a listener to be notified when there is a change in the set of
	 * advised elements (after a build)
	 * 
	 * @param adviceListener
	 */
	public void addAdviceListener(IAdviceChangedListener adviceListener);
	
	/**
	 * Remove a listener added via addAdviceListener
	 * 
	 * @param adviceListener
	 */
	public void removeAdviceListener(IAdviceChangedListener adviceListener);

	/**
	 * Called after a clean has been performed on the AspectJ project
	 */
	public void postAJClean(IProject project);
}
