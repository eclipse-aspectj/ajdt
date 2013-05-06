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
import org.eclipse.contribution.jdt.preferences.WeavableProjectListener;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.SourceMapper;
import org.eclipse.jdt.internal.core.search.indexing.SourceIndexer;
import org.eclipse.jdt.internal.core.search.indexing.SourceIndexerRequestor;
import org.eclipse.jdt.internal.corext.fix.CleanUpRefactoring;
import org.eclipse.jdt.internal.corext.fix.CleanUpRefactoring.CleanUpChange;

public privileged aspect SourceTransformerAspect {
    
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
        // See bug 265586
        // ignore BasicCompilationUnit because they are used for 
        // mocking up binary code
        // not sure if this should stay
        // this means that binary aspects look transformed, but they also don't have structure
        if (! (sourceUnit instanceof BasicCompilationUnit) && sourceString != null) {
            String extension = getExtension(sourceUnit);
            ISourceTransformer transformer = SourceTransformerRegistry.getInstance().getSelector(extension);
            if (transformer != null) {
                try {
                    char[] transformedSource = transformer.convert(sourceString);
                    proceed(transformedSource, sourceUnit);
                    return;
                } catch (Throwable t) {
                    JDTWeavingPlugin.logException(t);
                }
            }
        }
        proceed(sourceString, sourceUnit);
    }
    
    /**
     * Captures executions of {@link SourceMapper#mapSource(IType, char[], IBinaryType)}.
     * This method is used to map a binary file to its attached source code.
     * 
     * This will help make the outline view for binary files appropriately view Java-like
     * structure.
     */
    pointcut mappingSource(IType type, char[] contents, IBinaryType info) : 
        execution(public void SourceMapper.mapSource(IType, char[], IBinaryType)) && 
        args(type, contents, info);
    
    void around(IType type, char[] contents, IBinaryType info) : 
            mappingSource(type, contents, info) {
        char[] newContents = contents;
        if (isInterestingProject(type) && newContents != null) {
        
            String extension = getExtension(type, info);
            ISourceTransformer transformer = SourceTransformerRegistry.getInstance().getSelector(extension);
            if (transformer != null) {
                try {
                    newContents = transformer.convert(newContents);
                } catch (Throwable t) {
                    JDTWeavingPlugin.logException(t);
                }
            }
        }
        proceed(type, newContents, info);
    }
    
    /**
     * Captures calls to code formatting and other cleanUps when executed from outside of an AJEditor
     */
    pointcut gettingBufferForCleanUp(org.eclipse.jdt.core.ICompilationUnit unit) : 
        cflow(execution(public static CleanUpChange CleanUpRefactoring.calculateChange(..))) && 
        call(public IBuffer org.eclipse.jdt.core.ICompilationUnit.getBuffer()) && 
        !cflow(adviceexecution()) && target(unit);
    
    
    /**
     * Need to make sure that all cleanups access the actual contents, nothing translated
     */
    IBuffer around(org.eclipse.jdt.core.ICompilationUnit unit) : gettingBufferForCleanUp(unit) {
        if (isInterestingProject(unit)) {
            String extension = getExtension(unit.getElementName().toCharArray());
            ISourceTransformer transformer = SourceTransformerRegistry.getInstance().getSelector(extension);
            if (transformer != null) {
                try {
                    IBuffer buffer = transformer.ensureRealBuffer(unit);
                    return buffer;
                } catch (Throwable t) {
                    JDTWeavingPlugin.logException(t);
                }
            }
        }
        return proceed(unit);
    }
    
    
    //////////////////////////////////////////////
    // Extend indexing
    //////////////////////////////////////////////
    pointcut indexingSourceDocument() : call(public SourceIndexerRequestor.new(SourceIndexer))  &&
        // prevent infinite recursion when the transformer decides to return a SourceIndexerRequestor
        !cflowbelow(execution(public SourceIndexerRequestor ISourceTransformer.createIndexerRequestor(SourceIndexer)));
    
    SourceIndexerRequestor around(SourceIndexer indexer) : indexingSourceDocument() && args(indexer) {
        SearchDocument document = indexer.document;
        String extension = getExtension(document.getPath().toCharArray());
        ISourceTransformer transformer = SourceTransformerRegistry.getInstance().getSelector(extension);
        
        // unfortunately, we do not have access to an IJavaElement here, 
        // so we can't weed out uninteresting projects.
        if (transformer != null) {
            try {
                return transformer.createIndexerRequestor(indexer);
            } catch (Throwable t) {
                JDTWeavingPlugin.logException(t);
            }
        }
        return proceed(indexer);
    }
    
    
    private String getExtension(IType type, IBinaryType info) {
        String fName = null;
        if (type != null && type instanceof BinaryType) {
            fName = ((BinaryType) type).getSourceFileName(info);
        }
        return fName != null ? getExtension(fName.toCharArray()) : "" ;
    }

    private String getExtension(ICompilationUnit sourceUnit) {
        char[] name = sourceUnit.getFileName();
        if (name != null) {
            return getExtension(name);
        }
        return "";  //$NON-NLS-1$
    }
    
    private String getExtension(char[] sourceName) {
        int extensionIndex = sourceName.length - 1;
        while (extensionIndex >= 0) {
            if (sourceName[extensionIndex] == '.') {
                char[] extensionArr = new char[sourceName.length - extensionIndex-1];
                System.arraycopy(sourceName, extensionIndex+1, extensionArr, 0, extensionArr.length);
                return new String(extensionArr);
            }
            extensionIndex --;
        }
        return "";  //$NON-NLS-1$
    }
    
    private boolean isInterestingProject(IJavaElement elt) {
        return elt != null &&
                WeavableProjectListener.getInstance().isInWeavableProject(elt);
    }
}