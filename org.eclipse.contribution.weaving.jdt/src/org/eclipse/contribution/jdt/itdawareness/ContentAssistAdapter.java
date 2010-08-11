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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author Andrew Eisenberg
 * @created Dec 15, 2009
 *
 */
public class ContentAssistAdapter extends PlatformObject implements IAdaptable {
    
    private static final ContentAssistAdapter INSTANCE = new ContentAssistAdapter();
    
    public static ContentAssistAdapter getInstance() {
        return INSTANCE;
    }
    
    private ContentAssistAdapter() { }

    private IJavaContentAssistProvider provider = null;

    
    public IJavaContentAssistProvider getProvider() {
        if (provider == null) {
            provider = (IJavaContentAssistProvider) getAdapter(IJavaContentAssistProvider.class);
        }
        return provider;
    }
    
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IJavaContentAssistProvider.class) {
            return super.getAdapter(adapter);
        }
        return null;
    }
    
    /*
     * Not API!  For testing.
     */
    public void setProvider(IJavaContentAssistProvider provider) {
        this.provider = provider;
    }
}
