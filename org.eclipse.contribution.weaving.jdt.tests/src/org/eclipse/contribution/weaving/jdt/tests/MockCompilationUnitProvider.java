package org.eclipse.contribution.weaving.jdt.tests;

import org.eclipse.contribution.jdt.cuprovider.ICompilationUnitProvider;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragment;

public class MockCompilationUnitProvider implements ICompilationUnitProvider {

    public CompilationUnit create(PackageFragment parent, String name,
            WorkingCopyOwner owner) {
        return new MockCompilationUnit(parent, name, owner);
    }

}
