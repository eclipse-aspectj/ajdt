package org.eclipse.ajdt.internal.ui.refactoring;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class PushInRefactoringWizard extends RefactoringWizard {

    private PushInRefactoringDescriptor descriptor;
    
	public PushInRefactoringWizard(PushInRefactoring refactoring, String pageTitle, PushInRefactoringDescriptor descriptor) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
		this.descriptor = descriptor;
	}

	protected void addUserInputPages() {
		addPage(new PushInRefactoringInputPage("IntroduceIndirectionInputPage", descriptor));
	}
}