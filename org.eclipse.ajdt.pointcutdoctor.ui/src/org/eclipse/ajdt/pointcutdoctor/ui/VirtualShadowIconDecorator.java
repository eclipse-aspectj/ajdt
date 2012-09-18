/*******************************************************************************
 * Copyright (c) 2007 Linton Ye.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ajdt.pointcutdoctor.core.almost.VirtualJavaElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;


public class VirtualShadowIconDecorator implements ILabelDecorator {
	Image relImage; 
	
	Image getVirtualShadowImage() {
		if (relImage == null) {
			try {
				URL pluginInstallURL = Platform.getBundle(PointcutDoctorUIPlugin.PLUGIN_ID).getEntry("/"); //$NON-NLS-1$
				ImageDescriptor d =
					ImageDescriptor.createFromURL(
						new URL(pluginInstallURL, "icons/virtualshadow.gif")); //$NON-NLS-1$
				relImage = d.createImage();
			} catch (MalformedURLException mex) {
				System.err.println("Couldn't create relImage"); //$NON-NLS-1$
			}
		}
		return relImage;
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		if (relImage!=null)
			relImage.dispose();
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public Image decorateImage(Image image, Object element) {
//		if (element instanceof ShadowNode) {
//			if (((ShadowNode)element).getShadowWrapper().getShadow() instanceof VirtualShadow)
		if (element instanceof VirtualJavaElement) {
				return getVirtualShadowImage();
		}
		return image;
	}

	public String decorateText(String text, Object element) {
		return text;
	}
}
