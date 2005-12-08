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
import org.eclipse.ajdt.internal.buildconfig.BCResourceChangeListener;
import org.eclipse.ajdt.internal.buildconfig.BCWorkbenchWindowInitializer;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.DefaultBuildConfigurator;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class BuildConfigPlugin extends AbstractUIPlugin implements IStartup {
	public void start(BundleContext context) throws Exception {
		// register the our build config implementations with ajdt.ui
		DefaultBuildConfigurator.setBuildConfigurator(new BuildConfigurator());
		
		super.start(context);
		
		// Update project menu and listen for project selections
		new BCWorkbenchWindowInitializer();

		// listener for build configurator
		AspectJPlugin.getWorkspace().addResourceChangeListener(
				new BCResourceChangeListener(),
				IResourceChangeEvent.PRE_CLOSE
						| IResourceChangeEvent.PRE_DELETE
						| IResourceChangeEvent.POST_CHANGE
						| IResourceChangeEvent.PRE_BUILD);
	}

	public void earlyStartup() {
	}
}
