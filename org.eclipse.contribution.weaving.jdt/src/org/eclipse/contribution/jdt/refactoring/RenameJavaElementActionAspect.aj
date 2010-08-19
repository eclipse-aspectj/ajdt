/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.contribution.jdt.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.refactoring.actions.RenameJavaElementAction;

/**
 * This aspect ensures that ITDs are renamed with the proper rename refactoring
 * @author Andrew Eisenberg
 * @created May 21, 2010
 */
public aspect RenameJavaElementActionAspect {
    
    /**
     * This will be null if AJDT is not installed (ie- JDT Weaving installed, but no AJDT)
     */
    IRefactoringProvider provider = RefactoringAdapter.getInstance().getProvider();
    
    pointcut renameInvoked(IJavaElement element, boolean lightweight) : 
            execution(private void RenameJavaElementAction.run(
                    IJavaElement, boolean) 
                    throws CoreException) && args (element, lightweight) && 
                    within(RenameJavaElementAction);
    
    void around(IJavaElement element, boolean lightweight) throws CoreException : 
            renameInvoked(element, lightweight) {
        if (provider != null && provider.isInterestingElement(element)) {
            provider.performRefactoring(element, lightweight);
        } else {
            if (provider.belongsToInterestingCompilationUnit(element)) {
                lightweight = false;
            }
            proceed(element, lightweight);
        }
    }
}
