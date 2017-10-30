/*******************************************************************************
 * Copyright (c) 2009, 2014 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.core.javaelements;

import org.aspectj.asm.IProgramElement;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding.ExternalAnnotationStatus;
//import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding.ExternalAnnotationStatus;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.core.BinaryType;

/**
 * @author Andrew Eisenberg
 * @created Mar 18, 2009
 *
 */
public class BinaryAspectElementInfo /*extends ClassFileReader*/ implements IBinaryType {
    
    private char[] fileName;
    private char[] typeName;
    private char[] genericSignature;
    private char[] superName;
    private char[][] interfaceNames;

    public BinaryAspectElementInfo(char[] typeName) {
        this.typeName = typeName;
        
        fileName = new char[0];
        genericSignature = new char[0];
        superName = new char[0];
        interfaceNames = new char[0][];
    }
    
    public BinaryAspectElementInfo(IProgramElement elt) {
        try {
            fileName = elt.getSourceLocation().getSourceFile().getName().toCharArray();
        } catch (NullPointerException e) {
            fileName = new char[0];
        }
        typeName = elt.getName().toCharArray();
        
        genericSignature = new char[0];
        superName = new char[0];
        interfaceNames = new char[0][];
    }

    public IBinaryAnnotation[] getAnnotations() {
        return new IBinaryAnnotation[0];
    }

    public char[] getEnclosingTypeName() {
        return new char[0];
    }

    public IBinaryField[] getFields() {
        return new IBinaryField[0];
    }

    public char[] getGenericSignature() {
        return genericSignature;
    }

    public char[][] getInterfaceNames() {
        return interfaceNames;
    }

    public IBinaryNestedType[] getMemberTypes() {
        return new IBinaryNestedType[0];
    }

    public IBinaryMethod[] getMethods() {
        return new IBinaryMethod[0];
    }

    public char[][][] getMissingTypeNames() {
        return new char[0][][];
    }

    public char[] getName() {
        return typeName;
    }

    public char[] getSourceName() {
        return typeName;
    }

    public char[] getSuperclassName() {
        return superName;
    }

    public long getTagBits() {
        return 0;
    }

    public boolean isAnonymous() {
        return typeName.length == 0;
    }

    public boolean isLocal() {
        return false;
    }

    public boolean isMember() {
        return false;
    }

    public char[] sourceFileName() {
        return null;
    }

    public int getModifiers() {
        return 0;
    }

    public boolean isBinaryType() {
        return true;
    }

    public char[] getFileName() {
        return fileName;
    }

    /* AJDT 1.7 */
    public char[] getEnclosingMethod() {
        return null;
    }
    
    public IBinaryTypeAnnotation[] getTypeAnnotations() {
       return null;
    }

	public ITypeAnnotationWalker enrichWithExternalAnnotationsFor(ITypeAnnotationWalker arg0, Object arg1,
			LookupEnvironment arg2) {
		return arg0;
	}

	/**
	 * Answer whether a provider for external annotations is associated with this binary type.
	 */
	public ExternalAnnotationStatus getExternalAnnotationStatus() {
		return null;
	}

	public char[] getModule() {
		return null;
	}

}
