/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      SpringSource
 *      Andrew Eisenberg (initial implementation)
 *******************************************************************************/
package org.eclipse.contribution.jdt.imagedescriptor;

import org.eclipse.jface.resource.ImageDescriptor;

public interface IImageDescriptorSelector {
    public ImageDescriptor getTypeImageDescriptor(boolean isInner, boolean isInInterfaceOrAnnotation, int flags, boolean useLightIcons, Object element);
}
