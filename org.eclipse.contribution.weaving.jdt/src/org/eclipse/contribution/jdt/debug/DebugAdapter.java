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

package org.eclipse.contribution.jdt.debug;

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Adaptable object that provides general refactoring support
 * @author Andrew Eisenberg
 * @created Dec 15, 2009
 */
public class DebugAdapter extends PlatformObject implements IAdaptable {

    private static final DebugAdapter INSTANCE = new DebugAdapter();
    
    public static DebugAdapter getInstance() {
        return INSTANCE;
    }
    
    private DebugAdapter() { }

    private IDebugProvider provider = null;
    
    public IDebugProvider getProvider() {
        if (provider == null) {
            provider = (IDebugProvider) getAdapter(IDebugProvider.class);
        }
        return provider;
    }
    
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IDebugProvider.class) {
            // ensure the adapter factory is loaded
            return Platform.getAdapterManager().loadAdapter(this, adapter.getCanonicalName());
        }
        return null;
    }

    // Not API, for testing only
    public void setProvider(IDebugProvider provider) {
        this.provider = provider;
    }
}
