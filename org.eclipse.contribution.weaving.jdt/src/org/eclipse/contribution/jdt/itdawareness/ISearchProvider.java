/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.contribution.jdt.itdawareness;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

/**
 * 
 * @author Andrew Eisenberg
 * @created Mar 8, 2010
 */
public interface ISearchProvider {
    /**
     * Converts the element into something that should be searched for.
     * Should be a no-op if there is no change required
     * @return
     */
    public IJavaElement convertJavaElement(IJavaElement origElement);
    
    public LookupEnvironment createLookupEnvironment(
            LookupEnvironment lookupEnvironment,
            ICompilationUnit[] workingCopies, JavaProject project);
    
    public List<SearchMatch> findExtraMatches(PossibleMatch match, SearchPattern pattern, HierarchyResolver resolver) throws JavaModelException;
}
