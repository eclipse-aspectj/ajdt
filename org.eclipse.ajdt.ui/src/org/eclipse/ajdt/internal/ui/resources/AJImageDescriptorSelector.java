package org.eclipse.ajdt.internal.ui.resources;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.contribution.jdt.imagedescriptor.IImageDescriptorSelector;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.search.JavaSearchTypeNameMatch;
import org.eclipse.jface.resource.ImageDescriptor;

public class AJImageDescriptorSelector implements IImageDescriptorSelector {


    public ImageDescriptor getTypeImageDescriptor(boolean isInner,
            boolean isInInterfaceOrAnnotation, int flags,
            boolean useLightIcons, Object element) {
        IType type;
        if (element instanceof JavaSearchTypeNameMatch) {
            JavaSearchTypeNameMatch search = (JavaSearchTypeNameMatch) element;
            type = search.getType();
        } else if (element instanceof IType) {
            type = (IType) element;
        } else {
            type = null;
        }
        
        // this little hack will check to see if
        // the element is a pointcut or ITD
        if (element instanceof IMember) {
            IMember member = (IMember) element;
            try {
                // pointcut members throw an exception when the getFlags method is invoked.
                member.getFlags();
            } catch (Exception e) {
                if (member instanceof IField) {
                    // it is an itd field
                    return AspectJImages.ITD_FIELD_DEF.getImageDescriptor();
                } else if (member.getElementName().indexOf('$') != -1) {
                    // it is an itd method
                    return AspectJImages.ITD_METHOD_DEF.getImageDescriptor();
                } else if (member.getElementName().equals("before")) {
                    return AspectJImages.BEFORE_ADVICE.getImageDescriptor();
                } else if (member.getElementName().equals("around")) {
                    return AspectJImages.AROUND_ADVICE.getImageDescriptor();
                } else if (member.getElementName().equals("after")) {
                    return AspectJImages.AFTER_ADVICE.getImageDescriptor();
                } else {
                    // a pointcut
                    return AspectJImages.POINTCUT_DEF.getImageDescriptor();
                }
            }
        }
        
        if (isAspect(type)) {
            // we should be returning an aspect image descriptor
            if (Flags.isPublic(flags)) {
                return AspectJImages.ASPECT_PUBLIC.getImageDescriptor();
            } else if (Flags.isProtected(flags)) {
                return AspectJImages.ASPECT_PROTECTED.getImageDescriptor();
            } else if (Flags.isPackageDefault(flags)) {
                return AspectJImages.ASPECT_PACKAGE.getImageDescriptor();
            } else if (Flags.isPrivate(flags)) {
                return AspectJImages.ASPECT_PRIVATE.getImageDescriptor();
            }
        }
        return null;
    }

    // the type passed in will not be an AspectElement, so
    // we have to get all the types from the compilation unit and compare names
    private boolean isAspect(IType maybeAspect) {
        try {
            if (maybeAspect != null && maybeAspect.getCompilationUnit() instanceof AJCompilationUnit){
                ICompilationUnit unit = maybeAspect.getCompilationUnit();
                IType[] types = unit.getAllTypes();
                for (int i = 0; i < types.length; i++) {
                    IType type = types[i];
                    if (type.getFullyQualifiedName('.').equals(
                            maybeAspect.getFullyQualifiedName('.'))) {
                         return type instanceof AspectElement;
                    }
                }
            }
        } catch (JavaModelException e) {
        }
        return false;
    }
}
