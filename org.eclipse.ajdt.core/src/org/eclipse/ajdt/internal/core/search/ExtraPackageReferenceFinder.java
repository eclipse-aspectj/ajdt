/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.ajdt.internal.core.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.search.PackageReferenceMatch;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.DeclareElement;
import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;
import org.eclipse.jdt.internal.core.search.matching.PackageReferencePattern;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

/**
 * Looks for package names in aspect-specific locations
 * @author Andrew Eisenberg
 * @created Aug 6, 2010
 */
public class ExtraPackageReferenceFinder implements IExtraMatchFinder<PackageReferencePattern> {

    private char[] pkgNames;

    public List<SearchMatch> findExtraMatches(PossibleMatch match,
            PackageReferencePattern pattern, HierarchyResolver resolver)
            throws JavaModelException {
        if (! (match.openable instanceof AJCompilationUnit)) {
            return Collections.emptyList();
        }
        
        AJCompilationUnit unit = (AJCompilationUnit) match.openable;

        // first look for matches inside of ITDs (ie- the target type)
        List<IAspectJElement> allItds = getRelevantChildren(unit);
        
        List<SearchMatch> matches = new ArrayList<SearchMatch>(allItds.size());
        
        // now go through each child and look for references in places where 
        // a regular match locator will not find
        for (IAspectJElement elt : allItds) {
            if (elt instanceof IntertypeElement) {
                IntertypeElement itd = (IntertypeElement) elt;
                // determine if the package name matches the qualification of the target type
                if (isMatch(itd, pattern)) {
                    // convert to a match
                    matches.add(createMatch(itd, match.document.getParticipant()));
                }
            } else if (elt instanceof DeclareElement) {
                
            }
        }
        
        return matches;
    }
    
    private List<IAspectJElement> getRelevantChildren(IParent parent) throws JavaModelException {
        IJavaElement[] children = parent.getChildren();
        List<IAspectJElement> allItds = new LinkedList<IAspectJElement>();
        
        for (IJavaElement elt : children) {
            if (elt instanceof IntertypeElement || elt instanceof DeclareElement) {
                allItds.add((IAspectJElement) elt);
            } else if (elt.getElementType() == IJavaElement.TYPE) {
                allItds.addAll(getRelevantChildren((IParent) elt));
            }
        }
        return allItds;
    }

    private boolean isMatch(IntertypeElement itd,
            PackageReferencePattern pattern) {
        if (pkgNames == null) {
            char[] packageTmp = TargetTypeUtils.getPackage(pattern);
            if (packageTmp != null) {
                pkgNames = new char[packageTmp.length + 1];
                System.arraycopy(packageTmp, 0, pkgNames, 0, packageTmp.length);
                pkgNames[pkgNames.length-1] = '.';
            }
        }
        return pkgNames != null && CharOperation.compareWith(itd.getTargetTypeName().toCharArray(), pkgNames) == 0;
    }

    /**
     * Match was found in the target type name, which was fully qualified
     */
    private SearchMatch createMatch(IntertypeElement itd,
            SearchParticipant participant) throws JavaModelException {
        ISourceRange sourceRange = itd.getTargetTypeSourceRange();
        int offset = sourceRange.getOffset();
        int length = pkgNames.length - 1; // -1 because pkgNames includes a dot
        return new PackageReferenceMatch(itd, SearchMatch.A_ACCURATE, offset, length, false, participant, itd.getResource());
    }
}
