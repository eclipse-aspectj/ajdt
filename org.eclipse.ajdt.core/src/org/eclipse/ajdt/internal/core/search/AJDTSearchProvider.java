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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.aspectj.asm.IProgramElement.Kind;
import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.ASTParser;
import org.aspectj.org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.BodyDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.CompilationUnit;
import org.aspectj.org.eclipse.jdt.core.dom.InterTypeFieldDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration;
import org.eclipse.jdt.core.search.FieldDeclarationMatch;
import org.eclipse.jdt.core.search.MethodDeclarationMatch;
import org.eclipse.ajdt.core.ReflectionUtils;
import org.eclipse.ajdt.core.codeconversion.ITDAwareLookupEnvironment;
import org.eclipse.ajdt.core.codeconversion.ITDAwareNameEnvironment;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElementInfo;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.contribution.jdt.itdawareness.ISearchProvider;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;
import org.eclipse.jdt.internal.core.search.matching.ConstructorPattern;
import org.eclipse.jdt.internal.core.search.matching.FieldPattern;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;
import org.eclipse.jdt.internal.core.search.matching.OrPattern;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;
import org.eclipse.jdt.internal.core.search.matching.VariablePattern;

/**
 * 
 * @author Andrew Eisenberg
 * @created Mar 8, 2010
 */
public class AJDTSearchProvider implements ISearchProvider {
    
    
    public IntertypeElement findITDGetter(IField field) {
        return findITDAccessor(field, true);
    }

    public IntertypeElement findITDSetter(IField field) {
        return findITDAccessor(field, false);
    }
    
    private IntertypeElement findITDAccessor(IField field, boolean getter) {
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(field);
        List<IJavaElement> rels = model.getRelationshipsForElement(field.getDeclaringType(), AJRelationshipManager.ASPECT_DECLARATIONS);
        for (IJavaElement elt : rels) {
            if (elt instanceof IntertypeElement) {
                IntertypeElement itd = (IntertypeElement) elt;
                if (isAccessorITDName(itd.getElementName(), 
                        field.getElementName(), 
                        field.getDeclaringType().getElementName(), 
                        field.getDeclaringType().getFullyQualifiedName(),
                        getter) &&
                        checkParameters(itd, field, getter) &&
                        checkReturnType(itd, field, getter)) {
                    return itd;
                }
            }
        }
        return null;
    }
    
    private boolean checkReturnType(IntertypeElement itd, IField field,
            boolean getter) {
        try {
            if (getter) {
                    return itd.getReturnType().equals(field.getTypeSignature()) || 
                        field.getTypeSignature().equals(String.valueOf(itd.getQualifiedReturnType()));
            } else {
                return itd.getReturnType().equals(Signature.SIG_VOID);
            }
        } catch (JavaModelException e) {
        }
        return false;
    }

    private boolean checkParameters(IntertypeElement itd, IField field,
            boolean getter) {
        String[] parameterTypes = itd.getParameterTypes();
        if (getter) {
            return parameterTypes == null || 
                    parameterTypes.length == 0;
        } else {
            try {
                if (parameterTypes != null &&
                    parameterTypes.length == 1) {
                    String typeSignature = field.getTypeSignature();
                    if (parameterTypes[0].equals(typeSignature)) {
                        return true;
                    }
                    // now try fully qualified, and ensure that the type is unbound
                    String itdParamSignature = itd.getQualifiedParameterTypes()[0];
                    int arrayCount = Signature.getArrayCount(itdParamSignature);
                    if (itdParamSignature.charAt(arrayCount) == 'L') {
                        itdParamSignature = itdParamSignature.substring(0, arrayCount) + 'Q' + itdParamSignature.substring(arrayCount+1);
                    }
                    return typeSignature.equals(itdParamSignature);
                }
            } catch (JavaModelException e) {
            }
        }
        return false;
    }

    private boolean isAccessorITDName(String itdName, String fieldName,
            String declaringTypeName, String declaringFullyQualifedName, boolean getter) {
        String prefix = getter ? "get" : "set";
        // there might be a package fragment that starts with 'is'
        int lastDot = itdName.lastIndexOf('.');
        if (getter && lastDot >= 0 && itdName.indexOf(".is", lastDot) > 0) {
            prefix = "is";
        }
        String suffix = prefix + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String accessorName = declaringTypeName + "." + suffix; 
        if (itdName.equals(accessorName)) {
            return true;
        }
        accessorName = declaringFullyQualifedName + "." + suffix;
        return itdName.equals(accessorName);
    }

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
        try {
            ITDAwareNameEnvironment env = new ITDAwareNameEnvironment(project, workingCopies);
            return new ITDAwareLookupEnvironment(orig, env);
        } catch (JavaModelException e) {
        }
        return orig;
    }

    
    /**
     * This method finds extra matches where the search pattern refers to an intertype declaration.
     */
    public List<SearchMatch> findExtraMatches(PossibleMatch match, SearchPattern pattern, HierarchyResolver resolver) throws JavaModelException {
        List<SearchMatch> extraMatches = new ArrayList<SearchMatch>();
        if (pattern instanceof OrPattern) {
            SearchPattern[] patterns = (SearchPattern[]) ReflectionUtils.getPrivateField(OrPattern.class, "patterns", (OrPattern) pattern);
            for (SearchPattern orPattern : patterns) {
                extraMatches.addAll(findExtraMatches(match, orPattern, resolver));
            }
        } else {
            
            if ((match.openable instanceof AJCompilationUnit)) {
                boolean findDeclarations = findDeclarations(pattern);
                boolean findReferences = findReferences(pattern);
                
                AJCompilationUnit unit = (AJCompilationUnit) match.openable;
                List<IntertypeElement> allRelevantItds = findRelevantITDs(pattern,
                        resolver, unit);
                if (allRelevantItds.size() > 0) {
                    if (findReferences) {
                       extraMatches.addAll(findExtraReferenceMatches(unit, allRelevantItds, pattern, match));
                    }
                    
                    // for method and constructor patterns, also look for declarations
                    // can't do this for field patterns because there would be a class cast exception
                    // when trying to cast a IntertypeDeclaration to an IField.
                    // Note that IntertypeDeclaration happens to implement IMethod.
                    if (findDeclarations) {
                        extraMatches.addAll(findExtraDeclarationMatches(unit, allRelevantItds, pattern, match));
                    }
                }
            }
        }
        return extraMatches;
    }

    private List<IntertypeElement> findRelevantITDs(SearchPattern pattern,
            HierarchyResolver resolver, AJCompilationUnit unit)
            throws JavaModelException {
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
   
        // if target type is not known, then this means that a target
        // type is not specified and therefore do not remove any 
        // potential matches.
        if (targetTypeName != null && targetTypeName.length > 0) {
            if (resolver != null) {
                resolver.setFocusType(CharOperation.splitOn('.', targetTypeName));
            }
            
            for (Iterator<IntertypeElement> itdIter = allItds.iterator(); itdIter.hasNext();) {
                IntertypeElement itd = itdIter.next();
                if (!isSubtypeOfSearchPattern(targetTypeName, itd, resolver)) {
                    itdIter.remove();
                }
            }
        }
        return allItds;
    }

    private boolean findReferences(SearchPattern pattern) {
        if (pattern instanceof MethodPattern) {
            return (Boolean) ReflectionUtils.getPrivateField(MethodPattern.class, "findReferences", (MethodPattern) pattern);
        } else if (pattern instanceof FieldPattern) {
            // note that findReferences is actually declared in VariablePattern
            return (Boolean) ReflectionUtils.getPrivateField(VariablePattern.class, "findReferences", (VariablePattern)pattern);
        } else if (pattern instanceof ConstructorPattern) {
            return (Boolean) ReflectionUtils.getPrivateField(ConstructorPattern.class, "findReferences", (ConstructorPattern) pattern);
        } else {
            return false;
        }
    }

    private boolean findDeclarations(SearchPattern pattern) {
        if (pattern instanceof MethodPattern) {
            return (Boolean) ReflectionUtils.getPrivateField(MethodPattern.class, "findDeclarations", (MethodPattern) pattern);
        } else if (pattern instanceof FieldPattern) {
            // note that findDeclarations is actually declared in VariablePattern
            return (Boolean) ReflectionUtils.getPrivateField(VariablePattern.class, "findDeclarations", (VariablePattern)pattern);
        } else if (pattern instanceof ConstructorPattern) {
            return (Boolean) ReflectionUtils.getPrivateField(ConstructorPattern.class, "findDeclarations", (ConstructorPattern) pattern);
        } else {
            return false;
        }
    }

    /**
     * Perform searching inside of ITDs if appropriate.
     * Two situations that we care about:
     * 1. field reference pattern inside of ITD.
     * 2. method reference pattern inside of ITD
     * 
     * If the {@link PossibleMatch} is an {@link AJCompilationUnit}, then
     * look to see if it has any ITDs whose target type is of the type of the search 
     * pattern.  And if so, then delegate for further possible matching matching.
     * @throws JavaModelException 
     */
    private List<SearchMatch> findExtraReferenceMatches(AJCompilationUnit unit, List<IntertypeElement> allRelevantItds, SearchPattern pattern, PossibleMatch match)
            throws JavaModelException {
        CompilationUnit ajDomUnit = getDom(unit);
        List<SearchMatch> matches = walkITDs(ajDomUnit, allRelevantItds, pattern, match);
        return matches;
    }
    
    /**
     * Return all declaration matches that are ITDs of the proper type or in the type hierarchy of the expected type 
     * @throws JavaModelException 
     */
    private List<SearchMatch> findExtraDeclarationMatches(
            AJCompilationUnit unit, List<IntertypeElement> allRelevantItds, SearchPattern pattern, PossibleMatch match) throws JavaModelException {
        
        List<SearchMatch> extraDeclarationMatches = new ArrayList<SearchMatch>();
        // At this point, we know that the itds passed in have the same declaring type or are a subtype of the target type
        // So, just need to check selector and parameters
        // it is too time consuming to get the qualified parameters of the types, so
        // just match on simple names.
        if (pattern instanceof MethodPattern) {
            MethodPattern methPatt = (MethodPattern) pattern;
            char[] selector = methPatt.selector;
            char[][] simpleParamTypes = methPatt.parameterSimpleNames;

            for (IntertypeElement itd : allRelevantItds) {
                if (itd.getAJKind() == Kind.INTER_TYPE_METHOD &&
                        CharOperation.equals(selector, itd.getTargetName().toCharArray())) { 
                    char[][] itdSimpleParamNames = extractSimpleParamNames(itd);
                    if (CharOperation.equals(simpleParamTypes, itdSimpleParamNames)) {
                        ISourceRange sourceRange = itd.getNameRange();
                        extraDeclarationMatches.add(new MethodDeclarationMatch(itd, SearchMatch.A_ACCURATE, sourceRange.getOffset(), sourceRange.getLength(), 
                                match.document.getParticipant(), itd.getCompilationUnit().getResource()));
                    }
                }
            }
        } else if (pattern instanceof ConstructorPattern) {
            ConstructorPattern consPatt = (ConstructorPattern) pattern;
            // must match the exact type
            char[] targetTypeName = getName(consPatt.declaringQualification, consPatt.declaringSimpleName);
            char[][] simpleParamTypes = consPatt.parameterSimpleNames;
            for (IntertypeElement itd : allRelevantItds) {
                if (itd.getAJKind() == Kind.INTER_TYPE_CONSTRUCTOR && 
                        targetTypeName != null && CharOperation.equals(targetTypeName, fullyQualifiedTargetTypeName(itd))) { 
                    char[][] itdSimpleParamNames = extractSimpleParamNames(itd);
                    if (CharOperation.equals(simpleParamTypes, itdSimpleParamNames)) {
                        ISourceRange sourceRange = itd.getNameRange();
                        extraDeclarationMatches.add(new MethodDeclarationMatch(itd, SearchMatch.A_ACCURATE, sourceRange.getOffset(), sourceRange.getLength(), 
                                match.document.getParticipant(), itd.getCompilationUnit().getResource()));
                    }
                }
            }
        } else if (pattern instanceof FieldPattern) {
            FieldPattern fieldPatt = (FieldPattern) pattern;
            // must match the exact type
            char[] targetTypeName = getName(getQualName(fieldPatt), getSimpleName(fieldPatt));
            char[] fieldName = fieldPatt.getIndexKey();
            for (IntertypeElement itd : allRelevantItds) {
                if (itd.getAJKind() == Kind.INTER_TYPE_FIELD && CharOperation.equals(fieldName, itd.getTargetName().toCharArray()) &&
                        targetTypeName != null && CharOperation.equals(targetTypeName, fullyQualifiedTargetTypeName(itd))) {
                    ISourceRange sourceRange = itd.getNameRange();
                    extraDeclarationMatches.add(new FieldDeclarationMatch(itd, SearchMatch.A_ACCURATE, sourceRange.getOffset(), sourceRange.getLength(), 
                            match.document.getParticipant(), itd.getCompilationUnit().getResource()));
                }
            }
        }
        return extraDeclarationMatches;
    }



    private char[][] extractSimpleParamNames(IntertypeElement itd) {
        String[] parameterTypes = itd.getParameterTypes();
        if (parameterTypes == null) {
            return new char[0][];
        }
        char[][] simpleNames = new char[parameterTypes.length][];
        for (int i = 0; i < parameterTypes.length; i++) {
            try {
                simpleNames[i] = Signature.getSignatureSimpleName(Signature.getTypeErasure(parameterTypes[i].toCharArray()));
            } catch (Exception e) {
            }
        }
        return simpleNames;
    }

    private boolean isSubtypeOfSearchPattern(char[] targetTypeName,
            IntertypeElement itd, HierarchyResolver resolver) throws JavaModelException {
        char[] itdTargetTypeName = fullyQualifiedTargetTypeName(itd);
        if (CharOperation.equals(targetTypeName, itdTargetTypeName)) {
            return true;
        }
        if (resolver != null) {
            ReferenceBinding targetBinding = 
                    ((LookupEnvironment) ReflectionUtils.getPrivateField(HierarchyResolver.class, "lookupEnvironment", resolver)).
                    askForType(CharOperation.splitOn('.', itdTargetTypeName));
            if (targetBinding != null) {
                return resolver.subOrSuperOfFocus(targetBinding);
            }
        }
        return false;
    }

    private char[] fullyQualifiedTargetTypeName(IntertypeElement itd) {
        IType targetType = itd.findTargetType();
        char[] itdTargetTypeName = targetType == null ? new char[0] : targetType.getFullyQualifiedName().replace('$', '.').toCharArray();
        return itdTargetTypeName;
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
        Collection<SearchMatch> matches = visitor.doVisit(decl);
        // tentative matches are found elsewhere
//        matches.addAll(visitor.getTentativeMatches());
        return matches;
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

    static private char[] getQualName(FieldPattern pattern) {
        return (char[]) ReflectionUtils.getPrivateField(FieldPattern.class, "declaringQualification", pattern);
    }

    static private char[] getSimpleName(FieldPattern pattern) {
        return (char[]) ReflectionUtils.getPrivateField(FieldPattern.class, "declaringSimpleName", pattern);
    }
    
    private char[] getName(char[] qual, char[] name) {
        if (name == null) {
            return null;
        }
        
        char[] targetTypeName;
        if (qual != null && qual.length > 0) {
            qual = CharOperation.append(qual, '.');
            targetTypeName = CharOperation.append(qual, qual.length, name, 0, name.length);
        } else {
            targetTypeName = name;
        }
        // trim any \0 that are added to the end of target type
        int lastChar = targetTypeName.length;
        while (targetTypeName[lastChar-1] == '\0' && lastChar > 0) lastChar --;
        
        targetTypeName = CharOperation.subarray(targetTypeName, 0, lastChar);
        
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

    /**
     * convert ITD matches into the target types
     * Or ignore otherwise
     * @throws JavaModelException 
     */
    public IJavaElement filterJUnit4TestMatch(IJavaElement possibleTest) throws JavaModelException {
        // the results are returned as ResolvedSourceMethod, not an ITD
        // so must do a little work to get to the real ITD
        if (! (possibleTest instanceof IMethod)) {
            return possibleTest;
        }
        IJavaElement parent = possibleTest.getAncestor(IJavaElement.TYPE);
        if (parent instanceof AspectElement) {
            String itdName = possibleTest.getElementName().replace('$', '.');
            IntertypeElement matchingITD = findMatchingITD((AspectElement) parent, (IMethod) possibleTest, itdName);
            if (matchingITD != null) {
                return matchingITD.createMockDeclaration();
            }
        }
        return possibleTest;
    }

    private IntertypeElement findMatchingITD(AspectElement parent,
            IMethod possibleTest, String itdName) throws JavaModelException {
        IntertypeElement[] allITDs = parent.getITDs();
        for (IntertypeElement itd : allITDs) {
            if (itdName.equals(itd.getElementName()) && Arrays.equals(itd.getParameterTypes(), possibleTest.getParameterTypes())) {
                return itd;
            }
        }
        return null;
    }

}
