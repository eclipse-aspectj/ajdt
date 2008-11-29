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
import org.eclipse.jdt.internal.core.CompilationUnitProblemFinder;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyBuilder;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;


/**
 * Aspect to add ITD awareness to various kinds of searches in the IDE
 * This aspect swaps out a SearchableEnvironment with an ITDAwareNameEnvironment
 * 
 * See bug 256312 for an explanation of this aspect
 * 
 * @author andrew
 * @created Nov 22, 2008
 *
 */
public aspect ITDAwarenessAspect {
    
    /**
     * set by the AspectJPlugin on startup
     */
    public static INameEnvironmentProvider provider;
    
    
    /********************************************
     * This section deals with ensuring the focus type has its 
     * super types properly created
     */
    
    /**
     * This pointcut grabs all calls to the getting of the element info
     * for source types that occur within the HierarchyResolver.
     * 
     * Need to convert all SourceTypeInfos into ITDAwareSourceTypeInfos
     * when they occur in the HierarchyResolver
     */
    pointcut typeHierachySourceTypeInfoCreation() :
        call(public Object JavaElement+.getElementInfo()) &&
        within(HierarchyResolver);
    
    /** 
     * Capture all creations of source type element infos
     * and convert them into ITD aware source type element infos
     */
    Object around() : typeHierachySourceTypeInfoCreation() {
        Object info = proceed();
        if (info instanceof ISourceType &&
                provider != null) {
            info = provider.transformSourceTypeInfo((ISourceType) info);
        }
        return info;
    }

    /********************************************
     * This section deals with ensuring that all other types
     * have their super types properly created
     * 
     * Note that *sub* types are not properly found.
     * 
     * The reason is that sub types are found through the indexer.
     * The indexer does not index declare parents relationships
     * 
     * Don't do subtypes for now.
     */

    /**
     * Converts a name environment into an ITD Aware Name Environment
     * when we are finding type hierarchies
     */
    SearchableEnvironment around(JavaProject project,
            ICompilationUnit[] workingCopies) : 
                interestingSearchableEnvironmentCreation(project, workingCopies) {
        if (provider != null) {
            SearchableEnvironment newEnvironment = provider.getNameEnvironment(project, workingCopies);
            if (newEnvironment != null) {
                return newEnvironment;
            }
        }
        return proceed(project, workingCopies);
    }
    
    /**
     * The creation of a SearchableEnvironment
     */
    pointcut searchableEnvironmentCreation(JavaProject project,
            ICompilationUnit[] workingCopies) : 
                call(SearchableEnvironment.new(JavaProject,
                        ICompilationUnit[])) && args(project, workingCopies);
    
    /**
     * Only certain SearchableEnvironment creations are interesting
     * This pointcut determines which ones they are.
     */
    pointcut interestingSearchableEnvironmentCreation(JavaProject project,
            ICompilationUnit[] workingCopies) : 
                searchableEnvironmentCreation(JavaProject, ICompilationUnit[]) &&
                (
                        cflow(typeHierarchyCreation()) || // creation of type hierarchies
                        cflow(typeHierarchyComputing())  // computing the type hierarchy (do we need both?)
                ) && args(project, workingCopies);
            
    /**
     * The creation of a type hierarchy
     */
    pointcut typeHierarchyCreation() : execution(public HierarchyBuilder.new(TypeHierarchy));
    
    /**
     * the computation of a type hierarchy
     */
    pointcut typeHierarchyComputing() : execution(protected void TypeHierarchy.compute());

    
    
    /********************************************
     * This section handles reconciling of java CompilationUnits. 
     * Ensure that the Java compilation unit is reconciled with an AJReconcileWorkingCopyOperation
     * so that ITDs are properly ignored.
     */

    pointcut findProblemsInJava(
            CompilationUnit unitElement,
            SourceElementParser parser,
            WorkingCopyOwner workingCopyOwner,
            HashMap problems,
            boolean creatingAST,
            int reconcileFlags,
            IProgressMonitor monitor) : execution(public static CompilationUnitDeclaration CompilationUnitProblemFinder.process(CompilationUnit, SourceElementParser, WorkingCopyOwner, HashMap, boolean, int, IProgressMonitor)) &&
            args(unitElement, parser, workingCopyOwner, problems, creatingAST, reconcileFlags, monitor);

    
    CompilationUnitDeclaration around(
            CompilationUnit unitElement, 
            SourceElementParser parser,
            WorkingCopyOwner workingCopyOwner,
            HashMap problems,
            boolean creatingAST,
            int reconcileFlags,
            IProgressMonitor monitor) throws JavaModelException : findProblemsInJava(unitElement, parser, workingCopyOwner, problems, creatingAST, reconcileFlags, monitor) {
                if (provider != null) {
                    return provider.problemFind(unitElement, parser, workingCopyOwner, problems, creatingAST, reconcileFlags, monitor);
                } else {
                    return proceed(unitElement, parser, workingCopyOwner, problems, creatingAST, reconcileFlags, monitor);
                }
            }
    
//    pointcut reconcileOperationCreation(IJavaElement workingCopy, int astLevel, int reconcileFlags, WorkingCopyOwner workingCopyOwner) :
//        call(ReconcileWorkingCopyOperation.new(IJavaElement, int, int, WorkingCopyOwner)) &&
//        args(workingCopy, astLevel, reconcileFlags, workingCopyOwner);
//    
//    ReconcileWorkingCopyOperation around(IJavaElement workingCopy, int astLevel, int reconcileFlags, WorkingCopyOwner workingCopyOwner) :
//        reconcileOperationCreation(workingCopy, astLevel, reconcileFlags, workingCopyOwner) {
//        
//        if (provider != null) {
//            return provider.createReconcileOperation(workingCopy, astLevel, reconcileFlags, workingCopyOwner);
//        } else {
//            return proceed(workingCopy, astLevel, reconcileFlags, workingCopyOwner);
//        }
//    }
    
}
