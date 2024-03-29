/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.resources;

import java.util.HashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * A registry that maps <code>ImageDescriptors</code> to <code>Image</code>.
 * Copied from org.eclipse.jdt.internal.ui.viewsupport
 */
public class ImageDescriptorRegistry {

	private final HashMap fRegistry= new HashMap(10);
	private Display fDisplay;

	/**
	 * Creates a new image descriptor registry for the current or default display,
	 * respectively.
	 */
	public ImageDescriptorRegistry() {
		fDisplay = Display.getCurrent();
		if (fDisplay == null)
			fDisplay= Display.getDefault();
		Assert.isNotNull(fDisplay);
		hookDisplay();
	}

	/**
	 * Creates a new image descriptor registry for the given display. All images
	 * managed by this registry will be disposed when the display gets disposed.
	 *
	 * @param display the display the images managed by this registry are allocated for
	 */
	public ImageDescriptorRegistry(Display display) {
		fDisplay= display;
		Assert.isNotNull(fDisplay);
		hookDisplay();
	}

	/**
	 * Returns the image associated with the given image descriptor.
	 *
	 * @param descriptor the image descriptor for which the registry manages an image
	 * @return the image associated with the image descriptor or <code>null</code>
	 *  if the image descriptor can't create the requested image.
	 */
	public Image get(ImageDescriptor descriptor) {
		if (descriptor == null)
			descriptor= ImageDescriptor.getMissingImageDescriptor();

		Image result= (Image)fRegistry.get(descriptor);
		if (result != null)
			return result;

		result= descriptor.createImage();
		if (result != null)
			fRegistry.put(descriptor, result);
		return result;
	}

	/**
	 * Disposes all images managed by this registry.
	 */
	public void dispose() {
    for (Object o : fRegistry.values()) {
      Image image = (Image) o;
      image.dispose();
    }
		fRegistry.clear();
	}

	private void hookDisplay() {
		fDisplay.disposeExec(this::dispose);
	}
}
