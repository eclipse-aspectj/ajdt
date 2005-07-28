/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.ui.viewsupport.ImageImageDescriptor;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * LabelDecorator that decorates an element's image with error and warning overlays that 
 * represent the severity of markers attached to the element's underlying resource. To see 
 * a problem decoration for a marker, the marker needs to be a subtype of <code>IMarker.PROBLEM</code>.
 * 
 * @since 2.0
 */
class ProblemsLabelDecorator implements ILabelDecorator, ILightweightLabelDecorator {
	

	private static final int ERRORTICK_WARNING= JavaElementImageDescriptor.WARNING;
	private static final int ERRORTICK_ERROR= JavaElementImageDescriptor.ERROR;	

	private ImageDescriptorRegistry registry;
	private boolean fUseNewRegistry= false;
	
	/**
	 * Creates a new <code>ProblemsLabelDecorator</code>.
	 */
	public ProblemsLabelDecorator() {
		this(null);
		fUseNewRegistry= true;
	}
	
	/*
	 * Creates decorator with a shared image registry.
	 * 
	 * @param registry The registry to use or <code>null</code> to use the Java plugin's
	 * image registry.
	 */
	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 */
	public ProblemsLabelDecorator(ImageDescriptorRegistry registry) {
		this.registry= registry;
	}
	
	private ImageDescriptorRegistry getRegistry() {
		if (registry == null) {
			registry= fUseNewRegistry ? new ImageDescriptorRegistry() : JavaPlugin.getImageDescriptorRegistry();
		}
		return registry;
	}
	

	/* (non-Javadoc)
	 * @see ILabelDecorator#decorateText(String, Object)
	 */
	public String decorateText(String text, Object element) {
		return text;
	}	

	/* (non-Javadoc)
	 * @see ILabelDecorator#decorateImage(Image, Object)
	 */
	public Image decorateImage(Image image, Object obj) {
		int adornmentFlags= computeAdornmentFlags(obj);
		if (adornmentFlags != 0) {
			ImageDescriptor baseImage= new ImageImageDescriptor(image);
			Rectangle bounds= image.getBounds();
			return getRegistry().get(new JavaElementImageDescriptor(baseImage, adornmentFlags, new Point(bounds.width, bounds.height)));
		}
		return image;
	}

	/**
	 * Note: This method is for internal use only. Clients should not call this method.
	 */
	private int computeAdornmentFlags(Object obj) {
		try {
			if (obj instanceof ICompilationUnit) {
				ICompilationUnit cu= (ICompilationUnit) obj;
				if (cu != null) {
					return getErrorTicksFromMarkers(cu.getResource(), IResource.DEPTH_ONE);
				}
			}
		} catch (CoreException e) {
		}
		return 0;
	}

	private int getErrorTicksFromMarkers(IResource res, int depth) throws CoreException {
		if (res == null || !res.isAccessible()) {
			return 0;
		}
		int info= 0;
		
		IMarker[] markers= res.findMarkers(IMarker.PROBLEM, true, depth);
		if (markers != null) {
			for (int i= 0; i < markers.length && (info != ERRORTICK_ERROR); i++) {
				IMarker curr= markers[i];
				int priority= curr.getAttribute(IMarker.SEVERITY, -1);
				if (priority == IMarker.SEVERITY_WARNING) {
					info= ERRORTICK_WARNING;
				} else if (priority == IMarker.SEVERITY_ERROR) {
					info= ERRORTICK_ERROR;
				}
			}			
		}
		return info;
	}
	
	/* (non-Javadoc)
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		if (registry != null && fUseNewRegistry) {
			registry.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) { 
		int adornmentFlags= computeAdornmentFlags(element);
		if (adornmentFlags == ERRORTICK_ERROR) {
			decoration.addOverlay(JavaPluginImages.DESC_OVR_ERROR);
		} else if (adornmentFlags == ERRORTICK_WARNING) {
			decoration.addOverlay(JavaPluginImages.DESC_OVR_WARNING);
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

}
