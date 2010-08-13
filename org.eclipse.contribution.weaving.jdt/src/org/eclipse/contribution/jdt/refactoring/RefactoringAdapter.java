/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.contribution.jdt.refactoring;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Adaptable object that provides general refactoring support
 * @author Andrew Eisenberg
 * @created Dec 15, 2009
 */
public class RefactoringAdapter extends PlatformObject implements IAdaptable {

    private static final RefactoringAdapter INSTANCE = new RefactoringAdapter();
    
    public static RefactoringAdapter getInstance() {
        return INSTANCE;
    }
    
    private RefactoringAdapter() { }

    private IRefactoringProvider provider = null;
    
    public IRefactoringProvider getProvider() {
        if (provider == null) {
            provider = (IRefactoringProvider) getAdapter(IRefactoringProvider.class);
        }
        return provider;
    }
    
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IRefactoringProvider.class) {
            return super.getAdapter(adapter);
        }
        return null;
    }

    // Not API, for testing only
    public void setProvider(IRefactoringProvider provider) {
        this.provider = provider;
    }
}
