/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      SpringSource
 *      Andrew Eisenberg (initial implementation)
 *******************************************************************************/
package org.eclipse.contribution.jdt.sourceprovider;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.parser.Scanner;

public aspect SourceTransformerAspect {
    
    /**
     * Captures setting the source of a Scanner just before a parse is starting
     */
    void around(char[] sourceString, ICompilationUnit sourceUnit) : execution(public final void Scanner+.setSource(char[])) && 
            cflowbelow(startingParse(sourceUnit)) && args(sourceString) {
        String extension = getExtension(sourceUnit);
        ISourceTransformer transformer = SourceTransformerRegistry.getInstance().getSelector(extension);
        if (transformer != null) {
            proceed(transformer.convert(sourceString), sourceUnit);
        } else {
            proceed(sourceString, sourceUnit);
        }
    }
    
    
    pointcut startingParse(ICompilationUnit sourceUnit) : 
        execution(public CompilationUnitDeclaration parse(
                ICompilationUnit, CompilationResult)) &&
                args(sourceUnit, ..);
    
    private static String getExtension(ICompilationUnit sourceUnit) {
        char[] name = sourceUnit.getFileName();
        int extensionIndex = name.length - 1;
        while (extensionIndex >= 0) {
            if (name[extensionIndex] == '.') {
                char[] extensionArr = new char[name.length - extensionIndex-1];
                System.arraycopy(name, extensionIndex+1, extensionArr, 0, extensionArr.length);
                return new String(extensionArr);
            }
            extensionIndex --;
        }
        return "";  //$NON-NLS-1$
    }
}