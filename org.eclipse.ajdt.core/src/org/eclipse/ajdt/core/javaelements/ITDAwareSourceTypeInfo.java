/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others.
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.core.JavaElement;
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
    
    private interface ITDAwareType { }
    
    private final class ITDAwareSourceType extends SourceType implements ITDAwareType {
        final ITDAwareSourceTypeInfo info;
        private ITDAwareSourceType(JavaElement parent, String name, ITDAwareSourceTypeInfo info) {
            super(parent, name);
            this.info = info;
        }

        public Object getElementInfo() throws JavaModelException {
            return info;
        }
    }
    private final class ITDAwareAspectType extends AspectElement implements ITDAwareType {
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
            this.typeParameters = new ITypeParameter[parameters.length];
            System.arraycopy(parameters, 0, this.typeParameters, 0, parameters.length);
        } catch (JavaModelException e) {
        }
        
        IJavaElement[] children = augmentChildrenAndHierarchy(type);
        if (children != null) {
            this.setChildren(children);
        }
        
        if (shouldRemoveInterfaceFlag) {
            setFlags(removeInterfaceFlag(getModifiers()));
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
            
            
            List itdChildren = getITDs(type);
            if (itdChildren.size() > 0 || hasChanges) {
                IJavaElement[] allChildren = new IJavaElement[origChildren.length + itdChildren.size()];
                System.arraycopy(newChildren, 0, allChildren, 0, newChildren.length);
                int i = origChildren.length;
                for (Iterator childIter = itdChildren.iterator(); childIter.hasNext();) {
                    IJavaElement elt = (IJavaElement) childIter.next();
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
    
    private List/*IJavaElement*/ getITDs(SourceType type) throws JavaModelException {
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(type);
        if (model.hasModel()) {
            List/*IJavaElement*/ itds = new ArrayList();
            List/*IJavaElement*/ rels = model.getRelationshipsForElement(type, AJRelationshipManager.ASPECT_DECLARATIONS);
            for (Iterator relIter = rels.iterator(); relIter.hasNext();) {
                IJavaElement ije = (IJavaElement) relIter.next();
                if (ije instanceof IntertypeElement) {
                    IntertypeElement elt = (IntertypeElement) ije;
                    IMember member = elt.createMockDeclaration(type);
                    // null if the ITD doesn't exist in the AspectJ hierarchy
                    // will happen if the Java side has partial compilation 
                    // and aspectj sode does not
                    if (member != null) { 
                        itds.add(member);

                        // Interfaces will show ITD methods and fields 
                        // as being static.  Convert interfaces to 
                        // classes and this goes away.
                        // but this conversion causes other problems
                        // Since stored as a class, it is not actually inherited
                        // and causes class cast exceptions 
                        // See AJCompilationUnitProblemFinder.isARealProblem()
                        if (handle.isInterface()) {
                            shouldRemoveInterfaceFlag = true;
                        }
                    }
                } else if (ije instanceof DeclareElement) {
                    DeclareElement elt = (DeclareElement) ije;
                    
                    // use createElementInfo, not getElementInfo because 
                    // we don't want it cached
                    DeclareElementInfo info = (DeclareElementInfo) elt.createElementInfo();
                    if (info.isExtends()) {
                        this.setSuperclassName(info.getType());
                    } else if (info.isImplements()) {
                        char[][] origInterfaces = this.getInterfaceNames();
                        char[][] itdInterfaces = info.getTypes();
                        char[][] newInterfaces;
                        if (origInterfaces == null) {
                            newInterfaces = itdInterfaces;
                        } else if (itdInterfaces == null) {
                            newInterfaces = origInterfaces;
                        } else {
                            newInterfaces = new char[origInterfaces.length + info.getTypes().length][];
                            System.arraycopy(origInterfaces, 0, newInterfaces, 0, origInterfaces.length);
                            System.arraycopy(itdInterfaces, 0, newInterfaces, origInterfaces.length, itdInterfaces.length);
                        }
                        setSuperInterfaceNames(newInterfaces);
                    }
                }
            }
            return itds;
        } 
        return Collections.EMPTY_LIST;
    }

    private SourceType createITDAwareType(SourceType type, ITDAwareSourceTypeInfo info) {
        if (type instanceof AspectElement) {
            return new ITDAwareAspectType((JavaElement) type.getParent(), type.getElementName(), info);
        } else {
            return new ITDAwareSourceType((JavaElement) type.getParent(), type.getElementName(), info);
        }
    }
    
    private int removeInterfaceFlag(int flags) {
        return flags & (~ClassFileConstants.AccInterface);
    }
    
}
