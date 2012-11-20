package org.eclipse.ajdt.internal.ui.markers;

import java.io.File;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

public class DeleteAJMarkers {
    private IProject project;
    private final IFile[] sourceFiles;

    public DeleteAJMarkers(IProject project) {
        this.project = project;
        this.sourceFiles = null;
    }
    
    /**
     * @deprecated Use {@link DeleteAJMarkers#DeleteAJMarkers(IProject, IFile[])} instead
     */
    public DeleteAJMarkers(IProject project, File[] sourceFiles) {
        this.project = project;
        this.sourceFiles = DeleteAndUpdateAJMarkersJob.javaFileToIFile(sourceFiles, project);
    }
    public DeleteAJMarkers(IProject project, IFile[] sourceFiles) {
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
    
    /**
     * Delete the advice markers for a project
     * @throws CoreException 
     */
    public void deleteAllMarkers(IProgressMonitor monitor) throws CoreException {
        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 4);

        // Delete all the existing markers
        try {
            subMonitor.subTask("Delete advises markers");
            project.deleteMarkers(IAJModelMarker.ADVICE_MARKER,
                    true, IResource.DEPTH_INFINITE);
        } catch (ResourceException e) {
            // project has been closed
        }
        subMonitor.worked(1);

        if (subMonitor.isCanceled()) {
            throw new OperationCanceledException();
        }

        
        try {
            subMonitor.subTask("Delete advice markers");
            project.deleteMarkers(
                    IAJModelMarker.SOURCE_ADVICE_MARKER, true,
                    IResource.DEPTH_INFINITE);
        } catch (ResourceException e) {
            // project has been closed
        }
        subMonitor.worked(1);

        if (subMonitor.isCanceled()) {
            throw new OperationCanceledException();
        }

        try {
            subMonitor.subTask("Delete declare markers");
            project.deleteMarkers(
                    IAJModelMarker.DECLARATION_MARKER, true,
                    IResource.DEPTH_INFINITE);
        } catch (ResourceException e) {
            // project has been closed
        }
        subMonitor.worked(1);

        if (subMonitor.isCanceled()) {
            throw new OperationCanceledException();
        }

        try {
            subMonitor.subTask("Delete custom markers");
            project.deleteMarkers(IAJModelMarker.CUSTOM_MARKER,
                    true, IResource.DEPTH_INFINITE);
        } catch (ResourceException e) {
            // project has been closed
        }
        subMonitor.worked(1);

        if (subMonitor.isCanceled()) {
            throw new OperationCanceledException();
        }

        subMonitor.done();
    }

    
    private void deleteMarkersForFiles(IProgressMonitor monitor) throws CoreException {
        IWorkspace workspace= ResourcesPlugin.getWorkspace();
        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, sourceFiles.length * 4);
        for (int i = 0; i < sourceFiles.length; i++) {
            IFile file = sourceFiles[i];
            if (file.exists() && CoreUtils.ASPECTJ_SOURCE_FILTER.accept(file.getName())) {
                subMonitor.subTask("Delete markers for " + file.getName());
                file.deleteMarkers(IAJModelMarker.ADVICE_MARKER,
                        true, IResource.DEPTH_INFINITE);
                subMonitor.worked(1);

                if (subMonitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
                
                file.deleteMarkers(
                        IAJModelMarker.SOURCE_ADVICE_MARKER, true,
                        IResource.DEPTH_INFINITE);
                subMonitor.worked(1);

                if (subMonitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                file.deleteMarkers(
                        IAJModelMarker.DECLARATION_MARKER, true,
                        IResource.DEPTH_INFINITE);
                subMonitor.worked(1);
                
                if (subMonitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                file.deleteMarkers(IAJModelMarker.CUSTOM_MARKER,
                        true, IResource.DEPTH_INFINITE);
                subMonitor.worked(1);

                if (subMonitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
            }
        }
        subMonitor.done();
    }

    boolean deletionForProject(IProject otherProject) {
        return this.project.equals(otherProject);
    }
}
