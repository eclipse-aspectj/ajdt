/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.buildconfig.menu;

import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.jface.action.Action;

/**
 * @author Luzius Meisser
 * 
 * Action to select a build configuration
 */

public class BuildConfigurationSelectedAction extends Action {
	ProjectBuildConfigurator pbc;
	BuildConfiguration myBC; 
	
	public BuildConfigurationSelectedAction(BuildConfiguration bc){
		super(bc.getName());
		myBC = bc;
		pbc = BuildConfigurator.getBuildConfigurator().getActiveProjectBuildConfigurator();
	}
	
	public void run(){
		pbc.setActiveBuildConfiguration(myBC);
	}
	

}
