package org.eclipse.ajdt.internal.ui.refactoring;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

public class PushInRefactoringContribution extends RefactoringContribution {

    public PushInRefactoringContribution() {
    }

    public RefactoringDescriptor createDescriptor(String id, String project,
            String description, String comment, Map arguments, int flags)
            throws IllegalArgumentException {
        return new PushInRefactoringDescriptor(project, description, comment, arguments);
    }

    public Map retrieveArgumentMap(RefactoringDescriptor descriptor) {
        if (descriptor instanceof PushInRefactoringDescriptor) {
            return ((PushInRefactoringDescriptor) descriptor).getArguments();
        } else {
            return super.retrieveArgumentMap(descriptor);
        }
    }
}
