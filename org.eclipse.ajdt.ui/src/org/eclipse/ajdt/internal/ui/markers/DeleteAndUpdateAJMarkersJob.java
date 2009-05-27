package org.eclipse.ajdt.internal.ui.markers;

import java.io.File;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class DeleteAndUpdateAJMarkersJob extends Job {
    public final static Object UPDATE_DELETE_AJ_MARKERS_FAMILY = new Object();

    private DeleteAJMarkers delete;
    private UpdateAJMarkers update;
    private IProject project;
    private boolean deleteOnly = false;
    
    public DeleteAndUpdateAJMarkersJob(IProject project) {
        super("Delete and update AspectJ markers for " + project.getName());
        this.project = project;
        update = new UpdateAJMarkers(project);
        delete = new DeleteAJMarkers(project);
    }
    
    public DeleteAndUpdateAJMarkersJob(IProject project, File[] sourceFiles) {
        super("Delete and update AspectJ markers for " + project.getName());
        update = new UpdateAJMarkers(project, sourceFiles);
        delete = new DeleteAJMarkers(project, sourceFiles);
    }

    protected IStatus run(IProgressMonitor monitor) {
        try {
            

            try {
                // get a lock on the workspace so that no other marker operations can be performed at the same time
                manager.beginRule(project, monitor);
                
                IStatus deleteStatus = delete.run(monitor);
                
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
                
                IStatus updateStatus;
                if (!deleteOnly) {
                    updateStatus = update.run(monitor);
                } else {
                    updateStatus = Status.OK_STATUS;
                }
                
                
                return new MultiStatus(
                        AspectJUIPlugin.PLUGIN_ID, 
                        Math.max(updateStatus.getCode(), deleteStatus.getCode()), 
                        new IStatus[] { deleteStatus, updateStatus }, 
                        "Finished deleting and updating markers", null);
            } finally {
                manager.endRule(project);
            }
        } catch (OperationCanceledException e) {
            // we've been canceled.  Just exit.  No need to clean markers that have already been placed
            return Status.CANCEL_STATUS;
        } 
    }
    
    /**
     * set to trueif should delete markers, but not update them
     * @param deleteOnly
     */
    public void doDeleteOnly(boolean deleteOnly) {
        this.deleteOnly = deleteOnly;
    }
    
    
    public boolean belongsTo(Object family) {
        return family == UPDATE_DELETE_AJ_MARKERS_FAMILY;
    }
}
