package org.eclipse.ajdt.internal.ui.markers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.core.ajde.CoreCompilerConfiguration;
import org.eclipse.ajdt.internal.core.ajde.FileURICache;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

public class DeleteAndUpdateAJMarkersJob extends Job {
    public final static Object UPDATE_DELETE_AJ_MARKERS_FAMILY = new Object();

    private DeleteAJMarkers delete;
    private UpdateAJMarkers update;
    private boolean deleteOnly = false;
    private ISchedulingRule rule;
    
    public DeleteAndUpdateAJMarkersJob(IProject project) {
        super("Delete and update AspectJ markers for " + project.getName());
        update = new UpdateAJMarkers(project);
        delete = new DeleteAJMarkers(project);
        rule = createSchedulingRule(project, null);
    }
    
    public DeleteAndUpdateAJMarkersJob(IProject project, File[] sourceFiles) {
        super("Delete and update AspectJ markers for " + project.getName());
        IFile[] iFiles = javaFileToIFile(sourceFiles, project);
        update = new UpdateAJMarkers(project, iFiles);
        delete = new DeleteAJMarkers(project, iFiles);
        rule = createSchedulingRule(project, iFiles);
    }

    public IStatus run(IProgressMonitor monitor) {
        try {
            try {
                manager.beginRule(rule, monitor);
                
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
                manager.endRule(rule);
//                manager.endRule(project);
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

    /**
     * Creates the minimum scheduling rule required for this job.
     * This is a unique scheduling rule for each Job.
     */
    private ISchedulingRule createSchedulingRule(IProject thisProject,
            IFile[] sourceFiles) {
        return new AJMarkerSchedulingRule();
    }
    
    
    private static class AJMarkerSchedulingRule implements ISchedulingRule {

        public boolean contains(ISchedulingRule rule) {
            return this == rule;
        }

        public boolean isConflicting(ISchedulingRule rule) {
            return rule instanceof AJMarkerSchedulingRule;
        }
        
    }
    
    /**
     * converts from an array of java.io.File to an array of IFile
     */
    static IFile[] javaFileToIFile(File[] files, IProject project) {
        FileURICache fileCache = ((CoreCompilerConfiguration) AspectJPlugin.getDefault().getCompilerFactory().getCompilerForProject(project).getCompilerConfiguration()).getFileCache();
        List<IFile> iFiles = new ArrayList<IFile>(files.length);
        for (int i = 0; i < files.length; i++) {
            IFile[] newFiles = fileCache.findFilesForURI(files[i].toURI());
            // inner loop---if a single file is mapped to several linked files in the workspace
            for (int j = 0; j < newFiles.length; j++) {
                iFiles.add(newFiles[j]);
            }
        }
        return (IFile[]) iFiles.toArray(new IFile[iFiles.size()]);
    }
}
