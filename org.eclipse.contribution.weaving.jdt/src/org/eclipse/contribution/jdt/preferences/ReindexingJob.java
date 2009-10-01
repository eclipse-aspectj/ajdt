package org.eclipse.contribution.jdt.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.MessageBox;

/**
 * 
 * @author andrew
 * @created Dec 10, 2008
 * 
 * Reindexes all projects so that Java-like compilation units 
 * can be added to the index.
 */
public class ReindexingJob extends WorkspaceJob {

    public ReindexingJob() {
        super("Reindexing for JDT Weaving");
    }
    
    public ReindexingJob(int nothing) {
        this();
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor)
            throws CoreException {
        IndexManager indexer = JavaModelManager.getIndexManager();
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (int i = 0; i < projects.length; i++) {
            if (WeavableProjectListener.getInstance().isWeavableProject(projects[i])) {
                projects[i].accept(new TouchJavaLikeResourceVisitor(monitor));
                indexer.indexAll(projects[i]);
            }
        }
        return Status.OK_STATUS;
    }
    
    private class TouchJavaLikeResourceVisitor implements IResourceVisitor {
        IProgressMonitor monitor;
        
        TouchJavaLikeResourceVisitor(IProgressMonitor monitor) {
            this.monitor = monitor;
        }
        
        public boolean visit(IResource resource) throws CoreException {
            if (resource.getType() == IResource.FILE && Util.isJavaLikeFileName(resource.getName())) {
                resource.touch(monitor);
            }
            return true;
        }
        
    }
}
