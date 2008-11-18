package org.eclipse.contribution.jdt;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class JDTWeavingPlugin extends Plugin {
    
    private static JDTWeavingPlugin INSTANCE;
    
    public static String ID = "org.eclipse.contribution.jdt"; //$NON-NLS-1$
    
    public JDTWeavingPlugin() {
        super();
        INSTANCE = this;
    }

    
    public static void logException(Exception e) {
        INSTANCE.getLog().log(new Status(IStatus.ERROR, ID, e.getMessage(), e));
    }
    
    
    public static JDTWeavingPlugin getInstance() {
        return INSTANCE;
    }
    
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }
}