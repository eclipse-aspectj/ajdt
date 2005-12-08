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
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurationCreationException;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.buildconfig.DefaultBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IProjectBuildConfigurator;
import org.eclipse.jface.action.Action;

/**
 * @author Luzius Meisser
 *
 * Action to add a Build Configuration
 */
public class AddBuildConfigurationAction extends Action {
	static int counter = 0;

	IBuildConfigurator buildconf;
	
	AddBuildConfigurationAction(){
		super(UIMessages.BCLabels_SaveBCAs);
		buildconf = DefaultBuildConfigurator.getBuildConfigurator();
	}
	
	public void run(){
		if (buildconf.getActiveProjectBuildConfigurator() == null){
			return;
		}
		IProjectBuildConfigurator pbc = buildconf.getActiveProjectBuildConfigurator();
		
		try {
			new BuildConfiguration(pbc, null);
		} catch (BuildConfigurationCreationException e) {
			// creation failed, maybe user has cancelled it, do nothing
			// can be ignored
		}
		
	}
	
}
