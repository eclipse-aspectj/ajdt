package org.eclipse.contribution.jdt.imagedescriptor;

import org.eclipse.jface.resource.ImageDescriptor;

public interface IImageDescriptorSelector {
    public ImageDescriptor getTypeImageDescriptor(boolean isInner, boolean isInInterfaceOrAnnotation, int flags, boolean useLightIcons, Object element);
}
