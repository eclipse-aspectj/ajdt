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

package org.eclipse.ajdt.core.codeconversion;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.search.indexing.SourceIndexer;
import org.eclipse.jdt.internal.core.search.indexing.SourceIndexerRequestor;

/**
 * @author Andrew Eisenberg
 * @created May 25, 2010
 *
 */
public class AJSourceIndexerRequestor extends SourceIndexerRequestor {

    private SourceIndexer indexer;
    
    public AJSourceIndexerRequestor(SourceIndexer indexer) {
        super(indexer);
        this.indexer = indexer;
    }

    @Override
    public void enterField(FieldInfo fieldInfo) {
        super.enterField(fieldInfo);
        try {
            char[] fieldName = fieldInfo.name;
            int last = CharOperation.lastIndexOf('$',fieldName) + 1;
            if (last > 0 && last < fieldName.length) {
                // assume this is an itd
                char[] realFieldName = CharOperation.subarray(fieldName, last, fieldName.length);
                this.indexer.addFieldDeclaration(fieldInfo.type, realFieldName);
                
                // now index the type
                if (last > 1) { 
                    int prev = Math.max(CharOperation.lastIndexOf('$', fieldName, 0, last - 2), 0);
                    char[] targetTypeSimpleName = CharOperation.subarray(fieldName, prev, last-1);
                    super.acceptTypeReference(targetTypeSimpleName, fieldInfo.nameSourceStart);
                }
            }
        } catch (Exception e) {
        }
    }
    
    @Override
    public void enterMethod(MethodInfo methodInfo) {
        super.enterMethod(methodInfo);
        try {
            char[] methodName = methodInfo.name;
            int last = CharOperation.lastIndexOf('$',methodName) + 1;
            if (last > 0 && last < methodName.length) {
                // assume this is an itd
                char[] realMethodName = CharOperation.subarray(methodName, last, methodName.length);
                boolean isConstructor = false;
                if (CharOperation.equals("new".toCharArray(), realMethodName)) {
                    isConstructor = true;
                } else {
                    this.indexer.addMethodDeclaration(realMethodName, methodInfo.parameterTypes, methodInfo.returnType, methodInfo.exceptionTypes);
                }
                
                // now index the type
                if (last > 1) {
                    char[][] splits = CharOperation.splitAndTrimOn('$', methodName);
                    
                    // should be array of length 2 at least.  Last element is the realMethodName
                    // one before that is the simple name of the type
                    // if more than length 2, then the rest are package names
                    int length = splits.length;
                    
                    if (length > 1) {
                        // remove the last segment
                        char[][] newSplits = new char[splits.length-1][];
                        System.arraycopy(splits, 0, newSplits, 0, splits.length-1);
                        
                        super.acceptUnknownReference(newSplits, methodInfo.nameSourceStart, methodInfo.nameSourceEnd - splits[length-1].length -1);
                        if (isConstructor) {
                            int argCount = methodInfo.parameterTypes == null ? 0 : methodInfo.parameterTypes.length;
                            this.indexer.addConstructorDeclaration(splits[length-2], 
                                    argCount, null, methodInfo.parameterTypes, methodInfo.parameterNames, 
                                    methodInfo.modifiers, methodInfo.declaringPackageName, methodInfo.declaringTypeModifiers,
                                    methodInfo.exceptionTypes, methodInfo.extraFlags);
                        }
                    }
                    
                    
                }
            }
        } catch (Exception e) {
        }
    }
}
