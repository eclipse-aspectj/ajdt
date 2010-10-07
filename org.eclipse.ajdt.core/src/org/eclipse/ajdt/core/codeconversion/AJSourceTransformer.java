/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.codeconversion;

import org.eclipse.contribution.jdt.sourceprovider.ISourceTransformer;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.search.indexing.SourceIndexer;
import org.eclipse.jdt.internal.core.search.indexing.SourceIndexerRequestor;

public class AJSourceTransformer implements ISourceTransformer {

    public char[] convert(char[] toConvert) {
        AspectsConvertingParser parser = new AspectsConvertingParser((char[]) toConvert.clone());
        parser.convert(ConversionOptions.CONSTANT_SIZE);
        return parser.content;
    }

    public IBuffer ensureRealBuffer(ICompilationUnit unit) throws JavaModelException {
        IBuffer buffer = unit.getBuffer();
        if (buffer instanceof JavaCompatibleBuffer) {
            buffer = ((JavaCompatibleBuffer) buffer).getRealBuffer();
        }
        return buffer;
    }

    public SourceIndexerRequestor createIndexerRequestor(SourceIndexer indexer) {
        return new AJSourceIndexerRequestor(indexer);
    }

}
