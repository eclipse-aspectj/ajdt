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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;
import org.eclipse.jdt.internal.core.search.matching.TypeReferencePattern;

/**
 * Helps find Type references of a {@link SearchPattern} inside of an {@link AspectElement}
 * 
 * @author Andrew Eisenberg
 * @created Aug 6, 2010
 */
public class ExtraTypeReferenceFinder implements IExtraMatchFinder<TypeReferencePattern> {

    public List<SearchMatch> findExtraMatches(PossibleMatch match,
            TypeReferencePattern pattern, HierarchyResolver resolver)
            throws JavaModelException {
        if (! (match.openable instanceof AJCompilationUnit)) {
            return Collections.emptyList();
        }
        
        AJCompilationUnit unit = (AJCompilationUnit) match.openable;

        // first look for matches inside of ITDs (ie- the target type)
        List<IntertypeElement> allItds = getAllItds(unit);
        
        List<SearchMatch> matches = new ArrayList<SearchMatch>(allItds.size());
        // check each ITD's target type to see if it is a match
        for (IntertypeElement itd : allItds) {
            if (isMatch(itd, pattern)) {
                // convert to a match
                matches.add(createMatch(itd, match.document.getParticipant()));
            }
        }
        
        return matches;
    }

    private SearchMatch createMatch(IntertypeElement itd, SearchParticipant participant) throws JavaModelException {
        String itdName = itd.getElementName();
        // might find a fully qualified name
        int typeNameEnd = Math.max(itdName.lastIndexOf('.'), 0);
        String typeName = itdName.substring(0, typeNameEnd);
        
        // itd.getNameRange() returns the range of the name, but excludes the target type.  Must subtract from there.  Make assumption that there are no spaces
        // or comments between '.' and the rest of the name
        int sourceStart;
        if (itd.getAJKind() == Kind.INTER_TYPE_CONSTRUCTOR) {
            sourceStart = itd.getNameRange().getOffset();
        } else {
            sourceStart = itd.getNameRange().getOffset() - 1 - typeName.length();
        }
        return new TypeReferenceMatch(itd, SearchMatch.A_ACCURATE, sourceStart, typeName.length(), false, participant, itd.getResource());
    }

    private boolean isMatch(IntertypeElement itd, TypeReferencePattern pattern) throws JavaModelException {
        char[] targetTypeName = TargetTypeUtils.getName(pattern);
        IType targetType = itd.findTargetType();
        if (targetType != null) {
            return CharOperation.equals(targetTypeName, targetType.getFullyQualifiedName().toCharArray());
        } else {
            return false;
        }
    }

    private List<IntertypeElement> getAllItds(IParent parent) throws JavaModelException {
        IJavaElement[] children = parent.getChildren();
        List<IntertypeElement> allItds = new LinkedList<IntertypeElement>();
        
        for (IJavaElement elt : children) {
            if (elt instanceof IntertypeElement) {
                allItds.add((IntertypeElement) elt);
            } else if (elt.getElementType() == IJavaElement.TYPE) {
                allItds.addAll(getAllItds((IParent) elt));
            }
        }
        return allItds;
    }
}
