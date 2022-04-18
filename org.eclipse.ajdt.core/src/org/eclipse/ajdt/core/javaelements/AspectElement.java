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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.asm.IProgramElement.Modifiers;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IProgramElement.Accessibility;
import org.aspectj.asm.IProgramElement.ExtraInformation;
import org.aspectj.asm.IProgramElement.Kind;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;

/**
 * @author Luzius Meisser
 * @author Andrew Eisenberg
 */
public class AspectElement extends SourceType implements IAspectJElement {

    /********************************************************************************
     * \ Overview of Ascii table, characters used in handle element encoding:
     * (try to maintain this table whenever making changes to the JEM_XXX
     * constants!)
     *
     * 33 21 U+0021 ! JEM_COUNT 34 22 U+0022 " JEM_POINTCUT 35 23 U+0023 #
     * JEM_IMPORTDECLARATION 36 24 U+0024 $ occurs inside special identifiers 37
     * 25 U+0025 % JEM_PACKAGEDECLARATION 38 26 U+0026 & JEM_ADVICE 39 27 U+0027
     * ' JEM_ASPECT_TYPE 40 28 U+0028 ( JEM_CLASSFILE 41 29 U+0029 )
     * JEM_ITD_METHOD 42 2A U+002A * JEM_ASPECT_CU 43 2B U+002B + Used in type
     * signatures cannot use 44 2C U+002C , JEM_ITD_FIELD 45 2D U+002D - Used in
     * type signatures cannot use 46 2E U+002E . occurs in fully qualified names
     * 47 2F U+002F / JEM_PACKAGEFRAGMENTROOT
     *
     * 58 3A U+003A : ? unused ? used in type signatures (not sure if this is
     * important or not) 59 3B U+003B ; Terminator for parameter (I think), in
     * any case I've seen it in handles 60 3C U+003C < JEM_PACKAGEFRAGMENT Used
     * in type signatures, but apparently not a big deal 61 3D U+003D =
     * JEM_JAVAPROJECT 62 3E U+003E > Used in type signatures 63 3F U+003F ?
     * JEM_CODEELEMENT 64 40 U+0040 @ JEM_LOCALVARIABLE
     *
     * 91 5B U+005B [ JEM_TYPE 92 5C U+005C \ JEM_ESCAPE 93 5D U+005D ]
     * JEM_TYPE_PARAMETER 94 5E U+005E ^ JEM_FIELD 95 5F U+005F _ occurs in
     * identifiers 96 60 U+0060 ` JEM_DECLARE
     *
     * 123 7B U+007B { JEM_COMPILATIONUNIT 124 7C U+007C | JEM_INITIALIZER 125
     * 7D U+007D } JEM_ANNOTATION 126 7E U+007E ~ JEM_METHOD \
     *******************************************************************************/

    // characters to use for handle identifiers, alongside the ones in
    // JavaElement
    public static final char JEM_ASPECT_CU = '*';

    public static final char JEM_ADVICE = '&';

    public static final char JEM_ASPECT_TYPE = '\'';

    public static final char JEM_CODEELEMENT = '?';

    public static final char JEM_ITD_METHOD = ')';

    public static final char JEM_ITD_FIELD = ',';

    public static final char JEM_DECLARE = '`';

    public static final char JEM_POINTCUT = '"';

    public IMethod createMethod(String contents, IJavaElement sibling,
            boolean force, IProgressMonitor monitor) throws JavaModelException {
      return super.createMethod(contents, sibling, force, monitor);
    }

    public AspectElement(JavaElement parent, String name) {
        super(parent, name);
    }

    public int getType() {
        return TYPE;
    }

    protected Object createElementInfo() {

        AspectElementInfo info = new AspectElementInfo();
        info.setAJKind(IProgramElement.Kind.ASPECT);
        info.setHandle(this);
        info.setSourceRangeStart(0);

        IProgramElement ipe = AJProjectModelFactory.getInstance()
                .getModelForJavaElement(this).javaElementToProgramElement(this);
        if (ipe != null && ipe != IHierarchy.NO_STRUCTURE) {
            info.setAJExtraInfo(ipe.getExtraInfo());
            info.setAJModifiers(ipe.getModifiers());
            info.setFlags(getProgramElementModifiers(ipe));
            info.setAJAccessibility(ipe.getAccessibility());
            ISourceLocation sourceLocation = ipe.getSourceLocation();
            info.setSourceRangeStart(sourceLocation.getOffset());
            info.setNameSourceStart(sourceLocation.getOffset());
            info.setNameSourceEnd(sourceLocation.getOffset()
                    + ipe.getName().length());
            // info.setPrivileged(???); not setting this yet
        }
        return info;
    }

    public AspectElement getAspect(String name) {
        return new AspectElement(this, name);
    }

    /**
     * Returns the pointcuts declared by this type. If this is a source type,
     * the results are listed in the order in which they appear in the source,
     * otherwise, the results are in no particular order.
     *
     * @exception JavaModelException
     *                if this element does not exist or if an exception occurs
     *                while accessing its corresponding resource.
     * @return the pointcuts declared by this type
     */
    public PointcutElement[] getPointcuts() throws JavaModelException {
        // pointcuts appear as methods
        IMethod[] methods = getMethods();
        List<IMethod> list = new ArrayList<>();
      for (IMethod method : methods) {
        if (method instanceof PointcutElement) {
          list.add(method);
        }
      }
        PointcutElement[] array = new PointcutElement[list.size()];
        list.toArray(array);
        return array;
    }

    /**
     * Returns the advice elements declared by this type. If this is a source
     * type, the results are listed in the order in which they appear in the
     * source, otherwise, the results are in no particular order.
     *
     * @exception JavaModelException
     *                if this element does not exist or if an exception occurs
     *                while accessing its corresponding resource.
     * @return the advice elements declared by this type
     */
    public AdviceElement[] getAdvice() throws JavaModelException {
        // advice statements appear as methods
        IMethod[] methods = getMethods();
        List<IMethod> list = new ArrayList<>();
      for (IMethod method : methods) {
        if (method instanceof AdviceElement) {
          list.add(method);
        }
      }
        AdviceElement[] array = new AdviceElement[list.size()];
        list.toArray(array);
        return array;
    }

    /**
     * Returns the declare elements declared by this type. If this is a source
     * type, the results are listed in the order in which they appear in the
     * source, otherwise, the results are in no particular order.
     *
     * @exception JavaModelException
     *                if this element does not exist or if an exception occurs
     *                while accessing its corresponding resource.
     * @return the declare elements declared by this type
     */
    public DeclareElement[] getDeclares() throws JavaModelException {
        // declare statements appear as methods
        IMethod[] methods = getMethods();
        List<IMethod> list = new ArrayList<>();
      for (IMethod method : methods) {
        if (method instanceof DeclareElement) {
          list.add(method);
        }
      }
        DeclareElement[] array = new DeclareElement[list.size()];
        list.toArray(array);
        return array;
    }

    /**
     * Returns the declare elements declared by this type. If this is a source
     * type, the results are listed in the order in which they appear in the
     * source, otherwise, the results are in no particular order.
     *
     * @exception JavaModelException
     *                if this element does not exist or if an exception occurs
     *                while accessing its corresponding resource.
     * @return the declare elements declared by this type
     */
    public IntertypeElement[] getITDs() throws JavaModelException {
        // ITDs statements appear as methods
        IMethod[] methods = getMethods();
        List<IMethod> list = new ArrayList<>();
      for (IMethod method : methods) {
        if (method instanceof IntertypeElement) {
          list.add(method);
        }
      }
        IntertypeElement[] array = new IntertypeElement[list.size()];
        list.toArray(array);
        return array;
    }

    /**
     * Returns all aspect member elements declared in this type. If this is a
     * source type, the results are listed in the order in which they appear in
     * the source, otherwise, the results are in no particular order.
     *
     * @return the Aspect member elements declared by this type
     * @throws JavaModelException
     *             if this element does not exist or if an exception occurs
     *             while accessing its corresponding resource.
     */
    public IAspectJElement[] getAllAspectMemberElements()
            throws JavaModelException {
        IJavaElement[] allChildren = getChildren();
        List<IJavaElement> list = new ArrayList<>();
      for (IJavaElement allChild : allChildren) {
        if (allChild instanceof IAspectJElement) {
          list.add(allChild);
        }
      }
        IAspectJElement[] array = new IAspectJElement[list.size()];
        list.toArray(array);
        return array;
    }

    // TODO: forward call to ElementInfo (only for cosmetical reasons)
    public Kind getAJKind() throws JavaModelException {
        IAspectJElementInfo info = (IAspectJElementInfo) getElementInfo();
        return info.getAJKind();
    }

    public Accessibility getAJAccessibility() throws JavaModelException {
        Object info = getElementInfo();
        if (info instanceof IAspectJElementInfo) {
            IAspectJElementInfo ajInfo = (IAspectJElementInfo) info;
            return ajInfo.getAJAccessibility();
        } else {
            // this happens when an aspect is converted to a class in the
            // working copy
            // but compiler is not aware of it.
            return Accessibility.PUBLIC;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ajdt.javamodel.javaelements.IAspectJElement#getAJModifiers()
     */
    public List<Modifiers> getAJModifiers() throws JavaModelException {
        IAspectJElementInfo info = (IAspectJElementInfo) getElementInfo();
        return info.getAJModifiers();
    }

    public boolean isPrivileged() throws JavaModelException {
        Object info = getElementInfo();
        return info instanceof AspectElementInfo && ((AspectElementInfo) info)
          .isPrivileged();
    }

    @Override
    public IMethod getMethod(String selector, String[] parameterTypeSignatures) {
        int dollarIdx = selector.indexOf('$');
        // '$' must not be the first or last character
        if (dollarIdx > 0 && dollarIdx < selector.length()-1) {
            // probably an ITD
            return getITDMethod(selector.replace('$', '.'), parameterTypeSignatures);
        }
        return super.getMethod(selector, parameterTypeSignatures);
    }

    public IntertypeElement getITDMethod(String selector, String[] parameterTypeSignatures) {
        return IntertypeElement.create(JEM_ITD_METHOD, this, selector, parameterTypeSignatures);
    }

    @Override
    public IField getField(String fieldName) {
        int dollarIdx = fieldName.indexOf('$');
        // '$' must not be the first or last character
        if (dollarIdx > 0 && dollarIdx < fieldName.length()-1) {
            // probably an ITD
            return getITDField(fieldName.replace('$', '.'));
        }
        return super.getField(fieldName);
    }

    public IField getITDField(String fieldName) {
        return (IField) IntertypeElement.create(JEM_ITD_FIELD, this, fieldName, new String[0]);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ajdt.javamodel.javaelements.IAspectJElement#getAJExtraInformation
     * ()
     */
    public ExtraInformation getAJExtraInformation() throws JavaModelException {
        IAspectJElementInfo info = (IAspectJElementInfo) getElementInfo();
        return info.getAJExtraInfo();
    }

    /**
     * @see JavaElement#getHandleMemento()
     */
    protected char getHandleMementoDelimiter() {
        return AspectElement.JEM_ASPECT_TYPE;
    }

    /*
     * Derived from JEM_METHOD clause in SourceType Added support for advice,
     * ITDs, and declare statements
     *
     * @see JavaElement
     */
    public IJavaElement getHandleFromMemento(String token,
            MementoTokenizer memento, WorkingCopyOwner workingCopyOwner) {
        if (token.charAt(0) == AspectElement.JEM_ADVICE) {
            token = null;
            if (!memento.hasMoreTokens())
                return this;
            String name = memento.nextToken();

            ArrayList<String> params = new ArrayList<>();
            nextParam: while (memento.hasMoreTokens()) {
                token = memento.nextToken();
                switch (token.charAt(0)) {
                    case JEM_TYPE:
                        break nextParam;
                    case JavaElement.JEM_TYPE_PARAMETER:
                        token = null;
                        break nextParam;
                    case JEM_ADVICE:
                        if (!memento.hasMoreTokens())
                            return this;
                        String param = memento.nextToken();
                        StringBuilder buffer = new StringBuilder();
                        while (param.length() == 1
                                && Signature.C_ARRAY == param.charAt(0)) { // backward
                                                                           // compatible
                                                                           // with
                                                                           // 3.0
                                                                           // mementos
                            buffer.append(Signature.C_ARRAY);
                            if (!memento.hasMoreTokens())
                                return this;
                            param = memento.nextToken();
                        }
                        params.add(buffer + param);
                        token = null;
                        break;
                    default:
                        break nextParam;
                }
            }
            String[] parameters = new String[params.size()];
            params.toArray(parameters);

            JavaElement advice = new AdviceElement(this, name, parameters);
            if (token != null) {
                return advice.getHandleFromMemento(token, memento,
                        workingCopyOwner);
            } else {
                return advice;
            }
        } else if (token.charAt(0) == AspectElement.JEM_ITD_METHOD) {
            String name = memento.nextToken();
            ArrayList<String> params = new ArrayList<>();
            nextParam: while (memento.hasMoreTokens()) {
                token = memento.nextToken();
                switch (token.charAt(0)) {
                    case JEM_TYPE:
                    case JEM_TYPE_PARAMETER:
                        break nextParam;
                    case JEM_ITD_METHOD:
                        if (!memento.hasMoreTokens())
                            return this;
                        String param = memento.nextToken();
                        StringBuilder buffer = new StringBuilder();
                        while (param.length() == 1
                                && Signature.C_ARRAY == param.charAt(0)) { // backward
                                                                           // compatible
                                                                           // with
                                                                           // 3.0
                                                                           // mementos
                            buffer.append(Signature.C_ARRAY);
                            if (!memento.hasMoreTokens())
                                return this;
                            param = memento.nextToken();
                        }
                        params.add(buffer + param);
                        break;
                    default:
                        break nextParam;
                }
            }
            String[] parameters = new String[params.size()];
            params.toArray(parameters);
            JavaElement itd = new MethodIntertypeElement(this, name, parameters);
            if (memento.hasMoreTokens()) {
//                token = memento.nextToken();
                return itd.getHandleFromMemento(token, memento,
                        workingCopyOwner);
            } else {
                return itd;
            }
        } else if (token.charAt(0) == AspectElement.JEM_ITD_FIELD) {
            String name = memento.nextToken();
            JavaElement itd = new FieldIntertypeElement(this, name);
            if (memento.hasMoreTokens()) {
                token = memento.nextToken();
                return itd.getHandleFromMemento(token, memento,
                        workingCopyOwner);
            } else {
                return itd;
            }
        } else if (token.charAt(0) == AspectElement.JEM_DECLARE) {
            String name = memento.nextToken();
            ArrayList<String> params = new ArrayList<>();
            nextParam: while (memento.hasMoreTokens()) {
                token = memento.nextToken();
                switch (token.charAt(0)) {
                    case JEM_TYPE:
                    case JEM_TYPE_PARAMETER:
                        break nextParam;
                    case JEM_DECLARE:
                        if (!memento.hasMoreTokens())
                            return this;
                        String param = memento.nextToken();
                        StringBuilder buffer = new StringBuilder();
                        while (param.length() == 1
                                && Signature.C_ARRAY == param.charAt(0)) { // backward
                                                                           // compatible
                                                                           // with
                                                                           // 3.0
                                                                           // mementos
                            buffer.append(Signature.C_ARRAY);
                            if (!memento.hasMoreTokens())
                                return this;
                            param = memento.nextToken();
                        }
                        params.add(buffer + param);
                        break;
                    default:
                        break nextParam;
                }
            }
            String[] parameters = new String[params.size()];
            params.toArray(parameters);
            JavaElement itd = new DeclareElement(this, name, parameters);
            if (token.charAt(0) == JavaElement.JEM_COUNT) {
                return itd.getHandleFromMemento(token, memento,
                        workingCopyOwner);
            }
            return itd.getHandleFromMemento(memento, workingCopyOwner);
        } else if (token.charAt(0) == AspectElement.JEM_POINTCUT) {
            String name = memento.nextToken();
            ArrayList<String> params = new ArrayList<>();
            nextParam: while (memento.hasMoreTokens()) {
                token = memento.nextToken();
                switch (token.charAt(0)) {
                    case JEM_TYPE:
                    case JEM_TYPE_PARAMETER:
                        break nextParam;
                    case JEM_POINTCUT:
                        if (!memento.hasMoreTokens())
                            return this;
                        String param = memento.nextToken();
                        StringBuilder buffer = new StringBuilder();
                        while (param.length() == 1
                                && Signature.C_ARRAY == param.charAt(0)) { // backward
                                                                           // compatible
                                                                           // with
                                                                           // 3.0
                                                                           // mementos
                            buffer.append(Signature.C_ARRAY);
                            if (!memento.hasMoreTokens())
                                return this;
                            param = memento.nextToken();
                        }
                        params.add(buffer + param);
                        break;
                    default:
                        break nextParam;
                }
            }
            String[] parameters = new String[params.size()];
            params.toArray(parameters);
            JavaElement pointcut = new PointcutElement(this, name, parameters);
            return pointcut.getHandleFromMemento(memento, workingCopyOwner);
        } else if (token.charAt(0) == AspectElement.JEM_METHOD
                && !(this.getOpenable() instanceof AJCompilationUnit)) {
            // method must be mocked up if we are an aspect in a
            // .class or .java file
            // cannot get the JavaElementInfo otherwise
            String name = memento.nextToken();
            ArrayList<String> params = new ArrayList<>();
            nextParam: while (memento.hasMoreTokens()) {
                token = memento.nextToken();
                switch (token.charAt(0)) {
                    case JEM_TYPE:
                    case JEM_TYPE_PARAMETER:
                    case JEM_ANNOTATION:
                        break nextParam;
                    case JEM_METHOD:
                        if (!memento.hasMoreTokens())
                            return this;
                        String param = memento.nextToken();
                        StringBuilder buffer = new StringBuilder();
                        while (param.length() == 1
                                && Signature.C_ARRAY == param.charAt(0)) { // backward
                                                                           // compatible
                                                                           // with
                                                                           // 3.0
                                                                           // mementos
                            buffer.append(Signature.C_ARRAY);
                            if (!memento.hasMoreTokens())
                                return this;
                            param = memento.nextToken();
                        }
                        params.add(buffer + param);
                        break;
                    default:
                        break nextParam;
                }
            }
            String[] parameters = new String[params.size()];
            params.toArray(parameters);
            MockSourceMethod mockMethod = new MockSourceMethod(this, name,
                    parameters);
            switch (token.charAt(0)) {
                case JEM_TYPE:
                case JEM_TYPE_PARAMETER:
                case JEM_LOCALVARIABLE:
                case JEM_ANNOTATION:
                    return mockMethod.getHandleFromMemento(token, memento,
                            workingCopyOwner);
                default:
                    return mockMethod;
            }
        } else if (token.charAt(0) == AspectElement.JEM_ASPECT_TYPE) {
            // static inner aspect inside an aspect...rare, but could happen
            String typeName;
            if (memento.hasMoreTokens()) {
                typeName = memento.nextToken();
                char firstChar = typeName.charAt(0);
                if (firstChar == JEM_FIELD || firstChar == JEM_INITIALIZER
                        || firstChar == JEM_METHOD || firstChar == JEM_TYPE
                        || firstChar == JEM_COUNT) {
                    token = typeName;
                    typeName = ""; //$NON-NLS-1$
                } else {
                    token = null;
                }
            } else {
                typeName = ""; //$NON-NLS-1$
                token = null;
            }
            JavaElement type = getAspect(typeName);
            if (token == null) {
                return type.getHandleFromMemento(memento, workingCopyOwner);
            } else {
                return type.getHandleFromMemento(token, memento,
                        workingCopyOwner);
            }
        }
        return super.getHandleFromMemento(token, memento, workingCopyOwner);
    }

    static Field modfiersField = null;

    static int getProgramElementModifiers(IProgramElement ipe) {
        try {
            if (modfiersField == null) {
                modfiersField = ProgramElement.class
                        .getDeclaredField("modifiers");
                modfiersField.setAccessible(true);
            }
            return modfiersField.getInt(ipe);
        } catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException ignored) {}
      return -1;
    }

}
