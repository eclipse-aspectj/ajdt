/*******************************************************************************
 * Copyright (c) 2008, 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.core.CompilationUnitElementInfo;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceMethodInfo;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;

/**
 * This class provides element info for SourceTypes that know about 
 * which inter-type declarations are declared on them.
 * 
 * @author andrew
 */
public class ITDAwareSourceTypeInfo extends SourceTypeElementInfo {
    
    // if this type is an interface and there are ITD methods or 
    // fields on it, then remove the interface flag on it
    // since compiler will think these declarations are static
    boolean shouldRemoveInterfaceFlag = false;
    
    
    private final static class ITDAwareSourceType extends SourceType {
        final ITDAwareSourceTypeInfo info;
        private ITDAwareSourceType(JavaElement parent, String name, ITDAwareSourceTypeInfo info) {
            super(parent, name);
            this.info = info;
        }

        public Object getElementInfo() throws JavaModelException {
            return info;
        }
    }
    private final static class ITDAwareAspectType extends AspectElement {
        final ITDAwareSourceTypeInfo info;
        private ITDAwareAspectType(JavaElement parent, String name, ITDAwareSourceTypeInfo info) {
            super(parent, name);
            this.info = info;
        }

        public Object getElementInfo() throws JavaModelException {
            return info;
        }
    }

    public ITDAwareSourceTypeInfo(SourceType type) throws JavaModelException {
        this((ISourceType) type.getElementInfo(), type);
    }

    public ITDAwareSourceTypeInfo(ISourceType toCopy, SourceType type) {
        this.handle = createITDAwareType(type, this);

        this.setFlags(toCopy.getModifiers());
        this.setSuperclassName(toCopy.getSuperclassName());
        this.setSuperInterfaceNames(toCopy.getInterfaceNames());
        this.setNameSourceEnd(toCopy.getNameSourceEnd());
        this.setNameSourceStart(toCopy.getNameSourceStart());
        this.setSourceRangeEnd(toCopy.getDeclarationSourceEnd());
        this.setSourceRangeStart(toCopy.getDeclarationSourceStart());
        try {
            ITypeParameter[] parameters = type.getTypeParameters();
            if (parameters != null) {
                this.typeParameters = new ITypeParameter[parameters.length];
                System.arraycopy(parameters, 0, this.typeParameters, 0, parameters.length);
            }
        } catch (JavaModelException e) {
        }
        
        IJavaElement[] children = augmentChildrenAndHierarchy(type);
        if (children != null) {
            this.setChildren(children);
        }
        
        try {
            // this ensures that itd aware content assist 
            // still works when there are large numbers ofannotations
            Object info = ((JavaElement) handle.getCompilationUnit()).getElementInfo();
            if (info != null && info instanceof CompilationUnitElementInfo) {
                ((CompilationUnitElementInfo) info).annotationNumber = 0;
            }
        } catch (JavaModelException e) {
        }
    }

    private IJavaElement[] augmentChildrenAndHierarchy(SourceType type) {
        try {
            IJavaElement[] origChildren = type.getChildren();
            IJavaElement[] newChildren = new IJavaElement[origChildren.length];
            boolean hasChanges = false;
            // recur through original children
            // ensure that children are also ITD aware
            for (int i = 0; i < origChildren.length; i++) {
                if (origChildren[i].getElementType() == IJavaElement.TYPE) {
                    final SourceType innerType = (SourceType) origChildren[i];
                    if (!(innerType instanceof ITDAwareSourceType)) {
                        ITDAwareSourceTypeInfo innerInfo = new ITDAwareSourceTypeInfo(innerType);
                        newChildren[i] = createITDAwareType(innerType, innerInfo);
                        hasChanges = true;
                        continue;
                    } 
                }
                newChildren[i] = origChildren[i];
            }
            
            
            
            List<IJavaElement> augmentedChildren = getITDs(type);
            if (type instanceof AspectElement) {
                augmentedChildren.add(createAspectOf((AspectElement) this.handle));
            }
            if (augmentedChildren.size() > 0 || hasChanges) {
                IJavaElement[] allChildren = new IJavaElement[origChildren.length + augmentedChildren.size()];
                System.arraycopy(newChildren, 0, allChildren, 0, newChildren.length);
                int i = origChildren.length;
                for (IJavaElement elt : augmentedChildren) {
                    allChildren[i++] = elt;
                }
                return allChildren;
            } else {
                return origChildren;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    private List<IJavaElement> getITDs(SourceType type) throws JavaModelException {
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(type);
        if (model.hasModel()) {
            List<IJavaElement> itds = new ArrayList<IJavaElement>();
            List<IJavaElement> rels = model.getRelationshipsForElement(type, AJRelationshipManager.ASPECT_DECLARATIONS);
            
            List<IMethod> childMethods = null;

            for (IJavaElement ije : rels) {
                if (ije instanceof IntertypeElement) {
                    IntertypeElement elt = (IntertypeElement) ije;
                    IMember member = elt.createMockDeclaration(type);
                    // null if the ITD doesn't exist in the AspectJ hierarchy
                    // will happen if the Java side has partial compilation 
                    // and aspectj side does not
                    if (member != null) {
                        
                        // should not add this ITD if it is a duplicate
                        // of another ITD
                        if (!isAlreadyAnITD(itds, member)) {
                            continue;
                        }
                        
                        itds.add(member);

                        // additional processing for interfaces
                        if (handle.isInterface()) {
                            shouldRemoveInterfaceFlag = true;
                            
                            if (member.getElementType() == IJavaElement.FIELD) {
                                // Bug 262969
                                // Interfaces can't have fields, so ignore
                                // Reconciling errors occur if an ITD field is
                                // referenced outside of the aspect that declares them, 
                                // but only if the declared type of the object is an interface.
                                itds.remove(member);
                            } else if (member.getElementType() == IJavaElement.METHOD) {
                                // now look to see if this ITD a method that provides
                                // a default implementation for an interface method
                                // use IMethod.isSimilar
                                if (childMethods == null) {
                                    childMethods = (List<IMethod>) type.getChildrenOfType(IJavaElement.METHOD);
                                }
                                for (IMethod method : childMethods) {
                                    if (method.isSimilar((IMethod) member)) {
                                        itds.remove(member);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else if (ije instanceof DeclareElement) {
                    DeclareElement elt = (DeclareElement) ije;
                    
                    // use createElementInfo, not getElementInfo because 
                    // we don't want it cached
                    DeclareElementInfo info = (DeclareElementInfo) elt.createElementInfo();
                    if (info == null || info.getAJKind() != Kind.DECLARE_PARENTS) {
                        continue;
                    }
                    
                    char[][] newSupers = info.getTypes();
                    augmentHierarchy(newSupers);
                } else if (ije instanceof AspectElement) {
                    // likely a declare parents instantiated in a concrete aspect, but declared in a super aspect
                    IProgramElement ipe = model.javaElementToProgramElement(ije);
                    Map<String, List<String>> declareParentsMap = ipe.getDeclareParentsMap();
                    if (declareParentsMap != null) {
                        augmentHierarchy(declareParentsMap.get(type.getFullyQualifiedName()));
                    }
                }
            }
            return itds;
        } 
        return new LinkedList<IJavaElement>();
    }

    private void augmentHierarchy(List<String> newSupers) {
        if (newSupers != null) {
            char[][] newSupersArr = new char[newSupers.size()][];
            int i = 0;
            for (String newSuper : newSupers) {
                newSupersArr[i++] = newSuper.toCharArray();
            }
            augmentHierarchy(newSupersArr);
        }
    }

    /**
     * @param newSupers
     */
    private void augmentHierarchy(char[][] newSupers) {
        if (newSupers != null) {
            for (char[] newSuper : newSupers) {
                if (isClass(newSuper)) {
                    this.setSuperclassName(newSuper);
                } else {
                    // assume an interface
                    char[][] origInterfaces = this.getInterfaceNames();
                    char[][] newInterfaces;
                    if (origInterfaces == null) {
                        newInterfaces = new char[][] { newSuper };
                    } else {
                        newInterfaces = new char[origInterfaces.length + 1][];
                        System.arraycopy(origInterfaces, 0, newInterfaces, 0, origInterfaces.length);
                        newInterfaces[origInterfaces.length] = newSuper;
                    }
                    setSuperInterfaceNames(newInterfaces);
                }
            }
        }
    }

    private boolean isAlreadyAnITD(List<IJavaElement> itds, IMember member) {
        boolean shouldAdd = true;
        
        if (member.getElementType() == IJavaElement.FIELD) {
            for (IJavaElement itdElt : itds) {
                if (itdElt instanceof IField) {
                    IField itdField = (IField) itdElt;
                    if (member.getElementName().equals(itdField.getElementName())) {
                        // may not be same type, but we want to avoid conflicts, so remove
                        shouldAdd = false;
                        break;
                    }
                }
            }
        } else if (member.getElementType() == IJavaElement.METHOD) {
            for (IJavaElement itdElt : itds) {
                if (itdElt instanceof IMethod) {
                    IMethod itdMethod = (IMethod) itdElt;
                    if (itdMethod.isSimilar((IMethod) member)) {
                        shouldAdd = false;
                        break;
                    }
                }
            }
        }
        return shouldAdd;
    }

    
    /**
     * create the aspectOf method for 
     */
    private SourceMethod createAspectOf(AspectElement parent) {
        return new SourceMethod(
                (JavaElement) parent, 
                "aspectOf", 
                new String[0]) {
            protected Object createElementInfo() {
                return new SourceMethodInfo() {
                    @Override
                    public int getModifiers() {
                        return Flags.AccPublic | Flags.AccStatic;
                    }
                    
                    @Override
                    public char[] getReturnTypeName() {
                        return parent.getElementName().toCharArray();
                    }
                };
            }
            public boolean exists() {
                return true;
            }
        };

    }
    
    /**
     * True if a class false if an interface or doesn't exist
     * @param qualifiedName
     * @return
     */
    private boolean isClass(char[] qualifiedName) {
        try {
            IType type = this.handle.getJavaProject().findType(String.valueOf(qualifiedName), (IProgressMonitor) null);
            return type != null && type.isClass();
        } catch (JavaModelException e) {
            return false;
        }
    }
    
    private SourceType createITDAwareType(SourceType type, ITDAwareSourceTypeInfo info) {
        if (type instanceof AspectElement) {
            return new ITDAwareAspectType((JavaElement) type.getParent(), type.getElementName(), info);
        } else {
            return new ITDAwareSourceType((JavaElement) type.getParent(), type.getElementName(), info);
        }
    }
    
    public IJavaElement[] getChildren() {
        return super.getChildren();
    }
    
                        /* AJDT 1.7 */
    public void setChildren(IJavaElement[] children) {
        this.children = children;
    }
    
}
