/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.contribution.jdt.itdawareness;

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.contribution.jdt.preferences.WeavableProjectListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor;

/**
 * Finds getters and setters on a field that are defined as ITDs
 * @author Andrew Eisenberg
 * @created Apr 26, 2010
 *
 */
public aspect ExtraGettersSettersAspect {
    String around(RenameFieldProcessor processor) : execution(public String RenameFieldProcessor.canEnableGetterRenaming() throws CoreException) &&
            this(processor) {
        String result = proceed(processor);
        return findAccessor(processor, result, true);
    }
    String around(RenameFieldProcessor processor) : execution(public String RenameFieldProcessor.canEnableSetterRenaming() throws CoreException) &&
            this(processor) {
        String result = proceed(processor);
        return findAccessor(processor, result, false);
    }

    private String findAccessor(RenameFieldProcessor processor, String result, boolean getter) {
        try {
            IField element = processor.getField();
            if (result != null && result.equals("") && isInterestingProject(element)) {
                 // This will be null if AJDT is not installed (ie- JDT Weaving installed, but no AJDT)
                ISearchProvider searchProvider = SearchAdapter.getInstance().getProvider();
                if (searchProvider != null) {
                    IJavaElement maybe = getter ?
                            searchProvider.findITDGetter(element) : 
                                searchProvider.findITDSetter(element);
                    if (maybe != null) {
                        result = null;
                    }
                }
            }
        } catch (Exception e) {
            JDTWeavingPlugin.logException(e);
        }
        return result;
    }
    
    private boolean isInterestingProject(IJavaElement elt) {
        IProject proj = elt.getJavaProject().getProject();
        return proj != null &&
                WeavableProjectListener.getInstance().isWeavableProject(proj);
    }
}
