package org.eclipse.contribution.jdt.sourceprovider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.contribution.jdt.cuprovider.ICompilationUnitProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

public class SourceTransformerRegistry {
    public static String SOURCETRANSFORMERS_EXTENSION_POINT = "org.eclipse.contribution.weaving.jdt.sourcetransformer"; //$NON-NLS-1$

    private static final SourceTransformerRegistry INSTANCE = 
        new SourceTransformerRegistry();
    
    public static SourceTransformerRegistry getInstance() {
        return INSTANCE;
    }

    private SourceTransformerRegistry() { 
        // do nothing
    }
    
    private Map/* String, ISourceTransformer */ registry;
    
    void registerSelector(String key, ISourceTransformer transformer) {
        registry.put(key, transformer);
    }
    
    ISourceTransformer getSelector(String key) {
        if (! isRegistered()) {
            registerTransformers();
        }
        return (ISourceTransformer) registry.get(key);
    }
    
    public boolean isRegistered() {
        return registry != null;
    }
    
    public void registerTransformers() {
        registry = new HashMap();
        
        IExtensionPoint exP =
            Platform.getExtensionRegistry().getExtensionPoint(SOURCETRANSFORMERS_EXTENSION_POINT);
        if (exP != null) {
            IExtension[] exs = exP.getExtensions();
            for (int i = 0; i < exs.length; i++) {
                IConfigurationElement[] configs = exs[i].getConfigurationElements();
                for (int j = 0; j < configs.length; j++) {
                    try {
                        IConfigurationElement config = configs[j];
                        ISourceTransformer provider = (ISourceTransformer) 
                                config.createExecutableExtension("class"); //$NON-NLS-1$
                        registry.put(config.getAttribute("file_extension"), provider); //$NON-NLS-1$
                    } catch (CoreException e) {
                        JDTWeavingPlugin.logException(e);
                    } 
                }
            }
        }
    }
}
