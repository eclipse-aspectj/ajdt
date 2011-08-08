package org.eclipse.contribution.jdt;

import org.eclipse.contribution.jdt.preferences.WeavingStateConfigurer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * Simple application to test if weaving is enabled in this installation
 * 
 * @author Andrew Eisenberg
 * @created Jan 25, 2009
 *
 */
public class WeavingTestApplication implements IApplication {

    private static final int WEAVING_DISABLED = -1;
    private static final int WEAVING_ENABLED = EXIT_OK;

    public Object start(IApplicationContext context) throws Exception {
        System.out.println("Testing to see if weaving service is enabled...");
        if (IsWovenTester.isWeavingActive()) {
            System.out.println("Weaving service is enabled!");
        } else {
            System.out.println("Weaving service is disabled.");
            WeavingStateConfigurer configurer = new WeavingStateConfigurer();
            IStatus status = configurer.changeWeavingState(true);
            if (status.getSeverity() == IStatus.OK) {
                System.out.println("Weaving service has been enabled.");
            } else {
                System.out.println("Could not enable weaving service.  Reason:");
                System.out.println(status.getMessage());
                status.getException().printStackTrace();
            }
        }
        System.out.println("Shutting down");
        System.out.flush();
//        PlatformUI.getWorkbench().close();
        System.exit(0);
        
        // will not be reached
        return IsWovenTester.isWeavingActive() ? WEAVING_ENABLED : WEAVING_DISABLED;
    }

    public void stop() { }

}
