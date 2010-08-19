/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *     Kris De Volder, Andrew Eisenberg - Refactoring: make abstract and
 *                                        split of subclasses for IField and IMethod 
 *                                        interfaces
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import static org.eclipse.ajdt.core.javaelements.AspectElement.JEM_ITD_FIELD;
import static org.eclipse.ajdt.core.javaelements.AspectElement.JEM_ITD_METHOD;

import java.util.List;

import org.aspectj.ajdt.internal.compiler.ast.InterTypeDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeFieldDeclaration;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * @author Luzius Meisser
 * @author andrew
 * @author kdvolder
 */
public abstract class IntertypeElement extends AspectJMemberElement {

    private IType targetTypeCache = null;

    /**
     * Factory method to create an intertypeElement. 
     * @param jemDelimter Should be one of the JEM_ITD_FIELD or JEM_ITD_METHOD (see {@link AspectElement}) 
     */
    public static IntertypeElement create(char jemDelimeter, JavaElement parent,
 		   String name, String[] parameters) {
    	if (jemDelimeter == JEM_ITD_FIELD) {
    		Assert.isTrue(parameters==null || parameters.length==0, "Fields shouldn't have parameters!");
    		return new FieldIntertypeElement(parent, name);
    	}
    	else if (jemDelimeter == JEM_ITD_METHOD) {
    		return new MethodIntertypeElement(parent, name, parameters);
    	}
    	else throw new IllegalArgumentException("jemDelimeter should be one of JEM_ITD_FIELD or JEM_ITD_METHOD");
    }
    
    public char getJemDelimeter() {
    	return getHandleMementoDelimiter();
    }
    
    public static char getJemDelimter(InterTypeDeclaration decl) {
    	if (decl instanceof InterTypeFieldDeclaration) 
    		return JEM_ITD_FIELD;
    	else /* constructor or method */
    		return JEM_ITD_METHOD;
    }
    
    protected IntertypeElement(JavaElement parent, String name, String[] parameterTypes) {
        super(parent, name, parameterTypes);
    }
    
    public char[] getTargetType() throws JavaModelException{
        return ((IntertypeElementInfo)getElementInfo()).getTargetType();
    }
    
    protected Object createElementInfo() {
        IntertypeElementInfo info = new IntertypeElementInfo();
        
        IProgramElement ipe = AJProjectModelFactory.getInstance().getModelForJavaElement(this).javaElementToProgramElement(this);
        if (ipe != IHierarchy.NO_STRUCTURE) {
            info.setAJExtraInfo(ipe.getExtraInfo());
            info.setName(name.toCharArray());
            info.setAJKind(ipe.getKind());
            info.setAJModifiers(ipe.getModifiers());
            info.setFlags(ipe.getRawModifiers());
            info.setDeclaredModifiers(info.getModifiers());
            info.setAJAccessibility(ipe.getAccessibility());
            ISourceLocation sourceLocation = ipe.getSourceLocation();
            info.setSourceRangeStart(sourceLocation.getOffset());
            info.setNameSourceStart(sourceLocation.getOffset());
            info.setNameSourceEnd(sourceLocation.getOffset() + ipe.getName().length());
            info.setConstructor(info.getAJKind() == IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR);
            info.setArgumentNames(CoreUtils.listStringsToCharArrays(ipe.getParameterNames()));
            info.setArgumentTypeNames(CoreUtils.listCharsToCharArrays(ipe.getParameterTypes()));  // hmmmm..don't think this is working
            info.setReturnType(ipe.getCorrespondingType(false).toCharArray());
            info.setQualifiedReturnType(ipe.getCorrespondingType(true).toCharArray());
        } else {
            // no successful build yet, we don't know the contents
            info.setName(name.toCharArray());
            info.setAJKind(IProgramElement.Kind.ERROR);
        }
        return info;
    }
    
    /**
     * override this cached info because it was before we had a successful build
     */
    public Object getElementInfo() throws JavaModelException {
        IntertypeElementInfo info = (IntertypeElementInfo) super.getElementInfo();
        if (info.getAJKind() == IProgramElement.Kind.ERROR &&
                AJProjectModelFactory.getInstance().getModelForJavaElement(this).hasModel()) {
            // we have structure model now, but didn't before
            info = (IntertypeElementInfo) openWhenClosed(createElementInfo(), null);
        }
        return info;
    }

    public char[] getQualifiedReturnType() throws JavaModelException {
        IntertypeElementInfo info = (IntertypeElementInfo) getElementInfo();
        char[] returnTypeName = info.getQualifiedReturnType();
        if (returnTypeName == null) {
            returnTypeName = getQualifiedReturnTypeName(info);
            info.setQualifiedReturnType(returnTypeName);
        }
        return Signature.createCharArrayTypeSignature(returnTypeName, false);
    }

    /* AJDT 1.7 */
    protected Integer getParamNum() {
        return new Integer(IntertypeElement.this.getQualifiedParameterTypes().length);
    }
    /** 
     * may return null if target type is not found
     * @return
     */
    public IType findTargetType() {
        if (targetTypeCache == null) {
            AJProjectModelFacade model = AJProjectModelFactory
                .getInstance().getModelForJavaElement(this);
            
            // either the relationships are a single class declaration
            // or this is an interface with one or more class declarations
            List<IJavaElement> rels = model.getRelationshipsForElement(this,
                    AJRelationshipManager.DECLARED_ON);
            if (rels.size() == 1 && rels.get(0) instanceof IType) {
                targetTypeCache = (IType) rels.get(0);
            } else if (rels.size() > 1) {
                // we have an interface and several concreate types
                // we want to return the interface type
                for (IJavaElement rel : rels) {
                    try {
                        if (rel instanceof IType && ((IType) rel).isInterface()) {
                            targetTypeCache = (IType) rel;
                            break;
                        }
                    } catch (JavaModelException e) { }
                }
            }
        }
        return targetTypeCache;
    }
    
    /**
     * Shortcut for {@link #createMockDeclaration(IType)} when 
     * the target type is not known in advance
     */
    public IMember createMockDeclaration() {
        IType target = findTargetType();
        if (target != null) {
            return createMockDeclaration(target);
        } else {
            return null;
        }
    }
    
    /**
     * note that we set the accessibility to public because the modifiers 
     * apply to the ITD element, not the target declaration.
     * We are purposely being too liberal with the modifiers so that
     * we don't get accessibility problems when an ITD is declared private
     * and is used in the Aspect CU that declares it.
     * 
     * @param parent the type that this element declares on
     * @return a mock element representing the element that was introduced
     */
    public abstract IMember createMockDeclaration(IType parent);
    
    /**
     * Estimate the source range corresponding to the target type
     * Assumes that there are no spaces or other non-word chars
     * (other than '.') in the target type name 
     * @return
     * @throws JavaModelException 
     */
    public ISourceRange getTargetTypeSourceRange() throws JavaModelException {
        ISourceRange nameRange = getNameRange();
        int targetTypeEnd = nameRange.getOffset() -1;
        int targetTypeStart = nameRange.getOffset() + nameRange.getLength() - name.length();
        return new SourceRange(targetTypeStart, targetTypeEnd - targetTypeStart);
    }

    public String getTargetName() {
        String[] split = name.split("\\.");
        return split.length > 1 ? split[split.length-1] : name;
    }
    
    public String[] getQualifiedParameterTypes() {
        IProgramElement ipe = AJProjectModelFactory.getInstance().getModelForJavaElement(this).javaElementToProgramElement(this);
        if (ipe != IHierarchy.NO_STRUCTURE) {
            return CoreUtils.listAJSigToJavaSig(ipe.getParameterSignatures());
        } else {
            return getParameterTypes();
        }
    }
    
    char[] getQualifiedReturnTypeName(IntertypeElementInfo info) {
        char[] returnType = info.getQualifiedReturnType();
        if (returnType != null) {
            return returnType;
        }
        
        IProgramElement ipe = AJProjectModelFactory.getInstance().getModelForJavaElement(this).javaElementToProgramElement(this);
        if (ipe != IHierarchy.NO_STRUCTURE) {
            return ipe.getCorrespondingType(true).toCharArray();
        } else {
            return info.getReturnTypeName();
        }
    }
}