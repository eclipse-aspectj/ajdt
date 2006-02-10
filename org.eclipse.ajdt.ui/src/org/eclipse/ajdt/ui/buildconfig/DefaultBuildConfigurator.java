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

import java.util.HashMap;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.bc.ProjectBuildConfigurator;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;

public class DefaultBuildConfigurator implements IBuildConfigurator {
	private static IBuildConfigurator INSTANCE = new DefaultBuildConfigurator();
	private HashMap projectConfigurators = new HashMap();
	
	public static synchronized IBuildConfigurator getBuildConfigurator() {
		return INSTANCE;
	}

	public IProjectBuildConfigurator getProjectBuildConfigurator(IProject project) {
		if ((project == null) || (!project.isOpen()) || !AspectJPlugin.isAJProject(project)) {
			return null;
		} else {
			ProjectBuildConfigurator pbc = (ProjectBuildConfigurator) projectConfigurators
				.get(project.getName());
			if (pbc == null) {
				pbc =  new ProjectBuildConfigurator(project);
				projectConfigurators.put(project.getName(), pbc);
			}
			return pbc;
		}
	}

	public IProjectBuildConfigurator getActiveProjectBuildConfigurator() {
		return getProjectBuildConfigurator(AspectJPlugin.getDefault().getCurrentProject());
	}

	public IProjectBuildConfigurator getProjectBuildConfigurator(IJavaProject jp) {
		return getProjectBuildConfigurator(jp.getProject());
	}

}
