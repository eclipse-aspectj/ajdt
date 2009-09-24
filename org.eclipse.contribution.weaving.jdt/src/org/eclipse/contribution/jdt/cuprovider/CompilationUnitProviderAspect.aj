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

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragment;

/**
 * Captures all creations of {@link CompilationUnit}.  Uses a registry to determine 
 * what kind of CompilationUnit to create.  Clients can provide their own
 * CompilationUnit by using the cuprovider extension and associating a CompilationUnit
 * subclass with a file extension.
 * 
 * @author andrew
 * @created Dec 2, 2008
 */
public aspect CompilationUnitProviderAspect {
    
    /**
     * Captures creations of Compilation units
     */
    pointcut compilationUnitCreations(PackageFragment parent, String name, WorkingCopyOwner owner) : 
            call(public CompilationUnit.new(PackageFragment, String, WorkingCopyOwner)) &&
            (
                    within(org.eclipse.jdt..*) ||
                    within(org.codehaus.jdt.groovy.integration.internal.*) ||  // Captures GroovyLanguageSupport if groovy plugin is installed
                    within(org.codehaus.jdt.groovy.integration.*) // Captures DefaultLanguageSupport if groovy plugin is installed
            ) &&
            args(parent, name, owner);
    
    CompilationUnit around(PackageFragment parent, String name, WorkingCopyOwner owner) : 
        compilationUnitCreations(parent, name, owner) {
        
        String extension = findExtension(name);
        ICompilationUnitProvider provider = 
            CompilationUnitProviderRegistry.getInstance().getProvider(extension);
        if (provider != null) {
            try {
                return provider.create(parent, name, owner);
            } catch (Throwable t) {
                JDTWeavingPlugin.logException(t);
            }
        }        
        return proceed(parent, name, owner);
    }

    private String findExtension(String name) {
        int mementoIndex = name.indexOf('}');
        int extensionIndex = name.lastIndexOf('.');
        String extension;
        if (extensionIndex >= 0) {
            if (mementoIndex >= extensionIndex) {
                extension = name.substring(extensionIndex+1, mementoIndex);
            } else {
                extension = name.substring(extensionIndex+1);
            }
        } else {
            extension = ""; //$NON-NLS-1$
        }
        return extension;
    }
    
}
