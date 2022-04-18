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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

public class CompilationUnitProviderRegistry {
    public static String CUPROVIDERS_EXTENSION_POINT = "org.eclipse.contribution.weaving.jdt.cuprovider"; //$NON-NLS-1$

    private static final CompilationUnitProviderRegistry INSTANCE =
        new CompilationUnitProviderRegistry();

    public static CompilationUnitProviderRegistry getInstance() {
        return INSTANCE;
    }

    private CompilationUnitProviderRegistry() {
        // do nothing
    }

    private Map<String, ICompilationUnitProvider> registry;

    void registerCompilationUnitProvider(String key, ICompilationUnitProvider provider) {
        registry.put(key, provider);
    }

    ICompilationUnitProvider getProvider(String key) {
        if (!isRegistered()) {
            registerProviders();
        }
        return registry.get(key);
    }


    public boolean isRegistered() {
        return registry != null;
    }

    public void registerProviders() {
        registry = new HashMap<>();
        IExtensionPoint exP =
            Platform.getExtensionRegistry().getExtensionPoint(CUPROVIDERS_EXTENSION_POINT);
        if (exP != null) {
            IExtension[] exs = exP.getExtensions();
          for (IExtension ex : exs) {
            IConfigurationElement[] configs = ex.getConfigurationElements();
            for (IConfigurationElement iConfigurationElement : configs) {
              try {
                IConfigurationElement config = iConfigurationElement;
                if (config.isValid()) {
                  ICompilationUnitProvider provider = (ICompilationUnitProvider)
                    config.createExecutableExtension("class"); //$NON-NLS-1$
                  registerCompilationUnitProvider(config.getAttribute("file_extension"), provider); //$NON-NLS-1$
                }
              }
              catch (CoreException e) {
                JDTWeavingPlugin.logException(e);
              }
            }
          }
        }
    }
}
