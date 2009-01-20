package org.eclipse.contribution.weaving.jdt.tests;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class JDTWeavingTestsPlugin extends Plugin {
    
    private static JDTWeavingTestsPlugin DEFAULT;
    
    public JDTWeavingTestsPlugin() {
        JDTWeavingTestsPlugin.DEFAULT = this;
    }
    
    public static JDTWeavingTestsPlugin getDefault() {
        return DEFAULT;
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

}