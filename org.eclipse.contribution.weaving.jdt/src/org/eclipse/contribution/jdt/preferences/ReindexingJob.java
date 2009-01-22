package org.eclipse.contribution.jdt.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

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
        super("Reindexing");
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
            indexer.indexAll(projects[i]);
        }
        return Status.OK_STATUS;
    }

}
