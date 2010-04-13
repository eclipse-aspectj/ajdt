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
package org.eclipse.contribution.jdt.itdawareness;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;

/**
 * 
 * @author Andrew Eisenberg
 * @created Mar 8, 2010
 */
public interface ISearchProvider {
    /**
     * Converts the element into something that should be searched for.
     * Should be a no-op if there is no change required
     * @return
     */
    public IJavaElement convertJavaElement(IJavaElement origElement);
    
    public char[] translateForMatchProcessing(char[] original, CompilationUnit unit);
    
    public int translateLocationToOriginal(int translatedLocation);
    
    public LookupEnvironment createLookupEnvironment(
            LookupEnvironment lookupEnvironment,
            ICompilationUnit[] workingCopies, JavaProject project);
}
