/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;


import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceConstructorInfo;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceFieldElementInfo;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceMethodInfo;

/**
 * @author Luzius Meisser
 */
public class IntertypeElement extends AspectJMemberElement {
	public IntertypeElement(JavaElement parent, String name, String[] parameterTypes) {
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

	
	/**
	 * @see JavaElement#getHandleMemento()
	 */
	protected char getHandleMementoDelimiter() {
		return AspectElement.JEM_ITD;
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
	public IMember createMockDeclaration(IType parent) {
	    try {
            final IntertypeElementInfo info = (IntertypeElementInfo) getElementInfo();
            boolean isConstructor = info.getAJKind() == IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR;
            boolean isMethod = info.getAJKind() == IProgramElement.Kind.INTER_TYPE_METHOD;
            boolean isField = info.getAJKind() == IProgramElement.Kind.INTER_TYPE_FIELD;
            
            if (isConstructor) {
                IMethod itd = new SourceMethod(
                        (JavaElement) parent, 
                        parent.getElementName(), 
                        this.getQualifiedParameterTypes()) {
                    protected Object createElementInfo() {
                        ITDSourceConstructorElementInfo newInfo = new ITDSourceConstructorElementInfo(IntertypeElement.this);
                        newInfo.setChildren(info.getChildren());
                        newInfo.setFlags(CompilationUnitTools.getPublicModifierCode(info));
                        newInfo.setNameSourceEnd(info.getNameSourceEnd());
                        newInfo.setNameSourceStart(info.getNameSourceStart());
                        newInfo.setArgumentNames(info.getArgumentNames());
                        newInfo.setSourceRangeStart(info.getSourceRange().getOffset());
                        newInfo.setSourceRangeEnd(info.getSourceRange().getOffset() + info.getSourceRange().getLength());
                        return newInfo;

                    }
                    public boolean exists() {
                        return true;
                    }
                };
                return itd;
            } else if (isMethod) {
                IMethod itd = new SourceMethod(
                        (JavaElement) parent, 
                        extractName(), 
                        this.getQualifiedParameterTypes()) {
                    protected Object createElementInfo() {
                        ITDSourceMethodElementInfo newInfo = new ITDSourceMethodElementInfo(IntertypeElement.this);
                        newInfo.setChildren(info.getChildren());
                        newInfo.setReturnType(getQualifiedReturnTypeName(info));
                        newInfo.setFlags(CompilationUnitTools.getPublicModifierCode(info));
                        newInfo.setNameSourceEnd(info.getNameSourceEnd());
                        newInfo.setNameSourceStart(info.getNameSourceStart());
                        newInfo.setArgumentNames(info.getArgumentNames());
                        newInfo.setSourceRangeStart(info.getSourceRange().getOffset());
                        newInfo.setSourceRangeEnd(info.getSourceRange().getOffset() + info.getSourceRange().getLength());
                        return newInfo;

                    }
                    public boolean exists() {
                        return true;
                    }
                };
                return itd;

            } else if (isField) {
                // field
                IField itd = new SourceField((JavaElement) parent, extractName()) {
                    protected Object createElementInfo() {
                        ITDSourceFieldElementInfo newInfo = new ITDSourceFieldElementInfo(IntertypeElement.this);
                        newInfo.setChildren(info.getChildren());
                        newInfo.setFlags(CompilationUnitTools.getPublicModifierCode(info));
                        newInfo.setNameSourceEnd(info.getNameSourceEnd());
                        newInfo.setNameSourceStart(info.getNameSourceStart());
                        newInfo.setTypeName(getQualifiedReturnTypeName(info));
                        newInfo.setSourceRangeStart(info.getSourceRange().getOffset());
                        newInfo.setSourceRangeEnd(info.getSourceRange().getOffset() + info.getSourceRange().getLength());
                        return newInfo;
                    }
                    public boolean exists() {
                        return true;
                    }
                };
                return itd;
            }
        } catch (JavaModelException e) {
        }
        return null;
	}

    private String extractName() {
        String[] split = name.split("\\.");
        return split.length > 1 ? split[1] : name;
    }
	
	private String[] getQualifiedParameterTypes() {
	    IProgramElement ipe = AJProjectModelFactory.getInstance().getModelForJavaElement(this).javaElementToProgramElement(this);
	    if (ipe != IHierarchy.NO_STRUCTURE) {
	        return CoreUtils.listAJSigToJavaSig(ipe.getParameterSignatures());
	    } else {
	        return getParameterTypes();
	    }
    }
	
    private char[] getQualifiedReturnTypeName(IntertypeElementInfo info) {
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


    /**
	 * @author andrew
	 * just expose all the protected setter methods
	 */
	private static class ITDSourceFieldElementInfo extends SourceFieldElementInfo implements IIntertypeInfo {
        IntertypeElement original;

        public ITDSourceFieldElementInfo(IntertypeElement original) {
            this.original = original;
        }

        public IntertypeElement getOriginal() {
            return original;
        }

	    protected void setFlags(int flags) {
	        super.setFlags(flags);
	    }
	    protected void setTypeName(char[] typeName) {
	        super.setTypeName(typeName);
	    }
	    protected void setNameSourceEnd(int end) {
	        super.setNameSourceEnd(end);
	    }
	    protected void setNameSourceStart(int start) {
	        super.setNameSourceStart(start);
	    }
	    protected void setSourceRangeEnd(int end) {
	        super.setSourceRangeEnd(end);
	    }
	    protected void setSourceRangeStart(int start) {
	        super.setSourceRangeStart(start);
	    }
	}
	
	private static class ITDSourceMethodElementInfo extends SourceMethodInfo implements IIntertypeInfo {

        IntertypeElement original;

        public ITDSourceMethodElementInfo(IntertypeElement original) {
            this.original = original;
        }

        public IntertypeElement getOriginal() {
            return original;
        }

        protected void setReturnType(char[] type) {
            super.setReturnType(type);
        }

        protected void setArgumentNames(char[][] names) {
            super.setArgumentNames(names);
        }

        protected void setExceptionTypeNames(char[][] types) {
            super.setExceptionTypeNames(types);
        }

        protected void setFlags(int flags) {
            super.setFlags(flags);
        }

        protected void setNameSourceEnd(int end) {
            super.setNameSourceEnd(end);
        }

        protected void setNameSourceStart(int start) {
            super.setNameSourceStart(start);
        }

        protected void setSourceRangeEnd(int end) {
            super.setSourceRangeEnd(end);
        }

        protected void setSourceRangeStart(int start) {
            super.setSourceRangeStart(start);
        }
	    
	}
	
   private static class ITDSourceConstructorElementInfo extends SourceConstructorInfo implements IIntertypeInfo {

        IntertypeElement original;

        public ITDSourceConstructorElementInfo(IntertypeElement original) {
            this.original = original;
        }

        public IntertypeElement getOriginal() {
            return original;
        }

        protected void setArgumentNames(char[][] names) {
            super.setArgumentNames(names);
        }

        protected void setExceptionTypeNames(char[][] types) {
            super.setExceptionTypeNames(types);
        }

        protected void setFlags(int flags) {
            super.setFlags(flags);
        }

        protected void setNameSourceEnd(int end) {
            super.setNameSourceEnd(end);
        }

        protected void setNameSourceStart(int start) {
            super.setNameSourceStart(start);
        }

        protected void setSourceRangeEnd(int end) {
            super.setSourceRangeEnd(end);
        }

        protected void setSourceRangeStart(int start) {
            super.setSourceRangeStart(start);
        }

    }
}