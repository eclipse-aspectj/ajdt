package org.eclipse.contribution.jdt.preferences;

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Andrew Eisenberg
 * @created Jan 20, 2009
 *
 */
public final class EnableWeavingServiceJob extends UIJob {
    public EnableWeavingServiceJob() {
        super("Enable weaving service?");
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask("Enable weaving service", 2);
        WeavingStateConfigurerUI configurer = new WeavingStateConfigurerUI();
        try {
            monitor.worked(1);
            boolean ask = configurer.ask();
            monitor.worked(1);
            if (ask) {
                return Status.OK_STATUS;
            } else {
                return Status.CANCEL_STATUS;
            }
        } catch (Exception e) {
            return new Status(Status.ERROR, JDTWeavingPlugin.ID, "Error asking to activate weaving service");
        } finally {
            monitor.done();
        }
    }

    @Override
    public boolean belongsTo(Object family) {
        return family == WeavableProjectListener.INSTANCE;
    }
}