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
package org.eclipse.ajdt.core;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.eclipse.ajdt.core.builder.CompilerMonitor;
import org.eclipse.ajdt.core.builder.CoreBuildOptions;
import org.eclipse.ajdt.core.builder.CoreErrorHandler;
import org.eclipse.ajdt.core.builder.CoreProjectProperties;
import org.eclipse.ajdt.core.builder.CoreTaskListManager;
import org.eclipse.ajdt.core.builder.IAJCompilerMonitor;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.internal.core.StandinCoreOperations;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class AspectJPlugin extends Plugin {
	//The shared instance.
	private static AspectJPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	// id of this plugin
	public static final String PLUGIN_ID = "org.eclipse.ajdt.core"; //$NON-NLS-1$

	// plugin containing aspectjtools.jar, or the contents thereof
    public static final String TOOLS_PLUGIN_ID = "org.aspectj.ajde"; //$NON-NLS-1$

	// the plugin containing aspectjrt.jar
	public static final String RUNTIME_PLUGIN_ID = "org.aspectj.runtime"; //$NON-NLS-1$

	public static final String ID_BUILDER = PLUGIN_ID + ".ajbuilder"; //$NON-NLS-1$

	/**
	 * The name of the default build config file for an AspectJ project
	 */
	public static final String DEFAULT_CONFIG_FILE = ".generated.lst"; //$NON-NLS-1$

	public static final String UI_PLUGIN_ID = "org.eclipse.ajdt.ui"; //$NON-NLS-1$	
	private static final String ID_NATURE = UI_PLUGIN_ID + ".ajnature"; //$NON-NLS-1$

	public static final String JAVA_NATURE_ID = "org.eclipse.jdt.core.javanature"; //$NON-NLS-1$
	
	public static final String AJ_FILE_EXT = "aj"; //$NON-NLS-1$
	
	/**
	 * Folder separator used by Eclipse in paths irrespective if on Windows or
	 * *nix.
	 */
	public static final String NON_OS_SPECIFIC_SEPARATOR = "/"; //$NON-NLS-1$

	/**
	 * Compiler monitor listens to AspectJ compilation events (build progress
	 * and compilations errors/warnings)
	 */
	private IAJCompilerMonitor ajdtCompilerMonitor;

	private ICoreOperations coreOperations;

	/**
	 * The currently selected project
	 */
	private IProject currentProject;

	/**
	 * The constructor.
	 */
	public AspectJPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.eclipse.ajdt.core.resources.AspectJPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		IncrementalStateManager.recordIncrementalStates=true;
		IncrementalStateManager.debugIncrementalStates=true;
		Ajde.init(null, new CoreTaskListManager(), // task list manager
				AspectJPlugin.getDefault().getCompilerMonitor(), // build progress monitor
				new CoreProjectProperties(), new CoreBuildOptions(),
				null, null, new CoreErrorHandler());
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		
		AJModel.getInstance().saveAllModels();
	}

	/**
	 * Returns the shared instance.
	 */
	public static AspectJPlugin getDefault() {
		return plugin;
	}

	/**
	 * get the current project, if nobody has set a project yet, use the first
	 * open project in the workspace
	 */
	public IProject getCurrentProject() {
		IProject current = null;
		if (currentProject != null) {
			current = currentProject;
		} else {
			IProject[] projects = AspectJPlugin.getWorkspace().getRoot()
					.getProjects();
			for (int i = 0; i < projects.length; i++) {
				if (projects[i].isOpen()) {
					current = projects[i];
					break;
				}
			}
		}
		return current;
	}

	/**
	 * set the current project - called by the builder when we're about to do a
	 * build.
	 */
	public void setCurrentProject(IProject project) {
		currentProject = project;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = AspectJPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * @param key
	 * @param arg
	 * @return string from plugin's resource bundle that includes the inserted
	 *         argument.
	 */
	public static String getFormattedResourceString(String key, String arg) {
		return getFormattedResourceString(key, new String[] { arg });
	}

	/**
	 * @param key
	 * @param arg
	 * @return string from plugin's resource bundle that includes all of the
	 *         inserted arguments.
	 */
	public static String getFormattedResourceString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), args);
	}
	
	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns true if the given project has the AspectJ nature. Returns
	 * false otherwise, or if the nature could not be determined (e.g. the
	 * project is closed).
	 * @param project
	 * @return
	 */
	public static boolean isAJProject(IProject project) {
		// Fix for 106707 - check that project is open
		if(project.isOpen()) {			
			try {
				if ((project!=null) && project.hasNature(ID_NATURE)) {
					return true;
				}
			} catch (CoreException e) {
			}
		}
		return false;
	}
	
	/**
	 * return the compiler monitor used for build progress monitoring and
	 * compilation errors/warnings
	 */
	public IAJCompilerMonitor getCompilerMonitor() {
		if (ajdtCompilerMonitor==null) {
			ajdtCompilerMonitor = new CompilerMonitor();
		}
		return ajdtCompilerMonitor;
	}

	public void setCompilerMonitor(IAJCompilerMonitor monitor) {
		ajdtCompilerMonitor = monitor;
	}

	public ICoreOperations getCoreOperations() {
		if (coreOperations==null) {
			coreOperations = new StandinCoreOperations();
		}
		return coreOperations;
	}
	
	public void setCoreOperations(ICoreOperations coreOps) {
		coreOperations = coreOps;
	}
	
	public void setAJLogger(IAJLogger logger) {
		AJLog.setLogger(logger);
	}
	
	/**
	 * Get the build configuration file to be used for building this project.
	 * Use ".generated.lst" in the metadata directory, one per project
	 */
	public static String getBuildConfigurationFile(IProject project) {
		return getDefault().getStateLocation()
				.append(project.getName()+DEFAULT_CONFIG_FILE).toOSString();
	}

}
