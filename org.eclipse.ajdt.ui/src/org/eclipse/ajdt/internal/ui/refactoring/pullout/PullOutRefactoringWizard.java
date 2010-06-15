/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kris De Volder - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.refactoring.pullout;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class PullOutRefactoringWizard extends RefactoringWizard {

	public PullOutRefactoringWizard(PullOutRefactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle("Pull Out Intertype Declarations");
	}

	protected void addUserInputPages() {
		addPage(new PullOutRefactoringInputPage("PullOutRefactoringPage"));
	}

}
