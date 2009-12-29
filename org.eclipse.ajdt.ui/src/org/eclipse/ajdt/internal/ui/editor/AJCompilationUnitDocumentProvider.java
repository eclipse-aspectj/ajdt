package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.fix.CleanUpPostSaveListener;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider;
import org.eclipse.jdt.internal.ui.javaeditor.saveparticipant.IPostSaveListener;
import org.eclipse.jface.text.IRegion;

/**
 * Ensures AJCompilationUnits are created properly.
 * Also ensures that format on save does not occur for AJ files
 */
public class AJCompilationUnitDocumentProvider extends
        CompilationUnitDocumentProvider {
    
    public AJCompilationUnitDocumentProvider() {
        super();
    }

    protected ICompilationUnit createCompilationUnit(IFile file) {
        return (ICompilationUnit) AspectJCore.create(file);
    }
    
    /**
     * Bug 263313 
     * cannot do formatting on save.  remove the CleanUpPostSaveListener
     * if it exists
     */
    protected void notifyPostSaveListeners(CompilationUnitInfo info,
            IRegion[] changedRegions, IPostSaveListener[] listeners,
            IProgressMonitor monitor) throws CoreException {
        
        if (listeners == null) {
            return;
        }
        int found = -1;
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof CleanUpPostSaveListener) {
                found = i;
            }
        }
        if (found >= 0 && listeners.length > 0) {
            IPostSaveListener[] newListeners = new IPostSaveListener[listeners.length-1];
            System.arraycopy(listeners, 0, newListeners, 0, found);
            System.arraycopy(listeners, found+1, newListeners, found, listeners.length - found - 1);
            
            super.notifyPostSaveListeners(info, changedRegions, newListeners, monitor);
        } else {
            super.notifyPostSaveListeners(info, changedRegions, listeners, monitor);
        }
        
    }
}
