// Copied from org.eclipse.jdt.internal.ui.refactoring.reorg.RenameMethodWizard
/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.refactoring;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.refactoring.reorg.RenameRefactoringWizard;
import org.eclipse.ltk.core.refactoring.Refactoring;

public class RenameITDWizard extends RenameRefactoringWizard {

    private static final String INPUT_PAGE_DESCRIPTION = "Enter the new name for this intertype declaration.";
    private static final String DEFAULT_TITLE = "Rename Intertype Declaration";

    public RenameITDWizard(Refactoring refactoring) {
        super(refactoring, DEFAULT_TITLE,
                INPUT_PAGE_DESCRIPTION,
                JavaPluginImages.DESC_WIZBAN_REFACTOR_METHOD,  // FIXADE should we have a custom image here?
                IJavaHelpContextIds.RENAME_METHOD_WIZARD_PAGE);
    }
    
}
