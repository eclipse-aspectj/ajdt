package org.eclipse.contribution.jdt.itdawareness;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.core.CompilationUnit;
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
    
    @SuppressWarnings("unchecked")
    public CompilationUnitDeclaration problemFind(          
            CompilationUnit unitElement, 
            SourceElementParser parer,
            WorkingCopyOwner workingCopyOwner,
            HashMap problems,
            boolean creatingAST,
            int reconcileFlags,
            IProgressMonitor monitor) throws JavaModelException;

    public boolean shouldFindProblems(CompilationUnit unitElement);

}
