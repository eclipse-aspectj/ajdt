package org.eclipse.contribution.weaving.jdt.tests;

import org.eclipse.contribution.jdt.imagedescriptor.IImageDescriptorSelector;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.search.JavaSearchTypeNameMatch;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class MockImageDescriptorSelector implements IImageDescriptorSelector {

    public ImageDescriptor getTypeImageDescriptor(boolean isInner,
            boolean isInInterfaceOrAnnotation, int flags,
            boolean useLightIcons, Object element) {
        if (    (element instanceof MockCompilationUnit) ||
                ((element instanceof IType) && (((IType)element).getParent() instanceof MockCompilationUnit)) || 
                (element instanceof JavaSearchTypeNameMatch)) {
            isSet = true; // remember that we've been here
            return DESCRIPTOR;
        } else {
            return null;
        }
    }
    
    public static boolean isSet = false;

    public final static Image IMAGE = new Image(Display.getCurrent(), "icons/aspect.gif");
    public final static ImageDescriptor DESCRIPTOR = ImageDescriptor.createFromImage(IMAGE);
    
    
    public ImageDescriptor createCompletionProposalImageDescriptor(
            LazyJavaCompletionProposal proposal) {
        
        // XXX not tested for now.
        return null;
    }
}
