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
package org.eclipse.contribution.jdt.cuprovider;

import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragment;

public aspect CompilationUnitProviderAspect {
    
    /**
     * Captures creations of Compilation units
     */
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
        CompilationUnit unit = CompilationUnitProviderRegistry.getInstance().getProvider(extension).
                create(parent, name, owner);
        if (unit == null) {
            return proceed(parent, name, owner);
        } else {
            return unit; 
        }        
    }
    
}
