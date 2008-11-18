package org.eclipse.contribution.jdt.cuprovider;

import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragment;

public interface ICompilationUnitProvider {
    public CompilationUnit create(PackageFragment parent, String name, WorkingCopyOwner owner);
}
