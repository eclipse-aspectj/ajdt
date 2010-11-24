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

import java.util.LinkedList;
import java.util.List;

import org.aspectj.org.eclipse.jdt.core.dom.AnyWithAnnotationTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareAnnotationDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.IdentifierTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.PatternNode;
import org.aspectj.org.eclipse.jdt.core.dom.SimpleName;
import org.aspectj.org.eclipse.jdt.core.dom.TypeCategoryTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.TypePattern;
import org.eclipse.ajdt.core.javaelements.DeclareElement;
import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.PackageReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.internal.core.search.matching.PackageReferencePattern;

/**
 * Looks for package names in aspect-specific locations
 * @author Andrew Eisenberg
 * @created Aug 6, 2010
 */
public class ExtraPackageReferenceFinder extends AbstractExtraReferenceFinder<PackageReferencePattern> implements IExtraMatchFinder<PackageReferencePattern> {

    private char[] pkgNames;

    protected List<IAspectJElement> getRelevantChildren(IParent parent) throws JavaModelException {
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

    protected boolean isMatch(IntertypeElement itd,
            PackageReferencePattern pattern) {
        ensurePkgNames(pattern);
        return pkgNames != null && CharOperation.compareWith(itd.getTargetTypeName().toCharArray(), pkgNames) == 0;
    }

    private void ensurePkgNames(PackageReferencePattern pattern) {
        if (pkgNames == null) {
            char[] packageTmp = TargetTypeUtils.getPackage(pattern);
            if (packageTmp != null) {
                pkgNames = new char[packageTmp.length + 1];
                System.arraycopy(packageTmp, 0, pkgNames, 0, packageTmp.length);
                pkgNames[pkgNames.length-1] = '.';
            }
        }
    }

    /**
     * Match was found in the target type name, which was fully qualified
     */
    protected SearchMatch createITDMatch(IntertypeElement itd,
            SearchParticipant participant) throws JavaModelException {
        ISourceRange sourceRange = itd.getTargetTypeSourceRange();
        int offset = sourceRange.getOffset();
        int length = pkgNames.length - 1; // -1 because pkgNames includes a dot
        return new PackageReferenceMatch(itd, SearchMatch.A_ACCURATE, offset, length, false, participant, itd.getResource());
    }

    @Override
    protected DeclareVisitor createDeclareVisitor(
            char[] contents, DeclareElement decl,
            SearchParticipant participant, PackageReferencePattern pattern)
            throws JavaModelException {
        ensurePkgNames(pattern);
        return new PackageReferenceDeclareVisitor(pkgNames, participant, decl, contents);
    }
    
    class PackageReferenceDeclareVisitor extends DeclareVisitor {
        public PackageReferenceDeclareVisitor(char[] pkgNames, SearchParticipant participant,
                DeclareElement decl, char[] fileContents) throws JavaModelException {
            super(participant, decl, fileContents);
        }
        
        @Override
        public boolean visit(IdentifierTypePattern node) {
            findMatchInTypePattern(node);
            return super.visit(node);
        }
        
        @Override
        public boolean visit(AnyWithAnnotationTypePattern node) {
            findMatchInTypePattern(node);
            return true;
        }
        
        @Override
        public boolean visit(TypeCategoryTypePattern node) {
            findMatchInTypePattern(node);
            return true;
        }

        /**
         * @param node
         */
        protected void findMatchInTypePattern(TypePattern node) {
            String detail = node.getTypePatternExpression();
            if (detail != null) {
                if (isMatch(detail, 0, detail.length())) {
                    int actualStart = node.getStartPosition() + offset;
                    acceptMatch(new PackageReferenceMatch(decl, SearchMatch.A_ACCURATE, 
                            actualStart, pkgNames.length - 1, false, participant, decl.getResource()));
                } else if (isComplexTypePattern(detail)) {
                    // must do something more complex
                    findMatchesInComplexPattern(node);
                }
            }
        }

        @Override
        public boolean visit(SimpleName node) {
            if (node.getParent() instanceof DeclareAnnotationDeclaration) {
                String name = node.toString();
                if (name.charAt(0) == '@' && isMatch(name, 1, name.length())) {
                    // match found, now search for location
                    // +1 because the length is off by one...missing the '@'
                    int actualStart = node.getStartPosition() + 1 + offset;
                    int actualLength = pkgNames.length - 1;
                    acceptMatch(new PackageReferenceMatch(decl, SearchMatch.A_ACCURATE, 
                            actualStart, actualLength, false, participant, decl.getResource()));
                }
            }
            return super.visit(node);
        }


        private boolean isMatch(String name, int start, int end) {
            char[] nameArr = CharOperation.subarray(name.toCharArray(), start, end);
            char[][] actualPackageFragments = CharOperation.splitAndTrimOn('.', nameArr);
            // now recombine with whitespace stripped away
            char[] strippedNameArr = CharOperation.concatWith(actualPackageFragments, '.');
            return CharOperation.prefixEquals(pkgNames, strippedNameArr);
        }

        @Override
        protected void findMatchesInComplexPattern(PatternNode node) {
            // FIXADE not implemented.
        }
        
    }
}
