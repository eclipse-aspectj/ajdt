package org.eclipse.contribution.jdt.imagedescriptor;

import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.swt.graphics.Image;

public privileged aspect ImageDescriptorSelectorAspect {
    
    // getting the image for open type dialogs
    // note that if multiple image descriptor registries can work on the same kind of element,
    // then there is potential for conflicting.
    // all we do here is return the first descriptor that we find.  Conflicts be damned!
    // If there ever is a conflict (unlikely), we will deal with the consequences as it comes.
    ImageDescriptor around(boolean isInner, boolean isInInterfaceOrAnnotation, int flags, boolean useLightIcons, Object element) : 
        execution(public static ImageDescriptor JavaElementImageProvider.getTypeImageDescriptor(boolean, boolean, int, boolean)) &&
        args(isInner, isInInterfaceOrAnnotation, flags, useLightIcons) && cflow(typeSelectionDialogGettingLabel(element)) {
        
        for (Iterator iter = ImageDescriptorSelectorRegistry.getInstance().getAllSelectors(); iter.hasNext(); ) {
            IImageDescriptorSelector selector = (IImageDescriptorSelector) iter.next();
            ImageDescriptor descriptor = selector.getTypeImageDescriptor(isInner, isInInterfaceOrAnnotation, flags, useLightIcons, element);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return proceed(isInner, isInInterfaceOrAnnotation, flags, useLightIcons, element);
    }
    
    // I think that I don't need wildcards here
    // This pointcut captures the act of getting a label for elements in the type selection dialog
//    pointcut typeSelectionDialogGettingLabel(Object element) : execution(public Image *..TypeItemLabelProvider.getImage(Object)) && args(element);
//    pointcut typeSelectionDialogGettingLabel(Object element) : execution(public Image org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialogTypeItemLabelProvider.getImage(Object)) && args(element);
//    pointcut typeSelectionDialogGettingLabel(Object element) : execution(public Image org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialogTypeItemLabelProvider*.getImage(Object)) && args(element);
    pointcut typeSelectionDialogGettingLabel(Object element) : 
        (execution(public Image *..FilteredTypesSelectionDialog*.TypeItemLabelProvider.getImage(Object)) ||  // for open type dialog
         execution(public Image *..HierarchyLabelProvider.getImage(Object)) || // for type hierarchy view
         execution(public Image *..DebugTypeSelectionDialog*.DebugTypeLabelProvider.getImage(Object)))   // for choosing a main class
                 && args(element);


    // getting for other standard places where java elements go
    ImageDescriptor around(Object element, int flags) : execution(ImageDescriptor JavaElementImageProvider.computeDescriptor(Object, int)) &&
            args(element, flags) {
        for (Iterator iter = ImageDescriptorSelectorRegistry.getInstance().getAllSelectors(); iter.hasNext(); ) {
            IImageDescriptorSelector selector = (IImageDescriptorSelector) iter.next();
            ImageDescriptor descriptor = selector.getTypeImageDescriptor(false, false, flags, false, element);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return proceed(element, flags);
    }
}