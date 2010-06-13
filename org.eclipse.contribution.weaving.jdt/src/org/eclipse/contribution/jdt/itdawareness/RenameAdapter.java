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

package org.eclipse.contribution.jdt.itdawareness;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author Andrew Eisenberg
 * @created Dec 15, 2009
 *
 */
public class RenameAdapter extends PlatformObject implements IAdaptable {

    private IRenameRefactoringProvider provider = null;
    
    public IRenameRefactoringProvider getProvider() {
        if (provider == null) {
            provider = (IRenameRefactoringProvider) getAdapter(IRenameRefactoringProvider.class);
        }
        return provider;
    }
    
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IRenameRefactoringProvider.class) {
            return super.getAdapter(adapter);
        }
        return null;
    }

    // Not API, for testing only
    public void setProvider(IRenameRefactoringProvider provider) {
        this.provider = provider;
    }
}
