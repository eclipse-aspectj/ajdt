/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.contribution.jdt.itdawareness;

import java.util.HashMap;

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.contribution.jdt.preferences.WeavableProjectListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.CompilationUnitProblemFinder;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyBuilder;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;

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
     * This will be null if AJDT is not installed (ie- JDT Weaving installed, but no AJDT)
     * Made public for testing purposes only.
     */
    public NameEnvironmentAdapter nameEnvironmentAdapter = NameEnvironmentAdapter.getInstance();
    
    
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
    pointcut typeHierachySourceTypeInfoCreation(IJavaElement element) :
        call(public Object JavaElement+.getElementInfo()) && target(element) &&
        within(HierarchyResolver);
    
    /** 
     * Capture all creations of source type element infos
     * and convert them into ITD aware source type element infos
     */
    Object around(IJavaElement element) : typeHierachySourceTypeInfoCreation(element) {
        Object info = proceed(element);
        if (WeavableProjectListener.getInstance().isInWeavableProject(element)) {
            if (info instanceof ISourceType &&
                    nameEnvironmentAdapter.getProvider() != null) {
                info = nameEnvironmentAdapter.getProvider().transformSourceTypeInfo((ISourceType) info);
            }
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
        if (nameEnvironmentAdapter.getProvider() != null && isInWeavable(workingCopies)) {
            try {
                SearchableEnvironment newEnvironment = nameEnvironmentAdapter.getProvider().getNameEnvironment(project, workingCopies);
                if (newEnvironment != null) {
                    return newEnvironment;
                }
            } catch (RuntimeException e) {
                JDTWeavingPlugin.logException(e);
            }
        }
        return proceed(project, workingCopies);
    }
    
    private boolean isInWeavable(ICompilationUnit[] workingCopies) {
        if (workingCopies != null && nameEnvironmentAdapter.getProvider() != null) {
            for (int i = 0; i < workingCopies.length; i++) {
                if (workingCopies[i] instanceof CompilationUnit &&
                        nameEnvironmentAdapter.getProvider().shouldFindProblems((CompilationUnit) workingCopies[i])) {
                    return true;
                }
            }
        }
        return false;
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

    
    SearchableEnvironment around(JavaProject project,
            WorkingCopyOwner owner) : interestingSearchableEnvironmentCreation2(project, owner) {
        if (nameEnvironmentAdapter.getProvider() != null && WeavableProjectListener.getInstance().isWeavableProject(project.getProject())) {
            try {
                SearchableEnvironment newEnvironment = nameEnvironmentAdapter.getProvider().getNameEnvironment(project, 
                        owner == null ? null : JavaModelManager.getJavaModelManager().getWorkingCopies(owner, true/*add primary WCs*/));
                if (newEnvironment != null) {
                    return newEnvironment;
                }
            } catch (RuntimeException e) {
                JDTWeavingPlugin.logException(e);
            }
        }            
        return proceed(project, owner);
    }

    
    /**
     * Only certain SearchableEnvironment creations are interesting
     * This pointcut determines which ones they are.
     */
    pointcut interestingSearchableEnvironmentCreation2(JavaProject project,
            WorkingCopyOwner workingCopyOwner) : 
                searchableEnvironmentCreation2(JavaProject, WorkingCopyOwner) &&
                (
                        cflow(codeSelect())  // open type action
                ) && args(project, workingCopyOwner);
            
    // alternate creation of searchble environment
    pointcut searchableEnvironmentCreation2(JavaProject project,
            WorkingCopyOwner workingCopyOwner) : 
                call(SearchableEnvironment.new(JavaProject,
                        WorkingCopyOwner)) && args(project, workingCopyOwner); 
    
    /**
     * for determining hyperlinks and open action
     * Also used for ITD hyperlinking
     */
    pointcut codeSelect() : 
        execution(protected IJavaElement[] Openable.codeSelect(org.eclipse.jdt.internal.compiler.env.ICompilationUnit,int,int,WorkingCopyOwner) throws JavaModelException);
    
    /********************************************
     * This section handles reconciling of java CompilationUnits. 
     * Ensure that the Java compilation unit is reconciled with an AJReconcileWorkingCopyOperation
     * so that ITDs are properly ignored.
     */

    @SuppressWarnings("unchecked")
    pointcut findProblemsInJava(
            CompilationUnit unitElement,
            SourceElementParser parser,
            WorkingCopyOwner workingCopyOwner,
            HashMap problems,
            boolean creatingAST,
            int reconcileFlags,
            IProgressMonitor monitor) : execution(public static CompilationUnitDeclaration CompilationUnitProblemFinder.process(CompilationUnit, SourceElementParser, WorkingCopyOwner, HashMap, boolean, int, IProgressMonitor)) &&
            args(unitElement, parser, workingCopyOwner, problems, creatingAST, reconcileFlags, monitor);

    
    @SuppressWarnings("unchecked")
    CompilationUnitDeclaration around(
            CompilationUnit unitElement, 
            SourceElementParser parser,
            WorkingCopyOwner workingCopyOwner,
            HashMap problems,
            boolean creatingAST,
            int reconcileFlags,
            IProgressMonitor monitor) throws JavaModelException : findProblemsInJava(unitElement, parser, workingCopyOwner, problems, creatingAST, reconcileFlags, monitor) {
        if (nameEnvironmentAdapter.getProvider() != null && nameEnvironmentAdapter.getProvider().shouldFindProblems(unitElement)) {
            try {
                return nameEnvironmentAdapter.getProvider().problemFind(unitElement, parser, workingCopyOwner, problems, creatingAST, reconcileFlags, monitor);
            } catch (Exception e) {
                if (! (e instanceof OperationCanceledException)) {
                    JDTWeavingPlugin.logException(e);
                } else {
                    // rethrow the cancel
                    throw (OperationCanceledException) e;
                }
            }
        }
        return proceed(unitElement, parser, workingCopyOwner, problems, creatingAST, reconcileFlags, monitor);
    }
            
            
    /*********************************
     * This section handles ITD aware content assist in Java files
     * 
     * Hmmmm...maybe want to promote this one to its own package because other plugins may
     * want to add their own way of doing completions for Java files
     */
            
    /**
     * This will be null if AJDT is not installed (ie- JDT Weaving installed, but no AJDT)
     * Made public for testing purposes only.
     */
    public ContentAssistAdapter contentAssistAdapter = ContentAssistAdapter.getInstance();

    pointcut codeCompleteInJavaFile(org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu,
            org.eclipse.jdt.internal.compiler.env.ICompilationUnit unitToSkip,
            int position, CompletionRequestor requestor,
            WorkingCopyOwner owner,
            ITypeRoot typeRoot, Openable target, IProgressMonitor monitor /* AJDT 1.7 */) : 
        execution(protected void Openable.codeComplete(
                org.eclipse.jdt.internal.compiler.env.ICompilationUnit,
                org.eclipse.jdt.internal.compiler.env.ICompilationUnit,
                int, CompletionRequestor,
                WorkingCopyOwner,
                ITypeRoot, IProgressMonitor)) &&  /* AJDT 1.7 */
                within(Openable) && this(target) && 
                args(cu, unitToSkip, position, requestor, owner, typeRoot, monitor);  /* AJDT 1.7 */
    
    void around(org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu,
            org.eclipse.jdt.internal.compiler.env.ICompilationUnit unitToSkip,
            int position, CompletionRequestor requestor,
            WorkingCopyOwner owner,
            ITypeRoot typeRoot, Openable target, IProgressMonitor monitor) :  /* AJDT 1.7 */
                codeCompleteInJavaFile(cu, unitToSkip, position, requestor, owner, typeRoot, target, monitor) {
        
        boolean result = false;
        if (contentAssistAdapter.getProvider() != null && nameEnvironmentAdapter.getProvider() != null && 
                (cu instanceof CompilationUnit) && 
                nameEnvironmentAdapter.getProvider().shouldFindProblems((CompilationUnit) cu)) {
            try {
                result = contentAssistAdapter.getProvider().doContentAssist(cu, unitToSkip, position, requestor, owner, typeRoot, target, monitor); /* AJDT 1.7 */
            } catch (Exception e) {
                JDTWeavingPlugin.logException(e);
                result = false;
            }
        }            
        if (!result) {
            proceed(cu, unitToSkip, position, requestor, owner, typeRoot, target, monitor);  /* AJDT 1.7 */
        }
    }
    /**
     * 
     * used for ITD hyperlinking
     */
    pointcut codeSelectWithArgs(CompilationUnit unit, int offset, int length) : 
        execution(public IJavaElement[] CompilationUnit.codeSelect(int,int) throws JavaModelException) &&
        this(unit) && args(offset, length);

    
    /**
     * Performs codeSelect operations with ITDAwareness.  This will allow things 
     * like Hovers and OpenDeclaration to work as exepcted with ITDs.
     */
    IJavaElement[] around(CompilationUnit unit, int offset, int length) : 
            codeSelectWithArgs(unit, offset, length) {
        IJavaElement[] result = proceed(unit, offset, length);
        if (contentAssistAdapter.getProvider() != null && nameEnvironmentAdapter.getProvider() != null &&
                nameEnvironmentAdapter.getProvider().shouldFindProblems((CompilationUnit) unit)) {
            // look for ITDs at the current location if required
            try {
                result = contentAssistAdapter.getProvider().doCodeSelect(unit, offset, length, result);
            } catch (Exception e) {
                JDTWeavingPlugin.logException(e);
            }
        }
        return result;
    }
}
