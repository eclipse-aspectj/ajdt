package org.eclipse.ajdt.internal.ui.refactoring;

import java.util.Map;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMember;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class PushInRefactoringDescriptor extends RefactoringDescriptor {

    public static final String REFACTORING_ID = "org.eclipse.ajdt.ui.pushin";

    private final Map arguments;

    public PushInRefactoringDescriptor(String project, String description, String comment, Map arguments) {
        super(REFACTORING_ID, project, description, comment, RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
        this.arguments= arguments;
    }

    public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
        PushInRefactoring refactoring= new PushInRefactoring();
        status.merge(refactoring.initialize(arguments));
        return refactoring;
    }

    public Map getArguments() {
        return arguments;
    }
    
    public IMember[] getITDs() {
        String value = (String) arguments.get(PushInRefactoring.ALL_ITDS);
        if (value != null) {
            String[] values = value.split("\\n");
            IMember[] itds = new IMember[values.length];
            for (int i = 0; i < values.length; i++) {
                itds[i] = (IMember) AspectJCore.create(values[i]);
            }
            return itds;
        }
        return new IMember[0];
    }
}