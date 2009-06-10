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

package org.eclipse.contribution.weaving.jdt.tests.itdawareness;

import org.eclipse.contribution.jdt.itdawareness.IJavaContentAssistProvider;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.core.Openable;

/**
 * @author Andrew Eisenberg
 * @created Jan 30, 2009
 *
 */
public class MockContentAssistProvider implements IJavaContentAssistProvider {
    
    
    boolean contentAssistDone = false;
    boolean codeSelectDone = false;

    public boolean doContentAssist(ICompilationUnit cu,
            ICompilationUnit unitToSkip, int position,
            CompletionRequestor requestor, WorkingCopyOwner owner,
            ITypeRoot typeRoot, Openable target, IProgressMonitor monitor) throws Exception {  /* AJDT 1.7 */
        contentAssistDone = true;
        return true;
    }

    public IJavaElement[] doCodeSelect(
            org.eclipse.jdt.core.ICompilationUnit unit, int offset, int length,
            IJavaElement[] result) throws JavaModelException {
        codeSelectDone = true;
        return new IJavaElement[0];
    }
}
