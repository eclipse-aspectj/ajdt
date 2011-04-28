/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - changes for AJDT 2.0
 *******************************************************************************/
package org.eclipse.ajdt.core.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * 
 * @author Andrew Eisenberg
 * @created Oct 18, 2010
 */
public class AJCompilationParticipantResult extends BuildContext {
    
    private final IFile file;
    private char[] contents;

    protected List<CategorizedProblem> problems; // new problems to report against this compilationUnit
    protected String[] dependencies; // fully-qualified type names of any new dependencies, each name is of the form 'p1.p2.A.B'

    public AJCompilationParticipantResult(IFile file) {
        super();
        this.file = file;
    }

    @Override
    public char[] getContents() {
        try {
            contents = Util.getResourceContentsAsCharArray(this.file);
        } catch (CoreException e) {
            return CharOperation.NO_CHAR;
        }

        return this.contents;
    }
    
    @Override
    public IFile getFile() {
        return file;
    }

    /**
     * Record the fully-qualified type names of any new dependencies, each name is of the form "p1.p2.A.B".
     *
     * @param typeNameDependencies the fully-qualified type names of new dependencies
     */
    @Override
    public void recordDependencies(String[] typeNameDependencies) {
        int length2 = typeNameDependencies.length;
        if (length2 == 0) return;

        int length1 = this.dependencies == null ? 0 : this.dependencies.length;
        String[] merged = new String[length1 + length2];
        if (length1 > 0) // always make a copy even if currently empty
            System.arraycopy(this.dependencies, 0, merged, 0, length1);
        System.arraycopy(typeNameDependencies, 0, merged, length1, length2);
        this.dependencies = merged;
    }

    /**
     * Record new problems to report against this compilationUnit.
     * Markers are persisted for these problems only for the declared managed marker type
     * (see the 'compilationParticipant' extension point).
     *
     * @param newProblems the problems to report
     */
    @Override
    public void recordNewProblems(CategorizedProblem[] newProblems) {
        int length = newProblems.length;
        if (length == 0) return;

        if (problems == null) {
            problems = new ArrayList<CategorizedProblem>(length);
        }
        for (int i = 0; i < length; i++) {
            problems.add(newProblems[i]);
        }
    }
    
    public List<CategorizedProblem> getProblems() {
        return problems;
    }
    
    public String[] getDependencies() {
        return dependencies;
    }
    
    private Boolean hasAnnotationsCache = null;
    
    @Override
    public boolean hasAnnotations() {
        if (hasAnnotationsCache == null) {
            try {
                if (file != null) {
                    ITypeRoot root = (ITypeRoot) JavaCore.create(file);
                    if (root.exists()) {
                        hasAnnotationsCache = hasAnnotations(root);
                    } else {
                        hasAnnotationsCache = false;
                    }
                }
            } catch (JavaModelException e) {
                hasAnnotationsCache = false;
            }
            
        }
        
        return hasAnnotationsCache;
    }

    /**
     * @param parent
     * @throws JavaModelException
     */
    private boolean hasAnnotations(IParent parent) throws JavaModelException {
        IJavaElement[] children = parent.getChildren();
        for (IJavaElement child : children) {
            if (child instanceof IAnnotatable) {
                if (((IAnnotatable) child).getAnnotations().length > 0) {
                    return true;
                }
            }
            if (child instanceof IParent) {
                if (hasAnnotations((IParent) child)) {
                    return true;
                }
            }
        }
        return false;
    }
}
