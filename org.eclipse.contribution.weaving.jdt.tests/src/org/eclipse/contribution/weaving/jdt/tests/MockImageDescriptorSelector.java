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

    public final static Image IMAGE = createImage();
    public final static ImageDescriptor DESCRIPTOR = IMAGE != null ? ImageDescriptor.createFromImage(IMAGE) : null;
    
    
    public ImageDescriptor createCompletionProposalImageDescriptor(
            LazyJavaCompletionProposal proposal) {
        
        // XXX not tested for now.
        return null;
    }


    private static Image createImage() {
        try {
            return new Image(Display.getCurrent(), "icons/aspect.gif");
        } catch (Exception e) {
            return null;
        }
    }
}
