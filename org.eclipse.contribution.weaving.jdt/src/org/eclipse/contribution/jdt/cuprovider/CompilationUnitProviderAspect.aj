package org.eclipse.contribution.jdt.cuprovider;

import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragment;

public aspect CompilationUnitProviderAspect {
    CompilationUnit around(PackageFragment parent, String name, WorkingCopyOwner owner) : 
            call(public CompilationUnit.new(PackageFragment, String, WorkingCopyOwner)) && 
            args(parent, name, owner) {
        
        int mementoIndex = name.indexOf('}');
        int extensionIndex = name.lastIndexOf('.');
        String extension;
        if (extensionIndex >= 0) {
            if (mementoIndex >= 0) {
                extension = name.substring(extensionIndex+1, mementoIndex);
            } else {
                extension = name.substring(extensionIndex+1);
            }
        } else {
            extension = ""; //$NON-NLS-1$
        }
        
        if (extension.equals("java")) { //$NON-NLS-1$
            return proceed(parent, name, owner);
        } else {
            return CompilationUnitProviderRegistry.getInstance().getProvider(extension).
                   create(parent, name, owner);
        }        
    }
    
}
