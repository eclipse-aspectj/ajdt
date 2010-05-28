package org.eclipse.ajdt.internal.ui.refactoring;

import java.util.Map;

import org.eclipse.ajdt.core.ReflectionUtils;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.scripting.JavaUIRefactoringContribution;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public class ITDRenameRefactoringContribution extends JavaUIRefactoringContribution {
    public Refactoring createRefactoring(JavaRefactoringDescriptor descriptor, RefactoringStatus status) {
        JavaRefactoringArguments arguments= new JavaRefactoringArguments(descriptor.getProject(), retrieveArgumentMap(descriptor));
        ITDRenameRefactoringProcessor processor= new ITDRenameRefactoringProcessor(arguments, status);
        return new RenameRefactoring(processor);
    }

    public RefactoringDescriptor createDescriptor() {
        // must start with an invalid refactoring ID since the constructor does a legality check.`
        final RenameJavaElementDescriptor descriptor= RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_METHOD);
        ReflectionUtils.setPrivateField(RefactoringDescriptor.class, "fRefactoringId", descriptor, ITDRenameRefactoringProcessor.REFACTORING_ID);
        return descriptor;
    }

    public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map arguments, int flags) {
        // must start with an invalid refactoring ID since the constructor does a legality check.`
        final RenameJavaElementDescriptor descriptor= RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_METHOD, project, description, comment, arguments, flags);
        ReflectionUtils.setPrivateField(RefactoringDescriptor.class, "fRefactoringId", descriptor, id);
        return descriptor;
    }
}
