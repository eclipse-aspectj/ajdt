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

import org.aspectj.org.eclipse.jdt.core.dom.AjASTVisitor;
import org.aspectj.org.eclipse.jdt.core.dom.BodyDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.PatternNode;
import org.aspectj.org.eclipse.jdt.core.dom.SignaturePattern;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.DeclareElement;
import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.javaelements.PointcutUtilities;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

/**
 * Contains common logic for {@link ExtraPackageReferenceFinder} and
 * {@link ExtraTypeReferenceFinder}
 * 
 * @author Andrew Eisenberg
 * @created Aug 30, 2010
 */
abstract public class AbstractExtraReferenceFinder<T extends SearchPattern> {

    public AbstractExtraReferenceFinder() {
        super();
    }

    public List<SearchMatch> findExtraMatches(PossibleMatch match, T pattern,
            HierarchyResolver resolver) throws JavaModelException {
        if (!(match.openable instanceof AJCompilationUnit)) {
            return Collections.emptyList();
        }

        AJCompilationUnit unit = (AJCompilationUnit) match.openable;

        // first look for matches inside of ITDs (ie- the target type)
        List<IAspectJElement> allItds = getRelevantChildren(unit);

        List<SearchMatch> matches = new ArrayList<SearchMatch>(allItds.size());
        // check each ITD's target type to see if it is a match
        for (IAspectJElement elt : allItds) {
            if (elt instanceof IntertypeElement) {
                IntertypeElement itd = (IntertypeElement) elt;
                if (isMatch(itd, pattern)) {
                    // convert to a match
                    matches.add(createITDMatch(itd,
                            match.document.getParticipant()));
                }
            } else if (elt instanceof DeclareElement) {
                DeclareElement decl = (DeclareElement) elt;
                matches.addAll(findDeclareMatches(decl,
                        match.document.getParticipant(), pattern));
            }
        }
        return matches;
    }

    protected List<IAspectJElement> getRelevantChildren(IParent parent)
            throws JavaModelException {
        IJavaElement[] children = parent.getChildren();
        List<IAspectJElement> allItds = new LinkedList<IAspectJElement>();

        for (IJavaElement elt : children) {
            if (elt instanceof IntertypeElement
                    || elt instanceof DeclareElement) {
                allItds.add((IAspectJElement) elt);
            } else if (elt.getElementType() == IJavaElement.TYPE) {
                allItds.addAll(getRelevantChildren((IParent) elt));
            }
        }
        return allItds;
    }

    
    protected List<SearchMatch> findDeclareMatches(DeclareElement decl, SearchParticipant participant, T pattern) throws JavaModelException {
        AJCompilationUnit unit = ((AJCompilationUnit) decl.getCompilationUnit());
        // should already be in original content mode, but do this any way just in case.
        char[] contents = null;
        try { 
            unit.requestOriginalContentMode();
            contents = unit.getContents();
        } finally {
            unit.discardOriginalContentMode();
        }
        if (contents != null) {
            ISourceRange sourceRange = decl.getSourceRange();
            BodyDeclaration bdecl = PointcutUtilities.createSingleBodyDeclarationNode(sourceRange.getOffset(), sourceRange.getOffset() + sourceRange.getLength(), contents);
            // found it!
            if (bdecl != null) {
                DeclareVisitor visitor = createDeclareVisitor(contents, decl, participant, pattern);
                bdecl.accept(visitor);
                return visitor.getAccumulatedMatches();
            }
        }
        return Collections.emptyList();
    }
    
    abstract protected DeclareVisitor createDeclareVisitor(char[] contents, DeclareElement decl, SearchParticipant participant, T pattern) throws JavaModelException;
    
    abstract protected boolean isMatch(IntertypeElement itd, T pattern)
            throws JavaModelException;

    abstract protected SearchMatch createITDMatch(IntertypeElement itd,
            SearchParticipant participant) throws JavaModelException;
    
    abstract class DeclareVisitor extends AjASTVisitor {
        private final List<SearchMatch> accumulatedMatches;
        private final char[] fileContents;

        protected final int offset;
        protected final SearchParticipant participant;
        protected final DeclareElement decl;

        public DeclareVisitor(SearchParticipant participant, DeclareElement decl, char[] fileContents) throws JavaModelException {
            this.accumulatedMatches = new ArrayList<SearchMatch>();
            this.decl = decl;
            ISourceRange sourceRange = decl.getSourceRange();
            if (sourceRange != null) {
                this.offset = sourceRange.getOffset();
            } else {
                throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.NO_LOCAL_CONTENTS, 
                        "No source range available for " + decl.getElementName() + " " + decl.getParent().getElementName()));
            }
            this.fileContents = fileContents;
            this.participant = participant;
        }
        List<SearchMatch> getAccumulatedMatches() {
            return accumulatedMatches;
        }
        
        protected void acceptMatch(SearchMatch match) {
            accumulatedMatches.add(match);
        }
        
        /**
         * A type pattern is complex if it is more than just a type name
         * It will have more than just Java identifier characters
         * @param detail
         * @return
         */
        protected boolean isComplexTypePattern(String detail) {
            return !JavaConventions.validateJavaTypeName(detail, CompilerOptions.VERSION_1_3,CompilerOptions.VERSION_1_3).isOK();
        }

        @Override
        public boolean visit(SignaturePattern node) {
            findMatchesInComplexPattern(node);
            return super.visit(node);
        }

        abstract protected void findMatchesInComplexPattern(PatternNode node);
        
        protected String findQualifier(String actualPatternText, int matchLoc) {
            int patternEnd = matchLoc - 1;
            int patternStart = patternEnd;
            while (patternStart > 0) {
                patternStart--;
                if (Character.isJavaIdentifierPart(actualPatternText.charAt(patternStart))
                        || actualPatternText.charAt(patternStart) == '.') {
                    // do nothing
                } else {
                    patternStart++;
                    break;
                }
            }
            return actualPatternText.substring(patternStart, patternEnd);
        }

        /**
         * Can't use node.getDetail() here because that does not contain exactly what the underlying text is
         * @param node
         * @return
         */
        protected String getActualText(PatternNode node) {
            return String.valueOf(CharOperation.subarray(fileContents, offset + node.getStartPosition(), offset + node.getStartPosition() + node.getLength()));
        }
        
    }
}