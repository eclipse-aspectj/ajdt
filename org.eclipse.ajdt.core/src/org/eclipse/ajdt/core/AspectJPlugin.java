/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.aspectj.weaver.World;
import org.eclipse.ajdt.core.builder.CompilerMonitor;
import org.eclipse.ajdt.core.builder.CoreBuildOptions;
import org.eclipse.ajdt.core.builder.CoreErrorHandler;
import org.eclipse.ajdt.core.builder.CoreProjectProperties;
import org.eclipse.ajdt.core.builder.CoreTaskListManager;
import org.eclipse.ajdt.core.builder.IAJCompilerMonitor;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.internal.core.ResourceChangeListener;
import org.eclipse.ajdt.internal.core.model.BinaryWeavingSupport;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class AspectJPlugin extends Plugin {
	//The shared instance.
	private static AspectJPlugin plugin;

	// id of this plugin
	public static final String PLUGIN_ID = "org.eclipse.ajdt.core"; //$NON-NLS-1$

	// plugin containing aspectjtools.jar, or the contents thereof
    public static final String TOOLS_PLUGIN_ID = "org.aspectj.ajde"; //$NON-NLS-1$

	// plugin containing aspectjweaver.jar, or the contents thereof
    public static final String WEAVER_PLUGIN_ID = "org.aspectj.weaver"; //$NON-NLS-1$

	// the plugin containing aspectjrt.jar
	public static final String RUNTIME_PLUGIN_ID = "org.aspectj.runtime"; //$NON-NLS-1$

	public static final String ID_BUILDER = PLUGIN_ID + ".ajbuilder"; //$NON-NLS-1$

	/**
	 * The name of the default build config file for an AspectJ project
	 */
	public static final String DEFAULT_CONFIG_FILE = ".generated.lst"; //$NON-NLS-1$

	public static final String UI_PLUGIN_ID = "org.eclipse.ajdt.ui"; //$NON-NLS-1$	
	public static final String ID_NATURE = UI_PLUGIN_ID + ".ajnature"; //$NON-NLS-1$

	public static final String JAVA_NATURE_ID = "org.eclipse.jdt.core.javanature"; //$NON-NLS-1$
	
	public static final String AJ_FILE_EXT = "aj"; //$NON-NLS-1$
	
	public static final String ASPECTJRT_CONTAINER = PLUGIN_ID + ".ASPECTJRT_CONTAINER"; //$NON-NLS-1$
	
	// AspectJ keywords
    public static final String[] ajKeywords = { "aspect", "pointcut", "privileged", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// Pointcut designators: methods and constructora
		"call", "execution", "initialization", "preinitialization" , //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		// Pointcut designators: exception handlers
		"handler", //$NON-NLS-1$
		// Pointcut designators: fields
		"get", "set", //$NON-NLS-1$ //$NON-NLS-2$
		// Pointcut designators: static initialization
		"staticinitialization", //$NON-NLS-1$
		// Pointcut designators: object
		// (this already a Java keyword)
		"target", "args", //$NON-NLS-1$ //$NON-NLS-2$
		// Pointcut designators: lexical extents
		"within", "withincode", //$NON-NLS-1$ //$NON-NLS-2$
		// Pointcut designators: control flow
		"cflow", "cflowbelow", //$NON-NLS-1$ //$NON-NLS-2$
		// Pointcut Designators for annotations
		"annotation", //$NON-NLS-1$
		// Advice
		"before", "after", "around", "proceed", "throwing" , "returning" , //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		"adviceexecution" , //$NON-NLS-1$
		// Declarations
		"declare", "parents" , "warning" , "error", "soft" , "precedence" , //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		// variables
		"thisJoinPoint" , "thisJoinPointStaticPart" , "thisEnclosingJoinPointStaticPart" , //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// Associations
		"issingleton", "perthis", "pertarget", "percflow", "percflowbelow", "pertypewithin" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

	/**
	 * Folder separator used by Eclipse in paths irrespective if on Windows or
	 * *nix.
	 */
	public static final String NON_OS_SPECIFIC_SEPARATOR = "/"; //$NON-NLS-1$

	public static boolean usingCUprovider = false;
	
	/**
	 * Compiler monitor listens to AspectJ compilation events (build progress
	 * and compilations errors/warnings)
	 */
	private IAJCompilerMonitor ajdtCompilerMonitor;

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
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		checkForCUprovider();
		IncrementalStateManager.recordIncrementalStates=true;
		IncrementalStateManager.debugIncrementalStates=true;
		getWorkspace().addResourceChangeListener(
				new ResourceChangeListener(),
				IResourceChangeEvent.POST_CHANGE);
		World.createInjarHierarchy = BinaryWeavingSupport.isActive;
		Ajde.init(null, new CoreTaskListManager(), // task list manager
				AspectJPlugin.getDefault().getCompilerMonitor(), // build progress monitor
				new CoreProjectProperties(), new CoreBuildOptions(),
				null, null, new CoreErrorHandler());
	}

	/**
	 * Sets the usingCUprovider flag if the experimental JDT extension is available
	 *
	 */
	private void checkForCUprovider() {
		String EJDT_CU_PROVIDER_EXTENSION = "org.eclipse.jdt.core.compilationUnitProvider";
		IExtensionPoint exP = Platform.getExtensionRegistry()
			.getExtensionPoint(EJDT_CU_PROVIDER_EXTENSION);
		if (exP!=null) {
			// extension exists, check that org.eclipse.ajdt.cuprovider is there to use it
			if (Platform.getBundle("org.eclipse.ajdt.cuprovider")!=null) {
				usingCUprovider = true;
			}
		}
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
