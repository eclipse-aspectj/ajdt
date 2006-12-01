/**********************************************************************
 Copyright (c) 2002, 2005 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Adrian Colyer, Andy Clement, Tracy Gardner - initial version
 
 AMC 08/12/2002  Changed getAspectjrtClasspath to get version info 
 from Ajde instead of being hard-coded.
 
 Geoff Longman 11/27/2002 Change getClasspath to retrieve entire classpath from
 Project dependencies.

 Matt Chapman - moved getAspectjrtClasspath to core plugin (84967)

 **********************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.builder.CoreProjectProperties;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.core.resources.IProject;

/**
 * ProjectProperties is used to pass all the user, project and plugin settings
 * to AJ Tools.
 */
public class ProjectProperties extends CoreProjectProperties  {
	
	/*
	 * @see ProjectPropertiesAdapter#getExecutionArgs()
	 */
	public String getExecutionArgs() {
		IProject project = AspectJPlugin.getDefault().getCurrentProject();
		return AspectJPreferences.getCompilerOptions(project);
	}

}