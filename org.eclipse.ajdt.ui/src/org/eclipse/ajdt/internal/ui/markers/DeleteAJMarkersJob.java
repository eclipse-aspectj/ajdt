package org.eclipse.ajdt.internal.ui.markers;

import java.io.File;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

public class DeleteAJMarkersJob extends Job {
    
    public static Object DELETE_AJ_MARKERS_FAMILY = new Object();
    

    private IProject project;
    private final File[] sourceFiles;

    public DeleteAJMarkersJob(IProject project) {
        super("Deleting markers for project " + project.getName());
        this.project = project;
        this.sourceFiles = null;
    }
    
    public DeleteAJMarkersJob(IProject project, File[] sourceFiles) {
        super("Deleting markers for project " + project.getName());
        this.project = project;
        this.sourceFiles = sourceFiles;
    }
    
    protected IStatus run(IProgressMonitor monitor) {
        try {
            AJLog.logStart("Delete markers: " + project.getName());
            if (sourceFiles != null) {
                deleteMarkersForFiles(monitor);
            } else {
                deleteAllMarkers(monitor);
            }
            AJLog.logEnd(AJLog.BUILDER, "Delete markers: " + project.getName(), "Finished deleting markers for " + project.getName());
            return Status.OK_STATUS;
        } catch(CoreException e) {
            return new Status(IStatus.ERROR, AspectJUIPlugin.PLUGIN_ID, 
                    "Error while deleting markers from project " + project.getName(), e);
        }
    }
    
    public boolean belongsTo(Object family) {
        return family == DELETE_AJ_MARKERS_FAMILY;
    }
    
    /**
     * Delete the advice markers for a project
     * @throws CoreException 
     */
    public void deleteAllMarkers(IProgressMonitor monitor) throws CoreException {
        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 4);

        // Delete all the existing markers
        subMonitor.subTask("Delete advises markers");
        project.deleteMarkers(IAJModelMarker.ADVICE_MARKER,
                true, IResource.DEPTH_INFINITE);
        subMonitor.worked(1);
        
        subMonitor.subTask("Delete advice markers");
        project.deleteMarkers(
                IAJModelMarker.SOURCE_ADVICE_MARKER, true,
                IResource.DEPTH_INFINITE);
        subMonitor.worked(1);

        subMonitor.subTask("Delete declare markers");
        project.deleteMarkers(
                IAJModelMarker.DECLARATION_MARKER, true,
                IResource.DEPTH_INFINITE);
        subMonitor.worked(1);
        
        subMonitor.subTask("Delete custom markers");
        project.deleteMarkers(IAJModelMarker.CUSTOM_MARKER,
                true, IResource.DEPTH_INFINITE);
        subMonitor.worked(1);

        subMonitor.done();
    }

    
    private void deleteMarkersForFiles(IProgressMonitor monitor) throws CoreException {
        IWorkspace workspace= ResourcesPlugin.getWorkspace();
        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, sourceFiles.length * 4);
        for (int i = 0; i < sourceFiles.length; i++) {
            IPath location = Path.fromOSString(sourceFiles[i].getAbsolutePath());
            IFile[] files = workspace.getRoot().findFilesForLocation(location);
            // inner loop---if a single file is mapped to several linked files in the workspace
            for (int j = 0; j < files.length; j++) {
                IFile file = files[j];
                if (file.exists() && CoreUtils.ASPECTJ_SOURCE_FILTER.accept(file.getFileExtension())) {
                    subMonitor.subTask("Delete markers for " + file.getName());
                    file.deleteMarkers(IAJModelMarker.ADVICE_MARKER,
                            true, IResource.DEPTH_INFINITE);
                    subMonitor.worked(1);
                    
                    file.deleteMarkers(
                            IAJModelMarker.SOURCE_ADVICE_MARKER, true,
                            IResource.DEPTH_INFINITE);
                    subMonitor.worked(1);

                    file.deleteMarkers(
                            IAJModelMarker.DECLARATION_MARKER, true,
                            IResource.DEPTH_INFINITE);
                    subMonitor.worked(1);
                    
                    file.deleteMarkers(IAJModelMarker.CUSTOM_MARKER,
                            true, IResource.DEPTH_INFINITE);
                    subMonitor.worked(1);
                }
            }
        }
        subMonitor.done();
    }
}
