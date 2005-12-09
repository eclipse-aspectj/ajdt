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

import org.eclipse.ajdt.ui.buildconfig.IBuildConfiguration;
import org.eclipse.ajdt.ui.buildconfig.IProjectBuildConfigurator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;

public class ProjectBuildConfigurator implements IProjectBuildConfigurator {

	private IProject project;
	
	public ProjectBuildConfigurator(IProject project) {
		this.project = project;
	}

	public IBuildConfiguration getActiveBuildConfiguration() {
		return new BuildConfiguration(project);
	}

	public IJavaProject getJavaProject() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getBuildConfigurations() {
		// TODO Auto-generated method stub
		return null;
	}

	public IFile[] getConfigurationFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean fullBuildRequested() {
		// TODO Auto-generated method stub
		return false;
	}

	public void requestFullBuild(boolean b) {
		// TODO Auto-generated method stub
		
	}

	public void setActiveBuildConfiguration(IBuildConfiguration bc) {
		// TODO Auto-generated method stub	
	}

	public void reInit() {
		// TODO Auto-generated method stub
		
	}

	public IBuildConfiguration getBuildConfiguration(IFile f) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeBuildConfiguration(IBuildConfiguration bc) {
		// TODO Auto-generated method stub
		
	}

	public void addBuildConfiguration(IBuildConfiguration bc) {
		// TODO Auto-generated method stub
		
	}

	public void setActiveBuildConfiguration(IFile buildFile) {
		// TODO Auto-generated method stub
		
	}
}
