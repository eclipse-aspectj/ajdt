package org.eclipse.ajdt.core;

import java.io.File;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ajdt.core.builder.IAJCompilerMonitor;
import org.eclipse.ajdt.core.builder.CompilerMonitor;
import org.eclipse.ajdt.internal.core.AJLog;
import org.eclipse.ajdt.internal.core.IAJLogger;
import org.eclipse.ajdt.internal.core.ICoreOperations;
import org.eclipse.ajdt.internal.core.CoreUtils;
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

	private static final String UI_PLUGIN_ID = "org.eclipse.ajdt.ui"; //$NON-NLS-1$	
	private static final String ID_NATURE = UI_PLUGIN_ID + ".ajnature"; //$NON-NLS-1$

	/**
	 * Compiler monitor listens to AspectJ compilation events (build progress
	 * and compilations errors/warnings)
	 */
	private IAJCompilerMonitor ajdtCompilerMonitor;

	private ICoreOperations coreOperations;
	
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
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static AspectJPlugin getDefault() {
		return plugin;
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
		try {
			if ((project!=null) && project.hasNature(ID_NATURE)) {
				return true;
			}
		} catch (CoreException e) {
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
	 * Use ".generated.lst" in the project directory
	 */
	public static String getBuildConfigurationFile(IProject project) {
		return CoreUtils.getProjectRootDirectory( project ) + 
						 File.separator +
						 DEFAULT_CONFIG_FILE;
	}

}
