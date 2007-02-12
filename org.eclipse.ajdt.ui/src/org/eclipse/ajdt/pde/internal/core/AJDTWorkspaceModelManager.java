/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.pde.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.WorkspacePluginModelManager;

/**
 * Class to enable us to call getWorkspacePluginModel(IProject) since
 * visibility of this method has changed in 3.1 M7 
 */
public class AJDTWorkspaceModelManager extends WorkspacePluginModelManager {

	public IPluginModelBase getWorkspacePluginModel(IProject project) {
		return super.getPluginModel(project);
	}
	
}
