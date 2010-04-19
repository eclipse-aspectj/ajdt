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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.aspectj.ajdt.internal.core.builder.AjCompilerOptions;
import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.ASTParser;
import org.aspectj.org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.BodyDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.CompilationUnit;
import org.aspectj.org.eclipse.jdt.core.dom.InterTypeFieldDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.aspectj.org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.ajdt.core.codeconversion.ITDAwareLookupEnvironment;
import org.eclipse.ajdt.core.codeconversion.ITDAwareNameEnvironment;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitInfo;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElementInfo;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.parserbridge.AJCompilationUnitStructureRequestor;
import org.eclipse.ajdt.core.parserbridge.AJSourceElementParser;
import org.eclipse.contribution.jdt.itdawareness.ISearchProvider;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.search.matching.FieldPattern;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

/**
 * 
 * @author Andrew Eisenberg
 * @created Mar 8, 2010
 */
public class AJDTSearchProvider implements ISearchProvider {
    
    // not used yet
//    private static class Tuple <T> {
//        final T first;
//        final T second;
//        Tuple(T first, T second) {
//            this.first = first;
//            this.second = second;
//        }
//        static Tuple<List> EMPTY_TUPLE = new Tuple(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
//    }
    
    public IJavaElement convertJavaElement(IJavaElement origElement) {
        if (origElement instanceof IntertypeElement) {
            try {
                char[] targetTypeName = ((IntertypeElementInfo) ((IntertypeElement) origElement).getElementInfo()).getTargetType();
                if (targetTypeName != null) {
                    AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(origElement);
                    List<IJavaElement> rels = model.getRelationshipsForElement(origElement, AJRelationshipManager.DECLARED_ON);
                    if (rels.size() > 0 && rels.get(0) instanceof IType) {
                        IType targetType = (IType) rels.get(0);
                        IJavaElement newElement = ((IntertypeElement) origElement).createMockDeclaration(targetType);
                        if (newElement != null) {
                            return newElement;
                        }
                    }
                }
            } catch (JavaModelException e) {
            }
        }
        return origElement;
    }

    public LookupEnvironment createLookupEnvironment(
            LookupEnvironment orig,
            ICompilationUnit[] workingCopies, JavaProject project) {
        ITDAwareNameEnvironment env;
        try {
            env = new ITDAwareNameEnvironment(project, workingCopies);
            return new ITDAwareLookupEnvironment(orig, env);
        } catch (JavaModelException e) {
        }
        return orig;
    }

    
    /**
     * Perform searching inside of ITDs if appropriate.
     * Two situations that we care about:
     * 1. field reference pattern inside of ITD.
     * 2. method reference pattern inside of ITD
     * 
     * If the {@link PossibleMatch} is an AJ compilation unit, then
     * look to see if it has any ITDs whose target type is of the type of the search 
     * pattern (or a sub-type, but not yet supported).  And if so, then delegate 
     * for further possible matching matching.
     * @throws JavaModelException 
     */
    public List<SearchMatch> findExtraMatches(PossibleMatch match, SearchPattern pattern) throws JavaModelException {
        
        if (! (match.openable instanceof AJCompilationUnit)) {
            return Collections.EMPTY_LIST;
        }
        AJCompilationUnit unit = (AJCompilationUnit) match.openable;
        List<IntertypeElement> allItds = getAllItds(unit);
        if (allItds.size() == 0) {
            return Collections.emptyList();
        }
        
        
        // find target type
        char[] targetTypeName = null;
        if (pattern instanceof FieldPattern) {
            targetTypeName = getName(getQualName((FieldPattern) pattern), getSimpleName((FieldPattern) pattern));
        } else if (pattern instanceof MethodPattern) {
            targetTypeName = getName(((MethodPattern) pattern).declaringQualification, ((MethodPattern) pattern).declaringSimpleName);
        }
        
        if (targetTypeName == null || targetTypeName.length == 0) {
            return Collections.emptyList();
        }
        
        for (Iterator<IntertypeElement> itdIter = allItds.iterator(); itdIter.hasNext();) {
            IntertypeElement itd = itdIter.next();
            if (! CharOperation.equals(targetTypeName, itd.getTargetType())) {
                itdIter.remove();
            }
        }
        
        CompilationUnit ajDomUnit = getDom(unit);
        List<SearchMatch> matches = walkITDs(ajDomUnit, allItds, pattern, match);
        return matches;
    }

    private List<SearchMatch> walkITDs(CompilationUnit ajDomUnit,
            List<IntertypeElement> allItds, SearchPattern pattern,
            PossibleMatch possibleMatch) throws JavaModelException {
        
        List<SearchMatch> allExtraMatches = new LinkedList<SearchMatch>();
        for (IntertypeElement itd : allItds) {
            BodyDeclaration decl = findITDInDom(ajDomUnit, itd);
            // we only care about ITD methods
            if (decl instanceof InterTypeMethodDeclaration) {
                allExtraMatches.addAll(findPatternInITD((InterTypeMethodDeclaration) decl, itd, pattern, possibleMatch));
            }
            
        }
        return allExtraMatches;
    }

    private Collection<SearchMatch> findPatternInITD(
            InterTypeMethodDeclaration decl, IntertypeElement itd,
            SearchPattern pattern, PossibleMatch possibleMatch) {
        ITDReferenceVisitor visitor = new ITDReferenceVisitor(itd, 
                pattern, possibleMatch.document.getParticipant());
        return visitor.doVisit(decl);
    }

    private BodyDeclaration findITDInDom(CompilationUnit ajDomUnit,
            IntertypeElement itd) throws JavaModelException {
        List<AbstractTypeDeclaration> types = ajDomUnit.types();
        for (AbstractTypeDeclaration type : types) {
            BodyDeclaration maybeDecl = findITDInDom(type, itd);
            if (maybeDecl != null) return maybeDecl;
        }
        return null;
    }

    private BodyDeclaration findITDInDom(AbstractTypeDeclaration type,
            IntertypeElement itd) throws JavaModelException {
        List<BodyDeclaration> decls = type.bodyDeclarations();
        for (BodyDeclaration decl : decls) {
            BodyDeclaration maybeDecl = null;
            if (decl instanceof AbstractTypeDeclaration) {
                maybeDecl = findITDInDom((AbstractTypeDeclaration) decl, itd);
            } else if (decl instanceof InterTypeMethodDeclaration || decl instanceof InterTypeFieldDeclaration) {
                int domStart = decl.getStartPosition();
                int domEnd = domStart + decl.getLength();
                ISourceRange sourceRange = itd.getSourceRange();
                int eltStart = sourceRange.getOffset();
                int eltEnd = eltStart + sourceRange.getLength();
                
                // return this decl if one contains the other
                if ((domStart <= eltStart && domEnd >= eltEnd) ||
                        (eltStart <= domStart && eltEnd >= domEnd)) {
                    maybeDecl = decl;
                }
            }
            if (maybeDecl != null) {
                return maybeDecl;
            }
        }
        return null;
    }

    private CompilationUnit getDom(AJCompilationUnit unit) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        unit.requestOriginalContentMode();
        parser.setSource(unit.getContents());
        parser.setCompilerOptions(unit.getJavaProject().getOptions(true));
        unit.discardOriginalContentMode();
        return (CompilationUnit) parser.createAST(new NullProgressMonitor());
    }

    static private Field declaringQualificationField = null;
    static private char[] getQualName(FieldPattern pattern) {
        try {
            if (declaringQualificationField == null) {
                declaringQualificationField = FieldPattern.class.getDeclaredField("declaringQualification");
                declaringQualificationField.setAccessible(true);
            }
            return (char[]) declaringQualificationField.get(pattern);
        } catch (Exception e) {
            return new char[0];
        }
    }

    static private Field declaringSimpleNameField = null;
    static private char[] getSimpleName(FieldPattern pattern) {
        try {
            if (declaringSimpleNameField == null) {
                declaringSimpleNameField = FieldPattern.class.getDeclaredField("declaringSimpleName");
                declaringSimpleNameField.setAccessible(true);
            }
            return (char[]) declaringSimpleNameField.get(pattern);
        } catch (Exception e) {
            return new char[0];
        }
    }
    
    private char[] getName(char[] qual, char[] name) {
        char[] targetTypeName;
        if (qual != null && qual.length > 0) {
            qual = CharOperation.append(qual, '.');
            targetTypeName = CharOperation.append(qual, qual.length, name, 0, name.length);
        } else {
            targetTypeName = name;
        }
        return targetTypeName;
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
