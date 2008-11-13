package org.eclipse.ajdt.core.javaelements;

import org.eclipse.contribution.jdt.cuprovider.ICompilationUnitProvider;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragment;

public class AJCompilationUnitProvider implements ICompilationUnitProvider {
    public CompilationUnit create(PackageFragment parent, String name,
            WorkingCopyOwner owner) {
        return new AJCompilationUnit(parent, name, owner);
    }

}
