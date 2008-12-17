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

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.parser.Scanner;

public aspect SourceTransformerAspect {
    
    pointcut settingSource(char[] sourceString) : 
            execution(public final void Scanner+.setSource(char[])) &&
            args(sourceString);
    
    pointcut startingParse(ICompilationUnit sourceUnit) : 
        execution(public CompilationUnitDeclaration parse(
                ICompilationUnit, CompilationResult)) &&
                args(sourceUnit, ..);

    /**
     * Captures setting the source of a Scanner just before a parse is starting.
     * Transforms the source to something that is Java-compatible before sending it
     * to the scanner 
     */
    void around(char[] sourceString, ICompilationUnit sourceUnit) : settingSource(sourceString) && 
            cflowbelow(startingParse(sourceUnit)) {
        String extension = getExtension(sourceUnit);
        ISourceTransformer transformer = SourceTransformerRegistry.getInstance().getSelector(extension);
        if (transformer != null) {
            try {
                char[] transformedSource = transformer.convert(sourceString);
                proceed(transformedSource, sourceUnit);
            } catch (Throwable t) {
                JDTWeavingPlugin.logException(t);
            }
        }
        proceed(sourceString, sourceUnit);
    }
    
    
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