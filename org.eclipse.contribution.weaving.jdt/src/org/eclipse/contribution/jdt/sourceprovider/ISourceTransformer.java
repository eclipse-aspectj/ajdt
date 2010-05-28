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

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.search.indexing.SourceIndexer;
import org.eclipse.jdt.internal.core.search.indexing.SourceIndexerRequestor;

public interface ISourceTransformer {
    public char[] convert(char[] toConvert);
    
    public IBuffer ensureRealBuffer(ICompilationUnit unit) throws JavaModelException;

    /**
     * Create a SourceIndexerRequestor specific to the document type.
     * This allows clients to index names that are not otherwise 
     * available to the indexer.
     */
    public SourceIndexerRequestor createIndexerRequestor(SourceIndexer indexer);
}
