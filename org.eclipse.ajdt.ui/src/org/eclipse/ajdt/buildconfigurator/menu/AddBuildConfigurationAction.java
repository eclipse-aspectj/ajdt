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

package org.eclipse.ajdt.buildconfigurator.menu;

import org.eclipse.ajdt.buildconfigurator.BuildConfiguration;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurationCreationException;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurator;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.jface.action.Action;

/**
 * @author Luzius Meisser
 *
 * Action to add a Build Configuration
 */
public class AddBuildConfigurationAction extends Action {
	static int counter = 0;

	BuildConfigurator buildconf;
	
	AddBuildConfigurationAction(){
		super(AspectJUIPlugin.getResourceString("BCLabels.SaveBCAs"));
		buildconf = BuildConfigurator.getBuildConfigurator();
	}
	
	public void run(){
		if (buildconf.getActiveProjectBuildConfigurator() == null){
			return;
		}
		ProjectBuildConfigurator pbc = buildconf.getActiveProjectBuildConfigurator();
		
		try {
			new BuildConfiguration(pbc, null);
		} catch (BuildConfigurationCreationException e) {
			// creation failed, maybe user has cancelled it, do nothing
			// can be ignored
		}
		
	}
	
}
