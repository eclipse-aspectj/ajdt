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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

/**
 * 
 * @author Andrew Eisenberg
 * @created Mar 8, 2010
 */
public interface ISearchProvider {
    
    public IJavaElement findITDGetter(IField field);

    public IJavaElement findITDSetter(IField field);
    /**
     * Converts the element into something that should be searched for.
     * Should be a no-op if there is no change required
     * @return
     */
    public IJavaElement convertJavaElement(IJavaElement origElement);
    
    public LookupEnvironment createLookupEnvironment(
            LookupEnvironment lookupEnvironment,
            ICompilationUnit[] workingCopies, JavaProject project);
    
    /**
     * Finds any extra matches inside of this possible match not already found by jdt
     */
    public List<SearchMatch> findExtraMatches(PossibleMatch match, SearchPattern pattern, HierarchyResolver resolver) throws JavaModelException;
    
    /**
     * Callback for after the match has been added to the requestor
     * @param match
     */
    public void matchProcessed(PossibleMatch match);
    
    /**
     * This method will filter or convert posible test matches into real test matches.
     * Because of AspectJ's ITDs, there may be an @Test annotation on an ITD.
     * In this case, we should be using the mock target element instead of the original ITD.
     * @param possibleTest the original possible test
     * @return the real test method or class, or null if there should not be any
     */
    public IJavaElement filterJUnit4TestMatch(IJavaElement possibleTest) throws JavaModelException;
    
    public boolean isInteresting(IOpenable elt);
    
    public char[] findSource(IOpenable elt);
}
