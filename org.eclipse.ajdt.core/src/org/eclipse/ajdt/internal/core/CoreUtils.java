/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *     Matt Chapman - moved getAspectjrtClasspath here from ui plugin (84967)
 *******************************************************************************/
package org.eclipse.ajdt.internal.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * 
 * @author mchapman
 */
public class CoreUtils {

	/**
	 * Computed classpath to aspectjrt.jar
	 */
	private static String aspectjrtPath = null;
	
	/**
	 * Return the fully-qualified name of the root directory for a project.
	 */
	public static String getProjectRootDirectory(IProject project) {
		return project.getLocation().toOSString();
	}

	public static interface FilenameFilter {
		public boolean accept(String name);
	}

	public static final FilenameFilter ASPECTJ_SOURCE_ONLY_FILTER = new FilenameFilter() {
		public boolean accept(String name) {
			return (name.endsWith(".aj")); //$NON-NLS-1$
		}
	};
	
	public static final FilenameFilter ASPECTJ_SOURCE_FILTER = new FilenameFilter() {
		public boolean accept(String name) {
			return (name.endsWith(".java") || name.endsWith(".aj"));  //$NON-NLS-1$ //$NON-NLS-2$
		}
	};

	public static final FilenameFilter RESOURCE_FILTER = new FilenameFilter() {
		public boolean accept(String name) {
			return !(name.endsWith(".java") || name.endsWith(".aj") || name  //$NON-NLS-1$ //$NON-NLS-2$
					.endsWith(".class")); //$NON-NLS-1$
		}
	};

	/**
	 * Get the aspectjrt.jar classpath entry. This is usually in
	 * plugins/org.aspectj.ajde_ <VERSION>/aspectjrt.jar
	 */
	public static String getAspectjrtClasspath() {

		if (aspectjrtPath == null) {
			StringBuffer cpath = new StringBuffer();

			// This returns the bundle with the highest version or null if none
			// found
			// - for Eclipse 3.0 compatibility
			Bundle ajdeBundle = Platform
					.getBundle(AspectJPlugin.RUNTIME_PLUGIN_ID);

			String pluginLoc = null;
			// 3.0 using bundles instead of plugin descriptors
			// if (ajdePluginDesc != null) {
			// URL installLoc = ajdePluginDesc.getInstallURL();
			if (ajdeBundle != null) {
				URL installLoc = ajdeBundle.getEntry("/"); //$NON-NLS-1$
				URL resolved = null;
				try {
					resolved = Platform.resolve(installLoc);
					pluginLoc = resolved.toExternalForm();
				} catch (IOException e) {
				}
			}
			if (pluginLoc != null) {
				if (pluginLoc.startsWith("file:")) { //$NON-NLS-1$
					cpath.append(pluginLoc.substring("file:".length())); //$NON-NLS-1$
					cpath.append("aspectjrt.jar"); //$NON-NLS-1$
				}
			}

			// Verify that the file actually exists at the plugins location
			// derived above. If not then it might be because we are inside
			// a runtime workbench. Check under the workspace directory.
			if (new File(cpath.toString()).exists()) {
				// File does exist under the plugins directory
				aspectjrtPath = cpath.toString();
			} else {
				// File does *not* exist under plugins. Try under workspace...
				IPath rootPath = AspectJPlugin.getWorkspace().getRoot()
						.getLocation();
				IPath installPath = rootPath.removeLastSegments(1);
				cpath = new StringBuffer().append(installPath.toOSString());
				cpath.append(File.separator);
				// TODO: what if the workspace isn't called workspace!!!
				cpath.append("workspace"); //$NON-NLS-1$
				cpath.append(File.separator);
				cpath.append(AspectJPlugin.RUNTIME_PLUGIN_ID);
				cpath.append(File.separator);
				cpath.append("aspectjrt.jar"); //$NON-NLS-1$

				// Only set the aspectjrtPath if the jar file exists here.
				if (new File(cpath.toString()).exists())
					aspectjrtPath = cpath.toString();
			}
		}
		return aspectjrtPath;
	}
}
