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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElementInfo;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.codeassist.ISelectionRequestor;

/**
 * @author Andrew Eisenberg
 * @created Apr 28, 2009
 * 
 * A selection requestor that knows about ITDs
 */
public class ITDAwareSelectionRequestor implements ISelectionRequestor {
    
    private AJProjectModelFacade model;
    private ICompilationUnit currentUnit;
    private Set /* IJavaElement */ accepted;
    
    public ITDAwareSelectionRequestor(AJProjectModelFacade model, ICompilationUnit currentUnit) {
        this.model = model;
        this.currentUnit = currentUnit;
        this.accepted = new HashSet();
    }

    public void acceptError(CategorizedProblem error) {
        // can ignore
    }

    public void acceptField(char[] declaringTypePackageName,
            char[] declaringTypeName, char[] name, boolean isDeclaration,
            char[] uniqueKey, int start, int end) {
        try {
            IType targetType = currentUnit.getJavaProject().findType(toQualifiedName(declaringTypePackageName, declaringTypeName));
            List /*IJavaElement*/ itds = ensureModel(targetType).getRelationshipsForElement(targetType, AJRelationshipManager.ASPECT_DECLARATIONS);
            for (Iterator iterator = itds.iterator(); iterator.hasNext();) {
                IJavaElement elt = (IJavaElement) iterator.next();
                if (matchedField(elt, name)) {
                    accepted.add(elt);
                }
            }
        } catch (JavaModelException e) {
        }
    }

    public void acceptMethod(char[] declaringTypePackageName,
            char[] declaringTypeName, String enclosingDeclaringTypeSignature,
            char[] selector, char[][] parameterPackageNames,
            char[][] parameterTypeNames, String[] parameterSignatures,
            char[][] typeParameterNames, char[][][] typeParameterBoundNames,
            boolean isConstructor, boolean isDeclaration, char[] uniqueKey,
            int start, int end) {
        try {
            IType targetType = currentUnit.getJavaProject().findType(toQualifiedName(declaringTypePackageName, declaringTypeName));
            List /*IJavaElement*/ itds = ensureModel(targetType).getRelationshipsForElement(targetType, AJRelationshipManager.ASPECT_DECLARATIONS);
            for (Iterator iterator = itds.iterator(); iterator.hasNext();) {
                IJavaElement elt = (IJavaElement) iterator.next();
                if (matchedMethod(elt, selector, parameterSignatures)) {
                    accepted.add(elt);
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
        // can ignore
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

    private boolean matchedMethod(IJavaElement elt, char[] selector, 
            String[] parameterSignatures) throws JavaModelException {
        if (elt instanceof IntertypeElement) {
            IntertypeElement itd = (IntertypeElement) elt;
            IntertypeElementInfo info = (IntertypeElementInfo) 
                    ((IntertypeElement) elt).getElementInfo();
            if (info.getAJKind() == Kind.INTER_TYPE_METHOD ||
                    info.getAJKind() == Kind.INTER_TYPE_CONSTRUCTOR) {
                if (extractName(elt.getElementName()).equals(new String(selector))) {
                    String[] pTypes = itd.getParameterTypes();
                    if (pTypes != null && parameterSignatures!= null &&
                            pTypes.length == parameterSignatures.length) {
                        for (int i = 0; i < pTypes.length; i++) {
                            if (!pTypes[i].equals(parameterSignatures[i])) {
                                return false;
                            }
                        }
                        // all param sigs the same
                        return true;
                    } else { 
                        // both have 0 paramss
                        return pTypes == null && parameterSignatures == null;
                    }
                }
            }
        }
        return false;
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
        return split.length > 1 ? split[split.length-1] : name;
    }


    public IJavaElement[] getElements() {
        return (IJavaElement[]) accepted.toArray(new IJavaElement[accepted.size()]);
    }
    
}
