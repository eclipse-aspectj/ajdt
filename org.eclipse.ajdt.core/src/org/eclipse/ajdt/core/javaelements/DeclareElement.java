/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *     Andrew Eisenberg - changes for AJDT 2.0
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * @author Luzius Meisser
 */
public class DeclareElement extends AspectJMemberElement{
    
    public DeclareElement(JavaElement parent, String name, String[] parameterTypes) {
        // bug 267417 declare error and declare warning should not have message in its name
        super(parent, trimName(name), parameterTypes);
    }
    
    /**
     * @see JavaElement#getHandleMemento()
     */
    protected char getHandleMementoDelimiter() {
        return AspectElement.JEM_DECLARE;
    }
    
    
    protected Object createElementInfo() {
        try {
            IProgramElement ipe = AJProjectModelFactory.getInstance().getModelForJavaElement(this)
                    .javaElementToProgramElement(this);

            DeclareElementInfo elementInfo = new DeclareElementInfo();
            if (ipe != IHierarchy.NO_STRUCTURE) {
                elementInfo.setSourceRangeStart(ipe.getSourceLocation().getOffset());
                elementInfo.setName(name.toCharArray());
                elementInfo.setAJKind(getKindForString(name));
                
                List<String> types = ipe.getParentTypes();
                if (types != null) {
                    List<String> typesConverted = new ArrayList<String>(types.size());
                    for (String parentTypeName : types) {
                        
                        parentTypeName = parentTypeName.replaceAll("\\$", "\\.");
                        typesConverted.add(parentTypeName);
                    }
                    elementInfo.setTypes((String[]) typesConverted.toArray(new String[typesConverted.size()]));
                }
                
                elementInfo.setAnnotationRemover(ipe.isAnnotationRemover());
            }        
            return elementInfo;
        } catch (Exception e) {
            // can fail for any of a number of reasons.
            // return null so that we can try again later.
            return null;
        }
    }
    
    protected Kind getKindForString(String kindString) {
        for (int i = 0; i < IProgramElement.Kind.ALL.length; i++) {
            if (kindString.startsWith(IProgramElement.Kind.ALL[i].toString())) return IProgramElement.Kind.ALL[i];  
        }
        return IProgramElement.Kind.ERROR;
    }

   private static String trimName(String name) {
       return name == null ? null : name.split(":")[0];
   }
}