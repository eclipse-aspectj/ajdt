/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.contribution.xref.core.IXReferenceNode;
import org.eclipse.contribution.xref.internal.core.XReferenceAdapterFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class XReferenceUIPlugin extends AbstractUIPlugin {

    //  the plugin id
	public static final String PLUGIN_ID = "org.eclipse.contribution.xref.ui";
	//The shared instance.
	private static XReferenceUIPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	private static Image relImage = null;
	private static Image evaluateImage = null;

	/**
	 * 3.0 compatible constructor
	 */
	public XReferenceUIPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle =
				ResourceBundle.getBundle(
					"org.eclipse.contribution.xref.internal.ui.XReferenceUIPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}
	
	/**
	 * This method is called upon plug-in activation - process any 
	 * defined extensions and add the resource change listener.
	 * 
	 * 3.0 compatible
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		registerAdapter();
	}

	/**
	 * This method is called when the plug-in is stopped
	 * 
	 * 3.0 compatible
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static XReferenceUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle =
			XReferenceUIPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null ? bundle.getString(key) : key);
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

	/*
	 * Helper method to register the required resources with a
	 * XReferenceAdapterFactory.
	 *  
	 */
	private void registerAdapter() {
		XReferenceAdapterFactory xra = new XReferenceAdapterFactory();
		Platform.getAdapterManager().registerAdapters(xra, IJavaElement.class);
		Platform.getAdapterManager().registerAdapters(xra, IResource.class);
		Platform.getAdapterManager().registerAdapters(xra, IXReferenceNode.class);
		// below registers the Editor part of the workbench with the XReferenceAdapterFactory
		// this means that it does update when the editor is selected
		Platform.getAdapterManager().registerAdapters(xra, IEditorPart.class);//<--------
	}

	public Image getEvaluateImage() {
		if (evaluateImage == null) {
			try {
				URL pluginInstallURL = Platform.getBundle(PLUGIN_ID).getEntry("/");
				ImageDescriptor d =
					ImageDescriptor.createFromURL(
						new URL(pluginInstallURL, "icons/update.gif"));
				evaluateImage = d.createImage();
			} catch (MalformedURLException mex) {
				System.err.println("Couldn't create evaluateImage");
			}
		}
		return evaluateImage;
	}

	public Image getXReferenceImage() {
		if (relImage == null) {
			try {
				URL pluginInstallURL = Platform.getBundle(PLUGIN_ID).getEntry("/");
				ImageDescriptor d =
					ImageDescriptor.createFromURL(
						new URL(pluginInstallURL, "icons/arrow.gif"));
				relImage = d.createImage();
			} catch (MalformedURLException mex) {
				System.err.println("Couldn't create relImage");
			}
		}
		return relImage;
	}
}
