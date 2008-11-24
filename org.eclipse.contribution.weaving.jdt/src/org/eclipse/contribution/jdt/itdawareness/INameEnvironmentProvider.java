package org.eclipse.contribution.jdt.itdawareness;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;

/**
 * This class should only be implemented from within the AJDT framework
 * @author andrew
 * @created Nov 22, 2008
 * 
 * @noimplement
 *
 */
public interface INameEnvironmentProvider {
    public SearchableEnvironment getNameEnvironment(JavaProject project,
            WorkingCopyOwner owner);
    
    public SearchableEnvironment getNameEnvironment(JavaProject project,
            ICompilationUnit[] workingCopies);
    
    public ISourceType transformSourceTypeInfo(ISourceType info);
}
