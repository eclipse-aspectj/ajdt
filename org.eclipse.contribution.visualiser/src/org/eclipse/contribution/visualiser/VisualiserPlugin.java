/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial version
 *******************************************************************************/
package org.eclipse.contribution.visualiser;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.contribution.visualiser.core.ProviderManager;
import org.eclipse.contribution.visualiser.internal.preference.VisualiserPreferences;
import org.eclipse.contribution.visualiser.views.Menu;
import org.eclipse.contribution.visualiser.views.Visualiser;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class for the Visualiser plugin.
 */
public class VisualiserPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.contribution.visualiser"; //$NON-NLS-1$

	public static int LOGLEVEL = 2;

	public static Visualiser visualiser;
	
	public static Menu menu;
	
	private static VisualiserPlugin plugin;
	
	private ResourceBundle resourceBundle;
	
	
	/**
	 * Plugin ctor - load the resource bundle, process any defined extensions and add
	 * the resource change listener.
	 */
//	public VisualiserPlugin(IPluginDescriptor descriptor) {
//		super(descriptor);
//		plugin = this;
//		try {
//			resourceBundle= ResourceBundle.getBundle("org.eclipse.contribution.visualiser.VisualiserPluginResources");
//		} catch (MissingResourceException x) {
//			resourceBundle = null;
//		}				
//		ProviderManager.initialise();
//		VisualiserPreferencePage.initDefaults();
//	}


	/**
	 * 3.0 compatible Plugin constructor - load the resource bundle 
	 */
	public VisualiserPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle= ResourceBundle.getBundle("org.eclipse.contribution.visualiser.VisualiserPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}				
		// moved these two lines to the start method as otherwise get NPE
//		ProviderManager.initialise();
//		VisualiserPreferencePage.initDefaults();
	}

	/**
	 * Getter method for the provider manager 
	 * @return the provider manager
	 */
	public static ProviderManager getProviderManager() {
		return ProviderManager.getProviderManager();
	}


	/**
	 * Refresh the Visualiser views.  Checks if the visualiser viewpart is on screen before doing the refresh.
	 */
	public static void refresh() {
		if (visualiser != null) {
			Display.getDefault().asyncExec( new Runnable() {
				public void run() {
					if (visualiser!=null) {
						IWorkbenchWindow iww = VisualiserPlugin.getActiveWorkbenchWindow();
						if (iww!=null) {
							IWorkbenchPage iwp = iww.getActivePage();
							if (iwp!=null) {
								IViewPart ivp = iwp.findView("org.eclipse.contribution.visualiser.views.Visualiser");
								if (ivp!=null) { // viewpart is showing
									visualiser.updateDisplay(true);
								}
							}
						}
					}
				}
			});
		}
	}


	/**
	 * Gets the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}	


	/**
	 * Returns the shared instance as VisualiserPlugin is a singleton.
	 */
	public static VisualiserPlugin getDefault() {
		return plugin;
	}


	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}


	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = VisualiserPlugin.getDefault().getResourceBundle();
		if (bundle == null) return key;
		try {
			return bundle.getString(key);
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
	 * Simple trace routine - we can turn trace on and off by commenting out the body.
	 */
	public static void trace(String string) {
		System.err.println(string);
	}


	/**
	 * Set the Visualiser view
	 */
	public void setVisualiser(Visualiser visualiser) {
		VisualiserPlugin.visualiser = visualiser;
		// When the visualiser is known, tell it where to get its data from
		VisualiserPlugin.visualiser.setVisContentProvider(ProviderManager.getContentProvider());
		VisualiserPlugin.visualiser.setVisMarkupProvider(ProviderManager.getMarkupProvider());
	}


	/**
	 * Remove the Visualiser view
	 */
	public void removeVisualiser() {
		VisualiserPlugin.visualiser = null;
	}


	/**
	 * Set the Visualiser Menu view
	 */
	public void setMenu(Menu menu) {
		VisualiserPlugin.menu = menu;
		// When the menu is known, tell it where to get its data from
		VisualiserPlugin.menu.setVisMarkupProvider(ProviderManager.getMarkupProvider());
	}


	/**
	 * Remove the Visualiser Menu view
	 */
	public void removeMenu() {
		VisualiserPlugin.menu = null;
	}


	/**
	 * Log the given message at the given log level. 
	 * @param logLevel
	 * @param message
	 */
	public static void log(int logLevel, String message) {
//		if (logLevel<=LOGLEVEL) System.err.println(message);		
//		VisualiserPlugin.getDefault().getLog().log(new Status(Status.INFO,"org.eclipse.contribution.visualiser",0,string,(Exception)null)); 
	}

	/**
	 * Write the given exception or error to the log file (without displaying a dialog)
	 * @param e
	 */
	public static void logException(Throwable e) {
		IStatus status = null;
		if (e instanceof CoreException) {
			status = ((CoreException) e).getStatus();
		} else {
			String message = e.getMessage();
			if (message == null) {
				message = e.toString();
			}
			status = new Status(IStatus.ERROR, VisualiserPlugin.PLUGIN_ID, IStatus.OK, message, e);
		}
		getDefault().getLog().log(status);
	}

	/**
	 * This method is called upon plug-in activation - process any 
	 * defined extensions and add the resource change listener.
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		ProviderManager.initialise();
		VisualiserPreferences.initDefaults();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}
}
