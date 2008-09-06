/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import org.eclipse.ajdt.internal.core.ajde.CoreCompilerConfiguration;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.core.resources.IProject;

/**
 * A complete implementation of ICompilerConfiguration.  If the ajdt.ui 
 * plugin is not present in the platform, then CoreCompilerConfiguration
 * is used instead.
 */
public class UIComplierConfiguration extends CoreCompilerConfiguration {

	public UIComplierConfiguration(IProject project) {
		super(project);
	}
	
	public String getNonStandardOptions() {
		//IProject currentProject = AspectJPlugin.getDefault().getCurrentProject();
		String nonStandardOptions = AspectJPreferences.getCompilerOptions(project);		
		nonStandardOptions += AspectJPreferences.getLintOptions(project);
		nonStandardOptions += AspectJPreferences.getAdvancedOptions(project);
		if (AspectJPreferences.getShowWeaveMessagesOption(project)) {
			nonStandardOptions += " -showWeaveInfo"; //$NON-NLS-1$
		}
		return nonStandardOptions;
	}

	
}
