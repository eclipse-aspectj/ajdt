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
package org.eclipse.ajdt.internal.core.search;

import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

/**
 * 
 * @author Andrew Eisenberg
 * @created Aug 6, 2010
 */
public interface IExtraMatchFinder<T extends SearchPattern> {
    
    /**
     * 
     * @param match possible match that contains the AJCompilationUnit
     * @param pattern pattern to look for
     * @param resolver resolver for checking the type hierarchy
     * @return list of extra matches found in the aspect that would not normally be found by JDT search
     * Should never return null
     * @throws JavaModelException
     */
    public List<SearchMatch> findExtraMatches(PossibleMatch match, 
            T pattern, HierarchyResolver resolver) throws JavaModelException;
}
