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

package org.eclipse.ajdt.core.javaelements;

import java.util.ArrayList;

import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;

/**
 * @author Andrew Eisenberg
 * @created Mar 18, 2009
 *
 */
public class BinaryAspectElement extends BinaryType {

    public BinaryAspectElement(JavaElement parent, String name) {
        super(parent, name);
    }

    protected Object createElementInfo() {
        IProgramElement ipe = AJProjectModelFactory.getInstance().getModelForJavaElement(this).javaElementToProgramElement(this);
        return new BinaryAspectElementInfo(ipe);
    }
    
    protected char getHandleMementoDelimiter() {
        return AspectElement.JEM_ASPECT_TYPE;
    }
    
    /*
     * Derived from JEM_METHOD clause in SourceType
     * Added support for advice, ITDs, and declare statements
     * @see JavaElement
     */
    public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner workingCopyOwner) {
        if (token.charAt(0) == AspectElement.JEM_ADVICE) {
            token = null;
            if (!memento.hasMoreTokens()) return this;
            String name = memento.nextToken();
            
            ArrayList<String> params = new ArrayList<String>();
            nextParam: while (memento.hasMoreTokens()) {
                token = memento.nextToken();
                switch (token.charAt(0)) {
                    case JEM_TYPE:
                        break nextParam;
                    case JEM_TYPE_PARAMETER:
                        token = null;
                        break nextParam;
                    case AspectElement.JEM_ADVICE:
                        if (!memento.hasMoreTokens()) return this;
                        String param = memento.nextToken();
                        StringBuffer buffer = new StringBuffer();
                        while (param.length() == 1 && Signature.C_ARRAY == param.charAt(0)) { // backward compatible with 3.0 mementos
                            buffer.append(Signature.C_ARRAY);
                            if (!memento.hasMoreTokens()) return this;
                            param = memento.nextToken();
                        }
                        params.add(buffer.toString() + param);
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
                return advice.getHandleFromMemento(token, memento, workingCopyOwner);
            } else {
                return advice;
            }
        } else if (token.charAt(0) == AspectElement.JEM_ITD_METHOD) {
            String name = memento.nextToken();
            ArrayList<String> params = new ArrayList<String>();
            nextParam: while (memento.hasMoreTokens()) {
                token = memento.nextToken();
                switch (token.charAt(0)) {
                    case JEM_TYPE:
                    case JEM_TYPE_PARAMETER:
                        break nextParam;
                    case AspectElement.JEM_ITD_METHOD:
                        if (!memento.hasMoreTokens()) return this;
                        String param = memento.nextToken();
                        StringBuffer buffer = new StringBuffer();
                        while (param.length() == 1 && Signature.C_ARRAY == param.charAt(0)) { // backward compatible with 3.0 mementos
                            buffer.append(Signature.C_ARRAY);
                            if (!memento.hasMoreTokens()) return this;
                            param = memento.nextToken();
                        }
                        params.add(buffer.toString() + param);
                        break;
                    default:
                        break nextParam;
                }
            }
            String[] parameters = new String[params.size()];
            params.toArray(parameters);
            JavaElement itd = new MethodIntertypeElement(this, name, parameters);
            if (memento.hasMoreTokens()) {
                return itd.getHandleFromMemento(token, memento, workingCopyOwner);
            } else {
                return itd;
            }
		} else if (token.charAt(0) == AspectElement.JEM_ITD_FIELD) {
			String name = memento.nextToken();
			JavaElement itd = new FieldIntertypeElement(this, name);
			if (memento.hasMoreTokens()) {
			    return itd.getHandleFromMemento(token, memento, workingCopyOwner);
			} else {
			    return itd;
			}
        } else if (token.charAt(0) == AspectElement.JEM_DECLARE) {
            String name = memento.nextToken();
            ArrayList params = new ArrayList();
            nextParam: while (memento.hasMoreTokens()) {
                token = memento.nextToken();
                switch (token.charAt(0)) {
                    case JEM_TYPE:
                    case JEM_TYPE_PARAMETER:
                        break nextParam;
                    case AspectElement.JEM_DECLARE:
                        if (!memento.hasMoreTokens()) return this;
                        String param = memento.nextToken();
                        StringBuffer buffer = new StringBuffer();
                        while (param.length() == 1 && Signature.C_ARRAY == param.charAt(0)) { // backward compatible with 3.0 mementos
                            buffer.append(Signature.C_ARRAY);
                            if (!memento.hasMoreTokens()) return this;
                            param = memento.nextToken();
                        }
                        params.add(buffer.toString() + param);
                        break;
                    default:
                        break nextParam;
                }
            }
            String[] parameters = new String[params.size()];
            params.toArray(parameters);
            JavaElement itd = new DeclareElement(this, name, parameters);
            if (token.charAt(0) == JavaElement.JEM_COUNT) {
                return itd.getHandleFromMemento(token, memento, workingCopyOwner);
            }
            return itd.getHandleFromMemento(memento, workingCopyOwner);
        } else if (token.charAt(0) == AspectElement.JEM_POINTCUT) {
            String name = memento.nextToken();
            ArrayList params = new ArrayList();
            nextParam: while (memento.hasMoreTokens()) {
                token = memento.nextToken();
                switch (token.charAt(0)) {
                    case JEM_TYPE:
                    case JEM_TYPE_PARAMETER:
                        break nextParam;
                    case AspectElement.JEM_POINTCUT:
                        if (!memento.hasMoreTokens()) return this;
                        String param = memento.nextToken();
                        StringBuffer buffer = new StringBuffer();
                        while (param.length() == 1 && Signature.C_ARRAY == param.charAt(0)) { // backward compatible with 3.0 mementos
                            buffer.append(Signature.C_ARRAY);
                            if (!memento.hasMoreTokens()) return this;
                            param = memento.nextToken();
                        }
                        params.add(buffer.toString() + param);
                        break;
                    default:
                        break nextParam;
                }
            }
            String[] parameters = new String[params.size()];
            params.toArray(parameters);
            JavaElement pointcut = new PointcutElement(this, name, parameters);
            return pointcut.getHandleFromMemento(memento, workingCopyOwner);
        } else if (token.charAt(0) == AspectElement.JEM_METHOD &&
               ! (this.getOpenable() instanceof AJCompilationUnit)) {
            // method must be mocked up if we are an aspect in a 
            // .class or .java file
            // cannot get the JavaElementInfo otherwise
            String name = memento.nextToken();
            ArrayList params = new ArrayList();
            nextParam: while (memento.hasMoreTokens()) {
                token = memento.nextToken();
                switch (token.charAt(0)) {
                    case JEM_TYPE:
                    case JEM_TYPE_PARAMETER:
                    case JEM_ANNOTATION:
                        break nextParam;
                    case JEM_METHOD:
                        if (!memento.hasMoreTokens()) return this;
                        String param = memento.nextToken();
                        StringBuffer buffer = new StringBuffer();
                        while (param.length() == 1 && Signature.C_ARRAY == param.charAt(0)) { // backward compatible with 3.0 mementos
                            buffer.append(Signature.C_ARRAY);
                            if (!memento.hasMoreTokens()) return this;
                            param = memento.nextToken();
                        }
                        params.add(buffer.toString() + param);
                        break;
                    default:
                        break nextParam;
                }
            }
            String[] parameters = new String[params.size()];
            params.toArray(parameters);
            MockSourceMethod mockMethod = new MockSourceMethod(this, name, parameters);
            switch (token.charAt(0)) {
                case JEM_TYPE:
                case JEM_TYPE_PARAMETER:
                case JEM_LOCALVARIABLE:
                case JEM_ANNOTATION:
                    return mockMethod.getHandleFromMemento(token, memento, workingCopyOwner);
                default:
                    return mockMethod;
            }
        }
        return super.getHandleFromMemento(token, memento, workingCopyOwner);
    }

}
