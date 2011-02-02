/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.core.text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser.Replacement;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElementInfo;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.codeassist.ISelectionRequestor;

/**
 * @author Andrew Eisenberg
 * @created Apr 28, 2009
 * 
 * A selection requestor that knows about ITDs.
 * 
 * If the carat is on the ITD itself, this cannot be selected...
 * Need some different logic to deal with this
 */
public class ITDAwareSelectionRequestor implements ISelectionRequestor {
    
    private AJProjectModelFacade model;
    private ICompilationUnit currentUnit;
    private Set<IJavaElement> accepted;
    
    private ArrayList<Replacement> replacements;
    
    public ITDAwareSelectionRequestor(AJProjectModelFacade model, ICompilationUnit currentUnit) {
        this.model = model;
        this.currentUnit = currentUnit;
        this.accepted = new HashSet<IJavaElement>();
    }

    public void setReplacements(ArrayList<Replacement> replacements) {
        this.replacements = replacements;
    }
    
    public void acceptError(CategorizedProblem error) {
        // can ignore
    }

    public void acceptField(char[] declaringTypePackageName,
            char[] declaringTypeName, char[] name, boolean isDeclaration,
            char[] uniqueKey, int start, int end) {
        try {
            IType targetType = currentUnit.getJavaProject().findType(toQualifiedName(declaringTypePackageName, declaringTypeName));
            if (targetType == null) {
                // type couldn't be found.  this is really some kind of problem
                return;
            }
            List<IJavaElement> itds = ensureModel(targetType).getRelationshipsForElement(targetType, AJRelationshipManager.ASPECT_DECLARATIONS);
            for (IJavaElement elt : itds) {
                if (matchedField(elt, name)) {
                    accepted.add(elt);
                    return;
                }
            }
            
            // if we are selecting inside of an ITD and the field being matched is a regular field, we find it here.
            IntertypeElement itd = maybeGetITD(start);
            if (itd != null) {
                IField field = targetType.getField(String.valueOf(name));
                if (field.exists()) {
                    accepted.add(field);
                }
            }
        } catch (JavaModelException e) {
        }
    }

    private IntertypeElement maybeGetITD(int pos) throws JavaModelException {
        if (replacements != null && currentUnit instanceof AJCompilationUnit) {
            IJavaElement elt = currentUnit.getElementAt(AspectsConvertingParser.translatePositionToBeforeChanges(pos, replacements));
            if (elt instanceof IntertypeElement) {
                return (IntertypeElement) elt;
            }
        }
        return null;
    }

    public void acceptMethod(char[] declaringTypePackageName,
            char[] declaringTypeName, String enclosingDeclaringTypeSignature,
            char[] selector, char[][] parameterPackageNames,
            char[][] parameterTypeNames, String[] parameterSignatures,
            char[][] typeParameterNames, char[][][] typeParameterBoundNames,
            boolean isConstructor, boolean isDeclaration, char[] uniqueKey,
            int start, int end) {
        String[] simpleParameterSigs;
        if (parameterSignatures != null) {
            simpleParameterSigs = new String[parameterSignatures.length];
            for (int i = 0; i < parameterSignatures.length; i++) {
                simpleParameterSigs[i] = toSimpleName(parameterSignatures[i]);
            }
        } else {
            simpleParameterSigs = null;
        }
        
        try {
            IType targetType = currentUnit.getJavaProject().findType(toQualifiedName(declaringTypePackageName, declaringTypeName));
            if (targetType != null) {
                List<IJavaElement> itds = ensureModel(targetType).getRelationshipsForElement(targetType, AJRelationshipManager.ASPECT_DECLARATIONS);
                for (IJavaElement elt : itds) {
                    if (matchedMethod(elt, selector, simpleParameterSigs)) {
                        accepted.add(elt);
                        return;
                    }
                }
                
                IntertypeElement itd = maybeGetITD(start);
                if (itd != null) {
                    // if we are selecting inside of an ITD and the method being matched is a regular method, we find it here.
                    IMethod method = targetType.getMethod(String.valueOf(selector), parameterSignatures);
                    if (method.exists()) {
                        accepted.add(method);
                    }
                    
                    // still need to determine if the ITD declaration itself is being selected
                }
            }
        } catch (JavaModelException e) {
        }
    }

    public void acceptTypeParameter(char[] declaringTypePackageName,
            char[] declaringTypeName, char[] typeParameterName,
            boolean isDeclaration, int start, int end) {
        // can ignore
    }

    public void acceptMethodTypeParameter(char[] declaringTypePackageName,
            char[] declaringTypeName, char[] selector, int selectorStart,
            int selectorEnd, char[] typeParameterName, boolean isDeclaration,
            int start, int end) {
        // can ignore
    }

    public void acceptPackage(char[] packageName) {
        // can ignore
    }

    public void acceptType(char[] packageName, char[] annotationName,
            int modifiers, boolean isDeclaration, char[] genericTypeSignature,
            int start, int end) {
        try {
            int origStart = AspectsConvertingParser.translatePositionToBeforeChanges(start, replacements);
            int origEnd = AspectsConvertingParser.translatePositionToBeforeChanges(end, replacements);
            IntertypeElement itd = maybeGetITD(origStart);
            if (itd != null) {
                // find out if we are selecting the target type name part of an itd
                // itd.getNameRange() returns the range of the name, but excludes the target type.  Must subtract from there.  
                // Make assumption that there are no spaces
                // or comments between '.' and the rest of the name
                ISourceRange nameRange = itd.getNameRange();
                
                String itdName = itd.getElementName();
                int typeNameLength = Math.max(itdName.lastIndexOf('.'), 0);
                String typeName = itdName.substring(0, typeNameLength);
                
                int typeNameStart;
                if (itd.getAJKind() == Kind.INTER_TYPE_CONSTRUCTOR) {
                    typeNameStart = nameRange.getOffset();
                } else {
                    typeNameStart = nameRange.getOffset() - 1 - typeName.length();
                }
                // now determine if the selected section is completely contained within the type name
                if (contained(origStart, origEnd, typeNameStart, typeNameStart + typeNameLength)) {
                    IType targetType = itd.findTargetType();
                    if (targetType != null && targetType.getFullyQualifiedName('.').equals(toQualifiedName(packageName, annotationName))) {
                        accepted.add(targetType);
                    }
                }
            }
        } catch (JavaModelException e) {
        }
    }
    
    private boolean contained(int selStart, int selEnd, int typeNameStart,
            int typeNameEnd) {
        return selStart >= typeNameStart && selEnd <= typeNameEnd;
    }

    private AJProjectModelFacade ensureModel(IJavaElement elt) {
        try {
            if (model.getProject().equals(elt.getJavaProject().getProject())) {
                return model;
            } else {
                return AJProjectModelFactory.getInstance().getModelForJavaElement(elt);
            }
        } catch (Exception e) {
            // catch NPE if elt is null, or core exception if not stored to disk yet.
            return model;
        }
    }

    private boolean matchedField(IJavaElement elt, char[] name) throws JavaModelException {
        if (elt instanceof IntertypeElement) {
            IntertypeElementInfo info = (IntertypeElementInfo) 
            ((IntertypeElement) elt).getElementInfo();
            if (info.getAJKind() == Kind.INTER_TYPE_FIELD) {
                if (extractName(elt.getElementName()).equals(new String(name))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    // This method checks to see if the selected method matches the 
    // method we are searching for.  However, we take some shortcuts here.
    // Rather than looking at qualified, resolved type signatures of both methods
    // we look at the simple type names of all paramters.  The reason for this 
    // is that the parameters on the elt argument may be unresolved (ie- simple names)
    // whereas the paramter signatures passed in may be resolved (ie- fully qualified).
    // Thus, there could be a match that wouldn't be found.
    // The solution is to compare simple names only.  The danger is that there might be false
    // positives with the match, but they would be rare and not particularly worrisome if 
    // they exist.
    private boolean matchedMethod(IJavaElement elt, char[] selector, 
            String[] simpleParameterSigs) throws JavaModelException {
        if (elt instanceof IntertypeElement) {
            IntertypeElement itd = (IntertypeElement) elt;
            IntertypeElementInfo info = (IntertypeElementInfo) 
            ((IntertypeElement) elt).getElementInfo();
            if (info.getAJKind() == Kind.INTER_TYPE_METHOD ||
                    info.getAJKind() == Kind.INTER_TYPE_CONSTRUCTOR) {
                if (extractName(elt.getElementName()).equals(String.valueOf(selector))) {
                    String[] itdParameterSigs = itd.getParameterTypes();
                    if (itdParameterSigs == null || simpleParameterSigs == null) {
                        return (itdParameterSigs == null || itdParameterSigs.length == 0) && 
                               (simpleParameterSigs == null || simpleParameterSigs.length == 0);
                    }
                    
                    if (itdParameterSigs.length == simpleParameterSigs.length) {
                        for (int i = 0; i < itdParameterSigs.length; i++) {
                            String simple = toSimpleName(itdParameterSigs[i]);
                            if (! simple.equals(simpleParameterSigs[i])) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private String toSimpleName(String signature) {
        String simple = Signature.getSignatureSimpleName(signature);
        int typeParamIndex = simple.indexOf('<');
        if (typeParamIndex > 0) {
            simple = simple.substring(typeParamIndex+1);
        }
        int dotIndex = simple.indexOf('.');
        if (dotIndex > 0) {
            simple = simple.substring(dotIndex+1);
        }
        return simple;
    }
    


    private String toQualifiedName(char[] declaringTypePackageName,
            char[] declaringTypeName) {
        StringBuffer sb = new StringBuffer();
        sb.append(declaringTypePackageName);
        if (sb.length() > 0) {
            sb.append(".");
        }
        sb.append(declaringTypeName);
        return sb.toString();
    }

    private String extractName(String name) {
        
        String[] split = name.split("\\.");
        if (split.length <= 1) {
            return name;
        }
        int splitLength = split.length;
        // maybe this is a constructor ITD
        if (name.endsWith("_new")) {
            if ((split[splitLength-2] + "_new").equals(split[splitLength-1])) {
                return split[splitLength-2];
            }
        } else if (name.endsWith("$new")) {
            return name.substring(0, name.length()-"$new".length());
        }
        return split[splitLength-1];
    }

    public IJavaElement[] getElements() {
        return (IJavaElement[]) accepted.toArray(new IJavaElement[accepted.size()]);
    }

    /**
     * Converts from a 'Q' kind of signature to an 'L' kind of signature.
     * This is actually quite tricky.
     * @param signature
     * @return
     */
    // not used any more.  can likely delete
//    private String resolveSignture(IType type, String signature) {
//        String simple = Signature.getSignatureSimpleName(signature);
//        String qual = Signature.getSignatureQualifier(signature);
//        String[] typeParams = Signature.getTypeArguments(signature);
//        int arrayCount = Signature.getArrayCount(signature);
//        
//        String fullyQual = qual != null && qual.length() > 0 ? qual + '.' + simple : simple;
//        try {
//            String[][] resolvedArr = type.resolveType(fullyQual);
//            if (resolvedArr != null && resolvedArr.length > 0) {
//                String resolved = (resolvedArr[0][0].length() > 0) ? 
//                        resolvedArr[0][0] + "." + resolvedArr[0][1] : resolvedArr[0][1]; 
//                String newSig = Signature.createTypeSignature(resolved, true);
//                if (arrayCount > 0) {
//                    newSig = Signature.createArraySignature(newSig, arrayCount);
//                }
//                
//                // uggh...don't know if this will work
//                if (typeParams != null && typeParams.length > 0) {
//                    newSig = newSig.substring(0, newSig.length()-1) + "<";
//                    for (int i = 0; i < typeParams.length; i++) {
//                        typeParams[i] = resolveSignture(type, typeParams[i]);
//                        newSig = newSig.substring(0, newSig.length()-1) + typeParams[i];
//                    }
//                    newSig += ">;";
//                }
//                return newSig;
//            }
//        } catch (JavaModelException e) {
//        }
//        
//        // couldn't resolve
//        return signature;
//    }
//
//    /**
//     * @param signature
//     * @return true if this is an unresolved signature
//     */
//    private boolean isUnresolvedSignature(String signature) {
//        int typeStart = 0;
//        while (signature.length() < typeStart && signature.charAt(typeStart) == '[') {
//            typeStart++;
//        }
//        return signature.charAt(typeStart) == 'Q';
//    }
}
