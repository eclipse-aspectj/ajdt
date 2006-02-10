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
package org.eclipse.ajdt.internal.bc;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.ajdt.ui.buildconfig.IBuildConfiguration;
import org.eclipse.ajdt.ui.buildconfig.IProjectBuildConfigurator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class ProjectBuildConfigurator implements IProjectBuildConfigurator {

	private IProject project;
	
	public ProjectBuildConfigurator(IProject project) {
		this.project = project;
	}

	public IBuildConfiguration getActiveBuildConfiguration() {
		return new BuildConfiguration(project);
	}
	
	public IJavaProject getJavaProject() {
		return JavaCore.create(project);
	}

	public Collection getBuildConfigurations() {
		return Collections.EMPTY_LIST;
	}

	public IFile[] getConfigurationFiles() {
		return new IFile[0];
	}

}
