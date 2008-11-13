package org.eclipse.contribution.jdt.imagedescriptor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.contribution.jdt.cuprovider.ICompilationUnitProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

public class ImageDescriptorSelectorRegistry {
    private static final ImageDescriptorSelectorRegistry INSTANCE = 
        new ImageDescriptorSelectorRegistry();
    private static final String SELECTORS_EXTENSION_POINT = "org.eclipse.contribution.weaving.jdt.imagedescriptorselector"; //$NON-NLS-1$
    
    public static ImageDescriptorSelectorRegistry getInstance() {
        return INSTANCE;
    }

    private ImageDescriptorSelectorRegistry() { 
        // do nothing
    }
    
    private Set/* IImageDescriptorSelector */ registry;
    
    void registerSelector(IImageDescriptorSelector provider) {
        registry.add(provider);
    }
    
    Iterator getAllSelectors() {
        if (!isRegistered()) {
            registerSelectors();
        }
        return registry.iterator();
    }
    
    public boolean isRegistered() {
        return registry != null;
    }
    
    public void registerSelectors() {
        registry = new HashSet();
        IExtensionPoint exP =
            Platform.getExtensionRegistry().getExtensionPoint(SELECTORS_EXTENSION_POINT);
        if (exP != null) {
            IExtension[] exs = exP.getExtensions();
            if (exs != null) {
                for (int i = 0; i < exs.length; i++) {
                    IConfigurationElement[] configs = exs[i].getConfigurationElements();
                    for (int j = 0; j < configs.length; j++) {
                        try {
                            IConfigurationElement config = configs[j];
                            IImageDescriptorSelector provider = (IImageDescriptorSelector) 
                                    config.createExecutableExtension("class"); //$NON-NLS-1$
                            registry.add(provider);
                        } catch (CoreException e) {
                            JDTWeavingPlugin.logException(e);
                        } 
                    }
                }
            }
        }
    }
}
