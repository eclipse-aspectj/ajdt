/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Main plugin class. Used to determine the filesystem location of the plugin.
 */
public class AspectJTestPlugin extends AbstractUIPlugin {

	/**
	 * shared single instance of the plugin
	 */
	private static AspectJTestPlugin plugin;

	/**
	 * Creates an AspectJTestPlugin instance
	 */
	public AspectJTestPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Return the single default instance of this plugin
	 */
	public static AspectJTestPlugin getDefault() {
		return plugin;
	}

	/**
	 * Return the resolved filesystem location of this plugin
	 * @return the location as a String
	 */
	public static String getPluginDir() {
		String pluginDir=null;
		URL loc = plugin.getBundle().getEntry("/");
		try {
			URL resolved = Platform.resolve(loc);
			pluginDir = resolved.getFile();
		} catch (IOException e) {}		
		if ((pluginDir==null) || (pluginDir.length()==0)) {
			return System.getProperty("user.dir") + File.separator
				+ "workspace" + File.separator + "org.eclipse.ajdt.test"
				+ File.separator;
		}
		return pluginDir;
	}
}