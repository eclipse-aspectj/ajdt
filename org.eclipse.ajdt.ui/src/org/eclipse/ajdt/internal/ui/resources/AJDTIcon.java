/**********************************************************************
Copyright (c) 2002, 2005 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html
Contributors:
Adrian Colyer - initial version
Helen Hawkins - updated for new ajde interface (bug 148190) (removed
               redundant extension of Ajde's AbstractIcon)
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui.resources;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;

public class AJDTIcon  {

	public static final AJDTIcon MISSING_ICON = 
		new AJDTIcon( ImageDescriptor.getMissingImageDescriptor() );

	private ImageDescriptor descriptor;
	private static URL pluginInstallURL;

	/**
	 * Build icon from a URL pointing at the target image
	 * @param iconResource
	 */
	public AJDTIcon( URL iconResourcePath ) {
		this.descriptor = ImageDescriptor.createFromURL( iconResourcePath );		
	}

	/**
	 * Build icon from an eclipse image descriptor
	 */
	public AJDTIcon(ImageDescriptor imgDescriptor) {
		this.descriptor = imgDescriptor;
	}
	
	/**
	 * Build icon from a resource in this plugin's install directory
	 */
	public AJDTIcon(String localPath) {
		if ( pluginInstallURL == null ) {
			pluginInstallURL = AspectJUIPlugin.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
		} 
		try {
			URL url = new URL( pluginInstallURL, localPath );
			this.descriptor = ImageDescriptor.createFromURL( url );
		} catch ( MalformedURLException malFormedURL ) {
			this.descriptor = ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	public ImageDescriptor getImageDescriptor( ) {
		return descriptor;
	}

}
