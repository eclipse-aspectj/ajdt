/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.internal.ui.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.contribution.xref.ui.XReferenceUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Central point for Images in the XReference UI plugin
 */
public class XReferenceImages {

	// URL for the directory containing the XReference icons
	private static URL iconBaseURL = null;

	public static final ImageDescriptor XREFS_FOR_ENTIRE_FILE = create("file.gif"); //$NON-NLS-1$

	/**
	 * Get the URL for an icon file
	 * @param name
	 * @return
	 * @throws MalformedURLException
	 */
	private static URL makeIconFileURL(String name) throws MalformedURLException {
		if (iconBaseURL == null) {
			String pathSuffix= "icons/"; //$NON-NLS-1$ 
			iconBaseURL= new URL(XReferenceUIPlugin.getDefault().getBundle().getEntry("/"), pathSuffix); //$NON-NLS-1$
		}			
		return new URL(iconBaseURL, name);
	}	


	/**
	 * Create an image with the given name in the icons directory.
	 * @param name
	 * @return the ImageDescriptor created
	 */
	private static ImageDescriptor create(String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	
}
