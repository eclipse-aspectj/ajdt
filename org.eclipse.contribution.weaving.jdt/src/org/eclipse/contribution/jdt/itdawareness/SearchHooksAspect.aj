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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

/**
 * An aspect providing hooks into JDT search
 * @author Andrew Eisenberg
 * @created Aug 17, 2010
 */
public privileged aspect SearchHooksAspect {
    /****************************
     * This section handles searching for ITDs
     * All searches that involve ITDs should use the 
     * inserted Java element instead of the ITD itself.
     */
    
    SearchAdapter searchAdapter = SearchAdapter.getInstance();
    
    /**
     * Capture all creations of SearchPatterns from JavaElements
     */
    pointcut searchPatternCreation(IJavaElement element, int limitTo, int matchRule) : 
        execution(public static SearchPattern SearchPattern.createPattern(IJavaElement, int, int)) &&
                  args(element, limitTo, matchRule);
    
    SearchPattern around(IJavaElement element, int limitTo, int matchRule) :
        searchPatternCreation(element, limitTo, matchRule) {
    
        ISearchProvider searchProvider = searchAdapter.getProvider();
        if (searchProvider != null) {
            // here, the provider can convert the JavaElement to something that it really should be searching 
            // for.  Eg, convert an ITD field into a field
            element = searchProvider.convertJavaElement(element);
        }
        return proceed(element, limitTo, matchRule);
    }
 
    /**
     * Here, we capture the getting of source code from a {@link PossibleMatch}.  Must ensure that the source code is 
     * transformed.
     */
    pointcut getPossibleMatchContents(PossibleMatch match) : execution(public char[] PossibleMatch.getContents()) && this(match);
    
    before(PossibleMatch match) : getPossibleMatchContents(match) {
        ISearchProvider searchProvider = searchAdapter.getProvider();
        if (searchProvider != null && match.source == null && searchProvider.isInteresting(match.openable)) {
            match.source = searchProvider.findSource(match.openable);
        }
    }
}
