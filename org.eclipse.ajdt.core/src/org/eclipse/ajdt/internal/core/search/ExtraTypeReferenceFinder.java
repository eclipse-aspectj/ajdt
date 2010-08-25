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
import java.util.Map;

import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IProgramElement.Kind;
import org.aspectj.org.eclipse.jdt.core.dom.AjASTVisitor;
import org.aspectj.org.eclipse.jdt.core.dom.BodyDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareAnnotationDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DefaultTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.PatternNode;
import org.aspectj.org.eclipse.jdt.core.dom.SignaturePattern;
import org.aspectj.org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.DeclareElement;
import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.javaelements.PointcutUtilities;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
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
        List<IAspectJElement> allItds = getRelevantChildren(unit);
        
        List<SearchMatch> matches = new ArrayList<SearchMatch>(allItds.size());
        // check each ITD's target type to see if it is a match
        for (IAspectJElement elt : allItds) {
            if (elt instanceof IntertypeElement) {
                IntertypeElement itd = (IntertypeElement) elt; 
                if (isMatch(itd, pattern)) {
                    // convert to a match
                    matches.add(createMatch(itd, match.document.getParticipant()));
                }
            } else if (elt instanceof DeclareElement) {
                DeclareElement decl = (DeclareElement) elt; 
                matches.addAll(findMatches(decl, match.document.getParticipant(), pattern));
            }
        }
        return matches;
    }

    private List<SearchMatch> findMatches(DeclareElement decl, SearchParticipant participant, TypeReferencePattern pattern) throws JavaModelException {
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
                DeclareVisitor visitor = new DeclareVisitor(participant, 
                        String.valueOf(TargetTypeUtils.getSimpleName(pattern)), 
                        String.valueOf(TargetTypeUtils.getQualName(pattern)), 
                        sourceRange.getOffset(), decl, contents);
                bdecl.accept(visitor);
                return visitor.getAccumulatedMatches();
            }
        }
        return Collections.emptyList();
    }
    
    private SearchMatch createMatch(IntertypeElement itd, SearchParticipant participant) throws JavaModelException {
        ISourceRange range = itd.getTargetTypeSourceRange();
        return new TypeReferenceMatch(itd, SearchMatch.A_ACCURATE, range.getOffset(), range.getLength(), false, participant, itd.getResource());
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
    
    class DeclareVisitor extends AjASTVisitor {
        private final String searchTypeSimpleName;
        private final String searchQualifier;
        private final String dotSearchTypeSimpleName;
        private final String atSearchTypeSimpleName;
        private final String atSearchTypeQualName;
        private final SearchParticipant participant;
        private final int offset;
        private final DeclareElement decl;
        private final List<SearchMatch> accumulatedMatches;
        private final char[] fileContents;
        public DeclareVisitor(SearchParticipant participant, String searchTypeSimpleName,
                String searchQualifier, int offset, DeclareElement decl, char[] fileContents) {
            this.participant = participant;
            this.searchTypeSimpleName = searchTypeSimpleName;
            this.searchQualifier = searchQualifier == null ? "" : searchQualifier;
            this.dotSearchTypeSimpleName = "." + searchTypeSimpleName;
            this.atSearchTypeSimpleName = "@" + searchTypeSimpleName;
            this.atSearchTypeQualName = "@" + (searchQualifier.length() > 0 ? searchQualifier + "." : "") + searchTypeSimpleName;
            this.offset = offset;
            this.accumulatedMatches = new ArrayList<SearchMatch>();
            this.decl = decl;
            this.fileContents = fileContents;
        }

        List<SearchMatch> getAccumulatedMatches() {
            return accumulatedMatches;
        }
        
        @Override
        public boolean visit(DefaultTypePattern node) {
            // We have already checked qualified names at this point, 
            // so we can assume a match if the simple names match.
            String detail = node.getDetail();
            if (detail != null) {
                if (searchTypeSimpleName.equals(detail) || 
                        detail.endsWith(dotSearchTypeSimpleName)) {
                    int actualStart = node.getStartPosition() + offset;
                    accumulatedMatches.add(new TypeReferenceMatch(decl, SearchMatch.A_ACCURATE, 
                            actualStart, node.getLength(), false, participant, decl.getResource()));
                } else if (isComplexTypePattern(detail)) {
                    // must do something more complex
                    findMatchesInComplexPattern(node);
                }
            }
            return super.visit(node);
        }

        /**
         * A type pattern is complex if it is more than just a type name
         * It will have more than just Java identifier characters
         * @param detail
         * @return
         */
        private boolean isComplexTypePattern(String detail) {
            return !JavaConventions.validateJavaTypeName(detail, CompilerOptions.VERSION_1_3,CompilerOptions.VERSION_1_3).isOK();
        }

        @Override
        public boolean visit(SignaturePattern node) {
            findMatchesInComplexPattern(node);
            return super.visit(node);
        }

        /**
         * Looks for matches in a complex AJ pattern.  Since we don't have bindings here,
         * only look for simple names.  All matches marked as inaccurate
         * @param node
         */
        private void findMatchesInComplexPattern(PatternNode node) {
            String actualPatternText = getActualText(node);
            Map<String, List<Integer>> matches = PointcutUtilities.findAllIdentifiers(actualPatternText);
            List<Integer> matchLocs = matches.get(searchTypeSimpleName);
            if (matchLocs != null) {
                for (Integer matchLoc : matchLocs) {
                    
                    // make the assumption that if the match is at a simple name, then
                    // this is a type match, but if there is a qual name, then match 
                    // on the qualified name
                    if (matchLoc > 0 && actualPatternText.charAt(matchLoc - 1) == '.') {
                        // we have a qual name.  Assume no spaces or special chars in there
                        String foundQualifier = findQualifier(actualPatternText, matchLoc);
                        
                        if (! foundQualifier.equals(searchQualifier)) {
                            // qualifiers do not match
                            continue;
                        }
                        
                        // adjust the matchLoc appropriately
                        // since we have assumed no spaces, then we can use the qualifier 
                        // as a way to adjust the match location
                        matchLoc -= foundQualifier.length() + 1;
                    }

                    int start = matchLoc + // end of the name relative to the SignaturePattern's detail
                            node.getStartPosition() + // start of SignaturePattern relative to the declareDeclaration's start 
                            offset; // the offset of the declareDeclaration from the start of the file
                    accumulatedMatches.add(new TypeReferenceMatch(decl, SearchMatch.A_INACCURATE, 
                            start, searchTypeSimpleName.length(), false, participant, decl.getResource()));
                }
            }
        }

        /**
         * @param actualPatternText
         * @param matchLoc
         * @return
         */
        private String findQualifier(String actualPatternText, int matchLoc) {
            int patternEnd = matchLoc - 1;
            int patternStart = patternEnd;
            while (patternStart > 1) {
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
        private String getActualText(PatternNode node) {
            return String.valueOf(CharOperation.subarray(fileContents, offset + node.getStartPosition(), offset + node.getStartPosition() + node.getLength()));
        }
        
        @Override
        public boolean visit(SimpleName node) {
            if (node.getParent() instanceof DeclareAnnotationDeclaration) {
                String name = node.toString();
                if (name.equals(atSearchTypeSimpleName) || name.equals(atSearchTypeQualName)) {
                    // match found, now search for location
                    // +1 because the length is off by one...missing the '@'
                    int actualStart = node.getStartPosition() + 1 + offset;
                    int actualLength = node.getLength() - 1;
                    accumulatedMatches.add(new TypeReferenceMatch(decl, SearchMatch.A_ACCURATE, 
                            actualStart, actualLength, false, participant, decl.getResource()));
                }
            }
            return super.visit(node);
        }
        
    }
}
