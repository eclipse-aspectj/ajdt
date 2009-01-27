package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider;

/**
 * 
 * @deprecated Not used any more.  Use {@link CompilationUnitDocumentProvider} instead
 */
public class AJCompiltionUnitDocumentProvider extends
        CompilationUnitDocumentProvider {

    protected ICompilationUnit createCompilationUnit(IFile file) {
        return (ICompilationUnit) AspectJCore.create(file);
    }
}
