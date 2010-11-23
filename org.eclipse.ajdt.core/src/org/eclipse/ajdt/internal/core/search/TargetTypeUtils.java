/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.core.search;

import org.eclipse.ajdt.core.ReflectionUtils;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.search.matching.FieldPattern;
import org.eclipse.jdt.internal.core.search.matching.PackageReferencePattern;
import org.eclipse.jdt.internal.core.search.matching.TypeReferencePattern;

/**
 * @author Andrew Eisenberg
 * @created Aug 6, 2010
 */
public class TargetTypeUtils {

    static char[] getQualName(FieldPattern pattern) {
        return (char[]) ReflectionUtils.getPrivateField(FieldPattern.class, "declaringQualification", pattern);
    }

    static char[] getSimpleName(FieldPattern pattern) {
        return (char[]) ReflectionUtils.getPrivateField(FieldPattern.class, "declaringSimpleName", pattern);
    }

    
    static char[] getQualName(TypeReferencePattern pattern) {
        return (char[]) ReflectionUtils.getPrivateField(TypeReferencePattern.class, "qualification", pattern);
    }

    static String getSimpleNameStr(TypeReferencePattern pattern) {
        char[] simpleNameChars = getSimpleName(pattern);
        return simpleNameChars != null ? String.valueOf(simpleNameChars) : null;
    }
    
    static String getQualNameStr(TypeReferencePattern pattern) {
       char[] qualNameChars = getQualName(pattern);
       return qualNameChars != null ? String.valueOf(qualNameChars) : null;
    }

    static char[] getSimpleName(TypeReferencePattern pattern) {
        return (char[]) ReflectionUtils.getPrivateField(TypeReferencePattern.class, "simpleName", pattern);
    }


    static char[] getName(TypeReferencePattern pattern) {
        return getName(getQualName(pattern), getSimpleName(pattern));
    }
    
    static char[] getName(char[] qual, char[] name) {
        if (name == null) {
            return null;
        }
        
        char[] targetTypeName;
        if (qual != null && qual.length > 0) {
            qual = CharOperation.append(qual, '.');
            targetTypeName = CharOperation.append(qual, qual.length, name, 0, name.length);
        } else {
            targetTypeName = name;
        }
        // trim any \0 that are added to the end of target type
        int lastChar = targetTypeName.length;
        while (targetTypeName[lastChar-1] == '\0' && lastChar > 0) lastChar --;
        
        targetTypeName = CharOperation.subarray(targetTypeName, 0, lastChar);
        
        return targetTypeName;
    }

    static char[] getPackage(PackageReferencePattern pattern) {
        return (char[]) ReflectionUtils.getPrivateField(PackageReferencePattern.class, "pkgName", pattern);
    }
}
