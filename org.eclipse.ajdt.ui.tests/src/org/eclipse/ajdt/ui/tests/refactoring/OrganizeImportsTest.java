/*******************************************************************************
 * Copyright (c) 2008 SpringSourceand others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.refactoring;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.actions.OrganizeImportsAction;

public class OrganizeImportsTest extends UITestCase {

    /**
     * Should not remove the aspect in the imports statement
     */
    public void testBug188845() throws Exception {
        IProject proj = createPredefinedProject("Bug 188845"); //$NON-NLS-1$
        IFile concrete = proj.getFile("src/tmp/b/ConcreteAspect.aj"); //$NON-NLS-1$
        ICompilationUnit cu = (ICompilationUnit) AspectJCore.create(concrete);
        
        JavaEditor editor = (JavaEditor) EditorUtility.openInEditor(cu);
        
        assertEquals("Should start off with 2 import statements", 2, cu.getImportContainer().getChildren().length); //$NON-NLS-1$
        OrganizeImportsAction action = new OrganizeImportsAction(editor);
        action.run(cu);
        waitForJobsToComplete();
        assertEquals("Should have only 1 import statement after reorganizing", 1, cu.getImportContainer().getChildren().length); //$NON-NLS-1$
    }
}