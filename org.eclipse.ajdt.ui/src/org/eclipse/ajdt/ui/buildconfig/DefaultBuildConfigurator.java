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
package org.eclipse.ajdt.ui.buildconfig;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.bc.ProjectBuildConfigurator;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;

public class DefaultBuildConfigurator implements IBuildConfigurator {
	private static IBuildConfigurator INSTANCE = new DefaultBuildConfigurator();
	
	public static synchronized IBuildConfigurator getBuildConfigurator() {
		return INSTANCE;
	}

	public static void setBuildConfigurator(IBuildConfigurator bc) {
		INSTANCE = bc;
	}

	public IProjectBuildConfigurator getProjectBuildConfigurator(IProject project) {
		return new ProjectBuildConfigurator(project);
	}

	public IProjectBuildConfigurator getActiveProjectBuildConfigurator() {
		return getProjectBuildConfigurator(AspectJPlugin.getDefault().getCurrentProject());
	}

	public IProjectBuildConfigurator getProjectBuildConfigurator(IJavaProject jp) {
		return getProjectBuildConfigurator(jp.getProject());
	}

	public void setup(IProject project) {
	}

	public void restoreJDTState(IProject project) {
	}

}
