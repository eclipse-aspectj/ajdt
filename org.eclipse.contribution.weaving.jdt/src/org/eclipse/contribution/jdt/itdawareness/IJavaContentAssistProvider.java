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

package org.eclipse.contribution.jdt.itdawareness;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.Openable;

/**
 * @author Andrew Eisenberg
 * @created Jan 2, 2009
 * 
 * Allows a plugin to usurp content assist and code select calculation
 * from the Java one to a custom one
 *
 */
public interface IJavaContentAssistProvider {
    public boolean doContentAssist(org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu,
            org.eclipse.jdt.internal.compiler.env.ICompilationUnit unitToSkip,
            int position, CompletionRequestor requestor,
            WorkingCopyOwner owner,
                                                 /* AJDT 1.7 */
            ITypeRoot typeRoot, Openable target, IProgressMonitor monitor) throws Exception;
            
    public IJavaElement[] doCodeSelect(ICompilationUnit unit,
            int offset, int length, IJavaElement[] result) throws JavaModelException;
            
}
